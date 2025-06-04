import sqlite3
from datetime import datetime

def init_db():
    """Initialize SQLite database and create orders table."""
    conn = sqlite3.connect("store.db")
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_id TEXT NOT NULL UNIQUE,
            amount REAL NOT NULL,
            email TEXT NOT NULL,
            payment_method TEXT NOT NULL,
            status TEXT DEFAULT 'pending',
            reference TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()
    conn.close()

# technically we woould need a table that even tracks the orders anad orderitems per order for stock maintanace but not important for now    

def create_order(order_id: str, amount: float, email: str, payment_method: str, status: str, reference: str):
    """Create a new order in the database."""
    conn = sqlite3.connect("store.db")
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO orders (order_id, amount, email, payment_method, status, reference) VALUES (?, ?, ?, ?, ?, ?)",
        (order_id, amount, email, payment_method, status, reference)
    )
    conn.commit()
    conn.close()

def update_order_status(order_id: str, status: str):
    """Update order status in the database."""
    conn = sqlite3.connect("store.db")
    cursor = conn.cursor()
    cursor.execute(
        "UPDATE orders SET status = ? WHERE order_id = ?",
        (status, order_id)
    )
    conn.commit()
    conn.close()