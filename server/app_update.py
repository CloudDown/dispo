from __future__ import annotations

import json
from pathlib import Path

from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse

_DIR = Path(__file__).resolve().parent
_JSON = _DIR / "app-update.json"
_APK = _DIR / "app-latest.apk"

router = APIRouter(tags=["update"])


@router.get("/app-update")
def app_update():
    if not _JSON.is_file() or not _APK.is_file():
        return {"available": False}
    data = json.loads(_JSON.read_text(encoding="utf-8"))
    data["available"] = True
    return data


@router.get("/app-latest.apk")
def app_latest_apk():
    if not _APK.is_file():
        raise HTTPException(status_code=404, detail="Pas d'APK")
    return FileResponse(
        _APK,
        media_type="application/vnd.android.package-archive",
        filename="app-latest.apk",
    )
