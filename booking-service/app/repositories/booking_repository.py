from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta, date
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.exceptions import BookingNotFoundException
from app.schemas.booking import BookingPeriodStat


class BookingRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, booking_id: int) -> Booking | None:
        return self.db.query(Booking).filter(Booking.id == booking_id).first()

    def get_or_raise(self, booking_id: int) -> Booking:
        booking = self.get_by_id(booking_id)
        if booking is None:
            raise BookingNotFoundException()
        return booking

    def get_by_user(self, user_id: int) -> list[Booking]:
        return self.db.query(Booking).filter(Booking.user_id == user_id).all()

    def get_by_company(
        self,
        company_id: int,
        status: str | None = None,
        from_date: date | None = None,
        to_date: date | None = None,
    ) -> list[Booking]:
        query = self.db.query(Booking).filter(Booking.company_id == company_id)
        if status:
            query = query.filter(Booking.status == BookingStatus[status])
        if from_date:
            query = query.filter(Booking.start_time >= datetime.combine(from_date, datetime.min.time()))
        if to_date:
            query = query.filter(Booking.start_time <= datetime.combine(to_date, datetime.max.time()))
        return query.order_by(Booking.start_time).all()

    def find_overlap(self, user_id: int, start_time: datetime, end_time: datetime) -> Booking | None:
        return self.db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time,
            Booking.end_time > start_time,
        ).first()

    def find_gap_overlap(
        self, user_id: int, company_id: int, start_time: datetime, end_time: datetime, gap: timedelta
    ) -> Booking | None:
        return self.db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.company_id == company_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time + gap,
            Booking.end_time > start_time - gap,
        ).first()

    def get_config(self, company_id: int) -> BookingConfig | None:
        return self.db.query(BookingConfig).filter(BookingConfig.company_id == company_id).first()

    def save(self, booking: Booking) -> Booking:
        self.db.add(booking)
        self.db.commit()
        self.db.refresh(booking)
        return booking

    def commit(self) -> None:
        self.db.commit()

    def get_stats(self, company_id: int, start_date: datetime, end_date: datetime) -> dict:
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
