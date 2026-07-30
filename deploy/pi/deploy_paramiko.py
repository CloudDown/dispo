#!/usr/bin/env python3
"""Déploiement Dispo (serveur API) sur Raspberry Pi via Paramiko."""

from __future__ import annotations

import os
import sys
import tarfile
import tempfile
from pathlib import Path

import paramiko

PI_HOST = os.environ.get("PI_HOST", "192.168.2.170")
PI_USER = os.environ.get("PI_USER", "pi")


def _require_env(name: str) -> str:
    val = os.environ.get(name)
    if not val:
        print(f"{name} manquant — export ou oeuil/secrets.env", file=sys.stderr)
        sys.exit(1)
    return val


PI_PASS = _require_env("PI_PASS")
PI_DIR = os.environ.get("PI_DIR", "/home/pi/dispo")
DATA_DIR = os.environ.get("PI_DATA", "/var/lib/dispo")
ROOT = Path(__file__).resolve().parents[2]

INCLUDE_TOP = {
    "server",
    "deploy",
    "start.sh",
    "README.md",
    "AGENTS.md",
}


def _should_include(rel: Path) -> bool:
    if rel.parts and rel.parts[0] not in INCLUDE_TOP:
        return False
    if ".git" in rel.parts or ".venv" in rel.parts or "__pycache__" in rel.parts:
        return False
    if rel.suffix == ".pyc":
        return False
    if rel.parts[:2] == ("server", "dispo.db"):
        return False
    return True


def _make_tar() -> Path:
    tmp = Path(tempfile.mkstemp(suffix=".tar.gz")[1])
    print(f"==> Archive {tmp}")
    with tarfile.open(tmp, "w:gz") as tar:
        for item in ROOT.rglob("*"):
            rel = item.relative_to(ROOT)
            if not _should_include(rel):
                continue
            tar.add(item, arcname=str(rel))
    return tmp


def _run(client: paramiko.SSHClient, cmd: str, *, sudo: bool = False) -> None:
    if sudo:
        cmd = f"echo '{PI_PASS}' | sudo -S bash -lc {repr(cmd)}"
    print(f"\n>> {cmd[:140]}{'…' if len(cmd) > 140 else ''}")
    _, stdout, stderr = client.exec_command(cmd, get_pty=True)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    if out.strip():
        print(out.rstrip())
    if code != 0:
        if err.strip():
            print(err.rstrip(), file=sys.stderr)
        raise SystemExit(f"Command failed ({code}): {cmd[:80]}")


def main() -> None:
    tar_path = _make_tar()
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    print(f"==> Connexion {PI_USER}@{PI_HOST}")
    client.connect(
        PI_HOST,
        username=PI_USER,
        password=PI_PASS,
        timeout=15,
        allow_agent=False,
        look_for_keys=False,
    )

    sftp = client.open_sftp()
    remote_tar = f"/home/{PI_USER}/dispo-deploy.tar.gz"
    print(f"==> Upload {remote_tar}")
    sftp.put(str(tar_path), remote_tar)
    sftp.close()
    tar_path.unlink(missing_ok=True)

    _run(client, f"mkdir -p {PI_DIR} && tar xzf {remote_tar} -C {PI_DIR}")
    _run(client, f"rm -f {remote_tar}")

    _run(client, f"chmod +x {PI_DIR}/deploy/pi/setup-remote.sh")
    _run(
        client,
        f"SUDO_PASS={PI_PASS} DISPO_DIR={PI_DIR} DISPO_DATA={DATA_DIR} SERVICE_USER={PI_USER} "
        f"bash {PI_DIR}/deploy/pi/setup-remote.sh",
    )

    print("\n==> Déploiement terminé")
    print(f"Docs : http://{PI_HOST}:8000/docs")
    client.close()


if __name__ == "__main__":
    main()
