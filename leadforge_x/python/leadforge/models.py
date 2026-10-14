from __future__ import annotations
from dataclasses import dataclass, asdict, field
from typing import Any

@dataclass(slots=True)
class Lead:
    url: str
    domain: str = ""
    title: str = ""
    description: str = ""
    emails: list[str] = field(default_factory=list)
    phones: list[str] = field(default_factory=list)
    socials: dict[str, str] = field(default_factory=dict)
    keywords: list[str] = field(default_factory=list)
    score: float = 0.0
    fingerprint: str = ""
    status: str = "discovered"

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)
