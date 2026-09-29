# Contributing to PyStudio

First off, thank you for considering contributing to PyStudio! We welcome bug reports, feature proposals, and code contributions from developers of all skill levels.

---

## 🛠️ Development Setup

### Prerequisites
1. **JDK 17 or higher** (recommended: OpenJDK 17 or 21)
2. **Android Studio Ladybug (2024.2.1+)** or command-line Gradle toolchain
3. **Android SDK Platform 36** (`compileSdk = 36`) and **Build-Tools 34.0.0+**
4. **Android Device or Emulator** running Android 7.0 (API 24) or newer

### Getting Started
1. **Fork and clone the repository:**
   ```bash
   git clone https://github.com/your-username/pystudio.git
   cd pystudio
   ```
2. **Open the project in Android Studio:**
   - Select `Open an Existing Project` and choose the root `pystudio` folder.
   - Let Gradle sync and download required dependencies.
3. **Build and run tests via CLI:**
   ```bash
   # Run all JVM unit tests
   ./gradlew test

   # Assemble debug APK
   ./gradlew assembleDebug
   ```

---

## 🏗️ Architecture & Code Conventions

PyStudio follows modern Android architectural standards:
- **UI Framework:** 100% Jetpack Compose using Material Design 3 (M3) components and theming.
- **Architecture Pattern:** Model-View-ViewModel (MVVM) with Unidirectional Data Flow (UDF).
- **State Management:** Kotlin Coroutines `StateFlow` and `collectAsStateWithLifecycle()`.
- **Database & Persistence:** Android Jetpack Room with Kotlin Symbol Processing (`KSP`).
- **Python Execution Engine:** `Chaquopy` (Python 3.8+ native embedding) with custom standard I/O redirection for interactive console sessions.

### Key Directories
- `com.example.data`: Room database entity (`PythonFile`), DAO, and repository layer.
- `com.example.engine`: Python execution runtime, output chunk buffering, syntax parsing, and error diagnostics.
- `com.example.ui.editor`: Code editor component, line number gutter, quick access symbol bar, and syntax visual transformations.
- `com.example.ui.drawer`: VS Code-inspired file explorer tree, collapsible folder groups, and inline file/folder creation rows.
- `com.example.ui.screens`: High-level screens (`EditorScreen`, `OutputScreen`) and navigation routing.

---

## 🧪 Testing Guidelines

Before opening a pull request, ensure all tests pass cleanly:
```bash
./gradlew test
```
When introducing new features or refactoring core logic:
- Add corresponding unit tests in `app/src/test/java/com/example/`.
- Ensure new test cases cover edge cases (e.g. nested directory paths, Python syntax errors, input buffer edge cases).

---

## 🚀 Submitting a Pull Request

1. Create a descriptive feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Commit your changes with clear, semantic commit messages:
   ```bash
   git commit -m "feat(explorer): add nested directory creation"
   ```
3. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
4. Open a Pull Request against the `main` branch with a concise summary of what changed and test results.
