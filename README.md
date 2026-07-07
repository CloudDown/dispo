# Dispo 🎪

App Android pour dire à tes potes que tu es dispo jusqu'à la fin de la journée — en un tap.

> **Version 1.0** — MVP local avec direction artistique cirque / Looney Tunes.

## Concept

- Un **gros bouton** au centre : 😒 quand tu n'es pas dispo, 😀 quand tu l'es.
- Ta dispo **expire automatiquement à minuit**.
- Dès que **2 personnes** sont dispos, le **chat** se déverrouille.
- Une **carte** (OpenStreetMap) pour partager des lieux de rendez-vous depuis le chat.
- Un **widget écran d'accueil** pour basculer ta dispo sans ouvrir l'app.

## Ce qui est livré en v1.0

### Fonctionnalités

| Fonctionnalité | Détail |
|----------------|--------|
| Toggle dispo | Un tap active ta dispo jusqu'à minuit (fuseau local) |
| Persistance | État sauvegardé via DataStore, partagé entre l'app et le widget |
| Liste d'amis | Léa, Max et Sam simulés en mémoire (démo locale) |
| Déverrouillage chat | Le chat s'ouvre dès que 2 personnes sont dispos |
| Réponse auto | Léa répond quelques secondes après ton premier tap (démo) |
| Carte | OpenStreetMap plein écran (osmdroid), zoom au pincement, pins partagés |
| Widget | Jetpack Glance — toggle dispo depuis l'écran d'accueil |
| Navigation | 2 pages en swipe : Dispo · Chat & Carte (chat repliable posé sur la map) |

### Interface cirque

- **Bouton Dispo** : cercles concentriques rouge/jaune animés façon intro Looney Tunes
- **Fond dynamique** : rouge foncé sur l'accueil, crème sur chat et carte
- **Onglets fanions** : barre de navigation style chapiteau avec bordures encre
- **Panneaux LED** : enseignes lumineuses ambre (police VT323) pour titres et statuts
- **Typo Bangers** : titres façon carton d'intro de dessin animé
- **Pins carte** : marqueurs personnalisés thème cirque
- **Edge-to-edge** : affichage plein écran avec safe areas

## Stack technique

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Jetpack Glance** — widget écran d'accueil
- **DataStore Preferences** — persistance locale
- **osmdroid** — carte OpenStreetMap
- **Coroutines / Flow** — état réactif
- minSdk 26 · targetSdk 36 · JDK 17

## Structure du projet

```
app/src/main/java/com/dispo/app/
├── MainActivity.kt           # Pager 2 pages + onglets fanions + transitions
├── core/
│   ├── Models.kt            # Friend, ChatMessage, DispoUiState
│   └── DispoRepository.kt   # État local, expiration minuit, démo amis
├── ui/
│   ├── DispoButton.kt       # Bouton animé cercles Looney Tunes
│   ├── HomePanel.kt         # Accueil + anneaux animés
│   ├── ChatPanel.kt         # Chat repliable posé sur la carte plein écran
│   ├── MapPanel.kt          # CircusMap : osmdroid teinté + pins cirque
│   ├── LedText.kt           # Composants panneau LED
│   └── theme/Theme.kt       # Palette cirque (rouge, crème, ambre…)
└── widget/
    └── DispoWidget.kt       # Widget Glance
```

## Build & installation

**Prérequis** : JDK 17+, Android SDK (chemin dans `local.properties`).

```bash
# Compiler l'APK debug
./gradlew assembleDebug

# Installer sur un appareil connecté
adb install app/build/outputs/apk/debug/app-debug.apk
```

L'APK debug se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

## Limites connues (v1.0)

- Pas de backend : amis et messages sont simulés localement
- Pas de notifications push
- Pas de partage GPS réel ni de picker de lieu
- Pas d'authentification ni de groupes multi-utilisateurs

## Prochaines étapes

- Backend **Supabase** (auth, groupes réels, chat temps réel)
- Notifications push **FCM** (« X est dispo jusqu'à ce soir »)
- Partage de position GPS et sélection de lieu sur la carte
- Build release signé pour le Play Store

## Licence

Projet privé — tous droits réservés.
