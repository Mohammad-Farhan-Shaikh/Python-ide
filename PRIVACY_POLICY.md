# Privacy Policy for PyStudio

**Last updated:** September 2026

PyStudio ("the Application") is an open-source mobile Integrated Development Environment (IDE) designed for writing, editing, and executing Python code locally on Android devices.

### 1. Zero Personal Data Collection
PyStudio operates strictly on a local-first principle:
- **No telemetry or analytics:** We do not track user behavior, device IDs, or usage metrics.
- **No user account required:** The app does not require sign-up, registration, or logins.
- **No third-party ad networks:** PyStudio contains zero advertising SDKs or tracking pixels.

### 2. User Code and File Storage
- All Python scripts, directories, and project files created or imported in PyStudio are stored exclusively on your device's local database and private application storage sandbox (`androidx.room`).
- No source code or execution logs are ever transmitted to any remote servers or cloud databases.

### 3. Permissions
- **INTERNET:** Declared strictly for running Python network scripts (e.g. `urllib`, standard library networking) and opening external developer resources (such as GitHub Gist or LinkedIn web sharing) when explicitly initiated by the user.
- **Storage / File Access:** Uses standard Android Photo/Document Pickers (`ActivityResultContracts.OpenDocument`) to import or export `.py` scripts and `.zip` archives. The app does not require or request broad device storage permissions (`READ_EXTERNAL_STORAGE`).

### 4. Third-Party Integrations & Sharing
When you use the "Share Script" or "Connect (GitHub / LinkedIn)" actions, the app passes your selected code or text directly through Android's native system share intent to the destination application of your choice. No data is stored or intercepted by PyStudio.

### 5. Open Source & Transparency
PyStudio source code is publicly accessible on GitHub. Developers and users may inspect, compile, and audit the application independently.

### 6. Contact
For security inquiries or questions regarding this policy, please open an issue on the official GitHub repository.
