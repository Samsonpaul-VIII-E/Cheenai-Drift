from __future__ import annotations
import argparse, asyncio, sys
from .engine import Crawler
from .export import export_records
from . import __version__

def main():
    parser = argparse.ArgumentParser(prog="leadforge", description="LeadForge X — Public Lead Operations Platform")
    sub = parser.add_subparsers(dest="command", required=True)

    crawl_p = sub.add_parser("crawl", help="Execute an asynchronous crawl on an authorized public target")
    crawl_p.add_argument("--url", required=True, help="Target root URL")
    crawl_p.add_argument("--output", default="leads.csv", help="Output export path (.csv or .json)")
    crawl_p.add_argument("--max-pages", type=int, default=50, help="Maximum pages to process")
    crawl_p.add_argument("--rate", type=float, default=2.0, help="Politeness rate limit (req/sec)")
    crawl_p.add_argument("--concurrency", type=int, default=8, help="Bounded concurrency workers")
    crawl_p.add_argument("--keyword", action="append", default=[], help="Lead scoring keywords")
    crawl_p.add_argument("--db", default="leadforge.db", help="Local SQLite checkpoint database")

    sub.add_parser("version", help="Show system version")

    args = parser.parse_args()

    if args.command == "version":
        print(f"LeadForge X v{__version__}")
        return 0

    if args.command == "crawl":
        print(f"[*] LeadForge X v{__version__} launching operation on {args.url}")
        crawler = Crawler(rate=args.rate, concurrency=args.concurrency, keywords=args.keyword, db=args.db)
        records = asyncio.run(crawler.crawl(args.url, max_pages=args.max_pages))
        export_records(records, args.output)
        print(f"[+] Completed: {len(records)} verified leads secured -> {args.output}")
        return 0

if __name__ == "__main__":
    sys.exit(main() or 0)
