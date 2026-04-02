class MissingUserIdException(Exception):
    message_key = "missing_user_id"

class BookingNotFoundException(Exception):
    message_key = "booking_not_found"

class BookingConflictException(Exception):
    message_key = "booking_conflict"

class BookingGapConflictException(Exception):
    message_key = "booking_gap_conflict"

class BookingForbiddenException(Exception):
    message_key = "booking_forbidden"

class BookingAlreadyCancelledException(Exception):
    message_key = "booking_already_cancelled"

class InvalidBookingTimeException(Exception):
    message_key = "invalid_booking_time"

class InvalidStatusTransitionException(Exception):
    message_key = "invalid_status_transition"

    def __init__(self, current, target):
        self.current = current
        self.target = target

class InvalidTokenException(Exception):
    message_key = "invalid_token"

class InsufficientPermissionsException(Exception):
    message_key = "insufficient_permissions"
