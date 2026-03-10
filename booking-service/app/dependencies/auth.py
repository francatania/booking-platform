from dataclasses import dataclass
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import jwt, JWTError
from app.config import settings

bearer_scheme = HTTPBearer()

@dataclass
class UserPrincipal:
    user_id: int
    email: str
    role: str
    company_id: int | None

def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme)) -> UserPrincipal:
    token = credentials.credentials

    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=["HS256"])
        user_id: int = payload.get("userId")
        email: str = payload.get("sub")
        role: str = payload.get("role")
        company_id: int | None = payload.get("companyId")

        if user_id is None or email is None or role is None:
            raise HTTPException(status_code=401, detail="Invalid token")

    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    return UserPrincipal(user_id=user_id, email=email, role=role, company_id=company_id)
