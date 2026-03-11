from sqlalchemy.orm import Session
from app.models.booking import Booking, BookingStatus
from app.models.booking_config import BookingConfig
from app.schemas.booking import BookingCreate
from app.dependencies.auth import UserPrincipal
from fastapi import HTTPException
from app.dependencies.company_client import validate_service
from datetime import timedelta

class BookingService:
    def create_booking(self, dto: BookingCreate, current_user: UserPrincipal, db:Session):
        if current_user.role == "USER":
            target_user_id = current_user.user_id
        else:
            if dto.user_id is None:
                raise HTTPException(status_code=400, detail="user_id is required for admins")
            target_user_id = dto.user_id

        validate_service(dto.service_id)
        self._check_collision(target_user_id, dto.company_id, dto.start_time, dto.end_time, db)

        booking = Booking(
            user_id=target_user_id,
            service_id=dto.service_id,
            company_id=dto.company_id,
            start_time=dto.start_time,
            end_time=dto.end_time,
            status=BookingStatus.PENDING,
        )

        db.add(booking)
        db.commit()
        db.refresh(booking)
        return booking
    
    def _check_collision(self, user_id: int, company_id: int, start_time, end_time, db: Session):
        raw_conflict = db.query(Booking).filter(
            Booking.user_id == user_id,
            Booking.status != BookingStatus.CANCELLED,
            Booking.start_time < end_time,
            Booking.end_time > start_time,
        ).first()

        if raw_conflict:
            raise HTTPException(status_code=409, detail="Booking conflicts with an existing booking")

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
                raise HTTPException(status_code=409, detail="Booking conflicts with company gap policy")


    
    def get_my_bookings(self, user_id: int, db:Session):
        return db.query(Booking).filter(Booking.user_id == user_id).all()
    
    def get_booking(self, booking_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        if booking is None:
            raise HTTPException(status_code=404, detail="Booking not found")
        return booking
    
    def cancel_booking(self, booking_id: int, user_id: int, db: Session):
        booking = db.query(Booking).filter(Booking.id == booking_id).first()
        
        if booking is None:
            raise HTTPException(status_code=404, detail="Booking not found")
        
        if booking.user_id != user_id:
            raise HTTPException(status_code=403, detail="Not your booking")
        
        if booking.status == BookingStatus.CANCELLED:
            raise HTTPException(status_code=400, detail="Booking already cancelled")
        
        booking.status = BookingStatus.CANCELLED
        db.commit()
        db.refresh(booking)
        return booking