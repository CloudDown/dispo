# Dispo — App Android native + Widget

## Direction technique (mise à jour)

- **App Android native** : Kotlin + Jetpack Compose (animations Looney Tunes, 3 panneaux)
- **Widget écran d'accueil** : Jetpack Glance — gros bouton vert 😒/😀 pour toggle dispo sans ouvrir l'app
- **Backend** : Supabase (auth, realtime chat, postgres) + FCM pour les notifs push
- **Carte** : Google Maps SDK for Android

> Le widget nécessite du code natif de toute façon — Compose + Glance partagent la même logique métier Kotlin.

---

## SDK Android — état actuel

Chemin cible : `~/Android/Sdk`

| Composant | Statut |
|-----------|--------|
| `platform-tools` (adb) | OK — v37.0.0 |
| `build-tools` 36.1.0 / 37.0.0 | OK |
| `platforms/android-36.1` | OK |
| `emulator` | Présent |
| `licenses` | Présent |
| `cmdline-tools` | **Manquant** — nécessaire pour `sdkmanager` |
| Variables `ANDROID_HOME` | **Non configurées** dans le shell |

### À faire avant de coder

1. Installer **Android SDK Command-line Tools** dans le dossier Sdk
2. Configurer l'environnement :
   ```bash
   export ANDROID_HOME=~/Android/Sdk
   export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
   ```
3. Accepter les licences : `sdkmanager --licenses`
4. Vérifier : `adb devices` + build Gradle d'un projet test

---

## Architecture app

```
dispo/
├── app/                          # Module principal Compose
│   ├── ui/
│   │   ├── DispoScreen.kt        # 3 panneaux (bouton | chat | map)
│   │   ├── DispoButton.kt        # cercles LT + emoji toggle
│   │   ├── ChatPanel.kt          # actif si >= 2 dispos
│   │   └── MapPanel.kt
│   └── MainActivity.kt
├── widget/                       # Module Jetpack Glance
│   └── DispoWidget.kt            # bouton dispo sur l'écran d'accueil
├── core/                         # Logique partagée app + widget
│   ├── availability/
│   └── supabase/
└── build.gradle.kts
```

### Flow widget

1. L'utilisateur ajoute le widget Dispo sur son écran d'accueil
2. Tap sur le widget → toggle dispo (😒 → 😀) via `WorkManager` + API Supabase
3. Notif FCM envoyée aux membres du groupe
4. Ouvrir l'app depuis le widget → écran 3 panneaux complet

---

## Phases (après installation SDK)

1. **Setup** — Projet Android Studio / Gradle, modules `app` + `widget` + `core`, config Supabase
2. **DispoButton** — Compose, DA cirque, cercles animés, expiration fin de journée
3. **Groupes + dispo** — Auth, création groupe, liste temps réel des dispos
4. **3 panneaux** — Chat conditionnel (>= 2), map avec pins
5. **Widget Glance** — Bouton dispo sur home screen
6. **FCM** — « X est dispo jusqu'à ce soir »

---

## DA rappel

- Fond crème/bleu ciel cartoon
- Cercles concentriques rouge/jaune autour du bouton (animation pulse + rotation)
- Bouton central vert `#2ECC71`, emoji 😒 → 😀 au tap
- Typo display : Fredoka One ou Bangers
