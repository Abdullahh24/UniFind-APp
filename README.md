# UniFind: Campus Lost & Found Hub (Android Edition)

Welcome to **UniFind**, a modern, production-ready, university campus Lost & Found management system built natively using **Kotlin**, **Jetpack Compose**, and **Room Database**. 

UniFind features a signature Material 3 collegiate design (Blue + White + Soft Gray theme), supporting both Light and Dark modes. It relies on a local Room database engine to support out-of-the-box local testing, offline caching, persistent user profiles, simulated push notifications, and administrative moderation tools.

---

## 🚀 Key App Modules & Flows

1. **Splash Screen / Intro**: Animated campus logo with a session control check to automatically forward users depending on their session history.
2. **Student Login & Registration**: Modern input validations, secure password entry, and double instant-developer bypass buttons (**Student Alex** / **Dean Admin**) to allow testing as different roles instantly.
3. **Interactive Home Dashboard**: Real-time keywords searching, separate filter tags for item states (🔴 Lost / 🟢 Found), horizontal category pills (Electronics, Keys, Wallets, Books, Documents, Other), and pull-to-refresh.
4. **Report Item Form**: High-res Unsplash photo suggestion chips, coordinate-based GPS inputs, drop-down category menus, and contact card validations.
5. **Item Details Screen**: Large fluid cover banners, quick call/SMS triggers, anti-spam reporting forms, and single-click administrative resolution buttons.
6. **Student ID Center**: Interactive digital student card display, profile details editor, persistent dark mode toggle, and safe session logout.
7. **Simulated FCM Notification Feed**: Central inbox detailing real-time alerts whenever items are lost or discovered around campus.
8. **Chancellor Security Panel (Admins Only)**: Dedicated tab allowing actual administrative staff to review reported items, ban/deactivate offending student profiles, or delete spam posts.

---

## 🛠️ Architecture & Tech Stack

UniFind adheres strictly to the **MVVM (Model-View-ViewModel)** and **Repository Pattern** guidelines:

```
├── com/example/
│   ├── MainActivity.kt               # Application launcher entry point
│   ├── data/
│   │   ├── entity/
│   │   │   └── Entities.kt           # Room Database Entities (User, Post, Report, Notification)
│   │   ├── dao/
│   │   │   └── Daos.kt               # Local Queries (UserDao, PostDao, ReportDao, NotificationDao)
│   │   ├── database/
│   │   │   └── AppDatabase.kt        # Room Abstract Class & Singleton Thread-Safe instance
│   │   └── repository/
│   │       └── UniFindRepository.kt  # Unified Data Cache & Session management
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt              # Custom "Collegiate Blue" themed palette
│   │   │   ├── Theme.kt              # Light / Dark Material 3 schemes
│   │   │   └── Type.kt               # Display and Body texts typography
│   │   ├── screens/
│   │   │   └── UniFindScreens.kt     # Beautiful, functional Compose screens
│   │   └── viewmodel/
│   │       └── UniFindViewModel.kt   # View state machines, search debounces, and preferences
```

---

## 🔥 Firebase Integration & Security Rules

To transition from local Room storage/simulations to standard cloud services, integrate the official Firebase SDK. Below are the complete production-ready configurations:

### 1. Cloud Firestore Security Rules (`firestore.rules`)
Configure these rules in your Firebase Console to manage read/write permissions securely:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // User Profile Permissions
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid;
      allow destroy: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }
    
    // Posts / Lost & Found Listings
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && (resource.data.uid == request.auth.uid || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin");
      allow delete: if request.auth != null && (resource.data.uid == request.auth.uid || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin");
    }
    
    // Flagged/Spam Reports
    match /reports/{reportId} {
      allow read: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
      allow create: if request.auth != null;
      allow delete, update: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }
  }
}
```

### 2. Firebase Cloud Storage Rules (`storage.rules`)
Ensure pictures can only be uploaded and managed by authenticated students:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /posts/{postId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 5 * 1024 * 1024 && request.resource.contentType.matches('image/.*');
    }
    match /profiles/{uid}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid && request.resource.size < 2 * 1024 * 1024;
    }
  }
}
```

---

## 📦 Setting Up Live Firebase Dependencies

To activate production Firebase Sync, uncomment dynamic SDKs inside `build.gradle.kts`:

```kotlin
dependencies {
  // Firebase BOM
  implementation(platform(libs.firebase.bom))
  
  // Firebase SDK libraries
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.google.firebase:firebase-storage-ktx")
  implementation("com.google.firebase:firebase-messaging-ktx")
}
```

Then download your `google-services.json` from the Firebase Console, place it in the `/app` directory, and apply the plugins in your build config.

---

## 📲 APK Build Instructions

1. **Gradle Build Tools**: Standard Gradle is integrated. To compile a clean release-ready APK:
   ```bash
   gradle assembleRelease
   ```
2. **Output Location**: Output `.apk` files will be structured inside `/app/build/outputs/apk/release/app-release.apk`.
3. **Running Local Checks**: You can also easily trigger unit and Roborazzi tests by executing:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🔒 Environment Variable Checks

To store API credentials, use the secure **Secrets panel in Google AI Studio**.
- Ensure `.env` includes any necessary keys like `GEMINI_API_KEY`.
- Never post keys in plaintext inside of files. Read them dynamically through `BuildConfig` generated properties.
