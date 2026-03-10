from pydantic import BaseModel
from datetime import datetime

class BookingCreate(BaseModel):
    service_id: int
    user_id: int
    start_time: int
    end_time: int

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