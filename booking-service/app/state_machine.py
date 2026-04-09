from app.models.booking import BookingStatus
from app.exceptions import InvalidStatusTransitionException

TRANSITIONS: dict[BookingStatus, set[BookingStatus]] = {
    BookingStatus.PENDING:   {BookingStatus.CONFIRMED, BookingStatus.CANCELLED},
    BookingStatus.CONFIRMED: {BookingStatus.COMPLETED, BookingStatus.CANCELLED},
    BookingStatus.COMPLETED: set(),
    BookingStatus.CANCELLED: set(),
}

def transition(booking, target: BookingStatus) -> None:
    allowed = TRANSITIONS.get(booking.status, set())
    if target not in allowed:
        raise InvalidStatusTransitionException(booking.status.value, target.value)
    booking.status = target
