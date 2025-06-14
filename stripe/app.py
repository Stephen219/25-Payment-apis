from fastapi import FastAPI, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, HTMLResponse
from pydantic import BaseModel
import stripe
import uvicorn
import logging
import hmac
import hashlib
import json
import os


stripe.api_key = os.getenv('STRIPE_API_KEY', print("add api key in you env file"))
endpoint_secret = os.getenv('STRIPE_ENDPOINT_SECRET', print("add endpoint secret in you env file"))


app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("uvicorn")


orders = []
PRODUCTS = [
    {"id": 1, "name": "Washing Service", "price": 5000},
    {"id": 2, "name": "Home Cleaning", "price": 10000},
    {"id": 3, "name": "Carpet Cleaning", "price": 7500},
]


class OrderRequest(BaseModel):
    email: str
    product_id: int

class PaymentConfirm(BaseModel):
    order_id: int
    status: str
    reference: str

@app.get("/products")
def get_products():
    return PRODUCTS

@app.get("/orders")
def get_orders():
    return orders

@app.post("/create-checkout-session")
def create_checkout_session(data: OrderRequest):
    """
    Create a checkout session for the selected product.

    Args:
        data (OrderRequest): The order request containing email and product_id.

    Returns:
        JSONResponse: A response containing the checkout URL.
    """
    product = next((p for p in PRODUCTS if p["id"] == data.product_id), None)
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")

    order_id = len(orders) + 1
    order = {
        "id": order_id,
        "email": data.email,
        "product": product,
        "status": "pending",
    }
    orders.append(order)

    try:
        session = stripe.checkout.Session.create(
            # payment_method_types=["card"],
            # payment_method_types=[ "card", "ideal", "sepa_debit", "sofort", "klarna"],   this gives it to decide automatically if i comment it 
            line_items=[{
                "price_data": {
                    "currency": "usd",
                    "product_data": {"name": product["name"]},
                    "unit_amount": product["price"],
                },
                "quantity": 1,
            }],
            mode="payment",
            metadata={"order_id": order_id},
            success_url=f"http://localhost:8000/success?session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url="http://localhost:8000/cancel",
            customer_email=data.email,

            # automatic_payment_methods={"enabled": True},
 )
        return JSONResponse({"checkout_url": session.url})
    except Exception as e:
        logger.error(f"Stripe session creation failed: {e}")
        raise HTTPException(status_code=500, detail="Failed to create Stripe checkout session")

@app.post("/webhook")

async def stripe_webhook(request: Request):
    """Handle Stripe webhook events.
    Args:
        request (Request): The incoming request containing the webhook payload.
    Returns:
        
        JSONResponse: A response indicating the status of the webhook processing.
        this one has to be proxied from loclalhost:8000 to stripe
    1. Verify the webhook signature.
    2. Process the event based on its type.
    3. Update the order status in the local database.
    """
    payload = await request.body()
    print ("Received payload:", payload)
    logger.info("Received webhook payload")

  
    sig_header = request.headers.get("stripe-signature")

    
    try:
        event = stripe.Webhook.construct_event(payload, sig_header, endpoint_secret)
    except Exception as e:
        logger.error(f"Webhook signature verification failed: {e}")
        return JSONResponse({"error": "Invalid signature"}, status_code=400)
    

    print(event)
    print("Received event:", event["type"])

  
    if event["type"] == "checkout.session.completed":
        session = event["data"]["object"]

        order_id = int(session["metadata"].get("order_id", 0))
        payment_status = session.get("payment_status", "")
        payment_intent_id = session.get("payment_intent", "")

        
        for order in orders:
            if order["id"] == order_id:
                order["status"] = "paid" if payment_status == "paid" else payment_status
                order["payment_reference"] = payment_intent_id
                logger.info(f"Order {order_id} updated to {order['status']}")
                break

    return JSONResponse({"status": "success"})

@app.get("/success")
def success_page(session_id: str):
    html_content = f"""
    <html><body>
    <h1>Payment Success!</h1>
    <p>Session ID: {session_id}</p>
    <p><a href="/">Back to Home</a></p>
    </body></html>
    """
    return HTMLResponse(content=html_content)

@app.get("/cancel")
def cancel_page():
    html_content = """
    <html><body>
    <h1>Payment Cancelled</h1>
    <p>Your payment was cancelled.</p>
    <p><a href="/">Back to Home</a></p>
    </body></html>
    """
    return HTMLResponse(content=html_content)

if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
