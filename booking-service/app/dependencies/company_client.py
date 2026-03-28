from fastapi import HTTPException
import httpx
from app.config import settings

def validate_service(service_id: int):
    url = f"{settings.company_service_url}/internal/services/{service_id}"
    response = httpx.get(url)
    if response.status_code != 200:
        error = response.json()
        raise HTTPException(status_code=response.status_code, detail=error.get("error"))

def get_services_by_ids(ids: list[int]) -> dict[int, str]:
    if not ids:
        return {}
    url = f"{settings.company_service_url}/internal/services"
    response = httpx.get(url, params={"ids": ids})
    if response.status_code != 200:
        return {}
    return {s["id"]: s["name"] for s in response.json()}