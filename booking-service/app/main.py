from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from app.database import Base, engine
from app.models import booking, booking_config
from app.routers import booking as booking_router
from app.exceptions import (
    MissingUserIdException,
    BookingNotFoundException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
    InvalidStatusTransitionException,
)

Base.metadata.create_all(bind=engine)

app = FastAPI()

@app.exception_handler(MissingUserIdException)
def handle_missing_user_id(_request: Request, exc: MissingUserIdException):
    return JSONResponse(status_code=400, content={"detail": str(exc)})

@app.exception_handler(BookingNotFoundException)
def handle_not_found(_request: Request, exc: BookingNotFoundException):
    return JSONResponse(status_code=404, content={"detail": str(exc)})

@app.exception_handler(BookingConflictException)
def handle_conflict(_request: Request, exc: BookingConflictException):
    return JSONResponse(status_code=409, content={"detail": str(exc)})

@app.exception_handler(BookingGapConflictException)
def handle_gap_conflict(_request: Request, exc: BookingGapConflictException):
    return JSONResponse(status_code=409, content={"detail": str(exc)})

@app.exception_handler(BookingForbiddenException)
def handle_forbidden(_request: Request, exc: BookingForbiddenException):
    return JSONResponse(status_code=403, content={"detail": str(exc)})

@app.exception_handler(BookingAlreadyCancelledException)
def handle_already_cancelled(_request: Request, exc: BookingAlreadyCancelledException):
    return JSONResponse(status_code=400, content={"detail": str(exc)})

@app.exception_handler(InvalidStatusTransitionException)
def handle_invalid_transition(_request: Request, exc: InvalidStatusTransitionException):
    return JSONResponse(status_code=409, content={"detail": str(exc)})

app.include_router(booking_router.router)

@app.get("/ping")
def ping():
    return {"message": "pong"}
