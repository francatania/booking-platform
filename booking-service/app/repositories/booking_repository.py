from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta, date
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.exceptions import BookingNotFoundException
from app.schemas.booking import BookingPeriodStat


class BookingRepository:
    """Data access layer for bookings and booking configuration.

    All SQLAlchemy queries are encapsulated here, keeping business logic
    in the service layer free from ORM details.
    """

    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, booking_id: int) -> Booking | None:
        """Returns a booking by its ID, or None if not found.

        Args:
            booking_id: The booking primary key.

        Returns:
            The Booking entity, or None.
        """
        return self.db.query(Booking).filter(Booking.id == booking_id).first()

    def get_or_raise(self, booking_id: int) -> Booking:
        """Returns a booking by its ID, raising an exception if not found.

        Args:
            booking_id: The booking primary key.

        Returns:
            The Booking entity.

        Raises:
            BookingNotFoundException: If no booking exists with the given ID.
        """
        booking = self.get_by_id(booking_id)
        if booking is None:
            raise BookingNotFoundException()
        return booking

    def get_by_user(self, user_id: int) -> list[Booking]:
        """Returns all bookings belonging to a user.

        Args:
            user_id: The user ID to filter by.

        Returns:
            List of Booking entities.
        """
        return self.db.query(Booking).filter(Booking.user_id == user_id).all()

    def get_by_company(
        self,
        company_id: int,
        status: str | None = None,
        from_date: date | None = None,
        to_date: date | None = None,
    ) -> list[Booking]:
        """Returns bookings for a company with optional filters.

        Args:
            company_id: The company ID to filter by.
            status: Optional booking status filter (e.g. "PENDING", "CONFIRMED").
            from_date: Optional lower bound for start_time (inclusive).
            to_date: Optional upper bound for start_time (inclusive).

        Returns:
            List of Booking entities ordered by start_time ascending.
        """
        query = self.db.query(Booking).filter(Booking.company_id == company_id)
        if status:
            query = query.filter(Booking.status == BookingStatus[status])
        if from_date:
            query = query.filter(Booking.start_time >= datetime.combine(from_date, datetime.min.time()))
        if to_date:
            query = query.filter(Booking.start_time <= datetime.combine(to_date, datetime.max.time()))
        return query.order_by(Booking.start_time).all()

    def find_overlap(self, user_id: int, start_time: datetime, end_time: datetime) -> Booking | None:
        """Checks for any active booking that overlaps the given time window for a user.

        Used to enforce the global anti-collision rule: a user cannot have
        two overlapping bookings regardless of company.

        Args:
            user_id: The user ID to check.
            start_time: Start of the requested window.
            end_time: End of the requested window.

        Returns:
            The conflicting Booking, or None if no overlap exists.
        """
        return self.db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time,
            Booking.end_time > start_time,
        ).first()

    def find_gap_overlap(
        self, user_id: int, company_id: int, start_time: datetime, end_time: datetime, gap: timedelta
    ) -> Booking | None:
        """Checks for a booking that violates the company's required gap between appointments.

        Expands the requested window by `gap` on both sides before checking for
        overlaps within the same company.

        Args:
            user_id: The user ID to check.
            company_id: The company whose gap rule applies.
            start_time: Start of the requested window.
            end_time: End of the requested window.
            gap: The minimum required gap between bookings.

        Returns:
            The conflicting Booking, or None if the gap rule is satisfied.
        """
        return self.db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.company_id == company_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time + gap,
            Booking.end_time > start_time - gap,
        ).first()

    def get_config(self, company_id: int) -> BookingConfig | None:
        """Returns the booking configuration for a company.

        Args:
            company_id: The company ID.

        Returns:
            The BookingConfig entity, or None if no custom config exists.
        """
        return self.db.query(BookingConfig).filter(BookingConfig.company_id == company_id).first()

    def save(self, booking: Booking) -> Booking:
        """Persists a new booking and returns the refreshed entity.

        Args:
            booking: The Booking entity to persist.

        Returns:
            The saved Booking with its generated ID and timestamps.
        """
        self.db.add(booking)
        self.db.commit()
        self.db.refresh(booking)
        return booking

    def commit(self) -> None:
        """Commits the current transaction.

        Used after in-place mutations (e.g. status transitions, rescheduling).
        """
        self.db.commit()

    def get_stats(self, company_id: int, start_date: datetime, end_date: datetime) -> dict:
        """Returns aggregated booking statistics for a company over a date range.

        Args:
            company_id: The company ID.
            start_date: Start of the reporting period (inclusive).
            end_date: End of the reporting period (inclusive).

        Returns:
            A dict with keys:
                - total (int): total number of bookings in the period.
                - counts (dict): bookings grouped by status.
                - revenue (Decimal): total revenue from COMPLETED bookings.
                - period_rows (list[BookingPeriodStat]): daily breakdown of count and revenue.
        """
        bookings = self.db.query(Booking).filter(
            Booking.company_id == company_id,
            Booking.start_time >= start_date,
            Booking.start_time <= end_date,
        )

        counts = {
            "PENDING":   bookings.filter(Booking.status == BookingStatus.PENDING).count(),
            "CONFIRMED": bookings.filter(Booking.status == BookingStatus.CONFIRMED).count(),
            "CANCELLED": bookings.filter(Booking.status == BookingStatus.CANCELLED).count(),
            "COMPLETED": bookings.filter(Booking.status == BookingStatus.COMPLETED).count(),
        }

        revenue = (
            bookings.filter(Booking.status == BookingStatus.COMPLETED)
            .with_entities(func.sum(Booking.price))
            .scalar() or 0
        )

        period_rows = (
            self.db.query(
                func.date(Booking.start_time).label("date"),
                func.count(Booking.id).label("count"),
                func.sum(Booking.price).label("revenue"),
            )
            .filter(
                Booking.company_id == company_id,
                Booking.start_time >= start_date,
                Booking.start_time <= end_date,
                Booking.status == BookingStatus.COMPLETED,
            )
            .group_by(func.date(Booking.start_time))
            .order_by(func.date(Booking.start_time))
            .all()
        )

        return {
            "total": bookings.count(),
            "counts": counts,
            "revenue": revenue,
            "period_rows": [
                BookingPeriodStat(date=r.date, count=r.count, revenue=r.revenue or 0)
                for r in period_rows
            ],
        }
