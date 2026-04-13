from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.openapi.utils import get_openapi
from app.database import Base, engine
from app.models import booking, booking_config
from app.routers import booking as booking_router
from app.messages import get_message
from app.exceptions import (
    MissingUserIdException,
    BookingNotFoundException,
    BookingConflictException,
    BookingGapConflictException,
    BookingForbiddenException,
    BookingAlreadyCancelledException,
    InvalidStatusTransitionException,
    InvalidTokenException,
    InsufficientPermissionsException,
)

Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Booking Service",
    description="Manages bookings: creation, cancellation, rescheduling, confirmation, and stats.",
    version="1.0.0",
)


def _lang(request: Request) -> str:
    return request.headers.get("accept-language", "en")


@app.exception_handler(MissingUserIdException)
def handle_missing_user_id(request: Request, exc: MissingUserIdException):
    return JSONResponse(status_code=400, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(BookingNotFoundException)
def handle_not_found(request: Request, exc: BookingNotFoundException):
    return JSONResponse(status_code=404, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(BookingConflictException)
def handle_conflict(request: Request, exc: BookingConflictException):
    return JSONResponse(status_code=409, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(BookingGapConflictException)
def handle_gap_conflict(request: Request, exc: BookingGapConflictException):
    return JSONResponse(status_code=409, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(BookingForbiddenException)
def handle_forbidden(request: Request, exc: BookingForbiddenException):
    return JSONResponse(status_code=403, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(BookingAlreadyCancelledException)
def handle_already_cancelled(request: Request, exc: BookingAlreadyCancelledException):
    return JSONResponse(status_code=400, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(InvalidStatusTransitionException)
def handle_invalid_transition(request: Request, exc: InvalidStatusTransitionException):
    return JSONResponse(status_code=409, content={"detail": get_message(exc.message_key, _lang(request), current=exc.current, target=exc.target)})

@app.exception_handler(InvalidTokenException)
def handle_invalid_token(request: Request, exc: InvalidTokenException):
    return JSONResponse(status_code=401, content={"detail": get_message(exc.message_key, _lang(request))})

@app.exception_handler(InsufficientPermissionsException)
def handle_insufficient_permissions(request: Request, exc: InsufficientPermissionsException):
    return JSONResponse(status_code=403, content={"detail": get_message(exc.message_key, _lang(request))})

app.include_router(booking_router.router)

@app.get("/ping")
def ping():
    return {"message": "pong"}


def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema
    schema = get_openapi(
        title=app.title,
        version=app.version,
        description=app.description,
        routes=app.routes,
    )
    schema.setdefault("components", {})["securitySchemes"] = {
        "Bearer": {
            "type": "http",
            "scheme": "bearer",
            "bearerFormat": "JWT",
        }
    }
    schema["security"] = [{"Bearer": []}]

    accept_language_param = {
        "name": "Accept-Language",
        "in": "header",
        "required": False,
        "description": "Language for error messages",
        "schema": {"type": "string", "enum": ["en", "es"], "default": "en"},
    }
    for path in schema.get("paths", {}).values():
        for operation in path.values():
            if isinstance(operation, dict):
                operation["security"] = [{"Bearer": []}]
                operation.setdefault("parameters", []).append(accept_language_param)

    app.openapi_schema = schema
    return schema

app.openapi = custom_openapi
