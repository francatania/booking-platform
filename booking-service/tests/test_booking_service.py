import pytest
from unittest.mock import MagicMock, patch
from datetime import datetime

from app.services.booking import BookingService
from app.repositories.booking_repository import BookingRepository
from app.dependencies.auth import UserPrincipal
from app.schemas.booking import BookingCreate
from app.models.booking import Booking, BookingStatus
from app.exceptions import (
    MissingUserIdException,
    BookingNotFoundException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
)

USER  = UserPrincipal(user_id=1, email="user@test.com",  role="USER",  company_id=None)
ADMIN = UserPrincipal(user_id=2, email="admin@test.com", role="ADMIN", company_id=10)

START = datetime(2025, 6, 1, 10, 0)
END   = datetime(2025, 6, 1, 11, 0)

@pytest.fixture
def service():
    return BookingService()

@pytest.fixture
def repo():
    mock = MagicMock(spec=BookingRepository)
    mock.get_by_id.return_value = None
    mock.get_or_raise.return_value = None
    mock.find_overlap.return_value = None
    mock.find_gap_overlap.return_value = None
    mock.get_config.return_value = None
    return mock

def build_booking(id=1, user_id=1, status=BookingStatus.PENDING):
    booking = MagicMock(spec=Booking)
    booking.id = id
    booking.user_id = user_id
    booking.service_id = 5
    booking.company_id = 10
    booking.price = 5000.00
    booking.start_time = START
    booking.end_time = END
    booking.status = status
    booking.created_at = datetime(2025, 6, 1, 9, 0)
    return booking

def build_booking_dto(service_id=5, company_id=5, start_time=START, end_time=END, user_id=None, price=5000.00, service_name=None):
    dto = MagicMock(spec=BookingCreate)
    dto.service_id = service_id
    dto.company_id = company_id
    dto.start_time = start_time
    dto.end_time = end_time
    dto.user_id = user_id
    dto.price = price
    dto.service_name = service_name
    return dto


def test_create_booking_whenIsUser(service, repo):
    dto = build_booking_dto()
    booking = build_booking()
    repo.save.return_value = booking

    with patch("app.services.booking.validate_service"), \
         patch("app.services.booking.get_services_by_ids", return_value={}), \
         patch("app.services.booking.publish_event"):
        result = service.create_booking(dto, USER, repo)

    repo.save.assert_called_once()
    assert result.user_id == USER.user_id
    assert result.service_id == dto.service_id
    assert result.status == BookingStatus.PENDING.value

def test_create_booking_whenIsAdmin(service, repo):
    dto = build_booking_dto(user_id=1)
    booking = build_booking()
    repo.save.return_value = booking

    with patch("app.services.booking.validate_service"), \
         patch("app.services.booking.get_services_by_ids", return_value={}), \
         patch("app.services.booking.publish_event"):
        result = service.create_booking(dto, ADMIN, repo)

    repo.save.assert_called_once()
    assert result.user_id == USER.user_id
    assert result.service_id == dto.service_id
    assert result.status == BookingStatus.PENDING.value

def test_create_booking_whenAdminAndNoUserId_raisesException(service, repo):
    dto = build_booking_dto()
    with pytest.raises(MissingUserIdException):
        service.create_booking(dto, ADMIN, repo)


def test_check_collision_whenRawCollision_raisesException(service, repo):
    repo.find_overlap.return_value = MagicMock()
    with pytest.raises(BookingConflictException):
        service._check_collision(repo, user_id=1, company_id=10, start_time=START, end_time=END)

def test_check_collision_whenGapCollision_raisesGapException(service, repo):
    config = MagicMock()
    config.gap_minutes = 30
    repo.get_config.return_value = config
    repo.find_gap_overlap.return_value = MagicMock()
    with pytest.raises(BookingGapConflictException):
        service._check_collision(repo, user_id=1, company_id=10, start_time=START, end_time=END)

def test_check_collision_whenGapAndNoCollision_doesNotRaise(service, repo):
    config = MagicMock()
    config.gap_minutes = 30
    repo.get_config.return_value = config
    service._check_collision(repo, user_id=1, company_id=10, start_time=START, end_time=END)

def test_check_collision_whenNoGapAndNoCollision_doesNotRaise(service, repo):
    service._check_collision(repo, user_id=1, company_id=10, start_time=START, end_time=END)


def test_get_my_bookings_returnsBookingList(service, repo):
    repo.get_by_user.return_value = [build_booking(), build_booking(id=2)]

    with patch("app.services.booking.get_services_by_ids", return_value={}):
        result = service.get_my_bookings(user_id=1, repo=repo)

    assert len(result) == 2


def test_get_booking_whenExists_returnsBooking(service, repo):
    booking = build_booking(id=1)
    repo.get_or_raise.return_value = booking

    with patch("app.services.booking.get_services_by_ids", return_value={}):
        result = service.get_booking(booking_id=1, repo=repo)

    assert result.id == booking.id
    assert result.user_id == booking.user_id

def test_get_booking_whenDoesnotExists_raisesException(service, repo):
    repo.get_or_raise.side_effect = BookingNotFoundException
    with pytest.raises(BookingNotFoundException):
        service.get_booking(booking_id=1, repo=repo)


def test_cancel_booking_whenDoesnotExists_raisesBookingNotFoundException(service, repo):
    repo.get_or_raise.side_effect = BookingNotFoundException
    with pytest.raises(BookingNotFoundException):
        service.cancel_booking(1, USER, repo)

def test_cancel_booking_whenUserIdDoesnotMatch_raisesBookingForbiddenException(service, repo):
    repo.get_or_raise.return_value = build_booking(user_id=99)
    with pytest.raises(BookingForbiddenException):
        service.cancel_booking(1, USER, repo)

def test_cancel_booking_whenBookingAlreadyCancelled_raisesBookingAlreadyCancelledException(service, repo):
    repo.get_or_raise.return_value = build_booking(status=BookingStatus.CANCELLED)
    with pytest.raises(BookingAlreadyCancelledException):
        service.cancel_booking(1, USER, repo)

def test_cancel_booking_whenIsOk_returnsNone(service, repo):
    booking = build_booking()
    repo.get_or_raise.return_value = booking

    with patch("app.services.booking.get_services_by_ids", return_value={}), \
         patch("app.services.booking.publish_event"):
        result = service.cancel_booking(1, USER, repo)

    assert result is None
    assert booking.status == BookingStatus.CANCELLED
