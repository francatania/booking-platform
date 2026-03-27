from sqlalchemy.orm import Session
from sqlalchemy import func
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.schemas.booking import BookingCreate, RescheduleRequest, BookingStatsResponse, BookingPeriodStat
from app.dependencies.auth import UserPrincipal
from app.dependencies.company_client import validate_service
from app.exceptions import (
    InvalidBookingTimeException,
    InvalidStatusTransitionException,
    MissingUserIdException,
    BookingNotFoundException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
)
from datetime import datetime, timedelta

class BookingService:
    def create_booking(self, dto: BookingCreate, current_user: UserPrincipal, db:Session):
        if current_user.role == "USER":
            target_user_id = current_user.user_id
        else:
            if dto.user_id is None:
                raise MissingUserIdException()
            target_user_id = dto.user_id

        validate_service(dto.service_id)
        self._check_collision(target_user_id, dto.company_id, dto.start_time, dto.end_time, db)

        booking = Booking(
            user_id=target_user_id,
            service_id=dto.service_id,
            company_id=dto.company_id,
            start_time=dto.start_time,
            end_time=dto.end_time,
            price=dto.price,
            status=BookingStatus.PENDING,
        )

        db.add(booking)
        db.commit()
        db.refresh(booking)
        return booking
    
    def _find_booking_to_patch(self, booking_id: int, current_user: UserPrincipal, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()

        if booking is None:
            raise BookingNotFoundException()

        if current_user.role == "USER" and booking.user_id != current_user.user_id:
            raise BookingForbiddenException()

        if booking.status == BookingStatus.CANCELLED:
            raise BookingAlreadyCancelledException()
        return booking
    
    def _check_collision(self, user_id: int, company_id: int, start_time, end_time, db: Session):
        raw_conflict = db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time,
            Booking.end_time > start_time,
        ).first()

        if raw_conflict:
            raise BookingConflictException()

        config = db.query(BookingConfig).filter(BookingConfig.company_id == company_id).first()
        gap_minutes = config.gap_minutes if config else 0

        if gap_minutes > 0:
            gap = timedelta(minutes=gap_minutes)
            gap_conflict = db.query(Booking).filter(
                Booking.user_id == user_id,
                Booking.company_id == company_id,
                Booking.status != BookingStatus.CANCELLED,
                Booking.start_time < end_time + gap,
                Booking.end_time > start_time - gap,
            ).first()

            if gap_conflict:
                raise BookingGapConflictException()


    
    def get_my_bookings(self, user_id: int, db:Session):
        return db.query(Booking).filter(Booking.user_id == user_id).all()
    
    def get_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        return booking

    def cancel_booking(self, booking_id: int, current_user: UserPrincipal, db: Session):
        booking = self._find_booking_to_patch(booking_id, current_user, db)
        booking.status = BookingStatus.CANCELLED
        db.commit()
        db.refresh(booking)
        return booking

    def reschedule(self, booking_id:int, current_user: UserPrincipal, dto: RescheduleRequest, db: Session):

        if dto.start_time >= dto.end_time:
            raise InvalidBookingTimeException()

        booking = self._find_booking_to_patch(booking_id, current_user, db)
        self._check_collision(current_user.user_id, booking.company_id, dto.start_time, dto.end_time, db)

        booking.start_time = dto.start_time
        booking.end_time = dto.end_time
        db.commit()
        db.refresh(booking)
        return booking
    
    def get_company_bookings(self, company_id: int, status: str | None, db: Session):
        query = db.query(Booking).filter(Booking.company_id == company_id)
        if status:
            query = query.filter(Booking.status == BookingStatus[status])
        return query.order_by(Booking.start_time).all()

    def confirm_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        if booking.status != BookingStatus.PENDING:
            raise InvalidStatusTransitionException(booking.status.value, "CONFIRMED")
        booking.status = BookingStatus.CONFIRMED
        db.commit()
        db.refresh(booking)
        return booking

    def complete_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        if booking.status != BookingStatus.CONFIRMED:
            raise InvalidStatusTransitionException(booking.status.value, "COMPLETED")
        booking.status = BookingStatus.COMPLETED
        db.commit()
        db.refresh(booking)
        return booking

    def getStats(self, company_id: int, start_date: datetime, end_date: datetime, db:Session):
        bookings = db.query(Booking).filter(Booking.company_id == company_id,
                                            Booking.start_time >= start_date,
                                            Booking.start_time <= end_date)

        pending_count = bookings.filter(Booking.status == BookingStatus.PENDING).count()
        cancelled_count = bookings.filter(Booking.status == BookingStatus.CANCELLED).count()
        confirmed_count = bookings.filter(Booking.status == BookingStatus.CONFIRMED).count()
        completed_count = bookings.filter(Booking.status == BookingStatus.COMPLETED).count()


        revenue = bookings.filter(Booking.status == BookingStatus.COMPLETED).with_entities(func.sum(Booking.price)).scalar() or 0

        period_rows = (
            db.query(
                func.date(Booking.start_time).label("date"),
                func.count(Booking.id).label("count"),
                func.sum(Booking.price).label("revenue"),
            )
            .filter(
                Booking.company_id == company_id,
                Booking.start_time >= start_date,
                Booking.start_time <= end_date,
                Booking.status == BookingStatus.COMPLETED
            )
            .group_by(func.date(Booking.start_time))
            .order_by(func.date(Booking.start_time))
            .all()
        )

        bookings_by_period = [
            BookingPeriodStat(date=row.date, count=row.count, revenue=row.revenue or 0)
            for row in period_rows
        ]

        return BookingStatsResponse(
            total_bookings=bookings.count(),
            bookings_by_status={
                "PENDING": pending_count,
                "CONFIRMED": confirmed_count,
                "CANCELLED": cancelled_count,
                "COMPLETED": completed_count
            },
            total_revenue=revenue,
            bookings_by_period=bookings_by_period,
        )
