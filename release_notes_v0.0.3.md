# Discoursey v0.0.3-beta

> 🤖 **AI Creation Notice**: Discoursey was designed and developed with AI assistance to provide a modern, highly customizable open-source native Android client for Discourse communities. Released under the **GNU General Public License v3.0**.

---

### 📦 Release Assets
- **Single Universal Package**: `app-universal-release.apk`
  - Signed release APK compatible with all Android devices running Android 7.0 (API 24) or newer across all architectures (ARMv7, ARMv8/ARM64, x86, x86_64).

---

### ✨ What's New in v0.0.3-beta

#### 1. 🔄 Topic Feeds: Pull-to-Refresh & Real-Time Alerts
- Added swipe down / pull-to-refresh on both Modern and Classic topic feeds.
- Added an interactive floating notification banner ("New topics available • Tap to update") when newer topics are detected on the forum.
- Fixed root homepage navigation by removing the dead back arrow.
- Display full topic titles without truncation across both UI modes.
- Cleaned excerpt previews to omit empty placeholder messages.

#### 2. 📎 Direct Attachments & Video Streaming
- Added native file picker for images, uploading directly to Discourse's `/uploads.json` with automated markdown insertion.
- Added video upload support with automated handling for direct uploads or Cloudflare/Bunny video streaming via `discourse-video-stream`.
- Native inline rendering for post image attachments and video streams.

#### 3. 💬 Rich Emoji Reactions
- Added interactive Discourse emoji reaction picker (❤️, 👍, 😂, 😮, 😢, 🚀, 🎉, 🔥) alongside standard likes.
- Added live reaction counter chips on posts with active user state highlighting and toggle support.

#### 4. 👤 User Display Customization
- Added a new setting to prioritize nicknames (`@username`) or full names across all topic lists, posts, and notifications, with automatic fallback respecting the forum's configuration (`enable_names`).

#### 5. 📐 Classic Clean UI Refinements
- Tightened row spacing and padding to deliver a compact reading layout.
- Streamlined relative timestamp formatting across all views.
