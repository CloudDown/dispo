#!/usr/bin/env python3
"""Tests d'intégration Dispo — couvre les parcours API utilisés par l'app Android.

Usage :
  cd server && .venv/bin/python tests/test_integration.py
  BASE_URL=https://… .venv/bin/python tests/test_integration.py
"""
from __future__ import annotations

import os
import sys
import time
import uuid
from dataclasses import dataclass, field

import httpx

BASE_URL = os.getenv("BASE_URL", "http://127.0.0.1:8000").rstrip("/")
HEADERS_EXTRA = {"ngrok-skip-browser-warning": "true"}


@dataclass
class Result:
    name: str
    ok: bool
    detail: str = ""


@dataclass
class Suite:
    results: list[Result] = field(default_factory=list)

    def check(self, name: str, cond: bool, detail: str = "") -> bool:
        self.results.append(Result(name, cond, detail if not cond else detail))
        status = "PASS" if cond else "FAIL"
        suffix = f" — {detail}" if detail else ""
        print(f"  [{status}] {name}{suffix}")
        return cond

    def summary(self) -> int:
        passed = sum(1 for r in self.results if r.ok)
        failed = len(self.results) - passed
        print(f"\n=== {passed} ok / {failed} fail / {len(self.results)} total ===")
        return 0 if failed == 0 else 1


def auth_headers(token: str) -> dict:
    return {"Authorization": f"Bearer {token}", **HEADERS_EXTRA}


