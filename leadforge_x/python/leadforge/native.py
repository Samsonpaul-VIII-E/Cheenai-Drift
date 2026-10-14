"""Optional bridge to the compiled C++ acceleration module."""
try:
    import leadforge_native as native
except ImportError:
    native = None

def fast_fingerprint(value: str) -> str:
    if native:
        return native.fingerprint(value)
    # Deterministic fallback; no external dependency.
    import hashlib
    return hashlib.sha256(value.encode("utf-8", "ignore")).hexdigest()[:32]

def fast_score(text: str, keywords: list[str]) -> float:
    if native:
        return float(native.keyword_score(text, keywords))
    low = text.casefold()
    if not keywords:
        return 0.0
    hits = sum(1 for k in keywords if k.casefold() in low)
    return min(1.0, hits / max(1, len(keywords)))
