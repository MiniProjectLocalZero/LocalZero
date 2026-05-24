# 🌍 LocalZero

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)

> A modern, local community coordination and sustainability-tracking web application. LocalZero empowers residents and representatives to coordinate local green initiatives, post updates, exchange private messages, and visualize carbon-saving impact on both a personal and community-wide level.

---

## 📋 Table of Contents
1. [Overview & Core Features](#-overview--core-features)
2. [Software Architecture & Design Patterns](#-software-architecture--design-patterns)
3. [Technology Stack](#-technology-stack)
4. [Getting Started & Local Development](#-getting-started--local-development)
5. [Project Structure](#-project-structure)
6. [Testing Suite](#-testing-suite)

---

## 🌟 Overview & Core Features

LocalZero is built as part of the **Tillämpad Programvaruarkitektur** (Applied Software Architecture) course. The project aims to reduce local carbon footprints by providing communities with a unified digital dashboard for green activities.

### 👤 Role-Based Portals
*   **Residents (Standard Users):** Register to a specific community, log sustainability actions (like ride-sharing, gardening, or food swapping), create public or community-specific initiatives, write posts, leave comments, and engage in direct peer messaging.
*   **Representatives (Administrators):** Enjoy all Resident capabilities, plus the ability to create **Official Initiatives** and view a dedicated dashboard showing aggregated community-wide carbon savings.

### 🍃 Sustainability Impact Tracking
*   Users log green actions across categorized areas:
    *   `RIDE_SHARING` (5.0kg CO₂ savings)
    *   `TOOL_SHARING` (2.5kg CO₂ savings)
    *   `FOOD_SWAP` (1.2kg CO₂ savings)
    *   `GARDENING` (0.8kg CO₂ savings)
    *   `RECYCLING` (0.5kg CO₂ savings)
*   Dynamic calculation of personal impact alongside community totals.

### 💬 Decoupled Social Engine
*   **Messaging System:** Send private text messages, delete messages, or broadcast messages to all members of a community.
*   **Interactive Initiatives:** Create initiatives, post updates (with image attachments), like posts, and comment on updates.
*   **Intelligent Notification Hub:** Auto-generated, rule-based notifications for new messages, initiatives, posts, comments, and likes.

---

## 🏛 Software Architecture & Design Patterns

The backend follows **Clean Domain-Driven Design (DDD)** principles and utilizes standard Spring Boot conventions. To ensure low coupling, high cohesion, and strict compliance with the **SOLID** principles, several **Gang of Four (GoF)** design patterns are implemented:

### 1. Creational Patterns

#### 🏗 Factory Method & Registry Pattern (`se.mau.localzero.initiative.factory`)
Used to encapsulate the complex logic of creating initiatives and posts under a dynamic, scalable registry structure.
*   **Registry (`InitiativeFactoryRegistry`):** Contains a registry of all active creators. It automatically streams, filters, and selects the appropriate creator class based on the metadata in the `InitiativeDto`.
*   **Creators (`InitiativeCreator`):** Standard interface implemented by specific creation strategies like `PublicInitiativeCreator` and `CommunityInitiativeCreator`.
*   **Entity Factory (`PostFactory`):** Manages file byte streaming and content extraction for social posts with image attachments.

### 2. Behavioral Patterns

#### 🔗 Chain of Responsibility (`se.mau.localzero.messaging.validator` & `se.mau.localzero.auth.handler`)
Used to perform multi-stage validation workflows where each step is decoupled from the next, allowing validations to be reordered or bypassed dynamically.
*   **Messaging Validation:** `ValidationChain` dynamically links `ContentValidator` ➔ `UserAccessValidator` ➔ `CommunityValidator` to validate message and notification rules before persistence.
*   **Registration Validation:** `AuthService` dynamically links `ValidationHandler` ➔ `UserExistHandler` to ensure all structural constraints and uniqueness requirements are verified sequentially before a new user is saved.

#### 🎛 Mediator Pattern (`se.mau.localzero.messaging.mediator` & `se.mau.localzero.profile.mediator`)
Decouples complex object intercommunications. Instead of controllers and services communicating in a spiderweb-like structure, they send requests to a central mediator.
*   **`CommunityMessagingMediator`:** Orchestrates the flow of validating a message, invoking its command execution, checking cross-community rules, and spawning notifications.
*   **`NotificationMediator`:** Manages creation, verification, and database storage for notifications linked to messages, initiatives, comments, and likes.
*   **`ProfileMediator`:** Decouples user profiles, notifications, and settings modules from main controller flows.

#### 📜 Command Pattern (`se.mau.localzero.messaging.command`)
Encapsulates operations as self-contained objects, allowing for logging, queuing, and full transactional **undo capabilities**.
*   **Invoker (`MessageCommandInvoker`):** Maintains an internal stack of executed commands. Exposes an `undo()` method which pops the last successful command and triggers its specific undo operation.
*   **Commands (`MessageCommand`):** Implemented by `SendMessageCommand`, `BroadcastMessageCommand`, and `DeleteMessageCommand`. 
*   **Undo Execution:** For example, undoing `SendMessageCommand` marks the message as deleted in the database; undoing `DeleteMessageCommand` restores a deep copy of the message.

#### 📈 Strategy Pattern (`se.mau.localzero.sustainability.strategy`)
Defines a family of algorithms, encapsulates each one, and makes them interchangeable at runtime based on context or user credentials.
*   **`SustainabilityAnalyticsStrategy`:** An interface for calculating CO₂ metrics.
*   **`PersonalAnalyticsStrategy`:** Strategy for computing personal-only savings based on a user's logged actions.
*   **`CommunityAnalyticsStrategy`:** Strategy for computing aggregated carbon savings across an entire community (restricted to Representatives).
*   **Interchangeability:** Instantiated and called dynamically via the `SustainabilityActionService`.

#### 👁 Observer Pattern (`se.mau.localzero.auth.observer`)
Establishes a one-to-many relationship, letting multiple objects listen to state changes asynchronously.
*   **Subject (`SessionSubject`):** Implements `Observable` and maintains a list of session observers. It is triggered during logins and logouts.
*   **Observers (`SessionObserver`):** Implemented by `FileLoggerObserver`, which automatically appends timestamped success messages to `SessionLogger.txt` whenever a user authenticates or signs out.
*   **Integration:** Injected into `CustomLoginSuccessHandler` and `CustomLogoutSuccessHandler` within Spring Security.

---

## 🛠 Technology Stack

*   **Runtime:** Java 21 (Temurin JVM)
*   **Framework:** Spring Boot 4.0.5
    *   **Spring MVC:** For handling REST routes and MVC views.
    *   **Spring Data JPA:** Hibernate-based ORM mapping for PostgreSQL.
    *   **Spring Security:** Comprehensive authentication, CSRF protection, and role-based path authorization.
*   **Database:** PostgreSQL 16 (for dev/production), H2 Database (in-memory for JUnit testing).
*   **Frontend:** HTML5, CSS3, Thymeleaf 3 (with Spring Security 6 extras integration for dynamic navigation rendering).
*   **Configuration:** `.env` via `dotenv-java` to separate secrets from code.
*   **Containerization:** Multi-stage `Dockerfile` and developer-focused `docker-compose`.

---

## 🚀 Getting Started & Local Development

This guide outlines exactly what to do **before** starting the application and provides comprehensive startup options for both the shared PostgreSQL server (`postgres.mau.se`) and local database instances.

### 📋 Prerequisites

Depending on your preferred execution method, ensure the following tools are installed:

*   **For Docker execution:** Docker and Docker Compose (highly recommended).
*   **For Native Host execution:** Java 21 JDK (e.g., Eclipse Temurin) and Maven 3.x (or use the included wrapper `./mvnw`).
*   *(Optional)* A modern Java IDE (e.g., **IntelliJ IDEA** or VS Code with Java Extension Pack).

---

### ⚙️ Step 1: Pre-execution Configuration (`.env`)

Before launching the application, you **must** configure your environment variables. The Spring Boot application reads these dynamically using `dotenv-java`.

1. Create a file named `.env` in the project root directory (next to `pom.xml` and `Dockerfile`).
2. Populate the `.env` file according to the database option you are using:

#### Option A: Shared Course PostgreSQL Server (Recommended for Shared Data)
Use this option to connect directly to the shared PostgreSQL server hosted at Malmö University. This ensures you and your team are interacting with the exact same database.

```env
DB_URL=jdbc:postgresql://postgres.mau.se:55432/localzero_grupp1
DB_USERNAME=aq0574
DB_PASSWORD=2bf2wo06
```

#### Option B: Local PostgreSQL Database (Self-contained Development)
Use this option if you want to run a completely isolated database on your own machine.

```env
DB_URL=jdbc:postgresql://localhost:5432/localzero
DB_USERNAME=localzero
DB_PASSWORD=localzero
```

> [!NOTE]
> If you are using **Option B**, you must either have a PostgreSQL instance running locally on port `5432` or start the database service via Docker (see [Running with Docker](#2-running-with-docker-local-database) below).

---

### 💻 Step 2: Choose Your Startup Method

Once your `.env` file is ready, choose one of the following methods to start the application:

### 1. Running Natively (Host System)
Ideal for quick source debugging, step-by-step inspections, or IDE execution.

#### A. Command Line (Maven Wrapper)
Ensure your `.env` is populated, then compile and start the server:

1. Clean previous build artifacts and run the test suite:
   ```bash
   ./mvnw clean test
   ```
2. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

#### B. Using an IDE (e.g., IntelliJ IDEA)
1. Import the project as a **Maven** project.
2. Ensure the JDK is set to **Java 21** in your project settings.
3. Make sure the `.env` file is in the root of the project.
4. Locate `se.mau.localzero.LocalzeroApplication.java` inside `src/main/java`.
5. Right-click and select **Run 'LocalzeroApplication'** or debug.

---

### 2. Running with Docker (Local Database)
Ideal for a completely containerized experience without needing Java 21 or PostgreSQL installed on your host machine.

#### A. Development Mode (With Live Code Mounting & DevTools Reloading)
This runs the application within a lightweight container, mounts your host source code, and uses Spring DevTools. Any change you make in `src/` will automatically trigger a recompilation and container hot-restart.

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

#### B. Production Mode (Optimized Build)
This performs an optimized multi-stage build directly inside the container and boots a secure environment.

```bash
docker-compose up --build
```

---

### 🧭 Step 3: Accessing and Verifying the App

1. Open your browser and navigate to **[http://localhost:8080](http://localhost:8080)**.
2. **Registration / Login:** Register a new user, select your local community, and sign in.
3. **Log a Green Action:** Navigate to the "Log Action" page, choose a category (e.g. Ride Sharing), and submit it to see your carbon savings calculated on the homepage dashboard.
4. **Verify Session Logging (Observer Pattern check):** 
   You can verify that the Observer pattern is recording session logs by opening the `src/main/java/se/mau/localzero/auth/observer/SessionLogger.txt` file (or looking at `SessionLogger.txt` in the root). You should see timestamped entries like:
   ```text
   User has been logged in successfully: aq0574 - Date: 2026-05-24 Time: 10:14:52
   user has been logged out successfully: aq0574 - Date: 2026-05-24 Time: 10:15:30
   ```

---

## 📂 Project Structure

```
LocalZero/
├── .env                         # Local environment variables
├── Dockerfile                   # Multi-stage production build configuration
├── docker-compose.yml           # Base service configuration (App & Postgres)
├── docker-compose.dev.yml       # Development overrides for live code-mounting
├── pom.xml                      # Maven build descriptor with Spring Boot 4.0.5 dependencies
└── src/
    ├── main/
    │   ├── java/se/mau/localzero/
    │   │   ├── LocalzeroApplication.java   # Application Entrypoint (Dotenv Loader)
    │   │   ├── CommunityRepository.java    # Shared Global Community Repository
    │   │   ├── auth/                       # Spring Security & Registration Flow
    │   │   │   ├── config/                 # SecurityConfig & Route Rules
    │   │   │   ├── handler/                # Login, Logout Success & Registration COR Chains
    │   │   │   ├── observer/               # Session Observers (File Session Logger)
    │   │   │   └── service/                # AuthService & CustomUserDetailsService
    │   │   ├── controller/                 # Global UI Page Controllers
    │   │   ├── domain/                     # JPA Hibernate Entities (User, Post, Message, etc.)
    │   │   ├── initiative/                 # Initiative Creation & Social Post Core
    │   │   │   ├── factory/                # Initiative & Post Factory Method / Registry Pattern
    │   │   │   └── service/                # Comment, Like, and Post Services
    │   │   ├── messaging/                  # Social Chat & Messaging Core
    │   │   │   ├── command/                # Send, Broadcast & Delete Commands (With Undo/History)
    │   │   │   ├── mediator/               # CommunityMessaging & Notification Mediators
    │   │   │   └── validator/              # Messaging Validation Chain of Responsibility
    │   │   ├── profile/                    # User Profile Settings & Settings Mediator
    │   │   └── sustainability/             # Green Action Logging & Math calculations
    │   │       └── strategy/               # Personal vs. Community Savings Strategy Pattern
    │   └── resources/
    │       ├── application.properties      # Core properties & HikariCP limits
    │       ├── static/                     # Global CSS stylesheets and images
    │       └── templates/                  # Thymeleaf view templates & fragments
    └── test/
        └── java/se/mau/localzero/          # Robust JUnit and integration test suite
```

---

## 🧪 Testing Suite

LocalZero includes comprehensive unit and integration testing.
*   **Integration Tests:** Validate database interactions (via H2) and full Spring context loading.
*   **Unit Tests:** Focus on validation chains, factory registration selectors, and strategy math.

To run the test suite locally (requires JDK 21 installed on host):
```bash
./mvnw test
```