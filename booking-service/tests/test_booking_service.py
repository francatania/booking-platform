import pytest
from unittest.mock import MagicMock, patch
from datetime import datetime

from app.services.booking import BookingService
from app.dependencies.auth import UserPrincipal
from app.schemas.booking import BookingCreate
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.exceptions import (
    MissingUserIdException,
    BookingNotFoundException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
)

USER = UserPrincipal(user_id=1, email="user@test.com", role="USER", company_id=None)
ADMIN = UserPrincipal(user_id=2, email="admin@test.com", role="ADMIN", company_id=10)

START = datetime(2025, 6, 1, 10, 0)
END   = datetime(2025, 6, 1, 11, 0)

@pytest.fixture
def service():
    return BookingService()

@pytest.fixture
def db():
    mock = MagicMock()
    mock.query.return_value.filter.return_value.first.return_value = None
    return mock

def build_booking(id=1, user_id=1, status=BookingStatus.PENDING):
    booking = MagicMock(spec=Booking)
    booking.id = id
    booking.user_id = user_id
    booking.status = status
    return booking

def build_booking_dto(service_id=5, company_id=5, start_time=START, end_time=END, user_id=None, price=5000.00):
    dto = MagicMock(spec=BookingCreate)
    dto.service_id = service_id
    dto.company_id = company_id
    dto.start_time = start_time
    dto.end_time = end_time
    dto.user_id = user_id
    dto.price = price
    return dto

def make_query_side_effect(gap_minutes=0, raw_collision=False, gap_collision=False):
    booking_calls = [raw_collision, gap_collision]
    booking_index = [0]

    def side_effect(model):
        mock = MagicMock()
        if model == BookingConfig:
            config = MagicMock()
            config.gap_minutes = gap_minutes
            mock.filter.return_value.first.return_value = config
        else:
            has_collision = booking_calls[booking_index[0]]
            booking_index[0] += 1
            mock.filter.return_value.first.return_value = MagicMock() if has_collision else None
        return mock

    return side_effect

def test_create_booking_whenIsUser(service, db):
    dto = build_booking_dto()

    with patch("app.services.booking.validate_service"):
        result = service.create_booking(dto, USER, db)

    db.add.assert_called_once_with(result)
    db.commit.assert_called_once()
    db.refresh.assert_called_once_with(result)
    assert result.user_id == USER.user_id     
    assert result.service_id == dto.service_id
    assert result.company_id == dto.company_id
    assert result.status == BookingStatus.PENDING

def test_create_booking_whenIsAdmin(service, db):
    dto = build_booking_dto(user_id=1)

    with patch("app.services.booking.validate_service"):
        result = service.create_booking(dto, ADMIN, db)

    db.add.assert_called_once_with(result)
    db.commit.assert_called_once()
    db.refresh.assert_called_once_with(result)
    assert result.user_id == USER.user_id     
    assert result.service_id == dto.service_id
    assert result.company_id == dto.company_id
    assert result.status == BookingStatus.PENDING

def test_create_booking_whenAdminAndNoUserId_raisesException(service, db):
    dto = build_booking_dto()
    with pytest.raises(MissingUserIdException):
            service.create_booking(
            dto,
            ADMIN,
            db,
        )



def test_check_collision_whenRawCollision_raisesException(service, db):
    db.query.side_effect = make_query_side_effect(raw_collision=True)
    with pytest.raises(BookingConflictException):
        service._check_collision(user_id=1, company_id=10, start_time=START, end_time=END, db=db)

def test_check_collision_whenGapCollision_raisesGapException(service, db):
    db.query.side_effect = make_query_side_effect(gap_minutes=30, raw_collision=False, gap_collision=True)
    with pytest.raises(BookingGapConflictException):
        service._check_collision(user_id=1, company_id=10, start_time=START, end_time=END, db=db)

def test_check_collision_whenGapAndNoCollision_doesNotRaise(service, db):
    db.query.side_effect = make_query_side_effect(gap_minutes=30, raw_collision=False, gap_collision=False)
    service._check_collision(user_id=1, company_id=10, start_time=START, end_time=END, db=db)

def test_check_collision_whenNoGapAndNoCollision_doesNotRaise(service, db):
    db.query.side_effect = make_query_side_effect(gap_minutes=0, raw_collision=False)
    service._check_collision(user_id=1, company_id=10, start_time=START, end_time=END, db=db)

def test_get_my_bookings_returnsBookingList(service, db):
    bookings = [
        build_booking(),
        build_booking(id=2, user_id=1)
    ]
    db.query.return_value.filter.return_value.all.return_value = bookings

    result = service.get_my_bookings(user_id=1, db=db)

    assert result == bookings

def test_get_booking_whenExists_returnsBooking(service, db):
    booking = build_booking(id=1)
    db.query.return_value.filter.return_value.first.return_value = booking

    result = service.get_booking(booking_id=1, db=db)

    assert result == booking

def test_get_booking_whenDoesnotExists_raisesException(service, db):

    with pytest.raises(BookingNotFoundException):
        service.get_booking(booking_id=1, db=db)

def test_cancel_booking_whenDoesnotExists_raisesBookingNotFoundException(service,db):
    with pytest.raises(BookingNotFoundException):
        service.cancel_booking(1, USER, db)

def test_cancel_booking_whenUserIdDoesnotMatch_raisesBookingForbiddenException(service,db):
    booking = build_booking(user_id=99)
    db.query.return_value.filter.return_value.first.return_value = booking
    with pytest.raises(BookingForbiddenException):
        service.cancel_booking(1, USER, db)

def test_cancel_booking_whenBookingAlreadyCancelled_raisesBookingAlreadyCancelledException(service,db):
    booking = build_booking(status=BookingStatus.CANCELLED)
    db.query.return_value.filter.return_value.first.return_value = booking
    with pytest.raises(BookingAlreadyCancelledException):
        service.cancel_booking(1, USER, db)

def test_cancel_booking_whenIsOk_returnsBooking(service, db):
    booking = build_booking()
    db.query.return_value.filter.return_value.first.return_value = booking

    result = service.cancel_booking(1, USER, db)
    assert result.user_id == booking.user_id
    assert result.id == booking.id
    assert result.status == BookingStatus.CANCELLED