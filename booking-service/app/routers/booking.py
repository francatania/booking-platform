from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.dependencies.auth import get_current_user, UserPrincipal
from app.schemas.booking import BookingCreate, BookingResponse
from app.services.booking import BookingService

router = APIRouter(prefix="/bookings", tags=["bookings"])
service = BookingService()

@router.post("", status_code=status.HTTP_201_CREATED, response_model=BookingResponse)
def create_booking(
    dto: BookingCreate,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(get_current_user)
):
    return service.create_booking(dto, current_user, db)

@router.get("/my", response_model=list[BookingResponse])
def get_my_bookings(
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(get_current_user)
):
    return service.get_my_bookings(current_user.user_id, db)

@router.get("/{booking_id}", response_model=BookingResponse)
def get_booking(
    booking_id: int,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(get_current_user)
):
    return service.get_booking(booking_id, db)

@router.patch("/{booking_id}/cancel", response_model=BookingResponse)
def cancel_booking(
    booking_id: int,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(get_current_user)
):
    return service.cancel_booking(booking_id, current_user.user_id, db)
