from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).parents[1] / "python"))

from leadforge.extract import parse_html
from leadforge.security import validate_public_http_url
from leadforge.native import fast_score, fast_fingerprint

def test_extract():
    d = parse_html("https://example.com", "<html><head><title>Acme Corp</title></head><body><p>Contact contact@acme.com or +1-555-0199</p></body></html>")
    assert d["title"] == "Acme Corp"
    assert "contact@acme.com" in d["emails"]

def test_url_policy():
    assert validate_public_http_url("https://example.com")
    assert not validate_public_http_url("file:///etc/passwd")
    assert not validate_public_http_url("http://127.0.0.1:8080")
    assert not validate_public_http_url("http://localhost/admin")
    assert not validate_public_http_url("http://192.168.1.1")

def test_score():
    assert fast_score("python automation leads", ["python", "leads"]) == 1.0

def test_fingerprint():
    fp1 = fast_fingerprint("acme.com|contact@acme.com")
    fp2 = fast_fingerprint("acme.com|contact@acme.com")
    assert fp1 == fp2
