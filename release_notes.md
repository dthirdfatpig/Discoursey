# Discoursey v0.0.2-beta

> 🏷️ **Labels**: `Beta` | `Built with AI` | `GPL-3.0`
> 🤖 **AI Creation Notice**: Discoursey was architected and developed with AI assistance to provide a fast, customizable native Android client for Discourse communities worldwide.

## 🚀 What's Changed in v0.0.2-beta

### 🐛 Fixed HTTP 403 Forbidden on Community Forum Connection
- **Official Discourse Session Authentication Flow**:
  - Implemented the official Discourse session handshake via `GET /session/csrf.json` and form-encoded `POST /session` (`login` & `password`).
  - Added proper `X-CSRF-Token` and `X-Requested-With: XMLHttpRequest` request headers.
  - Eliminated erroneous transmission of user password in the `Api-Key` HTTP header, which caused Discourse's authentication middleware to reject requests with `HTTP 403 Forbidden`.
- **Persistent Session Storage**:
  - Integrated `PersistentCookieJar` backed by `SharedPreferences` to persist authentication session cookies (`_t` and `_forum_session`) across app launches.
- **Dedicated Authentication Toggle**:
  - Added a clean UI selector on the onboarding screen for **Password (Standard Session)** vs **API Key (User API Key or Admin API Key)** with auto-detection for 64-character tokens.
- **Enhanced Error Diagnostics**:
  - Surfaces exact Discourse server error responses (e.g. incorrect password, 2FA required, or login required) instead of generic HTTP status codes.

---

## 📦 Download Assets
- **`app-arm64-v8a-release.apk`**: For 64-bit ARM devices (modern phones & tablets) - *Recommended*
- **`app-armeabi-v7a-release.apk`**: For 32-bit ARM devices (older/budget devices)
- **`app-universal-release.apk`**: Universal APK for all supported architectures
- **Source Code**: Included automatically in GitHub release archives (`.zip` and `.tar.gz`)
