# Discoursey

![Discoursey Banner](banner.png)

[![Version](https://img.shields.io/badge/version-0.0.4--beta-blue.svg)](https://github.com/dthirdfatpig/Discoursey/releases)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-green.svg)](LICENSE)
[![Built with AI](https://img.shields.io/badge/built%20with-AI-blueviolet.svg)](#-ai-creation-disclosure)
[![Platform](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-success.svg)](https://github.com/dthirdfatpig/Discoursey/releases)
[![Package](https://img.shields.io/badge/APK-Universal-orange.svg)](https://github.com/dthirdfatpig/Discoursey/releases)
[![F-Droid](https://img.shields.io/badge/F--Droid-Ready-34D399.svg)](https://github.com/dthirdfatpig/Discoursey/releases)

> 🤖 **AI Creation Notice**: Discoursey is an open-source Android client designed and developed with AI assistance to deliver a fast, customizable experience for Discourse communities worldwide.

**Discoursey** is an open-source, highly customizable native client for Discourse forums. Designed for performance, flexibility, and user choice, Discoursey connects to any public or private Discourse community and supports both a dynamic, modern feed and an ultra-lean, classic reading layout.

📦 **[Download Latest Universal APK & Source Code on GitHub Releases](https://github.com/dthirdfatpig/Discoursey/releases)**

---

## 🌟 General Description & Overview

Discourse powers some of the most vibrant technical and community forums on the web. While standard mobile browser experiences can feel heavy or constrained, Discoursey provides a snappy, native Android experience built using modern Android architecture, Jetpack Compose, OkHttp, and Kotlin Coroutines.

Whether you prefer a rich card-based interface with last-poster activity streams or a minimalist, distraction-free reading experience, Discoursey gives you complete control over your forum browsing.

---

## ⚡ Core Functions & Capabilities

### 1. 🌐 Universal Discourse Connectivity
- **Connect to Any Forum**: Enter your Discourse instance URL (`https://yourcommunity.com`), username, and password or API key.
- **Robust Session Authentication**: Full support for persistent CSRF tokens, session cookie persistence, and password or API key authentication.
- **Built-in Demo Mode**: Test drive features instantly on public communities.

### 2. 🎨 Dual UI Engines
Discoursey comes built-in with two distinct UI layouts:
- **Modern Flow UI**:
  - Full topic title without truncation and status indicators (pinned, closed).
  - Rich activity stream showcasing the last poster's avatar, username, clean message excerpts, and relative timestamps.
  - Pull-to-refresh and real-time floating pill notifying when new topics are available.
  - Instant transition without flashing previously visited topics.
  - Quick action floating buttons for composing new topics and immediate replies.
- **Classic Clean UI**:
  - Ultra-clean, compact layout with tight spacing and high readability.
  - Full topic titles always displayed.
  - Instant **"Type to filter"** live search box at the top of topic feeds.
  - Comment counters with compact bubble icons and clean relative timestamps.
  - Pull-to-refresh support.

### 3. 💬 Rich Reactions & Post Interactions
- **Official Discourse Reactions**: Support for the exact default emojis enabled by the Discourse Reactions plugin (`heart` ❤️, `+1` 👍, `laughing` 😆, `open_mouth` 😮, `clap` 👏, `confetti_ball` 🎊, `hugs` 🫂).
- **1-Tap Reaction Toggling**: Tap an active reaction chip or emoji to cleanly remove/untoggle your reaction, or tap another to switch.
- **Interactive Reaction Chips**: View real-time reaction counts per emoji on each post with active user highlighting.

### 4. 📊 Native Polls & Topic Voting
- **Discourse Polls**: Full interactive rendering of polls embedded in posts. Shows selectable options, live vote counts, percentages, visual progress bars, voter counts, and one-tap voting.
- **Topic Voting**: Built-in support for the `discourse-topic-voting` plugin with interactive vote count badges and toggleable votes.

### 5. 📎 Direct Attachments & Video Streams
- **Clean Image Display**: Embedded images render smoothly via Coil without extraneous file names or dimension metadata cluttering the post.
- **Direct Image Uploads**: Select photos from your device to upload directly to Discourse (`/uploads.json`) with automated markdown formatting.
- **Cloudflare & Bunny Video Streams**: Full TUS chunked upload support for the `discourse-video-stream` plugin (Cloudflare Stream direct upload via `POST /video-stream/upload-url` and Bunny.net Stream via `POST /video-stream/prepare-upload`).
- **Internal Video Upload Tool**: Automated fallback to Discourse's internal video upload tool for communities without the video stream plugin.

### 6. 📬 Personal Messages & Notification Inbox
- **Two Dedicated Tabs**:
  - **Private Messages**: View, read, and reply to private discussions with community members.
  - **Notifications**: Real-time listing of mentions, replies, quotes, and badges.
- **Unread Counter Badges**: Visual badge on the navigation bar displaying unread count.
- **1-Tap Sync**: Instantly mark all notifications as read via Discourse's native API.

### 7. 🎛️ Customizable Lower Navigation Bar
Customize the 5 lower navigation buttons to fit your exact habits:
- Map buttons to **Home**, **Search**, **Personal Messages**, **Profile**, **Menu**, **Latest Topics**, **Categories**, or **Bookmarks**.
- Choose from preset configurations or customize individual slots.

### 8. 🛠️ Android & Storage Preferences
- **Username vs Nickname Display**: Choose whether user nicknames (`@handle`) or full names take the primary prominent spot across topics and posts, or let the app automatically follow the forum's configuration.
- **Disk & Image Cache Management**: Inspect real-time storage usage and clear cache with one tap.
- **Configurable Cache Limits**: Restrict storage usage to 50MB, 100MB, 250MB, 500MB, or 1GB.
- **Font Sizing**: Small, Normal, Large, and Extra Large typography options.
- **Color Themes**: Modern Dark, Classic Dark (slate), AMOLED Pure Black, and Clean Light themes.
- **Profile Updates**: Change your display name, bio, and website directly from the app.

---

## 📱 Releases & Architecture

Discoursey is packaged as a single **Universal APK** compatible with all supported Android device architectures (ARMv7, ARMv8/ARM64, x86, and x86_64):

| Package | Architecture | Description |
| :--- | :--- | :--- |
| `app-universal-release.apk` | **Universal** | Single unified package for all modern and legacy Android devices (Android 7.0+). |

Visit the **[Releases Page](https://github.com/dthirdfatpig/Discoursey/releases)** to download the latest build and source code archives.

---

## 📜 Latest Changelog

### [0.0.4-beta] - 2026-11-21

- **Uploads & Authentication**:
  - Resolved HTTP 403 Forbidden on image and video uploads by introducing synchronized CSRF token retrieval, automatic session persistence, and instant token refreshes on mutating endpoints.
- **Image Display & Formatting**:
  - Removed unwanted file names and dimension metadata from post bodies when viewing embedded images.
  - Stripped filename captions from uploaded markdown tags.
- **Video Uploads & Streaming**:
  - Added full TUS chunked upload support for the `discourse-video-stream` plugin (Cloudflare Stream direct upload via `POST /video-stream/upload-url` and Bunny.net Stream via `POST /video-stream/prepare-upload`).
  - Added seamless fallback to Discourse's internal video upload tool (`/uploads.json`).
- **Emoji Reactions**:
  - Aligned reaction emojis strictly with the official default set enabled by the `discourse-reactions` plugin (`heart` ❤️, `+1` 👍, `laughing` 😆, `open_mouth` 😮, `clap` 👏, `confetti_ball` 🎊, `hugs` 🫂).
  - Added clean reaction removal / untoggling when tapping an active reaction chip, like icon, or reaction picker item.
- **Navigation & Loading Experience**:
  - Eliminated the brief flash of the previously visited topic when opening a new discussion.
- **Interactive Polls & Topic Voting**:
  - Added full native rendering and participation for Discourse Polls (options, live vote counts, percentages, visual progress bars, voter counts, and one-tap voting).
  - Added built-in support for Discourse Topic Voting.
