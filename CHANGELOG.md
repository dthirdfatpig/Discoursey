# Changelog

All notable changes to the Discoursey project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.0.4-beta] - 2026-11-21

### 🚀 Added
- **Discourse Polls & Topic Voting**:
  - Interactive native rendering for Discourse Polls embedded in posts: selectable options, live vote counts, percentages, visual progress bars, voter counts, and one-tap voting.
  - Native integration with the `discourse-topic-voting` plugin.
- **Enhanced Video Streaming & Uploads**:
  - Full TUS chunked upload support for the `discourse-video-stream` plugin (Cloudflare Stream direct upload via `POST /video-stream/upload-url` and Bunny.net Stream via `POST /video-stream/prepare-upload`).
  - Seamless fallback to Discourse's internal video upload tool (`/uploads.json`).
- **Official Reactions & Toggling**:
  - Aligned reaction emojis strictly with the official default set enabled by the `discourse-reactions` plugin (`heart` ❤️, `+1` 👍, `laughing` 😆, `open_mouth` 😮, `clap` 👏, `confetti_ball` 🎊, `hugs` 🫂).
  - Added clean removal / untoggling of active reactions and likes by tapping active chips or reaction picker items.

### 🐛 Fixed
- **Resolved HTTP 403 Forbidden on Uploads**:
  - Added synchronized CSRF token retrieval and persistence across app sessions with automatic retry on token expiration for all mutating requests (`POST /uploads.json`, `/video-stream/upload-url`, etc.).
- **Clean Image Rendering**:
  - Removed unwanted file names and dimension metadata from post bodies when viewing embedded images.
  - Removed filename captions from inserted upload markdown tags.
- **Topic Navigation**:
  - Eliminated the brief flash of the previously visited topic when opening a new discussion.

---

## [0.0.3-beta] - 2026-11-21

### 🚀 Added
- Pull-to-refresh on Modern and Classic topic lists.
- Interactive top notification pill alerting users when new topics are detected.
- Full topic titles displayed without truncation across both UI modes.
- Preference setting for user name display: Auto, Nickname first, or Full Name first.
- Consolidated build into a single unified Universal APK (`app-universal-release.apk`).

---

## [0.0.2-beta] - 2026-11-21

### 🐛 Fixed
- Resolved HTTP 403 Forbidden on forum connection by implementing standard Discourse session authentication flow (`/session/csrf.json` + `POST /session` with persistent cookie store).

---

## [0.0.1-beta] - 2026-11-21

### 🤖 AI Creation Disclosure
> This initial release was architected and implemented with AI assistance, tailored for the Discourse community and built under the GNU General Public License v3.0.
