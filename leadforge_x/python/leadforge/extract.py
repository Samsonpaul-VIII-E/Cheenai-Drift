from __future__ import annotations
from urllib.parse import urljoin, urlparse
import re
from html.parser import HTMLParser

try:
    from bs4 import BeautifulSoup
except ImportError:
    BeautifulSoup = None

EMAIL_RE = re.compile(r"\b[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}\b")
PHONE_RE = re.compile(r"(?<!\w)(?:\+?\d[\d\s().-]{7,}\d)(?!\w)")

class SimpleHtmlExtractor(HTMLParser):
    def __init__(self):
        super().__init__()
        self.title = ""
        self.description = ""
        self.in_title = False
        self.text_parts = []
        self.links = []

    def handle_starttag(self, tag, attrs):
        attr_dict = dict(attrs)
        if tag.lower() == "title":
            self.in_title = True
        elif tag.lower() == "meta":
            name = attr_dict.get("name", "").lower()
            prop = attr_dict.get("property", "").lower()
            if name == "description" or prop == "og:description":
                self.description = attr_dict.get("content", "").strip()
        elif tag.lower() == "a" and "href" in attr_dict:
            self.links.append(attr_dict["href"])

    def handle_endtag(self, tag):
        if tag.lower() == "title":
            self.in_title = False

    def handle_data(self, data):
        cleaned = data.strip()
        if cleaned:
            if self.in_title:
                self.title += " " + cleaned
            self.text_parts.append(cleaned)

def parse_html(url: str, html: str) -> dict:
    if BeautifulSoup is not None:
        soup = BeautifulSoup(html, "html.parser")
        title = soup.title.get_text(" ", strip=True) if soup.title else ""
        description = ""
        meta = soup.find("meta", attrs={"name": re.compile("^description$", re.I)})
        if meta:
            description = (meta.get("content") or "").strip()
        text = soup.get_text(" ", strip=True)
        links = [a["href"] for a in soup.find_all("a", href=True)]
    else:
        parser = SimpleHtmlExtractor()
        try:
            parser.feed(html)
        except Exception:
            pass
        title = parser.title.strip()
        description = parser.description
        text = " ".join(parser.text_parts)
        links = parser.links

    emails = sorted(set(EMAIL_RE.findall(text)))
    phones = sorted(set(PHONE_RE.findall(text)))

    socials = {}
    for href in links:
        full_href = urljoin(url, href)
        host = urlparse(full_href).netloc.casefold()
        for key, marker in {
            "linkedin": "linkedin.com",
            "facebook": "facebook.com",
            "instagram": "instagram.com",
            "youtube": "youtube.com",
            "tiktok": "tiktok.com",
            "x": "x.com",
            "twitter": "twitter.com",
        }.items():
            if marker in host and key not in socials:
                socials[key] = full_href

    return {
        "title": title,
        "description": description,
        "text": text[:1_000_000],
        "emails": emails,
        "phones": phones,
        "socials": socials,
    }
