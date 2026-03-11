from app.database import Base
from sqlalchemy import Column, Integer

class BookingConfig(Base):
    __tablename__="booking_config"
    id = Column(Integer, primary_key=True)
    company_id = Column(Integer, nullable=False, unique=True)
    gap_minutes = Column(Integer, nullable=False, default=0)