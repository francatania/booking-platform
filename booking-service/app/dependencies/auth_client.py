import httpx
from app.config import settings

def get_users_by_ids(ids: list[int]) -> dict[int, dict]:
    if not ids:
        return {}
    url = f"{settings.auth_service_url}/internal/users"
    response = httpx.get(url, params={"ids": ids})
    if response.status_code != 200:
        return {}
    return {u["id"]: u for u in response.json()}
