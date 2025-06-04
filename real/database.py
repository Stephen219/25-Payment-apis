from contextlib import asynccontextmanager
import psycopg2
from psycopg2.extras import RealDictCursor
import os
from dotenv import load_dotenv

load_dotenv()

# Database configuration
DB_CONFIG = {
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASSWORD"),
    "host": os.getenv("DB_HOST", "localhost"),
    "database": os.getenv("DB_NAME"),
    "port": os.getenv("DB_PORT", "5432")
}

@asynccontextmanager
async def get_db():
    """
    Database connection context manager.
    """
    conn = psycopg2.connect(**DB_CONFIG, cursor_factory=RealDictCursor)
    try:
        yield conn
    finally:
        conn.close()

async def create_transaction(db, reference: str, order_id: str, amount: float, email: str, status: str):
    """
    Create a new transaction in the database.
    """
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO transactions (reference, order_id, amount, email, status) VALUES (%s, %s, %s, %s, %s)",
            (reference, order_id, amount, email, status)
        )
        db.commit()
    finally:
        cursor.close()

async def update_transaction_status(db, reference: str, status: str):
    """
    Update transaction status in the database.
    """
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE transactions SET status = %s WHERE reference = %s",
            (status, reference)
        )
        db.commit()
    finally:
        cursor.close()