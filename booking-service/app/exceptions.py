class MissingUserIdException(Exception):
    def __init__(self):
        super().__init__("user_id is required for admins")

class BookingNotFoundException(Exception):
    def __init__(self):
        super().__init__("Booking not found")

class BookingConflictException(Exception):
    def __init__(self):
        super().__init__("Booking conflicts with an existing booking")

class BookingGapConflictException(Exception):
    def __init__(self):
        super().__init__("Booking conflicts with company gap policy")

class BookingForbiddenException(Exception):
    def __init__(self):
        super().__init__("Not your booking")

class BookingAlreadyCancelledException(Exception):
    def __init__(self):
        super().__init__("Booking already cancelled")

class InvalidBookingTimeException(Exception):
    def __init__(self):
        super().__init__("start_time must be before end_time")
