"""
Accès base de données Dispo.
En démo : create_tables() via SQLModel. En prod : migrations Alembic plus tard.
"""
import sqlite3

from sqlalchemy import event
from sqlalchemy.engine import Engine
from sqlmodel import Session, SQLModel, create_engine

from config import DATABASE_URL

engine = create_engine(
    DATABASE_URL,
    echo=False,
    connect_args={"check_same_thread": False} if "sqlite" in DATABASE_URL else {},
)


@event.listens_for(Engine, "connect")
def set_sqlite_pragma(dbapi_connection, connection_record):
    if isinstance(dbapi_connection, sqlite3.Connection):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()


def get_session():
    with Session(engine) as session:
        yield session


def create_tables():
    SQLModel.metadata.create_all(engine)
