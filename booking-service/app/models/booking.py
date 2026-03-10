from sqlalchemy import Column, Integer, DateTime, Enum as SAEnum
from sqlalchemy.sql import func
from app.database import Base
import enum

class BookingStatus(enum.Enum):
    PENDING = "PENDING"
    CONFIRMED = "CONFIRMED"
    CANCELLED = "CANCELLED"

class Booking(Base):
    __tablename__ = "booking"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    service_id = Column(Integer, nullable=False)
    company_id = Column(Integer, nullable=False)
    start_time = Column(DateTime, nullable=False)
    end_time = Column(DateTime, nullable=False) 
    status = Column(SAEnum(BookingStatus), nullable=False, default=BookingStatus.PENDING)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())