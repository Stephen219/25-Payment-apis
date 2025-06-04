 Store and Payment App

A Flask application combining store and Paystack payment processing.

## Setup

1. **Prerequisites**:
   - Python 3.10+
   - SQLite (included with Python)
   - Paystack account (get secret key from dashboard)

2. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

3. **Environment Variables**:
   - Create a `.env` file:
     ```
     FLASK_SECRET_KEY=your-secret-key
     PAYSTACK_SECRET=sk_test_your_secret_key
     CALLBACK_URL=http://localhost:5000/checkout/return
     ```

4. **Run Locally**:
   ```bash
   flask run --host=0.0.0.0 --port=5000
   ```

5. **Test Payments**:
   - Use Paystack test cards/M-Pesa credentials (https://paystack.com/docs/payments/test-payments).
   - Configure webhook URL in Paystack dashboard: `http://localhost:5000/webhook`
   - Test locally with Ngrok: `ngrok http 5000`

6.

## Endpoints

- **GET /**: View product listings.
- **POST /add-to-cart**: Add product to cart.
- **GET /cart**: View cart and select payment method.
- **POST /checkout**: Initiate Paystack payment.
- **GET /checkout/return**: Handle Paystack redirect.


## Payment Flow

- Select payment method (card or M-Pesa) during checkout.
- Enter card details or mobile number on Paystack’s page.
- Payment status is verified and updated via redirect and webhook.


## to add

- ** recurrent payments**: Implement subscription plans using Paystack's recurring billing.
- ** refund management**: Add functionality to process refunds through Paystack.
