# LinkStack

<p align="center">
  <img src=".github/assets/app_icon.png" alt="LinkStack Icon" width="120" height="120">
</p>

LinkStack is a modern Android application for efficiently managing and organizing your links. Built with Clean Architecture and modern Android development practices, it offers a robust solution for saving, categorizing, and accessing your important links.

## Project Status

- 🚀 Active Development
- 📱 Android 12+ Support
- 🎨 Material Design 3 Implementation
- 🔒 Security Updates
- ♿ Accessibility Improvements

## Design & User Experience

The app follows Material Design 3 principles with focus on:
- Dynamic Color Theming
- Adaptive Layouts
- Consistent Typography
- Motion and Animation
- Accessibility Features
  - TalkBack Support
  - Content Descriptions
  - High Contrast Themes
  - Customizable Text Size

## Performance & Security

### Performance Optimizations
- Efficient database queries with Room
- Background processing with WorkManager
- Memory management with Kotlin Flows
- Image loading optimization
- View recycling in lists

### Security Features
- Encrypted data storage
- Secure GitHub authentication
- API key protection
- Safe network operations
- Input validation and sanitization

## Features

- **Link Management**: Save and organize links with custom tags
- **Smart Search**: Quickly find links using advanced search capabilities
- **GitHub Sync**: Backup and sync your links with GitHub
- **HackerNews Integration**: Share and interact with HackerNews community
- **Reminder System**: Set reminders for saved links
- **Theme Customization**: Personalize your app experience
- **Offline Support**: Access your links without internet connection
- **Tag Organization**: Categorize links with custom tags for better organization

## Screens

- **Home**: Main screen displaying all saved links with filtering options
- **Search**: Advanced search functionality with tag-based and text-based search
- **Add/Edit Link**: Form to add new links or edit existing ones with tag management
- **Settings**: App configuration including theme preferences and GitHub sync settings
- **WebView**: Built-in browser for viewing saved links

## Link Types

The app supports various link types with specialized handling:
- Articles
- Videos
- Social Media Posts
- Documentation
- GitHub Repositories
- General Links

## Reminder System

Linkstack includes a sophisticated reminder system that:
- Allows setting custom reminders for saved links
- Supports different reminder frequencies
- Uses WorkManager for reliable background processing
- Sends notifications to revisit important links
- Manages reminder states and history

## Testing

The project includes comprehensive testing:
- Unit Tests: Testing business logic and repositories
- Integration Tests: Testing database and network operations
- UI Tests: Testing Compose UI components
- Mock Repositories: For testing in isolation

## Architecture

The app follows Clean Architecture principles with a clear separation of concerns:

```
com.hp77.Linkstack/
├── data/                 # Data Layer
│   ├── local/           # Room Database
│   ├── remote/          # Network Services
│   ├── repository/      # Repository Implementations
│   ├── mapper/          # Data Mappers
│   └── preferences/     # DataStore Preferences
├── domain/              # Domain Layer
│   ├── model/           # Domain Models
│   ├── repository/      # Repository Interfaces
│   └── usecase/         # Business Logic
└── presentation/        # UI Layer
    ├── components/      # Reusable UI Components
    ├── home/           # Home Screen
    ├── search/         # Search Screen
    ├── settings/       # Settings Screen
    └── webview/        # WebView Screen
```

## Development Practices

- **Clean Architecture**: Strict separation of concerns with data, domain, and presentation layers
- **SOLID Principles**: Following SOLID principles for maintainable and scalable code
- **Repository Pattern**: Abstract data sources behind repository interfaces
- **Use Cases**: Single responsibility principle applied to business logic
- **Dependency Injection**: Using Hilt for clean dependency management
- **State Management**: Using sealed classes for UI states and events
- **Error Handling**: Comprehensive error handling with custom Logger utility
- **Code Style**: Following Kotlin coding conventions and best practices

## Database Schema

The app uses Room database with the following main entities:
- **LinkEntity**: Stores link information (URL, title, type, etc.)
- **TagEntity**: Stores tag information
- **LinkTagCrossRef**: Many-to-many relationship between links and tags
- **Migrations**: Versioned database migrations for schema updates

## HackerNews Integration

The app integrates with HackerNews API to:
- Share links to HackerNews community
- View HackerNews discussions
- Track share status and engagement
- Handle HackerNews authentication

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- **Architecture Pattern**: MVVM + Clean Architecture
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) - Simplified DI for Android
- **Local Storage**: [Room](https://developer.android.com/training/data-storage/room) - SQLite abstraction layer
- **Networking**: [Retrofit](https://square.github.io/retrofit/) - Type-safe HTTP client
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/)
- **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Data storage solution

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 21 or later

### Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/Linkstack.git
```

2. Open the project in Android Studio

3. Add your GitHub API credentials in `local.properties`:
```properties
github.client_id=your_client_id
github.client_secret=your_client_secret
```

4. Build and run the project

## Building the App

### Debug Build
```bash
# Generate debug APK
./gradlew assembleDebug

# The APK will be available at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
1. Create a keystore file (if you don't have one):
```bash
keytool -genkey -v -keystore linkstash.keystore -alias linkstash -keyalg RSA -keysize 2048 -validity 10000
```

2. Add keystore information in `local.properties`:
```properties
keystore.path=linkstash.keystore
keystore.password=your_keystore_password
keystore.alias=linkstash
keystore.alias_password=your_key_password
```

3. Generate release APK:
```bash
# Generate signed release APK
./gradlew assembleRelease

# The APK will be available at:
# app/build/outputs/apk/release/app-release.apk
```

### Android App Bundle
For Play Store distribution:
```bash
# Generate Android App Bundle (AAB)
./gradlew bundleRelease

# The AAB will be available at:
# app/build/outputs/bundle/release/app-release.aab
```

### Build Variants
- Debug: Development version with debugging enabled
- Release: Optimized version with ProGuard rules applied
- Both variants support different product flavors if configured

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## GitHub Sync

Linkstack provides seamless GitHub integration:
- Backup links to a GitHub repository
- Sync across multiple devices
- Version control for your link collection
- Secure authentication using GitHub OAuth
- Automatic conflict resolution

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Learning Resources & Best Practices

### Architecture & Design Patterns
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) - Understanding the architectural principles
- [Guide to App Architecture](https://developer.android.com/topic/architecture) - Android's official architecture guide
- [Repository Pattern](https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern) - Data layer abstraction
- [Use Case Pattern](https://proandroiddev.com/why-you-need-use-cases-interactors-142e8a6fe576) - Business logic organization

### Modern Android Development
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Kotlin Flows](https://developer.android.com/kotlin/flow) - Reactive programming
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Background processing
- [Room Database](https://developer.android.com/training/data-storage/room) - Local persistence

### Testing
- [Android Testing](https://developer.android.com/training/testing) - Comprehensive testing guide
- [Testing Compose](https://developer.android.com/jetpack/compose/testing) - UI testing
- [Repository Testing](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-survey#0) - Data layer testing

### Performance
- [App Performance](https://developer.android.com/topic/performance) - Performance best practices
- [Memory Management](https://developer.android.com/topic/performance/memory) - Memory optimization
- [Database Performance](https://developer.android.com/training/data-storage/room/testing-db) - Room optimization

### Security
- [Security Best Practices](https://developer.android.com/topic/security/best-practices) - Android security
- [OAuth Implementation](https://developer.android.com/training/id-auth/authenticate) - Authentication
- [Encryption](https://developer.android.com/topic/security/data) - Data security
