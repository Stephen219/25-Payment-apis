from flask import Flask, render_template, request, redirect, url_for, session, flash
import requests
import sqlite3
import os
import uuid
import hmac
import hashlib
from dotenv import load_dotenv
from database import init_db, create_order, update_order_status
import logging

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv("FLASK_SECRET_KEY", "super-secret-key")
PAYSTACK_SECRET = os.getenv("PAYSTACK_SECRET")
PAYSTACK_API = "https://api.paystack.co"

CALLBACK_URL = os.getenv("CALLBACK_URL", "http://localhost:5000/checkout/return")

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


PRODUCTS = [
    {"id": 1, "name": "Laptop", "price": 50, "description": "High-performance laptop"},
    {"id": 2, "name": "Smartphone", "price": 3, "description": "Latest smartphone model"},
    {"id": 3, "name": "Headphones", "price": 500, "description": "Noise-cancelling headphones"}
]

@app.route("/")
def index():
    """Display product listings."""
    return render_template("index.html", products=PRODUCTS)

@app.route("/add-to-cart", methods=["POST"])
def add_to_cart():
    """Add item to cart (session-based)."""
    product_id = request.form.get("product_id")
    quantity = int(request.form.get("quantity", 1))

    if "cart" not in session:
        session["cart"] = {}

    product = next((p for p in PRODUCTS if p["id"] == int(product_id)), None)
    if not product:
        flash("Product not found", "error")
        return redirect(url_for("index"))

    cart = session["cart"]
    cart[product_id] = cart.get(product_id, 0) + quantity
    session["cart"] = cart
    flash(f"Added {product['name']} to cart", "success")
    return redirect(url_for("index"))

@app.route("/cart")
def cart():
    """Display cart and allow payment method selection."""
    cart_items = []
    total = 0
    if "cart" in session:
        for product_id, quantity in session["cart"].items():
            product = next((p for p in PRODUCTS if p["id"] == int(product_id)), None)
            if product:
                item_total = product["price"] * quantity
                cart_items.append({
                    "name": product["name"],
                    "quantity": quantity,
                    "price": product["price"],
                    "total": item_total
                })
                total += item_total
    return render_template("cart.html", cart_items=cart_items, total=total)

@app.route("/checkout", methods=["POST"])
def checkout():
    """Initiate Paystack payment."""
    email = request.form.get("email")
    payment_method = request.form.get("payment_method") 

    if not email or not payment_method:
        flash("Email and payment method are required", "error")
        return redirect(url_for("cart"))

    if payment_method not in ["card", "mobile_money"]:
        flash("Invalid payment method", "error")
        return redirect(url_for("cart"))

    total = 0
    cart_items = session.get("cart", {})
    for product_id, quantity in cart_items.items():
        product = next((p for p in PRODUCTS if p["id"] == int(product_id)), None)
        if product:
            total += product["price"] * quantity

    if total == 0:
        flash("Cart is empty", "error")
        return redirect(url_for("cart"))

    order_id = str(uuid.uuid4())
    reference = f"txn_{int(os.times()[4])}_{order_id}"
    init_db()
    create_order(order_id, total, email, payment_method, "pending", reference)

    
    payload = {
        "amount": int(total * 100),  
        "email": email,
        "reference": reference,
        "callback_url": CALLBACK_URL,
        "channels": [payment_method], 
        "metadata": {"order_id": order_id}
    }

    logger.info(f"Sending payload to Paystack: {payload}")
    try:

        response = requests.post(
            f"{PAYSTACK_API}/transaction/initialize",
            json=payload,
            headers={"Authorization": f"Bearer {PAYSTACK_SECRET}"}
        )
        response.raise_for_status()
        print (response.text)
        logger.info(f"Paystack response: {response.status_code} {response.text}")
        data = response.json()
        logger.info(f"Paystack response: {data}")

        if not data.get("status"):
            flash(f"Paystack error: {data.get('message', 'Unknown error')}", "error")
            return redirect(url_for("cart"))

        session["payment_reference"] = reference
        return redirect(data["data"]["authorization_url"])
    except requests.RequestException as e:
        logger.error(f"Paystack API error: {e.response.text if e.response else e}")
        flash("Failed to initiate payment", "error")
        return redirect(url_for("cart"))

@app.route("/checkout/return")
def checkout_return():
    """Handle Paystack redirect after payment."""
    reference = request.args.get("reference")
    if not reference or reference != session.get("payment_reference"):
        flash("Invalid payment reference", "error")
        return redirect(url_for("cart"))
    try:
        response = requests.get(
            f"{PAYSTACK_API}/transaction/verify/{reference}",
            headers={"Authorization": f"Bearer {PAYSTACK_SECRET}"}
        )
        response.raise_for_status()
        data = response.json()
        logger.info(f"Paystack verify response: {data}")

        status = data["data"]["status"]
        order_id = data["data"]["metadata"]["order_id"]
        amount = data["data"]["amount"] / 100

        update_order_status(order_id, status)
        session.pop("payment_reference", None)
        if status == "success":
            session.pop("cart", None)  
        return render_template("checkout.html", status=status, order_id=order_id, amount=amount)
    except requests.RequestException as e:
        logger.error(f"Paystack verify error: {e.response.text if e.response else e}")
        flash("Failed to verify payment", "error")
        return redirect(url_for("cart"))

@app.route("/webhook", methods=["POST"])
def handle_webhook():
    """Handle Paystack webhook."""
    if not PAYSTACK_SECRET:
        logger.error("PAYSTACK_SECRET not set")
        return {"message": "Server configuration error"}, 500

    # to verify
    payload = request.get_data(as_text=True)
    computed_hmac = hmac.new(
        PAYSTACK_SECRET.encode(),
        msg=payload.encode(),
        digestmod=hashlib.sha512
    ).hexdigest()

    if not hmac.compare_digest(computed_hmac, request.headers.get("x-paystack-signature", "")):
        logger.warning("Invalid webhook signature")
        return {"message": "Invalid webhook signature"}, 400

    data = request.json
    event = data.get("event")
    event_data = data.get("data")

    if event in ["charge.success", "charge.failed"]:
        reference = event_data.get("reference")
        status = event_data.get("status")
        order_id = event_data.get("metadata", {}).get("order_id")

        try:
            update_order_status(order_id, status)
            logger.info(f"Webhook processed: {event} for reference {reference}")
            return {"message": "Webhook processed"}, 200
        except Exception as e:
            logger.error(f"Webhook error: {e}")
            return {"message": "Webhook processing failed"}, 500

    return {"message": "Webhook ignored"}, 200

if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5000, debug=True)