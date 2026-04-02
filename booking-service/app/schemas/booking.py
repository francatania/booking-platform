from pydantic import BaseModel
from datetime import datetime, date
from decimal import Decimal

class BookingCreate(BaseModel):
    service_id: int
    company_id: int
    start_time: datetime
    end_time: datetime
    price: Decimal
    user_id: int | None = None

class BookingResponse(BaseModel):
    id: int
    user_id: int
    service_id: int
    service_name: str
    start_time: datetime
    end_time: datetime
    price: Decimal
    status: str
    created_at: datetime
    updated_at: datetime | None = None

    class Config:
        from_attributes = True

class BookingPeriodStat(BaseModel):
    date: date
    count: int
    revenue: Decimal

class BookingStatsResponse(BaseModel):
    total_bookings: int
    bookings_by_status: dict[str, int]
    total_revenue: Decimal
    bookings_by_period: list[BookingPeriodStat]

class BookingDetailResponse(BaseModel):
    id: int
    user_id: int
    service_id: int
    company_id: int
    user_username: str
    user_first_name: str
    user_last_name: str
    service_name: str
    start_time: datetime
    end_time: datetime
    price: Decimal
    status: str
    created_at: datetime

    class Config:
        from_attributes = True

class RescheduleRequest(BaseModel):
    start_time: datetime
    end_time: datetime

