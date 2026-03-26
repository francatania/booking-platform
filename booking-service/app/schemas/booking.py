from pydantic import BaseModel
from datetime import datetime

class BookingCreate(BaseModel):
    service_id: int
    company_id: int
    start_time: datetime
    end_time: datetime
    user_id: int | None = None

class BookingResponse(BaseModel):
    id: int
    user_id: int
    service_id: int
    start_time: datetime
    end_time: datetime
    status: str
    created_at: datetime

    class Config:
        from_attributes = True

class RescheduleRequest(BaseModel):
    start_time: datetime
    end_time: datetime

class RescheduleResponse(BookingResponse):
    updated_at: datetime

    class Config:
        from_attributes = True
