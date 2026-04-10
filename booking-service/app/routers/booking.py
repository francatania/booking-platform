from fastapi import APIRouter, Depends, Request, Response, status
from app.database import get_repo
from app.dependencies.auth import get_current_user, UserPrincipal, require_roles
from app.repositories.booking_repository import BookingRepository
from app.schemas.booking import BookingCreate, BookingResponse, BookingDetailResponse, RescheduleRequest, BookingStatsResponse
from datetime import datetime, date
from app.services.booking import BookingService

router = APIRouter(prefix="/bookings", tags=["bookings"])
service = BookingService()

@router.post("", status_code=status.HTTP_201_CREATED, response_model=BookingResponse)
def create_booking(
    request: Request,
    dto: BookingCreate,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(get_current_user),
):
    language = request.headers.get("accept-language", "en")
    return service.create_booking(dto, current_user, repo, language)

@router.get("/my", response_model=list[BookingResponse])
def get_my_bookings(
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(get_current_user),
):
    return service.get_my_bookings(current_user.user_id, repo)

@router.get("/company", response_model=list[BookingDetailResponse])
def get_company_bookings(
    status: str | None = None,
    from_date: date | None = None,
    to_date: date | None = None,
    full_name: str | None = None,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR", "ADMIN")),
):
    return service.get_company_bookings(current_user.company_id, status, from_date, to_date, full_name, repo)

@router.patch("/{booking_id}/confirm", status_code=status.HTTP_204_NO_CONTENT, response_class=Response)
def confirm_booking(
    booking_id: int,
    request: Request,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR", "ADMIN")),
):
    language = request.headers.get("accept-language", "en")
    service.confirm_booking(booking_id, repo, language)

@router.patch("/{booking_id}/complete", status_code=status.HTTP_204_NO_CONTENT, response_class=Response)
def complete_booking(
    booking_id: int,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(require_roles("OPERATOR", "ADMIN")),
):
    service.complete_booking(booking_id, repo)

@router.get("/stats", response_model=BookingStatsResponse)
def get_stats(
    from_date: datetime,
    to_date: datetime,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(require_roles("ADMIN", "MANAGER")),
):
    return service.getStats(current_user.company_id, from_date, to_date, repo)

@router.get("/{booking_id}", response_model=BookingResponse)
def get_booking(
    booking_id: int,
    repo: BookingRepository = Depends(get_repo),
):
    return service.get_booking(booking_id, repo)

@router.patch("/{booking_id}/cancel", status_code=status.HTTP_204_NO_CONTENT, response_class=Response)
def cancel_booking(
    booking_id: int,
    request: Request,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(get_current_user),
):
    language = request.headers.get("accept-language", "en")
    service.cancel_booking(booking_id, current_user, repo, language)

@router.patch("/{booking_id}/reschedule", response_model=BookingResponse)
def reschedule_booking(
    booking_id: int,
    dto: RescheduleRequest,
    repo: BookingRepository = Depends(get_repo),
    current_user: UserPrincipal = Depends(get_current_user),
):
    return service.reschedule(booking_id, current_user, dto, repo)
