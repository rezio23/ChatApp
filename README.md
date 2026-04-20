# ChatApp — Kotlin + Firebase Android App

A real-time 1-on-1 chat messenger built with Kotlin and Firebase.

---

## Features
- Email/Password Authentication (Firebase Auth)
- Real-time 1-on-1 Messaging (Firestore)
- Chat list with last message preview & timestamp
- Read receipts (single tick = sent, blue double tick = read)
- User search to start new conversations
- Clean light theme (WhatsApp-inspired)

---

## Project Structure

```
app/src/main/java/com/chatapp/
├── auth/
│   ├── LoginActivity.kt        # Sign in screen
│   └── RegisterActivity.kt     # Sign up screen
├── chat/
│   ├── ChatActivity.kt         # 1-on-1 chat screen
│   └── ChatAdapter.kt          # Message bubbles adapter
├── users/
│   ├── UsersActivity.kt        # Find users to chat with
│   └── UsersAdapter.kt
├── model/
│   ├── User.kt
│   ├── Message.kt
│   └── ChatPreview.kt
├── utils/
│   ├── FirebaseUtils.kt        # Firebase singletons & helpers
│   └── TimeUtils.kt            # Timestamp formatting
├── ChatListAdapter.kt          # Home screen chat list
└── MainActivity.kt             # Home screen
```

---

## Setup Instructions

### Step 1: Create a Firebase Project

1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project** → name it "ChatApp"
3. Disable Google Analytics (optional) → **Create project**

### Step 2: Register Your Android App

1. In Firebase Console → **Project Overview** → click the Android icon
2. Enter package name: `com.chatapp`
3. Click **Register app**
4. **Download `google-services.json`**
5. Replace the placeholder `app/google-services.json` with the downloaded file

### Step 3: Enable Firebase Services

In Firebase Console:

**Authentication:**
- Go to **Authentication** → **Sign-in method**
- Enable **Email/Password**

**Firestore Database:**
- Go to **Firestore Database** → **Create database**
- Start in **test mode** (for development)
- Choose a region → **Done**

### Step 4: Set Firestore Security Rules (for production)

In Firestore → **Rules** tab, paste:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read all users, only write their own
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // Messages: only participants can read/write
    match /messages/{chatId}/chats/{messageId} {
      allow read, write: if request.auth != null &&
        (chatId.matches(request.auth.uid + "_.*") ||
         chatId.matches(".*_" + request.auth.uid));
    }

    // Chat previews: users can only access their own
    match /chats/{userId}/userChats/{chatId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Step 5: Open in Android Studio

1. Open **Android Studio**
2. **File → Open** → select the `ChatApp/` folder
3. Wait for Gradle sync to complete
4. Connect an Android device or start an emulator (API 24+)
5. Click **Run ▶**

---

## Firestore Data Structure

```
/users/{uid}
  - uid: String
  - name: String
  - email: String
  - profileImageUrl: String
  - status: String
  - lastSeen: Long (timestamp)

/messages/{chatRoomId}/chats/{messageId}
  - messageId: String
  - senderId: String
  - receiverId: String
  - message: String
  - timestamp: Long
  - isRead: Boolean
  - type: String ("text")

/chats/{userId}/userChats/{otherUserId}
  - userId: String
  - lastMessage: String
  - lastMessageTime: Long
```

> `chatRoomId` is deterministic: smaller UID + "_" + larger UID, so both users share the same chat room.

---

## Dependencies Used

| Library | Purpose |
|---|---|
| Firebase Auth | Email/password authentication |
| Firebase Firestore | Real-time database |
| Firebase Storage | File storage (ready to use) |
| Glide 4.x | Image loading & caching |
| CircleImageView | Circular avatar images |
| Material Components | UI components |
| Kotlin Coroutines | Async operations |

---

## Screenshots Flow

```
LoginActivity → RegisterActivity
     ↓
MainActivity (Chat List)
     ↓
UsersActivity (Find users) → ChatActivity (1-on-1 chat)
```

---

## Troubleshooting

- **Build fails on `google-services.json`**: Make sure you replaced the placeholder with your real file from Firebase Console.
- **Messages not appearing**: Check Firestore rules and ensure your app is connected to the internet.
- **Auth error**: Ensure Email/Password is enabled in Firebase Console → Authentication.

---

## Built By
Aditya Chauhan & Ishant Phull — MCA (AI/ML), Session 2023-2025
