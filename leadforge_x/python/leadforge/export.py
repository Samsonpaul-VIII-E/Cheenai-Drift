from __future__ import annotations
import csv, json
from pathlib import Path
from .models import Lead

def export_records(records: list[Lead], path: str):
    p = Path(path)
    p.parent.mkdir(parents=True, exist_ok=True)
    rows = []
    for x in records:
        d = x.to_dict()
        d["emails"] = ";".join(x.emails)
        d["phones"] = ";".join(x.phones)
        d["socials"] = json.dumps(x.socials, ensure_ascii=False)
        d["keywords"] = ";".join(x.keywords)
        rows.append(d)
    if p.suffix.lower() == ".json":
        p.write_text(json.dumps(rows, indent=2, ensure_ascii=False), encoding="utf-8")
    elif p.suffix.lower() == ".csv":
        with p.open("w", newline="", encoding="utf-8-sig") as f:
            w = csv.DictWriter(f, fieldnames=rows[0].keys() if rows else
                               ["url","domain","title","description","emails","phones","socials","keywords","score","fingerprint","status"])
            w.writeheader(); w.writerows(rows)
    else:
        raise ValueError("Use .csv or .json")
