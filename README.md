# MGM Lost & Found - Android App

A comprehensive Android application for MGM campus students to report and find lost items.

## Features

### ✅ Authentication
- Google Sign-In integration
- Student registration with validation
- Demo login for testing
- Campus-only access (domain/IP restricted)

### ✅ Lost & Found Reporting
- Report lost items with photos
- Report found items with location
- Item categories (Phone, ID Card, Laptop, Keys, Wallet, Bag, Other)
- Optional serial number and reward
- Unique ID and QR code generation for each item

### ✅ Real-Time Notifications
- Push notifications via Firebase Cloud Messaging
- Different sound alerts for "Lost" and "Found" items
- Matching item alerts
- Claim request notifications

### ✅ Smart Matching System
- Automatic matching based on:
  - Category match
  - Description similarity
  - Location proximity
- Real-time matching alerts

### ✅ Claim & Contact System
- Request to claim found items
- Finder approval system
- Secure contact sharing (only after approval)

### ✅ Admin Dashboard
- View all reports
- Approve, close, or flag cases
- Broadcast alerts to all students
- Security dashboard access

### ✅ Additional Features
- Map view of lost/found items (coming soon)
- QR code scanning
- Profile management
- My Reports section
- Share functionality

## Setup Instructions

### Prerequisites
- Android Studio (latest version)
- JDK 8 or higher
- Firebase account
- Google Sign-In credentials

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hdck
   ```

2. **Set up Firebase**
   - Create a Firebase project at https://console.firebase.google.com
   - Add Android app with package name: `com.mgm.lostfound`
   - Download `google-services.json` and place it in `app/` directory
   - Enable Authentication (Google Sign-In)
   - Enable Firestore Database
   - Enable Firebase Storage
   - Enable Cloud Messaging

3. **Configure Google Sign-In**
   - In Firebase Console, go to Authentication > Sign-in method
   - Enable Google Sign-In
   - Add your SHA-1 certificate fingerprint
   - Update `default_web_client_id` in `app/src/main/res/values/google-services.xml`

4. **Update Firebase Configuration**
   - Replace placeholder values in `app/google-services.json` with your actual Firebase project details

5. **Build and Run**
   ```bash
   ./gradlew build
   ```
   Or open in Android Studio and click "Run"

## Project Structure

```
app/
├── src/main/
│   ├── java/com/mgm/lostfound/
│   │   ├── data/
│   │   │   ├── model/          # Data models
│   │   │   └── repository/     # Data repositories
│   │   ├── ui/
│   │   │   ├── auth/           # Authentication screens
│   │   │   ├── main/           # Main activity
│   │   │   ├── fragments/      # Main fragments
│   │   │   ├── item/           # Item reporting & details
│   │   │   ├── admin/          # Admin dashboard
│   │   │   └── adapters/       # RecyclerView adapters
│   │   └── services/           # Background services
│   └── res/                    # Resources (layouts, strings, etc.)
```

## Dependencies

- **Firebase**: Authentication, Firestore, Storage, Cloud Messaging
- **Google Services**: Sign-In, Maps, Location
- **Material Components**: UI components
- **Glide**: Image loading
- **ZXing**: QR code generation and scanning
- **CameraX**: Camera functionality

## Permissions

- Internet
- Camera
- Location (Fine & Coarse)
- Storage (for images)
- Notifications

## Building APK

```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## Notes

- The app requires Firebase backend setup
- Google Sign-In requires proper SHA-1 configuration
- Location services need to be enabled on device
- Notifications require Firebase Cloud Messaging setup

## License

This project is for MGM campus use only.

