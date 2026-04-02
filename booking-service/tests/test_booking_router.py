import pytest
from unittest.mock import patch, MagicMock
from datetime import datetime

USER = None 

with patch("app.database.engine") as mock_engine, \
     patch("sqlalchemy.schema.MetaData.create_all"):
    from fastapi.testclient import TestClient
    from app.main import app
    from app.dependencies.auth import UserPrincipal, get_current_user
    from app.database import get_db
    from app.models.booking import Booking, BookingStatus
    from app.exceptions import (
        BookingNotFoundException,
        BookingConflictException,
        BookingGapConflictException,
        BookingForbiddenException,
        BookingAlreadyCancelledException,
    )

USER = UserPrincipal(user_id=1, email="user@test.com", role="USER", company_id=None)
ADMIN = UserPrincipal(user_id=2, email="admin@test.com", role="ADMIN", company_id=10)

mock_db = MagicMock()

app.dependency_overrides[get_db] = lambda: mock_db
app.dependency_overrides[get_current_user] = lambda: USER

client = TestClient(app)

def build_booking_response(id=1, user_id=1, service_id=5):
    return MagicMock(
        id=id,
        user_id=user_id,
        service_id=service_id,
        service_name="Test Service",
        start_time=datetime(2025, 6, 1, 10, 0),
        end_time=datetime(2025, 6, 1, 11, 0),
        price=5000.00,
        status="PENDING",
        created_at=datetime(2025, 6, 1, 9, 0),
    )


def test_create_booking_returns201():
    with patch("app.routers.booking.BookingService.create_booking") as mock_create:
        mock_create.return_value = MagicMock(
            id=1,
            user_id=USER.user_id,
            service_id=5,
            service_name="Test Service",
            start_time=datetime(2025, 6, 1, 10, 0),
            end_time=datetime(2025, 6, 1, 11, 0),
            price=5000.00,
            status="PENDING",
            created_at=datetime(2025, 6, 1, 9, 0),
        )

        response = client.post("/bookings", json={
            "service_id": 5,
            "company_id": 10,
            "start_time": "2025-06-01T10:00:00",
            "end_time": "2025-06-01T11:00:00",
            "price": 5000.00
        })

        assert response.status_code == 201
        data = response.json()
        assert data["service_id"] == 5
        assert data["status"] == "PENDING"
        assert data["user_id"] == USER.user_id
        assert data["id"] == 1
        assert data["start_time"] == "2025-06-01T10:00:00"
        assert data["end_time"] == "2025-06-01T11:00:00"
    
def test_get_my_bookings_returnsList():
    with patch("app.routers.booking.BookingService.get_my_bookings") as mock_get:
        mock_get.return_value = [MagicMock(
            id=1,
            user_id=USER.user_id,
            service_id=5,
            service_name="Test Service",
            start_time=datetime(2025, 6, 1, 10, 0),
            end_time=datetime(2025, 6, 1, 11, 0),
            price=5000.00,
            status="PENDING",
            created_at=datetime(2025, 6, 1, 9, 0),
        ),MagicMock(
            id=2,
            user_id=USER.user_id,
            service_id=4,
            service_name="Test Service 2",
            start_time=datetime(2025, 7, 1, 10, 0),
            end_time=datetime(2025, 7, 1, 11, 0),
            price=3000.00,
            status="PENDING",
            created_at=datetime(2025, 6, 1, 9, 0),
        )]

        response = client.get("/bookings/my")

        assert response.status_code == 200
        assert len(response.json()) == 2



def test_get_booking_whenExists_returns200():
    with patch("app.routers.booking.BookingService.get_booking") as mock_get:
        mock_get.return_value = build_booking_response()

        response = client.get("/bookings/1")

        assert response.status_code == 200
        assert response.json()["id"] == 1

def test_get_booking_whenNotFound_returns404():
    with patch("app.routers.booking.BookingService.get_booking") as mock_get:
        mock_get.side_effect = BookingNotFoundException()

        response = client.get("/bookings/1")

        assert response.status_code == 404



def test_cancel_booking_whenOwner_returns200():
    with patch("app.routers.booking.BookingService.cancel_booking") as mock_cancel:
        mock_cancel.return_value = build_booking_response()

        response = client.patch("/bookings/1/cancel")

        assert response.status_code == 200
        assert response.json()["id"] == 1

def test_cancel_booking_whenNotFound_returns404():
    with patch("app.routers.booking.BookingService.cancel_booking") as mock_cancel:
        mock_cancel.side_effect = BookingNotFoundException()

        response = client.patch("/bookings/1/cancel")

        assert response.status_code == 404

def test_cancel_booking_whenForbidden_returns403():
    with patch("app.routers.booking.BookingService.cancel_booking") as mock_cancel:
        mock_cancel.side_effect = BookingForbiddenException()

        response = client.patch("/bookings/1/cancel")

        assert response.status_code == 403

def test_cancel_booking_whenAlreadyCancelled_returns400():
    with patch("app.routers.booking.BookingService.cancel_booking") as mock_cancel:
        mock_cancel.side_effect = BookingAlreadyCancelledException()

        response = client.patch("/bookings/1/cancel")

        assert response.status_code == 400