# Discoursey v0.0.4-beta

> 🤖 **AI Creation Notice**: Discoursey was designed and developed with AI assistance to provide a modern, highly customizable open-source native Android client for Discourse communities. Released under the **GNU General Public License v3.0**.

---

### 📦 Release Assets
- **Single Universal Package**: `app-universal-release.apk`
  - Signed release APK compatible with all Android devices running Android 7.0 (API 24) or newer across all architectures (ARMv7, ARMv8/ARM64, x86, x86_64).

---

### ✨ What's New in v0.0.4-beta

#### 1. 🔒 Fixed HTTP 403 on Image and Video Uploads
- Resolved HTTP 403 Forbidden on uploads by implementing synchronized CSRF token retrieval, automatic session persistence, and automatic token refreshes for all mutating endpoints (`POST /uploads.json`, `/video-stream/upload-url`, etc.).

#### 2. 🖼️ Clean Image Rendering Without File Names
- Stripped lightbox metadata, filenames, and resolution text from cooked HTML so only clean images are displayed without extraneous text.
- Cleaned markdown tag insertion in the composer to omit filename text.

#### 3. 🎥 Video Uploads (Discourse Video Stream Plugin & Internal)
- Added full TUS chunked upload support for the `discourse-video-stream` plugin (Cloudflare Stream direct upload via `POST /video-stream/upload-url` and Bunny.net Stream via `POST /video-stream/prepare-upload`).
- Added automated fallback to Discourse's internal video upload tool (`/uploads.json`).

#### 4. ❤️ Official Discourse Reactions & Clean Untoggling
- Aligned reaction emojis strictly with the official default set enabled by the `discourse-reactions` plugin (`heart` ❤️, `+1` 👍, `laughing` 😆, `open_mouth` 😮, `clap` 👏, `confetti_ball` 🎊, `hugs` 🫂).
- Added clean reaction removal / untoggling when tapping an active reaction chip, like icon, or picker item.

#### 5. ⚡ Instant Topic Opening (No Topic Flash)
- Cleared topic details immediately when opening or leaving a discussion so the previously visited topic never flashes on screen.

#### 6. 📊 Native Interactive Polls & Topic Voting
- Added full interactive rendering for Discourse Polls embedded in posts: selectable choices (radio / checkbox), live vote counts, percentages, visual progress bars, voter counts, and one-tap voting.
- Added built-in support for the `discourse-topic-voting` plugin.
