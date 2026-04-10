from app.models.booking import Booking, BookingStatus
from app.repositories.booking_repository import BookingRepository
from app.schemas.booking import BookingCreate, BookingDetailResponse, BookingResponse, RescheduleRequest, BookingStatsResponse
from app.dependencies.auth import UserPrincipal
from app.dependencies.company_client import validate_service, get_services_by_ids
from app.dependencies.auth_client import get_users_by_ids
from app.exceptions import (
    InvalidBookingTimeException,
    MissingUserIdException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
)
from app.state_machine import transition
from datetime import datetime, timedelta, date
from app.dependencies.rabbitmq_publisher import publish_event


class BookingService:
    def create_booking(self, dto: BookingCreate, current_user: UserPrincipal, repo: BookingRepository, language: str = "en"):
        if current_user.role == "USER":
            target_user_id = current_user.user_id
        else:
            if dto.user_id is None:
                raise MissingUserIdException()
            target_user_id = dto.user_id

        validate_service(dto.service_id)
        self._check_collision(repo, target_user_id, dto.company_id, dto.start_time, dto.end_time)

        booking = repo.save(Booking(
            user_id=target_user_id,
            service_id=dto.service_id,
            company_id=dto.company_id,
            start_time=dto.start_time,
            end_time=dto.end_time,
            price=dto.price,
            status=BookingStatus.PENDING,
        ))

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

    def _find_booking_to_patch(self, repo: BookingRepository, booking_id: int, current_user: UserPrincipal) -> Booking:
        booking = repo.get_or_raise(booking_id)

        if current_user.role == "USER" and booking.user_id != current_user.user_id:
            raise BookingForbiddenException()

        if booking.status == BookingStatus.CANCELLED:
            raise BookingAlreadyCancelledException()

        return booking

    def _check_collision(self, repo: BookingRepository, user_id: int, company_id: int, start_time, end_time):
        if repo.find_overlap(user_id, start_time, end_time):
            raise BookingConflictException()

        config = repo.get_config(company_id)
        gap_minutes = config.gap_minutes if config else 0

        if gap_minutes > 0:
            gap = timedelta(minutes=gap_minutes)
            if repo.find_gap_overlap(user_id, company_id, start_time, end_time, gap):
                raise BookingGapConflictException()

    def get_my_bookings(self, user_id: int, repo: BookingRepository):
        bookings = repo.get_by_user(user_id)
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
                created_at=b.created_at,
            )
            for b in bookings
        ]

    def get_booking(self, booking_id: int, repo: BookingRepository):
        booking = repo.get_or_raise(booking_id)
        return self._to_response(booking)

    def cancel_booking(self, booking_id: int, current_user: UserPrincipal, repo: BookingRepository, language: str = "en"):
        booking = self._find_booking_to_patch(repo, booking_id, current_user)
        transition(booking, BookingStatus.CANCELLED)
        repo.commit()

        service_names = get_services_by_ids([booking.service_id])
        publish_event("booking.cancelled", {
            "bookingId": booking.id,
            "userId": booking.user_id,
            "operatorId": booking.company_id,
            "serviceName": service_names.get(booking.service_id, ""),
            "date": booking.start_time.strftime("%Y-%m-%d"),
            "startTime": booking.start_time.strftime("%H:%M"),
            "cancelledBy": current_user.role,
            "language": language,
        })

    def reschedule(self, booking_id: int, current_user: UserPrincipal, dto: RescheduleRequest, repo: BookingRepository):
        if dto.start_time >= dto.end_time:
            raise InvalidBookingTimeException()

        booking = self._find_booking_to_patch(repo, booking_id, current_user)
        self._check_collision(repo, current_user.user_id, booking.company_id, dto.start_time, dto.end_time)

        booking.start_time = dto.start_time
        booking.end_time = dto.end_time
        repo.commit()
        return self._to_response(booking)

    def confirm_booking(self, booking_id: int, repo: BookingRepository, language: str = "en"):
        booking = repo.get_or_raise(booking_id)
        transition(booking, BookingStatus.CONFIRMED)
        repo.commit()

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

    def complete_booking(self, booking_id: int, repo: BookingRepository):
        booking = repo.get_or_raise(booking_id)
        transition(booking, BookingStatus.COMPLETED)
        repo.commit()

    def get_company_bookings(self, company_id: int, status: str | None, from_date: date | None, to_date: date | None, full_name: str | None, repo: BookingRepository) -> list[BookingDetailResponse]:
        bookings = repo.get_by_company(company_id, status, from_date, to_date)

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

    def getStats(self, company_id: int, start_date: datetime, end_date: datetime, repo: BookingRepository):
        stats = repo.get_stats(company_id, start_date, end_date)
        return BookingStatsResponse(
            total_bookings=stats["total"],
            bookings_by_status=stats["counts"],
            total_revenue=stats["revenue"],
            bookings_by_period=stats["period_rows"],
        )

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
