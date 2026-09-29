# PyStudio — Mobile Python IDE & Compiler for Android

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Python](https://img.shields.io/badge/Python-Embedded%20Engine-3776AB.svg?style=flat&logo=python)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**PyStudio** is a full-featured, offline-capable mobile Integrated Development Environment (IDE) built specifically for Android using modern **Kotlin** and **Jetpack Compose (Material 3)**. It provides a real development experience directly on phones and tablets with embedded Python runtime execution, interactive standard I/O (`input()`), multi-file workspace navigation, and developer community sharing.

---

## 📸 Key Capabilities & Highlights

- **Embedded Python Runtime:** Runs genuine Python code on-device without requiring external servers or persistent internet connectivity.
- **Interactive Terminal Console:** Fully supports standard stream redirection (`sys.stdout`, `sys.stderr`) and interactive user input (`sys.stdin` / `input()`).
- **VS Code-Inspired File Explorer:**
  - Workspace file tree with collapsible/expandable directory structures.
  - One-tap inline file creation (`📄+`) and folder creation (`📁+`) at root or within any nested folder.
  - Multi-tab file switching with unsaved changes tracking and automated persistence.
- **Developer-Grade Code Editor:**
  - Real-time syntax highlighting for keywords, strings, decorators, functions, built-ins, and comments.
  - Synchronized line numbering gutter with error line highlighting.
  - Quick-key symbol access bar (`Tab`, `def`, `class`, `import`, brackets, quotes).
  - Multi-level Undo / Redo history stack.
  - In-editor Find & Replace with occurrence count and navigation.
  - Code auto-formatter based on PEP 8 indentation standards.
- **Python Learning & Templates Hub:**
  - Curated collection of standard algorithms (Fibonacci, Palindromes, Binary Search, Sorting).
  - Ready-to-run interactive code templates (File I/O, OOP, Generators, RegEx).
- **One-Click Workspace Export & Developer Connect:**
  - Export all project scripts and directories as a standard `.ZIP` archive.
  - 1-tap clipboard and share integration with **GitHub Gist** and **LinkedIn**.

---

## 🏛️ Architecture & Tech Stack

PyStudio is architected according to Google's official Android Architecture Guidelines:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                   │
│   EditorScreen  •  OutputScreen  •  ProjectExplorer    │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│              ViewModel Layer (State Holder)            │
│   PyStudioViewModel  •  EditorState  •  TerminalState  │
└─────────────┬──────────────────────────────┬───────────┘
              │                              │
┌─────────────▼──────────────┐ ┌─────────────▼───────────┐
│     Persistence Layer      │ │     Execution Engine    │
│  Room DB (SQLite + KSP)    │ │   Embedded Python /     │
│  PythonFileRepository      │ │   I/O Interceptor       │
└────────────────────────────┘ └─────────────────────────┘
```

- **Languages:** Kotlin (100% codebase), Python (runtime engine)
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Local Persistence:** Android Jetpack Room with Kotlin Symbol Processing (`KSP`)
- **Concurrency & Reactivity:** Kotlin Coroutines, `StateFlow`, `collectAsStateWithLifecycle`
- **Testing:** Local JVM Unit Testing with JUnit 4 and Robolectric

---

## 📂 Project Structure

```text
pystudio/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/            # Room Entity, DAO, Repository
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── FileRepository.kt
│   │   │   │   │   ├── PythonFile.kt
│   │   │   │   │   └── PythonFileDao.kt
│   │   │   │   ├── engine/          # Python engine, syntax parsing, I/O interceptor
│   │   │   │   │   ├── AutoCompleteEngine.kt
│   │   │   │   │   ├── CodeFormatter.kt
│   │   │   │   │   ├── PythonErrorParser.kt
│   │   │   │   │   └── PythonRunner.kt
│   │   │   │   └── ui/              # Compose screens, components, theme
│   │   │   │       ├── components/  # TabBar, QuickKeys, SearchBar, Dialogs
│   │   │   │       ├── drawer/      # VS Code style Explorer Drawer
│   │   │   │       ├── editor/      # Syntax visual transformation, code view
│   │   │   │       ├── screens/     # EditorScreen, OutputScreen
│   │   │   │       ├── theme/       # Color palette, Typography, Dark Theme
│   │   │   │       └── PyStudioViewModel.kt
│   │   │   └── res/                 # Vector drawables, adaptive launcher icon, strings
│   │   └── test/java/com/example/   # JVM unit tests (Runner, DAO, Syntax, Formatter)
│   └── build.gradle.kts             # App module dependencies & SDK configuration
├── gradle/                          # Gradle wrapper and version catalogs
├── CONTRIBUTING.md                  # Development guidelines & contribution workflow
├── LICENSE                          # MIT Open Source License
├── PRIVACY_POLICY.md                # Local-first zero telemetry policy
└── README.md                        # Project documentation (this file)
```

---

## ⚡ Getting Started (For Developers)

### 1. Prerequisites
- **JDK 17** or **JDK 21** installed and configured in your environment (`JAVA_HOME`).
- **Android Studio** (Ladybug 2024.2.1 or newer recommended) with Android SDK Platform 36 installed.

### 2. Build & Run
Clone the repository:
```bash
git clone https://github.com/your-username/pystudio.git
cd pystudio
```

Run tests to verify configuration:
```bash
./gradlew test
```

Assemble the debug APK:
```bash
./gradlew assembleDebug
```
The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permissions & Security

PyStudio adheres to strict least-privilege permissions and Google Play safety standards:
- **`android.permission.INTERNET`:** Only used when user-written Python code makes explicit network calls (e.g. `urllib`), or when opening external GitHub/LinkedIn links via user action.
- **Storage Access:** Uses zero-permission Android Document Pickers (`ActivityResultContracts.OpenDocument`). No sensitive storage permissions (`READ_EXTERNAL_STORAGE`) are requested.
- **No Analytics / Telemetry:** Your scripts, files, and outputs never leave your physical device.

---

## 🤝 Contributing

Contributions, bug fixes, and feature requests are welcome! Please check out [CONTRIBUTING.md](CONTRIBUTING.md) for branch naming conventions, architecture details, and pull request guidelines.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
