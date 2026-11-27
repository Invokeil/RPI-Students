# RPI Student App — Android (Kotlin + Jetpack Compose + Liquid Glass)

Rangpur Polytechnic Institute–র জন্য all-in-one student app — **Blueprint v2.0 FINAL** অনুযায়ী।
Backend: **Appwrite** (sgp.cloud.appwrite.io) · Build: **GitHub Actions** (প্রতি push-এ signed APK + Release) · Update: **OTA** (GitHub Releases)

## ⚡ সবচেয়ে দ্রুত শুরু

1. Android Studio (Koala/এর পরের) খোলো → **Open** → এই repo clone/open করো।
2. Sync হতে দাও (Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21)।
3. ফোন/emulator লাগাও → **Run** ▶

**CI build (APK হাতে ছোঁয়া ছাড়া):** এই repo-তে `main` এ push করলেই GitHub Actions অটোমেটিক signed release APK বানিয়ে **GitHub Release** তৈরি করে — Actions/Releases ট্যাব থেকে APK নামাও।

## ✏️ App name instantly বদলানো

`gradle.properties` ফাইল খোলো:

```properties
APP_NAME=RPI Student          # ← এটা বদলাও (যেমন: RPI GO)
ORG_NAME=Rangpur Polytechnic Institute
```

Sync/Rebuild করলে launcher label + in-app header + splash + About — **সব জায়গায় একসাথে** নতুন নাম বসে যাবে (single source of truth)। Push করলে নতুন release-ও নতুন নামে আসবে।

## 🔑 Backend — Appwrite

- Endpoint: `https://sgp.cloud.appwrite.io/v1`, Project: `6b091cf5002c71f31ffb` — কোডে (BuildConfig) বসানো আছে, বদলাতে চাইলে `local.properties`-এ `APPWRITE_ENDPOINT` / `APPWRITE_PROJECT_ID` দিলে override হবে।
- **API key app-এ নেই** — API key শুধু server side (Appwrite Console / admin panel) চলে। কখনো repo-তে commit করো না।
- **Email OTP** পাঠাতে Appwrite Console → Auth → SMTP-তে Resend SMTP credentials যোগ করতে হবে (owner-gated)। SMTP না থাকলে signup flow-র "OTP পাইনি? সরাসরি এগিয়ে যাও" fallback দিয়ে ঢোকা যায়।
- Database `rpi_main` + collections (`profiles`, `groups`, `hot_messages`, `notices`, `subjects`, `routines`, `results`, `exam_routines`, `bus_routes`, `mess_entries`, `mess_deposits`, `pomo_sessions`, `app_config`, `change_requests`, `audit_log`) — setup script দিয়ে তৈরি (দেখো repo-র `backend/appwrite-setup.py`)।

## 📱 OTA update — কীভাবে কাজ করে

1. `main`-এ push → Actions → signed APK (`versionCode = run number`) → GitHub Release `v1.0.N`
2. App চালু হলে নতুন release check করে (Home-এ banner)
3. Profile → Developer → "নতুন update চেক করো" → download → install prompt
4. Signing key এক থাকলে সব আপডেট ক্লিন ইনস্টল হবে — **keystore হারালে পুরনো ইউজারদের update দেওয়া যাবে না!**

## 🔐 Signing (owner-gated)

- Keystore GitHub **Actions secrets**-এ এনক্রিপ্টেড আছে: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`।
- Keystore + password-এর backup ফাইল সাবধানে রাখো (সাইনিং কী পুনরুদ্ধার করা যায় না)। Secrets না থাকলে CI debug keystore দিয়ে sign করবে।

## 📱 কী কী আছে এখন (v1)

| Module | Screens | Status |
|---|---|---|
| Auth | Splash, Login, Signup, Academic form (name/roll/reg/session/tech/sem/shift/section/bus/mess), Email OTP + fallback, Pending | **Appwrite-এ live** |
| Home | Dashboard, OTA banner, quick actions, Daily Discipline, notices | Done |
| Academic | Hub, Routine, Results, GPA calc (Reg-2022), CGPA calc (weights 5/5/10/10/20/20/20/10), Subject Credit browser, Exam routine + focus banner | UI + calculation done, seed data বাকি |
| Chat | Group list (৪টা non-leaveable), Chat room — **Appwrite hot_messages live** (poll), Media Share flag | Live |
| Mess | Dashboard (rate/meals/bill), Meal on/off, Bazar ledger, Deposits, Monthly bill | UI done |
| Productivity | Pomodoro (Theory 25/5, Math 40/8, Lab 50/10, Exam 45/10), Fitness | Timer done, sensor v1.1 |
| Profile | Profile + badges, Settings, Change request, About, **Logout** | Done |
| **Developer** | ডেভেলপার ডিটেইলস, GitHub/FB/IG/Email redirect, **OTA update** | **Live** |
| Branding | logo.svg → adaptive icon (blue+white), logo-loader.svg → RpiLoader animation (সব লোডিং স্ক্রিনে) | Done |

## 🏗 Architecture

```
app/src/main/java/com/rpi/student/
├── MainActivity.kt / RpiApp.kt
├── core/
│   ├── navigation/  Routes.kt (সব page-এর list) + NavGraph.kt
│   ├── theme/       Liquid Glass theme, GlassSurface, tokens (API 31+ full, 26–30 flat)
│   └── ui/          MainShell (glass bottom tabs), SubScaffold, RpiLoader, common
├── data/
│   ├── local/       Room: entities/daos/db (offline-first cache)
│   └── remote/      Appwrite provider + AuthRepo + CloudRepo
├── update/          UpdateManager (GitHub Releases OTA)
└── features/        auth · home · academic · bus · chat · mess · productivity · profile · developer
```

## 🧯 Build-এ সমস্যা হলে

- Actions log দেখো — প্রথম build সময় নেয় (Gradle + deps ডাউনলোড)।
- Kotlin/Compose mismatch হলে: Kotlin 2.0.21 + compose plugin 2.0.21 মিলিয়ে রাখো।
- minSdk 26 (Android 8.0), targetSdk 35।

## 🔜 পরের slices (roadmap §20)

1. Seed importer (btebresultszone booklists → Appwrite subjects)
2. Appwrite Realtime chat (poll → subscribe) + torrent-style segment sealing cron
3. Admin panel (web, React + glisse) — verify, badges, routine/notice upload
4. FCM push + Firebase glue
5. Play Store AAB (publish হলে OTA → Play in-app update)
