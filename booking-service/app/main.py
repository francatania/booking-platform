from fastapi import FastAPI
from app.database import Base, engine
from app.models import booking, booking_config
from app.routers import booking as booking_router

Base.metadata.create_all(bind=engine)

app = FastAPI()

app.include_router(booking_router.router)

@app.get("/ping")
def ping():
    return {"message": "pong"}
