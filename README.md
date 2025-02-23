# Martı Test Case Project

## APK Download
[Download APK File](https://github.com/MuratCay/marti_test_case/blob/development/assets/app-debug.apk)

## Demo Video

[Demo Video](https://github.com/MuratCay/marti_test_case/blob/development/assets/martı.video.mp4)

## Project Overview
This Android application is developed as a test case project for Martı. The app demonstrates modern Android development practices and follows clean architecture principles.

## Features
- Clean Architecture with MVVM pattern
- Kotlin Coroutines for asynchronous operations
- Dependency Injection using Dagger Hilt
- Material Design implementation
- Navigation Component for screen management
- ViewBinding for view interactions
- StateFlow for reactive state management

## Tech Stack
- **Language:** Kotlin
- **Minimum SDK:** [version]
- **Target SDK:** [version]

## Architecture
The project follows Clean Architecture principles and is organized into three main layers:
- **Presentation Layer:** Activities, Fragments, ViewModels
- **Domain Layer:** Use Cases, Domain Models
- **Data Layer:** Repositories, Data Sources, DTOs

## Libraries Used
- AndroidX Components
- Dagger Hilt for dependency injection
- Kotlin Coroutines & Flow
- Navigation Component
- Material Design Components
- [Other libraries used in the project]

## Getting Started
1. Clone the repository
```bash
git clone https://github.com/[your-username]/marti_test_case.git
```

2. Open the project in Android Studio

3. Build and run the application

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/[package]/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   ├── presentation/
│   │   │   ├── di/
│   │   │   └── utils/
│   │   └── res/
│   ├── test/
│   └── androidTest/
```

## Testing
The project includes both unit tests and instrumentation tests:
- Unit tests for ViewModels and Use Cases
- UI tests using Espresso
- Repository tests with fake implementations

## Contributing
This is a test case project, but suggestions and improvements are welcome. Please feel free to fork and submit pull requests.

## License
[Add your license information here] 