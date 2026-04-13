from dataclasses import dataclass
from fastapi import Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import jwt, JWTError
from app.config import settings
from app.exceptions import InvalidTokenException, InsufficientPermissionsException

bearer_scheme = HTTPBearer(auto_error=False)

@dataclass
class UserPrincipal:
    user_id: int
    email: str
    role: str
    company_id: int | None

def get_current_user(credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme)) -> UserPrincipal:
    if credentials is None:
        raise InvalidTokenException()
    token = credentials.credentials

    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=["HS256"])
        user_id: int = payload.get("userId")
        email: str = payload.get("sub")
        role: str = payload.get("role")
        company_id: int | None = payload.get("companyId")

        if user_id is None or email is None or role is None:
            raise InvalidTokenException()

    except JWTError:
        raise InvalidTokenException()

    return UserPrincipal(user_id=user_id, email=email, role=role, company_id=company_id)

def require_roles(*roles: str):
    def dependency(current_user: UserPrincipal = Depends(get_current_user)) -> UserPrincipal:
        if current_user.role not in roles:
            raise InsufficientPermissionsException()
        return current_user
    return dependency
