# Reverse Engineering Report: Acode (`com.foxdebug.acode` v1.13.2)

> **Target Package**: `com.foxdebug.acode` (`versionCode: 1008`, `versionName: 1.13.2`)  
> **Source Repository / Distribution**: [F-Droid (`com.foxdebug.acode`)](https://f-droid.org/packages/com.foxdebug.acode)  
> **Artifact Path**: `com.foxdebug.acode_1008.apk`  
> **Artifact SHA-256**: `a8ed4864da95493d84bcd430b47a8900815eced7fe7c763929ef7a38c3987c39`  
> **Artifact Size**: `16,826,562` bytes (`16.04 MiB`)  
> **Primary Tooling**: Reverse Engineer Anything (`rea-agents@6.8.0` MCP), JADX (`0.7.1`), Android SDK `apkanalyzer`

---

## 1. Evidence & Provenance Ledger

Following the **Reverse Engineer Anything (REA)** methodology, static observations from REA MCP tools are recorded with immutable Evidence IDs and distinguished from external helper tool outputs (`JadxDecompiler` / `apkanalyzer`) and architectural inferences.

### 1.1 REA MCP Retained Evidence Records

| Analysis Phase | REA MCP Tool | Summary Evidence ID | Parent / Full Evidence ID | Target SHA-256 / Scope |
| :--- | :--- | :--- | :--- | :--- |
| **APK Signature Verification** | `inspect_apk_signature` | `ev_4fa7746125745a01076d16e56b5ff9eb328f26433d20c8ed28274218b99f4901` | `ev_8237ed2c90be495c8390ef56fe89116761baf23f410c1a1a2071cbd9c58253bf` | `a8ed4864da95493d84bcd430b47a8900815eced7fe7c763929ef7a38c3987c39` |
| **Container & Graph Triage** | `inspect_artifact` | `ev_f1e7d69787b1ac553db5f8e25f88a0b9727848c4c259794691ef912ea1a69100` | `ev_8bdc972a1985189e5ec9472f1093d92dfabed0668e38b2e8ac5adcca75795b6d` (Inventory: `ev_414580af8010adb25e431668da0830e73a109da583375589594e6a0bdc49ff7d`, Manifest: `agm_cf333f561430471ef33fcb933ac95bb78285b206ab65b67e1055b70be5507f98`) | `948` nodes, `1031` occurrences, `1030` edges |
| **Deterministic Extraction** | `extract_artifact` | `ev_3c4046ac345546c1aa1b8c949b65d8710e5d045b71d03f0f5adad516a355a959` | Extraction ID: `aex_38e1f2c114013813afeb1e873c9795882ca5f0e9726e36d2b68203c6f1d074f6` | Extracted tree SHA-256: `da40389f35ce67050aebe0b026526c4c202c256c777785beb1e2a75bd03845ca` |
| **JS Entry & Cordova Bridge** | `analyze_javascript_application` | `ev_44318fcb5fc4d93ad13d3a89c94107ef44e085c11953e0882e4bb0d2b1a9a668` | `ev_6a2b74a51b97ecc0d08ce873111f3c992065de3e9d8e9fef9da47907a8a62269` | `assets/www` (`index.html`, `cordova.js`, `cordova_plugins.js`), root SHA-256: `a2b956dbfb6ae681b3324d90959626a5d407c0a602e514db798a6528ebde74bb` |
| **JS Boot & Custom Plugins** | `analyze_javascript_application` | `ev_bdc1f3fc0b40a1c3d427da5011a5d959d61d5e8ee3fee13e69d561f45b4d89d7` | `ev_4aba62deff0d6191a7596c7afd8e3130af609ada675710ba58dc5bc45753854d` | `build/boot.js` + 9 custom Cordova plugin JS interfaces |

### 1.2 Supplementary Non-MCP Static Tooling
- **JADX Decompiler (`jadx-headless-mcp-0.7.1-all.jar` / `jadx.api.JadxDecompiler`)**: Decompiled all `129` application and Cordova plugin top-level Java classes (`66` in `classes.dex`, `63` in `classes2.dex`) into `android/app/src/main/java/`.
- **Android SDK `apkanalyzer`**: Decoded binary `AndroidManifest.xml` (`android/app/src/main/AndroidManifest.xml`) and XML resources (`android/app/src/main/res/xml/*.xml`).

---

## 2. Static Observations

### 2.1 APK Signing & Container Integrity (`ev_4fa77461...`, `ev_f1e7d697...`)
- **Signing Schemes**: JAR Signing (v1), APK Signature Scheme v2, and APK Signature Scheme v3 are all present (`v3_1` is absent).
- **Certificate Consistency**: All 3 schemes share a single self-consistent signer certificate (`certificate_sha256: 4ad655ec828b607be7a37dde5d41753e8f332778c21efab8845f44df72de5fff`, `self_consistent: true`).
- **Artifact Graph (`agm_cf333f56...`)**:
  - Total container nodes: `948` (`1,031` occurrences, `1,030` edges, `0` integrity contradictions).
  - Multi-DEX bytecode layout:
    - `classes.dex` (`8,453,476` bytes, SHA-256: `a8083bcba0896ae7bc1f44406e2e4910bc982610cf81d46e835df8b7454dcbe8`): Contains `com.foxdebug.*`, `com.silkimen.*`, `com.verso.cordova.clipboard.*`, AndroidX, Kotlin stdlib, OkHttp3, NanoHTTPD, Apache Commons Net, and Maverick Synergy SSH (`com.sshtools`).
    - `classes2.dex` (`9,543,008` bytes, SHA-256: `534473b84aa31d0318de5ab20ec7ce5d01b48091285874c9091fa0e1bd2e89d1`): Contains `org.apache.cordova.*` framework and core plugins, BouncyCastle (`org.bouncycastle`), Rhino (`org.mozilla`), and Google Tink (`com.google.crypto.tink`).
  - No native `.so` libraries (`lib/<abi>/*.so`) are bundled directly inside the APK container.

### 2.2 Android Manifest & Component Topology (`AndroidManifest.xml`)
- **SDK Levels**: `minSdkVersion="24"` (Android 7.0), `targetSdkVersion="28"` (Android 9.0), `compileSdkVersion="35"`.
  - *Why `targetSdkVersion="28"`?* In `com.foxdebug.acode.rk.exec.terminal.ProcessManager.isFdroidBuild()`, the app explicitly checks `targetSdkVersion <= 28`. Targeting SDK 28 allows executing binaries (`proot`, `axs`, shell scripts) directly from the app's writable data directory (`$FILES_DIR`) without Android 10+ (`targetSdkVersion >= 29`) W^X `SELinux` `app_data_file` execute restrictions.
- **Declared Permissions**:
  - Storage: `MANAGE_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`
  - Network: `INTERNET`, `ACCESS_NETWORK_STATE`
  - Execution & Services: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `WAKE_LOCK`, `POST_NOTIFICATIONS`, `VIBRATE`
  - Shortcuts: `com.android.launcher.permission.INSTALL_SHORTCUT`
- **Activities (4)**:
  1. `com.foxdebug.acode.MainActivity` (`exported="true"`, `launchMode="singleTask"`): Main Cordova host activity. Handles `MAIN`/`LAUNCHER`, file `VIEW`/`EDIT`/`SEND`/`SEND_MULTIPLE` intents across text/code MIME types, and deep links (`acode://auth/callback`, `acode://plugin/view/*`).
  2. `com.foxdebug.browser.BrowserActivity` (`exported="false"`, `launchMode="singleTask"`): Built-in browser preview activity with DevTools/Eruda injection and desktop/mobile device emulation (`Emulator`, `Device`).
  3. `com.foxdebug.webview.WebViewActivity` (`exported="false"`): Secondary WebView activity managed by `WebViewPlugin`.
  4. `com.foxdebug.crashhandler.CrashActivity` (`exported="false"`, `process=":crash"`): Out-of-process crash reporter UI triggered by `CrashHandler`.
- **Services (1)**:
  1. `com.foxdebug.acode.rk.exec.terminal.TerminalService` (`exported="false"`, `foregroundServiceType="specialUse"`): Manages background/foreground terminal sessions, `PowerManager.WakeLock` (`AcodeTerminal:WakeLock`), notification actions (`action_stop`, `action_wakelock`), and `ProcessManager` lifecycle.
- **Content Providers (4)**:
  1. `com.foxdebug.acode.rk.exec.terminal.AlpineDocumentProvider` (`authorities="com.foxdebug.acode.documents"`, `exported="true"`, `grantUriPermissions="true"`, `permission="android.permission.MANAGE_DOCUMENTS"`): Exposes the Alpine Linux sandbox directory (`$FILES_DIR/public`) to Android's Storage Access Framework (SAF).
  2. `com.foxdebug.system.FileProvider` (`authorities="com.foxdebug.acode.provider"`, `exported="false"`, `grantUriPermissions="true"`): Custom `ContentProvider` serving local files and images.
  3. `org.apache.cordova.file.FileProvider` (`authorities="com.foxdebug.acode.cdv.core.file.provider"`): Cordova file plugin provider.
  4. `androidx.startup.InitializationProvider` (`authorities="com.foxdebug.acode.androidx-startup"`).

### 2.3 Cordova Bridge & Native Plugins (`ev_44318fcb...`, `ev_bdc1f3fc...`, Decompiled Java)
`assets/www/cordova_plugins.js` registers **18 Cordova plugin modules** across **15 plugins**:

| Plugin ID / Module | Native Java Class (`android/app/src/main/java/...`) | JS Bridge (`assets/www/plugins/...`) | Key Native Actions & Responsibilities |
| :--- | :--- | :--- | :--- |
| `com.foxdebug.acode.rk.auth` | `com.foxdebug.acode.rk.auth.Authenticator`, `EncryptedPreferenceManager` | `www/Authenticator.js` | OAuth2 PKCE (`S256`) login via Chrome Custom Tabs (`https://acode.app/login?redirect=app&authFlow=app-code...`), token exchange at `POST https://acode.app/api/user/app-token/exchange`, AES256-GCM Tink `EncryptedSharedPreferences`, and WebView cookie injection (`token` on `.acode.app`). |
| `com.foxdebug.acode.rk.exec.terminal` | `Executor`, `TerminalService`, `ProcessManager`, `ProcessServer`, `AlpineDocumentProvider`, `BackgroundExecutor`, `StreamHandler`, `ProcessUtils` | `www/Executor.js`, `www/TerminalService.js` | Interactive & background shell execution (`sh -c`). Starts `TerminalService`, spawns localhost `ProcessServer` (`org.java_websocket.server.WebSocketServer`) streaming raw `ByteBuffer` stdout/stdin, and initializes PRoot Alpine sandbox via `$PREFIX/init-sandbox.sh`. |
| `com.foxdebug.acode.rk.customtabs` | `com.foxdebug.acode.rk.customtabs.CustomTabsPlugin` | `www/CustomTabs.js` | Launches `androidx.browser.customtabs.CustomTabsIntent` with configurable toolbar color and title visibility. |
| `com.foxdebug.acode.rk.plugin.plugincontext` | `com.foxdebug.acode.rk.plugin.plugincontext.Tee` | `www/plugin.js` | Diagnostic test plugin (`sayHello`). |
| `cordova-plugin-system` (`1.0.0`) | `com.foxdebug.system.System`, `Ui`, `RewardPassManager`, `FileProvider`, `ShortcutActivity` | `www/plugin.js` | 48 native bridge methods: SAF file/URI operations, `copyAssets` (extracts `init-alpine.sh`, `init-sandbox.sh`, `rm-wrapper.sh`), permissions (`MANAGE_EXTERNAL_STORAGE`), character encoding conversion, home screen shortcuts, Intent launching, package inspection, status/navigation bar theming (`Ui`), and signed ad-free reward passes (`RewardPassManager`). |
| `cordova-plugin-sdcard` (`0.0.1`) | `com.foxdebug.sdcard.SDcard`, `MyFileObserver`, `WorkspaceIndex` | `www/plugin.js` | Storage Access Framework (`DocumentsContract` / `DocumentFile`) directory traversal, copy/move/rename/delete, `FileObserver` file watching, and parallel workspace file indexing (`WorkspaceIndex`). |
| `cordova-plugin-server` (`0.0.1`) | `com.foxdebug.server.Server`, `NanoHTTPDWebserver` | `www/plugin.js` | Embedded HTTP server (`fi.iki.elonen.NanoHTTPD`) on configurable port for live HTML/JS preview. |
| `cordova-plugin-ftp` (`0.0.1`) | `com.foxdebug.ftp.Ftp` | `www/plugin.js` | Full FTP/FTPS client built on Apache Commons Net (`org.apache.commons.net.ftp.FTPClient`). |
| `cordova-plugin-sftp` (`1.0.0`) | `com.foxdebug.sftp.Sftp`, `SftpSecurityStore` | `www/plugin.js` | SSH/SFTP client built on Maverick Synergy (`com.sshtools.client.sftp.SftpClient`) with password and key-file authentication and host key verification (`SftpSecurityStore`). |
| `cordova-plugin-websocket` (`1.0.0`) | `com.foxdebug.websocket.WebSocketPlugin`, `WebSocketInstance` | `www/plugin.js` | OkHttp3-backed native WebSocket client bridging binary/text frames to JS. |
| `cordova-plugin-browser` (`0.0.1`) | `com.foxdebug.browser.Browser`, `BrowserActivity`, `Emulator`, `Device` | `www/plugin.js` | Launches `BrowserActivity` or `WebViewActivity` with desktop/device user-agent & viewport emulation. |
| `cordova-plugin-acode-webview` (`1.0.0`) | `com.foxdebug.webview.WebViewPlugin`, `WebViewActivity`, `WebViewInstance` | `www/plugin.js` | Multi-instance floating/fullscreen WebView controller with console & DOM inspection bridges. |
| `cordova-plugin-crashhandler` (`1.0.0`) | `com.foxdebug.crashhandler.CrashHandler`, `CrashActivity` | *(Native initialization)* | Registers global `Thread.UncaughtExceptionHandler` and launches `CrashActivity` with device metadata and stack trace. |
| Standard Cordova Plugins | `com.verso.cordova.clipboard.Clipboard`, `org.apache.cordova.device.Device`, `org.apache.cordova.file.FileUtils`, `org.apache.cordova.buildinfo.BuildInfo`, `com.silkimen.cordovahttp.CordovaHttpPlugin` | `assets/www/plugins/...` | Clipboard (`1.3.0`), Device (`2.1.0`), File (`8.1.3`), BuildInfo (`4.0.0`), Advanced HTTP (`3.3.1`). |

### 2.4 Embedded Shell Scripts & Alpine Linux PRoot Sandbox (`assets/*.sh`)
Three critical shell scripts in `assets/` implement Acode's built-in Linux terminal environment (`axs` / Alpine Linux on PRoot):
1. `assets/init-alpine.sh`:
   - Checks CPU architecture (`aarch64`, `arm64`, `x86_64`, `arm`).
   - Downloads `proot.tar.xz` from `https://github.com/Acode-Foundation/acode-terminal-proot/releases/download/20250529/proot_<arch>.tar.xz`.
   - Downloads the latest Alpine minirootfs release via GitHub API (`https://api.github.com/repos/Acode-Foundation/acode-Wait-Alpine/releases/latest`).
   - Extracts `alpine` into `$ALPINE_DIR`, configures `/etc/resolv.conf` (`8.8.8.8`, `8.8.4.4`) and `/etc/profile`, installs `rm-wrapper.sh` at `/usr/local/bin/rm`, and downloads the `axs` terminal server binary from `https://github.com/Acode-Foundation/axs/releases/latest/download/axs-musl-android-<arch>`.
2. `assets/init-sandbox.sh`:
   - Invokes `proot` (`--kill-on-exit`, `-w /root`, `-0` root spoofing, `--link2symlink`, `-L`) mounting `$ALPINE_DIR` as `/` and binding `/dev`, `/proc`, `/sys`, `/sdcard`, `$SHARED_DIR/public` (`/public`), and Android runtime apex paths (`com.android.runtime`, `com.android.art`).
3. `assets/rm-wrapper.sh`:
   - Safety wrapper installed inside Alpine at `/usr/local/bin/rm` that prevents accidental recursive deletion (`rm -rf`) of bind-mounted host directories (`/sdcard`, `/storage/emulated/0`).

### 2.5 Frontend Web Bundle (`assets/www`)
- Built with Webpack 5 (`webpackChunkacode`), bootstrapped by `assets/www/index.html` -> `cordova.js` + `./build/boot.js` -> `./build/main.js` and lazy-loaded chunks (`build/*.build.js`).
- Includes Ace Editor (`build/src-noconflict/`) with 120+ syntax modes, themes, and workers (`worker-javascript.js`, `worker-html.js`, `worker-css.js`, `worker-json.js`, `worker-xml.js`, `worker-yaml.js`, `worker-php.js`, `worker-lua.js`, `worker-coffee.js`, `worker-xquery.js`), Markdown-it, Prettier, Acorn, Purify, and 38 localization bundles (`assets/www/res/lang/*.json`).

---

## 3. Architectural Inferences

1. **Dual-Mode Terminal Architecture (`axs` vs. `ProcessServer`)**:
   - Based on `init-alpine.sh`, `ProcessManager.java`, and `ProcessServer.java`, Acode supports both a local Java WebSocket process bridge (`ProcessServer` streaming `ProcessBuilder("sh", "-c", ...)` over `127.0.0.1:<random_port>`) and the external Go/Rust `axs` binary downloaded inside Alpine (`axs-musl-android-<arch>`).
   - In F-Droid builds (`targetSdkVersion == 28`), `ProcessManager.isFdroidBuild()` returns `true`, so `axs` is downloaded dynamically by `init-alpine.sh` rather than bundled as `libaxs.so` in `jniLibs`.
2. **Play Store vs. F-Droid Build Flavor Separation**:
   - `RewardPassManager.java` manages cryptographically signed JSON reward passes (`reward_passes_state_v1` in `EncryptedSharedPreferences`) using an embedded Ed25519/RSA verification flow (`PUBLIC_KEY_SPKI_B64`) to grant temporary ad-free windows, confirming the upstream codebase builds both an ad-supported Play Store flavor and the F-Droid flavor from the same Java source tree.

---

## 4. Limitations & Unknowns

- **Static-Only Scope**: No live Android device/emulator (`adb` / `frida`) runtime trace was requested or executed; all findings are derived from static inspection of the shipped APK (`a8ed4864da95493d84bcd430b47a8900815eced7fe7c763929ef7a38c3987c39`).
- **Minified Frontend Bundles**: While `cordova.js`, `cordova_plugins.js`, and all plugin JS bridges (`assets/www/plugins/**`) are unminified, the Webpack chunks in `assets/www/build/*.js` are production-minified without `.map` source maps inside the APK.
- **Resource Path Obfuscation**: Binary layout/drawable resources under `res/` use AAPT2 short resource names (`res/zz.png`, `res/zq.xml`); key XML configuration resources (`config.xml`, `file_provider.xml`, `cdv_core_file_provider_paths.xml`, `image_share_filepaths.xml`, `network_security_config.xml`) have been decoded back to their canonical names in `android/app/src/main/res/xml/`.
