from __future__ import annotations
import sqlite3, json, time
from pathlib import Path
from .models import Lead

SCHEMA = """
CREATE TABLE IF NOT EXISTS jobs(
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 created_at REAL NOT NULL,
 status TEXT NOT NULL,
 config TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS pages(
 url TEXT PRIMARY KEY,
 fetched_at REAL NOT NULL,
 status INTEGER,
 title TEXT,
 body_hash TEXT
);
CREATE TABLE IF NOT EXISTS leads(
 fingerprint TEXT PRIMARY KEY,
 url TEXT NOT NULL,
 payload TEXT NOT NULL,
 updated_at REAL NOT NULL
);
CREATE TABLE IF NOT EXISTS audit(
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 ts REAL NOT NULL,
 event TEXT NOT NULL,
 detail TEXT NOT NULL
);
"""

class Store:
    def __init__(self, path: str = "leadforge.db"):
        self.path = Path(path)
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self.conn = sqlite3.connect(self.path)
        self.conn.executescript(SCHEMA)
        self.conn.commit()

    def audit(self, event: str, detail: str = ""):
        self.conn.execute("INSERT INTO audit(ts,event,detail) VALUES(?,?,?)",
                          (time.time(), event, detail))
        self.conn.commit()

    def seen(self, url: str) -> bool:
        return self.conn.execute("SELECT 1 FROM pages WHERE url=?", (url,)).fetchone() is not None

    def page(self, url: str, status: int, title: str, body_hash: str):
        self.conn.execute(
            "INSERT OR REPLACE INTO pages(url,fetched_at,status,title,body_hash) VALUES(?,?,?,?,?)",
            (url, time.time(), status, title, body_hash))
        self.conn.commit()

    def lead(self, lead: Lead):
        self.conn.execute(
            "INSERT OR REPLACE INTO leads(fingerprint,url,payload,updated_at) VALUES(?,?,?,?)",
            (lead.fingerprint, lead.url, json.dumps(lead.to_dict()), time.time()))
        self.conn.commit()

    def read_leads(self) -> list[dict]:
        rows = self.conn.execute("SELECT payload FROM leads").fetchall()
        out = []
        for (p,) in rows:
            try:
                out.append(json.loads(p))
            except json.JSONDecodeError:
                continue
        return out
