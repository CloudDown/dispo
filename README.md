# Dispo 🎪

App Android pour dire à tes potes que tu es dispo jusqu'à la fin de la journée — en un tap.

Architecture monorepo inspirée de **Vif** : client Android + API FastAPI.

## Structure

```
dispo/
├── mobile_app/     # App Android (Compose, widget, chat, carte, profil)
├── server/         # API FastAPI (auth, amis, groupes, dispo, chat)
├── dev/            # Exemple d'environnement
└── start.sh        # Démarre le serveur local
```

## Serveur

```bash
./start.sh
# → http://localhost:8000/docs
```

Endpoints principaux :

| Préfixe | Rôle |
|---------|------|
| `/auth` | Register / login / profil (ID public) |
| `/friends` | Ajouter / lister / retirer par ID |
| `/groups` | Crews + codes d'invitation |
| `/availability` | Toggle « je suis dispo » |
| `/chat` | Messages de groupe (débloqué à 2 dispos) |

Comptes démo : `LEA001` / `MAX002` / `SAM003` (mdp `demo`), groupe `CREWDEMO`.

## Mobile

```bash
cd mobile_app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**État actuel** : l'UI et la logique locale (DataStore) fonctionnent offline.
Le serveur est prêt ; le branchement Retrofit (comme Vif) est la prochaine étape.

## Concept produit

- Gros bouton central : pas dispo → dispo jusqu'à minuit
- Chat déverrouillé dès que 2 personnes du crew sont dispos
- Carte plein écran pour envoyer un lieu (pin + **Envoyer**)
- Profil : nom, ID public, avatar, ajout d'amis par ID

## Stack

| Couche | Techno |
|--------|--------|
| Mobile | Kotlin, Jetpack Compose, DataStore, Glance, osmdroid |
| API | FastAPI, SQLModel, JWT, SQLite (Postgres possible via `DISPO_DATABASE_URL`) |