def main() -> int:
    suite = Suite()
    suffix = uuid.uuid4().hex[:8]
    print(f"BASE_URL={BASE_URL}\n")

    with httpx.Client(base_url=BASE_URL, headers=HEADERS_EXTRA, timeout=20.0) as client:
        # --- infra ---
        print("## Infra")
        try:
            r = client.get("/health")
            suite.check("GET /health", r.status_code == 200 and r.json().get("status") == "ok", r.text)
        except httpx.HTTPError as exc:
            suite.check("GET /health", False, str(exc))
            return suite.summary()

        r = client.get("/config")
        suite.check("GET /config", r.status_code == 200 and "demo_mode" in r.json(), r.text)

        # --- auth ---
        print("\n## Auth")
        r = client.post("/auth/login", json={"public_id": "lea", "password": "demo"})
        suite.check("login lea/demo", r.status_code == 200 and "access_token" in r.json(), r.text)
        if r.status_code != 200:
            return suite.summary()
        lea = r.json()
        lea_tok = lea["access_token"]

        r = client.post("/auth/login", json={"public_id": "max", "password": "demo"})
        suite.check("login max/demo", r.status_code == 200, r.text)
        max_tok = r.json()["access_token"] if r.status_code == 200 else ""

        r = client.post("/auth/login", json={"public_id": "sam", "password": "demo"})
        suite.check("login sam/demo", r.status_code == 200, r.text)
        sam_tok = r.json()["access_token"] if r.status_code == 200 else ""

        r = client.post("/auth/login", json={"public_id": "lea", "password": "wrong"})
        suite.check("login mauvais mdp → 401", r.status_code == 401, r.text)

        r = client.post("/auth/login", json={"public_id": "LEA001", "password": "demo"})
        suite.check(
            "login LEA001 (casse legacy)",
            r.status_code == 200 and r.json().get("public_id", "").lower() == "lea001",
            r.text,
        )

        new_id = f"t{suffix}"
        r = client.post(
            "/auth/register",
            json={"display_name": "Testeur", "password": "demo", "public_id": new_id},
        )
        suite.check("register nouvel user", r.status_code == 201 and r.json().get("public_id") == new_id, r.text)
        tester_tok = r.json()["access_token"] if r.status_code == 201 else ""

        r = client.post(
            "/auth/register",
            json={"display_name": "Dup", "password": "demo", "public_id": new_id},
        )
        suite.check("register ID déjà pris → 400", r.status_code == 400, r.text)

        r = client.get("/auth/me", headers=auth_headers(lea_tok))
        suite.check("GET /auth/me", r.status_code == 200 and r.json().get("public_id") == "lea", r.text)

        r = client.patch(
            "/auth/me",
            headers=auth_headers(lea_tok),
            json={"display_name": "Léa Test", "avatar_color": 3},
        )
        suite.check(
            "PATCH /auth/me",
            r.status_code == 200
            and r.json().get("display_name") == "Léa Test"
            and r.json().get("avatar_color") == 3,
            r.text,
        )
        # restore
        client.patch("/auth/me", headers=auth_headers(lea_tok), json={"display_name": "lea", "avatar_color": 1})

        r = client.get("/auth/users/max")
        suite.check("lookup /auth/users/max", r.status_code == 200 and r.json().get("public_id") == "max", r.text)

        r = client.get("/auth/users/doesnotexist999")
        suite.check("lookup inconnu → 404", r.status_code == 404, r.text)

        # --- friends ---
        print("\n## Friends")
        r = client.get("/friends", headers=auth_headers(lea_tok))
        suite.check("GET /friends lea", r.status_code == 200 and isinstance(r.json(), list), r.text)
        lea_friends = {f["public_id"] for f in r.json()} if r.status_code == 200 else set()
        suite.check("lea a max & sam (seed)", {"max", "sam"} <= lea_friends, str(lea_friends))

        # add friendship tester ↔ lea
        r = client.post("/friends", headers=auth_headers(tester_tok), json={"public_id": "lea"})
        suite.check("POST /friends tester→lea", r.status_code == 201, r.text)

        r = client.post("/friends", headers=auth_headers(tester_tok), json={"public_id": new_id})
        suite.check("POST /friends soi-même → 400", r.status_code == 400, r.text)

        r = client.post("/friends", headers=auth_headers(tester_tok), json={"public_id": "nobodyxyz"})
        suite.check("POST /friends inconnu → 404", r.status_code == 404, r.text)

        r = client.delete(f"/friends/{new_id}", headers=auth_headers(lea_tok))
        suite.check("DELETE /friends lea retire tester", r.status_code == 204, r.text)
        # re-add for group tests
        client.post("/friends", headers=auth_headers(tester_tok), json={"public_id": "lea"})

        # --- groups ---
        print("\n## Groups")
        # Assure lea dans CREWDEMO (seed historique pouvait laisser lea hors groupe)
        r = client.post("/groups/join", headers=auth_headers(lea_tok), json={"invite_code": "CREWDEMO"})
        suite.check("join CREWDEMO (lea)", r.status_code == 200 and r.json().get("invite_code") == "CREWDEMO", r.text)
        crew_id = r.json().get("id") if r.status_code == 200 else None

        r = client.get("/groups", headers=auth_headers(lea_tok))
        suite.check("GET /groups lea", r.status_code == 200 and len(r.json()) >= 1, r.text)
        groups = r.json() if r.status_code == 200 else []
        crew = next((g for g in groups if g.get("invite_code") == "CREWDEMO"), None)
        suite.check("groupe CREWDEMO visible pour lea", crew is not None, str([g.get("invite_code") for g in groups]))
        if crew:
            crew_id = crew["id"]

        r = client.post("/groups", headers=auth_headers(tester_tok), json={"name": f"Crew {suffix}"})
        suite.check("POST /groups create", r.status_code == 201 and "invite_code" in r.json(), r.text)
        new_group = r.json() if r.status_code == 201 else {}
        new_gid = new_group.get("id")
        invite = new_group.get("invite_code", "")

        r = client.post("/groups/join", headers=auth_headers(lea_tok), json={"invite_code": invite})
        suite.check("POST /groups/join lea", r.status_code == 200, r.text)

        # add friend to group (must be friends) — lea adds max to tester's group
        r = client.post(
            f"/groups/{new_gid}/members",
            headers=auth_headers(lea_tok),
            json={"public_id": "max"},
        )
        suite.check("POST members add friend max", r.status_code == 200, r.text)

        r = client.post(
            f"/groups/{new_gid}/members",
            headers=auth_headers(lea_tok),
            json={"public_id": "nobodyxyz"},
        )
        suite.check("POST members inconnu → 404", r.status_code == 404, r.text)

        r = client.get(f"/groups/{new_gid}", headers=auth_headers(tester_tok))
        suite.check("GET /groups/{id}", r.status_code == 200 and r.json().get("id") == new_gid, r.text)

        r = client.get(f"/groups/{new_gid}", headers=auth_headers(sam_tok))
        suite.check("GET group non-membre → 403", r.status_code == 403, r.text)

        # join CREWDEMO as tester
        r = client.post("/groups/join", headers=auth_headers(tester_tok), json={"invite_code": "CREWDEMO"})
        suite.check("join CREWDEMO (tester)", r.status_code == 200, r.text)

        # --- availability + chat gate (cœur produit) ---
        print("\n## Availability + chat gate")
        if crew_id is None:
            suite.check("crew_id disponible", False, "CREWDEMO introuvable")
            return suite.summary()

        # ensure lea is OFF first (toggle until off)
        r = client.get("/availability/me", headers=auth_headers(lea_tok), params={"group_id": crew_id})
        suite.check("GET /availability/me", r.status_code == 200, r.text)
        if r.status_code == 200 and r.json().get("active"):
            client.post("/availability/toggle", headers=auth_headers(lea_tok), params={"group_id": crew_id})

        # also turn off max/sam if active
        for tok in (max_tok, sam_tok):
            st = client.get("/availability/me", headers=auth_headers(tok), params={"group_id": crew_id})
            if st.status_code == 200 and st.json().get("active"):
                client.post("/availability/toggle", headers=auth_headers(tok), params={"group_id": crew_id})

        r = client.get("/availability/me", headers=auth_headers(lea_tok), params={"group_id": crew_id})
        suite.check("tous off → active=false", r.status_code == 200 and r.json().get("active") is False, r.text)
        count_off = r.json().get("dispo_count_in_group", -1) if r.status_code == 200 else -1
        suite.check("dispo_count_in_group == 0", count_off == 0, str(r.json()))

        # chat locked
        r = client.post(
            f"/chat/{crew_id}/messages",
            headers=auth_headers(lea_tok),
            json={"text": "hello avant dispo"},
        )
        suite.check("chat bloqué sans dispo → 403", r.status_code == 403, r.text)

        # toggle lea ON
        r = client.post("/availability/toggle", headers=auth_headers(lea_tok), params={"group_id": crew_id})
        suite.check(
            "toggle lea ON",
            r.status_code == 200 and r.json().get("active") is True and r.json().get("dispo_count_in_group", 0) >= 1,
            r.text,
        )

        r = client.get("/availability/me", headers=auth_headers(max_tok), params={"group_id": crew_id})
        suite.check(
            "max voit ≥1 dispo (chatUnlocked app)",
            r.status_code == 200 and r.json().get("dispo_count_in_group", 0) >= 1,
            r.text,
        )

        r = client.post(
            f"/chat/{crew_id}/messages",
            headers=auth_headers(lea_tok),
            json={"text": f"coucou integration {suffix}"},
        )
        suite.check("chat OK avec dispo", r.status_code == 201 and r.json().get("text"), r.text)
        msg_id = r.json().get("id") if r.status_code == 201 else None

        r = client.post(
            f"/chat/{crew_id}/messages",
            headers=auth_headers(lea_tok),
            json={"text": "", "lat": 45.5017, "lon": -73.5673},
        )
        suite.check(
            "share location (lat/lon)",
            r.status_code == 201 and r.json().get("lat") == 45.5017 and r.json().get("lon") == -73.5673,
            r.text,
        )

        r = client.get(f"/chat/{crew_id}/messages", headers=auth_headers(max_tok))
        suite.check("GET messages (max membre)", r.status_code == 200 and isinstance(r.json(), list), r.text)
        if r.status_code == 200 and msg_id:
            ids = {m["id"] for m in r.json()}
            suite.check("message visible dans le thread", msg_id in ids, str(ids)[:120])

        r = client.get(f"/chat/{crew_id}/messages", headers=auth_headers(tester_tok))
        # tester joined CREWDEMO above
        suite.check("GET messages (tester membre CREWDEMO)", r.status_code == 200, r.text)

        r = client.post(
            f"/chat/{crew_id}/messages",
            headers=auth_headers(lea_tok),
            json={"text": ""},
        )
        suite.check("message vide sans geo → 400", r.status_code == 400, r.text)

        # toggle OFF
        r = client.post("/availability/toggle", headers=auth_headers(lea_tok), params={"group_id": crew_id})
        suite.check("toggle lea OFF", r.status_code == 200 and r.json().get("active") is False, r.text)

        # if nobody else dispo, chat locks again
        r = client.get("/availability/me", headers=auth_headers(lea_tok), params={"group_id": crew_id})
        count_after = r.json().get("dispo_count_in_group", -1) if r.status_code == 200 else -1
        if count_after == 0:
            r = client.post(
                f"/chat/{crew_id}/messages",
                headers=auth_headers(lea_tok),
                json={"text": "devrait être bloqué"},
            )
            suite.check("chat re-bloqué après OFF", r.status_code == 403, r.text)
        else:
            suite.check("chat re-bloqué après OFF", False, f"encore {count_after} dispos actives")

        # friends list exposes dispo flags
        r = client.get("/friends", headers=auth_headers(lea_tok))
        suite.check("friends list avec champ dispo", r.status_code == 200 and all("dispo" in f for f in r.json()), r.text)

        # --- leave group ---
        print("\n## Leave / cleanup")
        r = client.delete(f"/groups/{new_gid}/members/me", headers=auth_headers(max_tok))
        suite.check("max leave groupe test", r.status_code == 204, r.text)

        r = client.delete(f"/groups/{new_gid}/members/me", headers=auth_headers(lea_tok))
        suite.check("lea leave groupe test", r.status_code == 204, r.text)

        r = client.delete(f"/groups/{new_gid}/members/me", headers=auth_headers(tester_tok))
        suite.check("owner leave → groupe vide supprimé", r.status_code == 204, r.text)

        r = client.get(f"/groups/{new_gid}", headers=auth_headers(tester_tok))
        suite.check("groupe disparu → 403/404", r.status_code in (403, 404), r.text)

        # unauthorized
        print("\n## Auth guard")
        r = client.get("/friends")
        suite.check("sans token → 401", r.status_code == 401, r.text)
        r = client.get("/friends", headers=auth_headers("not.a.jwt"))
        suite.check("token invalide → 401", r.status_code == 401, r.text)

    return suite.summary()


if __name__ == "__main__":
    # petite attente si le serveur vient de démarrer
    for _ in range(20):
        try:
            httpx.get(f"{BASE_URL}/health", timeout=1.0)
            break
        except httpx.HTTPError:
            time.sleep(0.25)
    sys.exit(main())
