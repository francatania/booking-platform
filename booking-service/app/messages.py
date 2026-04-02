MESSAGES = {
    "en": {
        "missing_user_id": "user_id is required for admins",
        "booking_not_found": "Booking not found",
        "booking_conflict": "Booking conflicts with an existing booking",
        "booking_gap_conflict": "Booking conflicts with company gap policy",
        "booking_forbidden": "Not your booking",
        "booking_already_cancelled": "Booking already cancelled",
        "invalid_booking_time": "start_time must be before end_time",
        "invalid_status_transition": "Cannot transition from {current} to {target}",
        "invalid_token": "Invalid token",
        "insufficient_permissions": "Insufficient permissions",
    },
    "es": {
        "missing_user_id": "user_id es obligatorio para administradores",
        "booking_not_found": "Reserva no encontrada",
        "booking_conflict": "La reserva tiene conflicto con una reserva existente",
        "booking_gap_conflict": "La reserva tiene conflicto con la politica de espacio de la empresa",
        "booking_forbidden": "Esta no es tu reserva",
        "booking_already_cancelled": "La reserva ya fue cancelada",
        "invalid_booking_time": "La hora de inicio debe ser anterior a la hora de fin",
        "invalid_status_transition": "No se puede cambiar de {current} a {target}",
        "invalid_token": "Token invalido",
        "insufficient_permissions": "Permisos insuficientes",
    },
}


def get_message(key: str, lang: str = "en", **kwargs) -> str:
    lang = lang if lang in MESSAGES else "en"
    msg = MESSAGES[lang].get(key, MESSAGES["en"].get(key, key))
    return msg.format(**kwargs) if kwargs else msg
