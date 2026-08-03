#!/usr/bin/env python3
"""Wrapper → scripts/deploy-pi.py"""
import os
import runpy
from pathlib import Path

os.environ.setdefault("PROJECT", "dispo")
os.environ.setdefault("PROJECT_ROOT", str(Path(__file__).resolve().parents[2]))
runpy.run_path(str(Path(__file__).resolve().parents[3] / "scripts" / "deploy-pi.py"), run_name="__main__")
