from __future__ import annotations
import asyncio, hashlib, time
from collections import deque
from urllib.parse import urljoin, urlparse
import httpx
from .extract import parse_html
from .models import Lead
from .native import fast_fingerprint, fast_score
from .security import validate_public_http_url
from .db import Store

class RateLimiter:
    def __init__(self, rate: float):
        self.interval = 1.0 / max(rate, 0.05)
        self._lock = asyncio.Lock()
        self._next = 0.0

    async def wait(self):
        async with self._lock:
            now = time.monotonic()
            if now < self._next:
                await asyncio.sleep(self._next - now)
            self._next = max(now, self._next) + self.interval

class Crawler:
    def __init__(self, rate=2.0, concurrency=8, max_bytes=5_000_000,
                 keywords=None, db="leadforge.db"):
        self.rate = rate
        self.sem = asyncio.Semaphore(max(1, concurrency))
        self.max_bytes = max_bytes
        self.keywords = keywords or []
        self.store = Store(db)
        self.limiter = RateLimiter(rate)

    async def fetch(self, client, url):
        if not validate_public_http_url(url):
            raise ValueError(f"Rejected URL (SSRF Policy): {url}")
        await self.limiter.wait()
        async with self.sem:
            for attempt in range(3):
                try:
                    r = await client.get(url, follow_redirects=True)
                    body = r.content[:self.max_bytes]
                    return r.status_code, r.headers.get("content-type", ""), body
                except (httpx.HTTPError, TimeoutError):
                    if attempt == 2:
                        raise
                    await asyncio.sleep(0.5 * (2 ** attempt))

    async def crawl(self, start, max_pages=50):
        if not validate_public_http_url(start):
            raise ValueError(f"SSRF Shield: Blocked non-public destination {start}")

        origin = f"{urlparse(start).scheme}://{urlparse(start).netloc}"
        queue = deque([start])
        results = []
        headers = {"User-Agent": "LeadForge-X/2.0 (+public-data; contact-owner-before-high-volume-use)"}
        timeout = httpx.Timeout(20.0, connect=10.0)
        async with httpx.AsyncClient(headers=headers, timeout=timeout) as client:
            while queue and len(results) < max_pages:
                url = queue.popleft()
                if self.store.seen(url):
                    continue
                try:
                    status, ctype, body = await self.fetch(client, url)
                    if "text/html" not in ctype:
                        self.store.page(url, status, "", hashlib.sha256(body).hexdigest())
                        continue
                    html = body.decode("utf-8", "ignore")
                    data = parse_html(url, html)
                    score = fast_score(data["text"], self.keywords)
                    fp = fast_fingerprint((urlparse(url).netloc + "|" + "|".join(data["emails"])).casefold())
                    lead = Lead(url=url, domain=urlparse(url).netloc, title=data["title"],
                                description=data["description"], emails=data["emails"],
                                phones=data["phones"], socials=data["socials"],
                                keywords=self.keywords, score=score, fingerprint=fp,
                                status="validated")
                    self.store.page(url, status, data["title"], hashlib.sha256(body).hexdigest())
                    self.store.lead(lead)
                    results.append(lead)

                    from bs4 import BeautifulSoup
                    soup_links = BeautifulSoup(html, "html.parser").find_all("a", href=True)
                    for a in soup_links:
                        nxt = urljoin(url, a["href"])
                        if nxt.startswith(origin) and nxt not in queue and not self.store.seen(nxt):
                            queue.append(nxt)
                except Exception as exc:
                    self.store.audit("crawl_error", f"{url}: {type(exc).__name__}")
        return results
