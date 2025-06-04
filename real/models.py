from pydantic import BaseModel, EmailStr

class PaymentInitiateRequest(BaseModel):
    amount: float
    email: EmailStr
    order_id: str

class PaymentVerifyResponse(BaseModel):
    status: str
    amount: float
    order_id: str
