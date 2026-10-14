# LeadForge X — Public Data Intelligence & Lead Operations Platform

Commercial-grade public-data lead intelligence and cyber-operations toolkit.

## Architecture
- **Android**: Native Kotlin + Jetpack Compose + Room SQLite + OkHttp 4.10 high-performance mobile intelligence console.
- **Python**: Async acquisition engine (`httpx`, `bs4`, `sqlite3`), pluggable extraction, and CLI.
- **C++17**: Deterministic high-throughput normalization, hashing, deduplication, and scoring primitives.
- **Storage**: SQLite / Room persistence for jobs, visited pages, leads, and tamper-evident audit logs.

## Security & SSRF Defense
LeadForge X enforces defense-in-depth:
- URL Scheme validation (HTTP/HTTPS only).
- DNS Resolution with strict blocking of loopback (`127.0.0.0/8`, `::1`), RFC 1918 private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), link-local, multicast, and cloud metadata endpoints (`169.254.169.254`).
- Rate limiting per domain with randomized jitter.
- Max response size caps (5MB) to prevent memory exhaustion.
- Strict robots.txt adherence without anti-bot evasion.

## Quick Start (Android)
Build and run on your Android device or streaming emulator. All Room database tables, SSRF shields, live terminal logs, and export mechanisms run locally on-device.

## Quick Start (Python CLI)
```bash
python -m venv .venv
source .venv/bin/activate  # Or .venv\Scripts\activate on Windows
pip install -r leadforge_x/requirements.txt
python -m leadforge.cli crawl --url https://example.com --output leads.csv --max-pages 25
```
