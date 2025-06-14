
# Stripe Checkout API with FastAPI

A simple API for handling Stripe Checkout sessions using **FastAPI**, including product listing, order creation, and webhook handling.

---

## Features

* List available services (products)
* Create a checkout session with Stripe
* Handle Stripe webhook events (e.g., `checkout.session.completed`)
* Serve success and cancel pages
* CORS-enabled
* Environment-based Stripe secrets

---

## Requirements

* Python 3.8+
* `pip install -r requirements.txt`:

  ```bash
  fastapi
  uvicorn
  pydantic
  stripe
  ```

---

## Environment Variables

Set the following in your `.env` file or shell:

```env
STRIPE_API_KEY=sk_test_...
STRIPE_ENDPOINT_SECRET=whsec_...
```

---

## Running the App

```bash
uvicorn app:app --reload --host 0.0.0.0 --port 8000 OR python app.py if __name__ == "__main__":
```

---

## API Endpoints

### `GET /products`

Returns a list of available services:



---

### `GET /orders`

Returns current order state (in-memory):



---

### `POST /create-checkout-session`

Creates a Stripe Checkout session.

#### Request Body



#

**Note:** You can enable multiple payment methods (e.g. `card`, `klarna`, `ideal`) depending on your Stripe account's eligibility. If you omit `payment_method_types`, Stripe will choose based on customer geography and session settings.

---

### `POST /webhook`

Stripe will send events here (e.g. `checkout.session.completed`). You must:

1. Set up your webhook URL in the Stripe Dashboard or using Stripe CLI.
2. Proxy `localhost:8000/webhook` via ngrok or similar:

   ```bash
   ngrok http 8000
   ```


### `GET /success?session_id=...`

Displays success page after payment.

---

### `GET /cancel`

Displays cancel page when checkout is abandoned.



* This version stores orders **in-memory**; you'll lose them on restart.
* Avoid hardcoding secrets.
* Stripe Checkout UI will auto-detect applicable payment methods depending on:

  * Customer's country
  * Your account's enabled payment methods
  * Currency compatibility

---

## 🧾 Sample `.env`

```env
STRIPE_API_KEY=sk_test_...
STRIPE_ENDPOINT_SECRET=whsec_...
```

