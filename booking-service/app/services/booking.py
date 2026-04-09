from sqlalchemy.orm import Session
from sqlalchemy import func
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.schemas.booking import BookingCreate, BookingDetailResponse, BookingResponse, RescheduleRequest, BookingStatsResponse, BookingPeriodStat
from app.dependencies.auth import UserPrincipal
from app.dependencies.company_client import validate_service, get_services_by_ids
from app.dependencies.auth_client import get_users_by_ids
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
from datetime import datetime, timedelta, date
from app.dependencies.rabbitmq_publisher import publish_event

class BookingService:
    def create_booking(self, dto: BookingCreate, current_user: UserPrincipal, db: Session, language: str = "en"):
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

        publish_event("booking.created", {
            "bookingId": booking.id,
            "userId": booking.user_id,
            "operatorId": booking.company_id,
            "serviceId": booking.service_id,
            "serviceName": dto.service_name or "",
            "date": booking.start_time.strftime("%Y-%m-%d"),
            "startTime": booking.start_time.strftime("%H:%M"),
            "language": language,
        })

        service_names = get_services_by_ids([booking.service_id])
        return BookingResponse(
            id=booking.id,
            user_id=booking.user_id,
            service_id=booking.service_id,
            service_name=service_names.get(booking.service_id, ""),
            price=booking.price,
            start_time=booking.start_time,
            end_time=booking.end_time,
            status=booking.status.value,
            created_at=booking.created_at,
        )
    
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
        bookings: list[Booking] =  db.query(Booking).filter(Booking.user_id == user_id).all()
        service_ids = list({b.service_id for b in bookings})
        services = get_services_by_ids(service_ids)

        return [ 
            BookingResponse(
                id=b.id,
                user_id=b.user_id,
                service_id=b.service_id,
                service_name=services.get(b.service_id, ""),
                price=b.price,
                start_time=b.start_time,
                end_time=b.end_time,
                status=b.status,
                created_at=b.created_at
            )
            for b in bookings
        ]

    
    def get_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        return self._to_response(booking)

    def cancel_booking(self, booking_id: int, current_user: UserPrincipal, db: Session):
        booking = self._find_booking_to_patch(booking_id, current_user, db)
        booking.status = BookingStatus.CANCELLED
        db.commit()

    def reschedule(self, booking_id:int, current_user: UserPrincipal, dto: RescheduleRequest, db: Session):

        if dto.start_time >= dto.end_time:
            raise InvalidBookingTimeException()

        booking = self._find_booking_to_patch(booking_id, current_user, db)
        self._check_collision(current_user.user_id, booking.company_id, dto.start_time, dto.end_time, db)

        booking.start_time = dto.start_time
        booking.end_time = dto.end_time
        db.commit()
        db.refresh(booking)
        return self._to_response(booking)

    def _to_response(self, booking: Booking) -> BookingResponse:
        service_names = get_services_by_ids([booking.service_id])
        return BookingResponse(
            id=booking.id,
            user_id=booking.user_id,
            service_id=booking.service_id,
            service_name=service_names.get(booking.service_id, ""),
            price=booking.price,
            start_time=booking.start_time,
            end_time=booking.end_time,
            status=booking.status.value,
            created_at=booking.created_at,
            updated_at=booking.updated_at,
        )
    
    def get_company_bookings(self, company_id: int, status: str | None, from_date: date | None, to_date: date | None, full_name: str | None, db: Session) -> list[BookingDetailResponse]:
        query = db.query(Booking).filter(Booking.company_id == company_id)
        if status:
            query = query.filter(Booking.status == BookingStatus[status])
        if from_date:
            query = query.filter(Booking.start_time >= datetime.combine(from_date, datetime.min.time()))
        if to_date:
            query = query.filter(Booking.start_time <= datetime.combine(to_date, datetime.max.time()))
        bookings = query.order_by(Booking.start_time).all()

        service_ids = list({b.service_id for b in bookings})
        user_ids = list({b.user_id for b in bookings})

        services = get_services_by_ids(service_ids)
        users = get_users_by_ids(user_ids)

        results = [
            BookingDetailResponse(
                id=b.id,
                user_id=b.user_id,
                service_id=b.service_id,
                company_id=b.company_id,
                user_username=users.get(b.user_id, {}).get("username", ""),
                user_first_name=users.get(b.user_id, {}).get("firstName", ""),
                user_last_name=users.get(b.user_id, {}).get("lastName", ""),
                service_name=services.get(b.service_id, ""),
                start_time=b.start_time,
                end_time=b.end_time,
                price=b.price,
                status=b.status.value,
                created_at=b.created_at,
            )
            for b in bookings
        ]

        if full_name:
            search = full_name.lower()
            results = [
                r for r in results
                if search in r.user_first_name.lower()
                or search in r.user_last_name.lower()
                or search in f"{r.user_first_name} {r.user_last_name}".lower()
            ]

        return results

    def confirm_booking(self, booking_id: int, db: Session, language: str = "en"):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        if booking.status != BookingStatus.PENDING:
            raise InvalidStatusTransitionException(booking.status.value, "CONFIRMED")
        booking.status = BookingStatus.CONFIRMED
        db.commit()
        db.refresh(booking)

        service_names = get_services_by_ids([booking.service_id])
        publish_event("booking.confirmed", {
            "bookingId": booking.id,
            "userId": booking.user_id,
            "operatorId": booking.company_id,
            "serviceId": booking.service_id,
            "serviceName": service_names.get(booking.service_id, ""),
            "date": booking.start_time.strftime("%Y-%m-%d"),
            "startTime": booking.start_time.strftime("%H:%M"),
            "language": language,
        })

    def complete_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise BookingNotFoundException()
        if booking.status != BookingStatus.CONFIRMED:
            raise InvalidStatusTransitionException(booking.status.value, "COMPLETED")
        booking.status = BookingStatus.COMPLETED
        db.commit()

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
