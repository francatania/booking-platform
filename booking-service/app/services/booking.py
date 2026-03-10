from sqlalchemy.orm import Session
from app.models.booking import Booking, BookingStatus
from app.schemas.booking import BookingCreate
from fastapi import HTTPException

class BookingService:
    def create_booking(self, dto: BookingCreate, user_id: int, db:Session):
        booking = Booking(
            user_id=user_id,
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