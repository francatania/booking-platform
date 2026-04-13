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
    """Business logic for booking operations.

    Coordinates validation, state transitions, persistence via BookingRepository,
    and event publishing to RabbitMQ. Has no direct knowledge of the ORM.
    """

    def create_booking(self, dto: BookingCreate, current_user: UserPrincipal, repo: BookingRepository, language: str = "en"):
        """Creates a new booking after validating the service and checking for time conflicts.

        For USER role, the booking is always assigned to themselves.
        For ADMIN/OPERATOR role, a target user_id must be provided in the DTO.
        Publishes a ``booking.created`` event to RabbitMQ after successful creation.

        Args:
            dto: The booking creation data (service, company, time window, price).
            current_user: The authenticated user making the request.
            repo: The booking repository for data access.
            language: The Accept-Language header value used for notification emails.

        Returns:
            A BookingResponse with the created booking's data.

        Raises:
            MissingUserIdException: If an ADMIN/OPERATOR does not provide a user_id.
            BookingConflictException: If the time window overlaps with an existing booking.
            BookingGapConflictException: If the company's gap rule is violated.
        """
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
        """Fetches a booking and validates the current user is allowed to modify it.

        Args:
            repo: The booking repository.
            booking_id: The booking to fetch.
            current_user: The authenticated user.

        Returns:
            The Booking entity if the user is authorized and the booking is not cancelled.

        Raises:
            BookingNotFoundException: If the booking does not exist.
            BookingForbiddenException: If a USER tries to modify another user's booking.
            BookingAlreadyCancelledException: If the booking is already cancelled.
        """
        booking = repo.get_or_raise(booking_id)

        if current_user.role == "USER" and booking.user_id != current_user.user_id:
            raise BookingForbiddenException()

        if booking.status == BookingStatus.CANCELLED:
            raise BookingAlreadyCancelledException()

        return booking

    def _check_collision(self, repo: BookingRepository, user_id: int, company_id: int, start_time, end_time):
        """Validates a time window does not conflict with existing bookings.

        Runs two independent checks:

        1. **Global overlap** — the user cannot have two overlapping bookings regardless of company.
        2. **Gap check** — the company's configured ``gap_minutes`` must be respected.

        Args:
            repo: The booking repository.
            user_id: The user whose bookings are checked.
            company_id: The company whose gap rule applies.
            start_time: Start of the requested window.
            end_time: End of the requested window.

        Raises:
            BookingConflictException: If the window overlaps an existing active booking.
            BookingGapConflictException: If the company's gap rule is violated.
        """
        if repo.find_overlap(user_id, start_time, end_time):
            raise BookingConflictException()

        config = repo.get_config(company_id)
        gap_minutes = config.gap_minutes if config else 0

        if gap_minutes > 0:
            gap = timedelta(minutes=gap_minutes)
            if repo.find_gap_overlap(user_id, company_id, start_time, end_time, gap):
                raise BookingGapConflictException()

    def get_my_bookings(self, user_id: int, repo: BookingRepository):
        """Returns all bookings for a user, enriched with service names.

        Args:
            user_id: The user whose bookings to fetch.
            repo: The booking repository.

        Returns:
            List of BookingResponse with service names resolved from company-service.
        """
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
        """Returns a single booking by ID, enriched with service name.

        Args:
            booking_id: The booking primary key.
            repo: The booking repository.

        Returns:
            A BookingResponse.

        Raises:
            BookingNotFoundException: If no booking exists with the given ID.
        """
        booking = repo.get_or_raise(booking_id)
        return self._to_response(booking)

    def cancel_booking(self, booking_id: int, current_user: UserPrincipal, repo: BookingRepository, language: str = "en"):
        """Cancels a booking and publishes a ``booking.cancelled`` event.

        Args:
            booking_id: The booking to cancel.
            current_user: The authenticated user performing the cancellation.
            repo: The booking repository.
            language: The Accept-Language header value for notification emails.

        Raises:
            BookingNotFoundException: If the booking does not exist.
            BookingForbiddenException: If a USER tries to cancel another user's booking.
            BookingAlreadyCancelledException: If the booking is already cancelled.
        """
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
        """Reschedules a booking to a new time window.

        Args:
            booking_id: The booking to reschedule.
            current_user: The authenticated user performing the reschedule.
            dto: The new start and end times.
            repo: The booking repository.

        Returns:
            A BookingResponse with the updated times.

        Raises:
            InvalidBookingTimeException: If start_time >= end_time.
            BookingNotFoundException: If the booking does not exist.
            BookingForbiddenException: If a USER tries to reschedule another user's booking.
            BookingAlreadyCancelledException: If the booking is already cancelled.
            BookingConflictException: If the new window overlaps another booking.
            BookingGapConflictException: If the company's gap rule is violated.
        """
        if dto.start_time >= dto.end_time:
            raise InvalidBookingTimeException()

        booking = self._find_booking_to_patch(repo, booking_id, current_user)
        self._check_collision(repo, current_user.user_id, booking.company_id, dto.start_time, dto.end_time)

        booking.start_time = dto.start_time
        booking.end_time = dto.end_time
        repo.commit()
        return self._to_response(booking)

    def confirm_booking(self, booking_id: int, repo: BookingRepository, language: str = "en"):
        """Confirms a PENDING booking and publishes a ``booking.confirmed`` event.

        Args:
            booking_id: The booking to confirm.
            repo: The booking repository.
            language: The Accept-Language header value for notification emails.

        Raises:
            BookingNotFoundException: If the booking does not exist.
            InvalidStatusTransitionException: If the booking is not in PENDING status.
        """
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
        """Marks a CONFIRMED booking as COMPLETED.

        Args:
            booking_id: The booking to complete.
            repo: The booking repository.

        Raises:
            BookingNotFoundException: If the booking does not exist.
            InvalidStatusTransitionException: If the booking is not in CONFIRMED status.
        """
        booking = repo.get_or_raise(booking_id)
        transition(booking, BookingStatus.COMPLETED)
        repo.commit()

    def get_company_bookings(self, company_id: int, status: str | None, from_date: date | None, to_date: date | None, full_name: str | None, repo: BookingRepository) -> list[BookingDetailResponse]:
        """Returns bookings for a company, enriched with user and service names.

        User and service data are fetched in two batch calls to avoid N+1 queries.
        Full name filtering is applied in-memory after fetching.

        Args:
            company_id: The company whose bookings to fetch.
            status: Optional status filter (e.g. "PENDING").
            from_date: Optional lower bound for start_time.
            to_date: Optional upper bound for start_time.
            full_name: Optional full name search (first name, last name, or combined).
            repo: The booking repository.

        Returns:
            List of BookingDetailResponse with user and service data resolved.
        """
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
        """Returns aggregated booking statistics for a company over a date range.

        Args:
            company_id: The company ID.
            start_date: Start of the reporting period.
            end_date: End of the reporting period.
            repo: The booking repository.

        Returns:
            A BookingStatsResponse with totals, status breakdown, revenue, and daily series.
        """
        stats = repo.get_stats(company_id, start_date, end_date)
        return BookingStatsResponse(
            total_bookings=stats["total"],
            bookings_by_status=stats["counts"],
            total_revenue=stats["revenue"],
            bookings_by_period=stats["period_rows"],
        )

    def _to_response(self, booking: Booking) -> BookingResponse:
        """Maps a Booking entity to a BookingResponse, resolving the service name.

        Args:
            booking: The Booking entity to map.

        Returns:
            A BookingResponse with service name resolved from company-service.
        """
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
