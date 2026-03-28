from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.dependencies.auth import get_current_user, UserPrincipal, require_roles
from app.schemas.booking import BookingCreate, BookingResponse, BookingDetailResponse, RescheduleRequest, RescheduleResponse, BookingStatsResponse
from datetime import datetime
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

@router.get("/company", response_model=list[BookingDetailResponse])
def get_company_bookings(
    status: str | None = None,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR", "ADMIN"))
):
    return service.get_company_bookings(current_user.company_id, status, db)

@router.patch("/{booking_id}/confirm", response_model=BookingResponse)
def confirm_booking(
    booking_id: int,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR"))
):
    return service.confirm_booking(booking_id, db)

@router.patch("/{booking_id}/complete", response_model=BookingResponse)
def complete_booking(
    booking_id: int,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR", "SUPER_ADMIN"))
):
    return service.complete_booking(booking_id, db)

@router.get("/stats", response_model=BookingStatsResponse)
def get_stats(
    from_date: datetime,
    to_date: datetime,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(require_roles("ADMIN", "MANAGER"))
):
    return service.getStats(current_user.company_id, from_date, to_date, db)

@router.get("/{booking_id}", response_model=BookingResponse)
def get_booking(
    booking_id: int,
    db: Session = Depends(get_db),):
    return service.get_booking(booking_id, db)

@router.patch("/{booking_id}/cancel", response_model=BookingResponse)
def cancel_booking(
    booking_id: int,
    db: Session = Depends(get_db),
    current_user: UserPrincipal = Depends(get_current_user)
):
    return service.cancel_booking(booking_id, current_user, db)


@router.patch("/{booking_id}/reschedule", response_model=RescheduleResponse)
def reschedule_booking(
    booking_id:int,
    dto: RescheduleRequest,
    current_user: UserPrincipal = Depends(get_current_user),
    db: Session = Depends(get_db)):
        return service.reschedule(booking_id, current_user, dto, db)