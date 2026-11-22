# Changelog

All notable changes to the Discoursey project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.0.3-beta] - 2026-11-21

### 🚀 Added
- **Feed Enhancements**:
  - Pull-to-refresh on Modern and Classic topic lists.
  - Interactive top notification pill that signals when new topics are detected, allowing one-tap feed refresh.
  - Full topic titles always displayed without truncation across both UI modes.
  - Formatted relative timestamps matching standardized intervals.
- **Media & Attachment Uploads**:
  - Direct image picking and uploading via Discourse `/uploads.json` with automated markdown insertion.
  - Video upload support with automated handling for direct uploads or Cloudflare/Bunny video streaming via `discourse-video-stream`.
  - Native inline rendering for post image attachments and video streams.
- **Emoji Reactions**:
  - Full Discourse custom reaction picker (❤️, 👍, 😂, 😮, 😢, 🚀, 🎉, 🔥).
  - Reaction chips on posts displaying counts and highlighting active user reactions.
- **User Display Settings**:
  - Preference setting for user name display: Auto (forum default), Nickname first, or Full Name first.
- **Classic UI Refinements**:
  - Compact row spacing and padding matching minimalist layout design.
- **Packaging**:
  - Unified single Universal APK (`app-universal-release.apk`) supporting all architectures.

### 🐛 Fixed
- Removed redundant inactive back arrow on the root home screen.
- Eliminated placeholder snippet text when topics lack excerpts.

---

## [0.0.2-beta] - 2026-11-21

### 🐛 Fixed
- **Resolved HTTP 403 Forbidden on Forum Connection**:
  - Implemented standard Discourse session authentication flow via `GET /session/csrf.json` and `POST /session` with form-encoded credentials, CSRF token, and `X-Requested-With` headers.
  - Resolved issue where account passwords were sent in the `Api-Key` HTTP header, which caused Discourse's authentication middleware to reject requests with `HTTP 403 Forbidden`.
  - Added `PersistentCookieJar` backed by `SharedPreferences` to persist authentication cookies (`_t` and `_forum_session`) across app launches.
  - Added dedicated UI toggle on the onboarding screen for **Password (Standard Session)** vs **API Key (User API Key or Admin API Key)** with auto-detection for 64-character tokens.
  - Enhanced error parsing to surface actual Discourse API error responses (e.g., incorrect credentials or 2FA required) rather than raw HTTP status codes.

---

## [0.0.1-beta] - 2026-11-21

### 🤖 AI Creation Disclosure
> This initial release was architected and implemented with AI assistance, tailored for the Discourse community and built under the GNU General Public License v3.0.

### 🚀 Highlights & Features
- **Universal Discourse Connection**: Connect seamlessly to any Discourse instance by providing URL, username, and password or API key.
- **Dual UI Engines**:
  - **Modern Flow UI**: Dark card stream featuring full topic names, post counts, topic status, embedded last-poster avatars, excerpt previews, and timestamps.
  - **Classic Clean UI**: Ultra-lean, retro-inspired interface featuring an instant "Type to filter" topic search bar, post comment counts, clean horizontal dividers, and a modular top tab layout (Profile, Messages, Topics).
- **Personal Messages & Notifications Inbox**:
  - Divided into two distinct synchronized tabs: **Private Messages** and **Notifications**.
  - One-tap "Mark all as read" API sync with Discourse.
- **Customizable Lower Navigation Bar**:
  - Configure the 5 lower buttons to match your preferred workflow (Home, Latest, Categories, Messages with red notification badge, Search, Profile, or Menu).
- **Plugin Ecosystem Support**:
  - **Discourse Image Album**: Integrated photo album viewer and composer insertion modal for saved community collections and sub-albums.
  - **Discourse Video Stream**: BBCode helper and adaptive video stream playback for Cloudflare Stream & Bunny.net uploads.
- **Deep Android Settings & Customization**:
  - Real-time disk cache management with 1-tap cache cleaner and cache limit controls.
  - Granular font sizing (Small, Normal, Large, Extra Large).
  - Multiple color themes: Modern Dark, Classic Dark, AMOLED Pure Black, and Clean Light.
  - Profile customization (update display name, bio, and website).
