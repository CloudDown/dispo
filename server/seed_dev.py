"""Seed démo : Léa, Max, Sam + un groupe Crew — comme les IDs du client local."""
from sqlmodel import Session, select

from auth.models import User
from auth.service import hash_password
from db import engine
from friends.service import add_friendship
from groups.models import Group, GroupMember


# Pseudos style Instagram (public_id = nom)
DEMO_USERS = [
    ("lea", "lea", 1, "demo"),
    ("max", "max", 2, "demo"),
    ("sam", "sam", 3, "demo"),
]


def seed():
    with Session(engine) as session:
        created: list[User] = []
        for public_id, name, color, password in DEMO_USERS:
            existing = session.exec(select(User).where(User.public_id == public_id)).first()
            if existing:
                created.append(existing)
                continue
            user = User(
                public_id=public_id,
                display_name=name,
                hashed_password=hash_password(password),
                avatar_color=color,
            )
            session.add(user)
            session.commit()
            session.refresh(user)
            created.append(user)
            print(f"  + user {public_id} ({name})")

        # Groupe démo partagé
        group = session.exec(select(Group).where(Group.invite_code == "CREWDEMO")).first()
        if not group and created:
            group = Group(
                name="Crew Démo",
                invite_code="CREWDEMO",
                created_by=created[0].id,
            )
            session.add(group)
            session.commit()
            session.refresh(group)
            for user in created:
                session.add(GroupMember(group_id=group.id, user_id=user.id))
            session.commit()
            print("  + groupe CREWDEMO")

        # Amis entre eux
        if len(created) >= 2:
            add_friendship(session, created[0].id, created[1].id)
        if len(created) >= 3:
            add_friendship(session, created[0].id, created[2].id)
            add_friendship(session, created[1].id, created[2].id)

    print("✅ Seed démo OK — pseudos lea / max / sam (mdp: demo)")
