# WasteWiseTn

A modular JavaFX 21 desktop platform for smart waste management, citizen engagement, and valorizer/admin workflows.

## Table of Contents

1. [Overview](#overview)
2. [Core Features](#core-features)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Configuration](#configuration)
7. [Build and Run](#build-and-run)
8. [Module System Notes (JPMS)](#module-system-notes-jpms)
9. [Troubleshooting](#troubleshooting)
10. [Developers](#developers)

## Overview

WasteWiseTn is a Java 21 + JavaFX application that supports multiple user roles (Citizen, Valorizer, Admin, Organizer, Partner) through a role-aware shell UI. It centralizes waste declaration flows, dashboards, statistics, map-based views, event participation, face login, QR-based validation, and 2FA.

## Core Features

- Role-based app shell and navigation (`AppShellController`)
- Authentication flows:
  - Email/password login
  - Face login / face enrollment (OpenCV)
  - Two-factor verification (TOTP)
- Citizen area:
  - Waste declaration and history
  - Statistics and air quality views
  - News and settings
- Valorizer area:
  - Received waste management
  - Valorization workflow
  - QR validation
  - Statistics
- Admin area:
  - User management
  - Waste type and declaration management
  - Dashboard and advanced monitoring
- Event management module:
  - Events, participations, badges, and organizer views
- External integration utilities:
  - HTTP APIs (OkHttp / Java HTTP Client)
  - Email support
  - PDF/QR generation

## Tech Stack

- Java 21
- JavaFX 21.0.2 (`controls`, `fxml`, `web`)
- Maven (module-based build)
- MySQL Connector/J
- OpenCV (`org.openpnp:opencv`)
- TOTP (`dev.samstevens.totp`)
- ZXing (QR handling)
- iText7 + PDFBox
- OkHttp
- Gson / JSON
- Jakarta Mail / Activation

## Project Structure

```text
java-project/
  src/
    main/
      java/
        module-info.java
        org/example/
          Launcher.java
          Main.java
          controllers/
          services/
          entities/
          models/
      resources/
        org/example/views/
        org/example/styles/
        database.properties
        fxml/
  uploads/
  captures/
  pidev.sql
  pom.xml
```

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL Server
- Windows is currently the primary tested target (project contains JavaFX Windows classifier dependencies)

## Configuration

1. Database:
   - Create/import schema using `pidev.sql`.
   - Update connection settings in `src/main/resources/database.properties` as needed.

2. External keys/services:
   - Configure any API keys used by services (news, air quality, AI integrations, etc.) in your local config/code paths.

3. File output directories:
   - Ensure app has write access to `uploads/` and `captures/`.

## Build and Run

From project root:

```bash
mvn clean compile
mvn javafx:run
```

Create jar artifact:

```bash
mvn clean package
```

Main launcher configured in Maven:

- `org.example.Launcher`

## Module System Notes (JPMS)

This project is modular (`module org.example.wastewise`).
If you add a new dependency or FXML controller package, update `module-info.java` accordingly:

- Add `requires ...;` for new libraries.
- Add `opens <controller.package> to javafx.fxml;` for FXML controllers.

Without these updates, JavaFX can throw `IllegalAccessException` / `LoadException` during FXML loading.

## Troubleshooting

### 1) `javafx.fxml.LoadException` for controller access

Cause: package not opened to `javafx.fxml` in `module-info.java`.
Fix: add missing `opens ... to javafx.fxml;` entries for the controller package.

### 2) `package ... is not visible` in module mode

Cause: missing `requires ...;` for the dependency automatic module.
Fix: inspect jar module name (`jar --describe-module --file <jar>`) and add it in `module-info.java`.

### 3) Stylesheet/resource not found

- Verify CSS path exists under `src/main/resources`.
- Use classpath absolute paths in FXML when needed (for example: `/org/example/styles/style.css`).
- Rebuild project to refresh `target/classes`.

### 4) Runtime still shows old errors after fixes

Run a full clean build:

```bash
mvn clean
mvn compile
```

Also invalidate/rebuild from IntelliJ if needed.

## Developers

Developed by:

- Eya Ben Hassine
- Louay Houimli
- Khalil Sammoudy
- Islem Hadriche
- Mohammed Ghammam