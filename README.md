# PharmTrack 💊
> **Offline-First Clinic & Dispensary Medicine Inventory Manager**  
> *Android Application built with Kotlin, Jetpack libraries, MVVM + Clean Architecture, and Room (SQLite).*

---

## 📋 Problem & Solution

**PharmTrack** addresses three core challenges faced by small clinics and dispensaries:
1. **Stock Blindness**: Provides real-time stock dashboards and reorder levels to prevent running out of critical medicines.
2. **Expiry Losses**: Alerts users to expiring medicines (critical, warning, and already expired tabs) to reduce financial waste.
3. **Manual Record Errors**: Digitizes catalog management and logs every stock transaction (Stock IN/OUT) for complete accountability.

---

## 🏛️ Architecture & Clean Design

This application is built according to **Clean Architecture** and **MVVM** design patterns. The layers are decoupled, highly testable, and scale seamlessly:

```
[Presentation Layer (UI/Fragment/Adapter/ViewModel)] 
                  ↓
       [Domain Layer (Use Cases/Models)] 
                  ↓
[Data Layer (Room Entity/DAO/Repository Implementation)]
```

### Key Pillars:
* **Reactive UI**: Exposes lifecycle-aware states using Kotlin Coroutines `StateFlow` and combining multiple database flows.
* **Atomic Operations**: All Stock IN/Stock OUT movements insert transaction history records and update stock quantities atomically under a single database transaction (`database.withTransaction {}`).
* **Dependency Injection**: Fully configured Hilt module injections.
* **ProGuard Security**: Integrated rules to preserve Hilt entry points, SafeArgs parameters, and Room database entities under release builds.

---

## ✨ Features & Screens

### 1. Dashboard (Home)
- Unified overview showing count summaries: Total Catalog, Low Stock warnings, Expiring Soon limits, and Total Inventory Value in PKR.
- **Urgent Alerts Widget**: Dynamically highlights the top 5 critical medicines needing immediate reordering or rotation.
- **Recent Activity Ledger**: Records and logs the last 5 transactions instantly.

### 2. Medicine Catalog
- Interactive SearchView filter chips (*All*, *Low Stock*, *Expiring Soon*, *Expired*).
- Swipe-to-delete cards with instant Undo snackbar alerts.
- FAB shortcuts to register new items.

### 3. Detailed View & Stock Movements
- Dynamic progress bars visualising stock counts relative to threshold limits.
- Instant computations of profit margins.
- **Stock IN & Stock OUT Dialogs**: Auto-focused Bottom Sheets providing live calculations of new inventory levels dynamically as you type.

### 4. Alerts Tab Group
- TabLayout highlighting *Low Stock*, *Expiring Soon*, and *Expired* items with custom tags (e.g., "5d left", "Expired 2d ago") and warning card tints.

### 5. Audit History Log
- Transaction ledger grouped and filtered by *Today*, *This Week*, *This Month*, or *All time*.
- Bottom Navigation badge indicators updating in real-time.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: Kotlin
* **Architecture**: MVVM, Clean Architecture, Repository Pattern
* **Database**: Room (SQLite) reactively observed via Flow
* **DI Framework**: Dagger-Hilt
* **Navigation**: Navigation Component (SafeArgs)
* **Design Kit**: Material Design 3, CoordinatorLayout transitions, and Custom Vector Assets
* **System Integration**: Jetpack SplashScreen API (Android 12+)
* **Engine**: Gradle 9.4 + Kotlin Symbol Processing (KSP)

---

## 🚀 Building & Launching

Compile the debug APK cleanly using Gradle:

```powershell
# Clean and compile debug build
.\gradlew.bat clean assembleDebug
```

---

## 🔒 ProGuard Configuration
The project is packaged with optimized rules in `app/proguard-rules.pro` to keep:
- Room Entities & DAO queries intact.
- Hilt generated entry-points and qualifiers.
- Navigation arguments parameters.
