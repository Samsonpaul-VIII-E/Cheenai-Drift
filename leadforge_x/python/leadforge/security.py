from __future__ import annotations
from urllib.parse import urlparse
import ipaddress

PRIVATE_HOST_MARKERS = {"localhost", "localhost.localdomain", "0.0.0.0", "169.254.169.254"}

def validate_public_http_url(url: str) -> bool:
    p = urlparse(url)
    if p.scheme not in {"http", "https"} or not p.hostname:
        return False
    host = p.hostname.strip("[]").casefold()
    if host in PRIVATE_HOST_MARKERS or host.endswith(".local") or host.endswith(".localhost"):
        return False
    try:
        ip = ipaddress.ip_address(host)
        return not (ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_multicast or ip.is_unspecified)
    except ValueError:
        return True

def redact_secret(value: str) -> str:
    if len(value) <= 6:
        return "***"
    return value[:3] + "***" + value[-3:]
