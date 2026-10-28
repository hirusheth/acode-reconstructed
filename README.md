# Acode (`com.foxdebug.acode` v1.13.2) — Reconstructed Repository

[![Featured on Made with REA](https://madewithrea.com/badges/featured.svg)](https://madewithrea.com/builds/acode-v1-13-2-android-cordova-project-reconstructed-from-the-apk-with)

Reconstructed source tree, Cordova hybrid bundle, native Android plugins, Alpine/PRoot terminal scripts, and **Reverse Engineer Anything (REA)** static analysis report for **Acode (`com.foxdebug.acode` v1.13.2, `versionCode: 1008`)** from [F-Droid](https://f-droid.org/packages/com.foxdebug.acode).

---

## Artifact Metadata

| Property | Value |
| :--- | :--- |
| **Package ID** | `com.foxdebug.acode` |
| **Version** | `1.13.2` (`versionCode: 1008`) |
| **Source URL** | `https://f-droid.org/repo/com.foxdebug.acode_1008.apk` |
| **APK SHA-256** | `a8ed4864da95493d84bcd430b47a8900815eced7fe7c763929ef7a38c3987c39` |
| **APK Signing Certificate SHA-256** | `4ad655ec828b607be7a37dde5d41753e8f332778c21efab8845f44df72de5fff` (v1, v2, v3) |
| **SDK Levels** | `minSdkVersion: 24`, `targetSdkVersion: 28` (F-Droid PRoot execution mode), `compileSdkVersion: 35` |
| **Analysis Engine** | `rea-agents@6.8.0` (MCP) + `jadx-headless-mcp-0.7.1` + Android SDK `apkanalyzer` |

---

## Download & Build APK

### 1. Prebuilt Signed APK (GitHub Release & `dist/`)
- **GitHub Release**: [`v1.13.2` — `acode-reconstructed-v1.13.2-debug.apk`](https://github.com/tnq54/acode-reconstructed/releases/tag/v1.13.2)
- **In-Repository Build Output**: [`dist/acode-reconstructed-v1.13.2-debug.apk`](./dist/acode-reconstructed-v1.13.2-debug.apk) (SHA-256 in [`dist/acode-reconstructed-v1.13.2-debug.apk.sha256`](./dist/acode-reconstructed-v1.13.2-debug.apk.sha256))
- **GitHub Actions CI**: Every push to `main` automatically runs [`.github/workflows/build-apk.yml`](./.github/workflows/build-apk.yml) to assemble, `zipalign`, and `apksigner`-sign the APK artifact.

### 2. Build Locally from Source Tree
Modify any frontend files in `android/app/src/main/assets/www/` or shell initialization scripts (`init-alpine.sh`, `init-sandbox.sh`, `rm-wrapper.sh`) and run:

```bash
./scripts/build-apk.sh
```

This script packages the live `android/app/src/main/assets/` tree with `android/app/src/main/apk-container/`, aligns `resources.arsc` on 4-byte boundaries with `zipalign`, signs the APK with APK Signature Schemes v2 & v3 via `apksigner`, and outputs `dist/acode-reconstructed-v1.13.2-debug.apk`.

---

## Repository Layout

```text
acode-reconstructed/
├── README.md                          # Project overview and navigation guide
├── REVERSE_ENGINEERING_REPORT.md      # Full REA Reverse Engineering Report (Evidence IDs, Observations, Inferences, Unknowns)
├── config.xml                         # Decoded Cordova widget configuration & feature mappings
├── package.json                       # Reconstructed Cordova project manifest
├── evidence/
│   ├── rea-evidence-summary.json      # Structured index of REA Evidence IDs, hashes, and graph metrics
│   └── rea-evidence-bundle.json.gz    # Full deterministic REA MCP session Evidence bundle (gzip-compressed)
└── android/
    └── app/
        ├── build.gradle               # Reconstructed Android app Gradle configuration
        └── src/main/
            ├── AndroidManifest.xml    # Decoded AndroidManifest.xml
            ├── assets/
            │   ├── init-alpine.sh     # Downloads & configures PRoot + Alpine Linux rootfs + axs binary
            │   ├── init-sandbox.sh    # Launches PRoot Alpine Linux container with Android bind mounts
            │   ├── rm-wrapper.sh      # Protects bind-mounted /sdcard from accidental recursive rm
            │   ├── font/              # Bundled editor fonts
            │   └── www/               # Cordova WebView frontend (index.html, cordova.js, cordova_plugins.js, build/, plugins/, res/)
            ├── java/                  # 129 decompiled Java classes (from classes.dex & classes2.dex)
            │   ├── com/foxdebug/
            │   │   ├── acode/         # MainActivity, rk.auth (OAuth2 PKCE), rk.exec.terminal (TerminalService, ProcessServer, AlpineDocumentProvider), rk.customtabs
            │   │   ├── browser/       # BrowserActivity, Emulator, Device, Plugin
            │   │   ├── crashhandler/  # CrashActivity (:crash process), CrashHandler
            │   │   ├── ftp/           # Ftp (Apache Commons Net FTPClient)
            │   │   ├── sdcard/        # SDcard (SAF DocumentFile), MyFileObserver, WorkspaceIndex
            │   │   ├── server/        # Server, NanoHTTPDWebserver
            │   │   ├── sftp/          # Sftp (Maverick Synergy SSH), SftpSecurityStore
            │   │   ├── system/        # System (48 native actions), Ui, RewardPassManager, FileProvider, ShortcutActivity
            │   │   ├── websocket/     # WebSocketPlugin, WebSocketInstance (OkHttp3)
            │   │   └── webview/       # WebViewPlugin, WebViewActivity, WebViewInstance
            │   ├── com/silkimen/      # CordovaHttpPlugin & HTTP helpers
            │   ├── com/verso/         # Clipboard plugin
            │   └── org/apache/cordova/# Cordova framework & core plugins (Device, File, BuildInfo)
            └── res/
                └── xml/               # Decoded XML resources (config.xml, file_provider.xml, network_security_config.xml, etc.)
```

---

## Highlights from the Reverse Engineering Analysis

See [`REVERSE_ENGINEERING_REPORT.md`](./REVERSE_ENGINEERING_REPORT.md) for the complete technical analysis, including:
1. **REA Evidence Provenance**: Full ledger of `inspect_apk_signature`, `inspect_artifact`, `extract_artifact`, and `analyze_javascript_application` Evidence IDs.
2. **Alpine Linux / PRoot Terminal Sandbox (`com.foxdebug.acode.rk.exec.terminal`)**:
   - How `init-alpine.sh` and `init-sandbox.sh` bootstrap an Alpine rootfs and the `axs` server binary.
   - Why F-Droid builds set `targetSdkVersion="28"` (`ProcessManager.isFdroidBuild()`) to allow executing binaries directly from `$FILES_DIR`.
   - How `AlpineDocumentProvider` exposes `$FILES_DIR/public` to Android's Storage Access Framework (`com.foxdebug.acode.documents`).
3. **OAuth2 PKCE Authentication (`com.foxdebug.acode.rk.auth.Authenticator`)**:
   - S256 PKCE challenge generation, Chrome Custom Tabs flow (`https://acode.app/login`), `acode://auth/callback` deep-link handling, token exchange (`POST /api/user/app-token/exchange`), and Tink `EncryptedSharedPreferences` persistence.
4. **18 Cordova Native Plugins**: Full mapping between `assets/www/cordova_plugins.js`, `res/xml/config.xml`, and the decompiled Java classes under `android/app/src/main/java/`.
