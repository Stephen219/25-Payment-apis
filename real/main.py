from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel
import httpx
import hmac
import hashlib
import os
from dotenv import load_dotenv
from database import get_db, create_transaction, update_transaction_status
from models import PaymentInitiateRequest, PaymentVerifyResponse
import logging

# Load environment variables
load_dotenv()

# FastAPI app
app = FastAPI(title="Payment Gateway API for Paystack")

# Paystack configuration
PAYSTACK_SECRET = os.getenv("PAYSTACK_SECRET")
PAYSTACK_API = "https://api.paystack.co"
CALLBACK_URL = os.getenv("CALLBACK_URL", "http://localhost:5000/checkout/return")
STORE_CALLBACK_URL = os.getenv("STORE_CALLBACK_URL", "http://localhost:5000/api/payment-callback")

# Logging setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.post("/payment/initiate", response_model=dict)
async def initiate_payment(request: PaymentInitiateRequest):
    """
    Initiate a payment with Paystack.
    """
    reference = f"txn_{int(os.times()[4])}_{request.order_id}"  # Unique reference
    payload = {
        "amount": int(request.amount * 100),  # Convert to kobo
        "email": request.email,
        "reference": reference,
        "callback_url": CALLBACK_URL,
        "channels": ["card", "mobile_money"],  # Card and M-Pesa
        "metadata": {"order_id": request.order_id}
    }

    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{PAYSTACK_API}/transaction/initialize",
                json=payload,
                headers={"Authorization": f"Bearer {PAYSTACK_SECRET}"}
            )
            response.raise_for_status()
            data = response.json()
            print("fastapi") 
            
            print(data)


            # Store transaction
            async with get_db() as db:
                await create_transaction(db, reference, request.order_id, request.amount, request.email, "pending")

            return {
                "payment_url": data["data"]["authorization_url"],
                "reference": reference
            }
        except httpx.HTTPStatusError as e:
            logger.error(f"Paystack API error: {e}")
            raise HTTPException(status_code=500, detail="Failed to initiate payment")
        except Exception as e:
            logger.error(f"Internal error: {e}")
            raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/payment/verify/{reference}", response_model=PaymentVerifyResponse)
async def verify_payment(reference: str):
    """
    Verify payment status with Paystack.
    """
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{PAYSTACK_API}/transaction/verify/{reference}",
                headers={"Authorization": f"Bearer {PAYSTACK_SECRET}"}
            )
            response.raise_for_status()
            data = response.json()

            status = data["data"]["status"]
            amount = data["data"]["amount"] / 100  # Convert from kobo
            order_id = data["data"]["metadata"]["order_id"]

            # Update transaction status
            async with get_db() as db:
                await update_transaction_status(db, reference, status)

            return PaymentVerifyResponse(status=status, amount=amount, order_id=order_id)
        except httpx.HTTPStatusError as e:
            logger.error(f"Paystack verify error: {e}")
            raise HTTPException(status_code=500, detail="Failed to verify payment")
        except Exception as e:
            logger.error(f"Internal error: {e}")
            raise HTTPException(status_code=500, detail="Internal server error")

@app.post("/payment/webhook")
async def handle_webhook(payload: dict, x_paystack_signature: str = Header(...)):
    """
    Handle Paystack webhook events.
    """
    # Verify webhook signature
    computed_hmac = hmac.new(
        PAYSTACK_SECRET.encode(),
        msg=str(payload).encode(),
        digestmod=hashlib.sha512
    ).hexdigest()

    if not hmac.compare_digest(computed_hmac, x_paystack_signature):
        logger.warning("Invalid webhook signature")
        raise HTTPException(status_code=400, detail="Invalid webhook signature")

    event = payload.get("event")
    data = payload.get("data")

    if event in ["charge.success", "charge.failed"]:
        reference = data.get("reference")
        status = data.get("status")
        order_id = data.get("metadata", {}).get("order_id")

        try:
            async with get_db() as db:
                # Update transaction status
                await update_transaction_status(db, reference, status)

            # Notify store via callback
            async with httpx.AsyncClient() as client:
                await client.post(
                    STORE_CALLBACK_URL,
                    json={"order_id": order_id, "status": status, "reference": reference}
                )
            logger.info(f"Webhook processed: {event} for reference {reference}")
            return {"message": "Webhook processed"}
        except Exception as e:
            logger.error(f"Webhook error: {e}")
            raise HTTPException(status_code=500, detail="Webhook processing failed")

    return {"message": "Webhook ignored"}