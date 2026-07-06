# Dispo 🎪

App Android pour dire à tes potes que tu es dispo jusqu'à la fin de la journée, en un tap.

## Concept

- Un **gros bouton vert** au centre : 😒 quand tu n'es pas dispo, 😀 quand tu l'es.
- Ta dispo **expire automatiquement à minuit**.
- Dès que **2 personnes** sont dispos, le **chat** se déverrouille.
- Une **carte** (OpenStreetMap) pour partager les lieux de rendez-vous depuis le chat.
- Un **widget écran d'accueil** pour toggle ta dispo sans ouvrir l'app.

Direction artistique : cirque / intro Looney Tunes — cercles concentriques rouge et jaune animés autour du bouton, fond crème.

## Structure

```
app/src/main/java/com/dispo/app/
├── MainActivity.kt          # Pager 3 panneaux : Dispo | Chat | Carte
├── core/
│   ├── Models.kt             # Friend, ChatMessage, DispoUiState
│   └── DispoRepository.kt    # État local (DataStore), expiration minuit
├── ui/
│   ├── DispoButton.kt        # Bouton animé cercles Looney Tunes
│   ├── HomePanel.kt
│   ├── ChatPanel.kt          # Verrouillé tant que < 2 dispos
│   ├── MapPanel.kt           # osmdroid, pins partagés
│   └── theme/Theme.kt        # Palette cirque
└── widget/
    └── DispoWidget.kt        # Widget Glance (toggle depuis l'accueil)
```

## Build

Prérequis : JDK 17+, Android SDK (chemin dans `local.properties`).

```bash
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
```

## État actuel (MVP local)

Le backend n'est pas encore branché : les amis (Léa, Max, Sam) sont simulés
en mémoire, et Léa répond automatiquement quelques secondes après ton premier
tap pour faire la démo du déverrouillage du chat.

Prochaines étapes :
- Backend Supabase (auth, groupes réels, chat temps réel)
- Notifications push FCM (« X est dispo jusqu'à ce soir »)
- Partage de la vraie position GPS / picker de lieu sur la carte
