<div align="center">

# GTB Employee Management System

**A desktop employee management platform for a water construction company**

Built with JavaFX, Spring Boot, and PostgreSQL

</div>

---

## Overview

GTB Employee Management System is a full-featured HR and operations platform designed for a construction company managing water infrastructure projects. It replaces manual, spreadsheet-driven staff administration with a unified desktop application covering the complete employee lifecycle — from directory and department management to payroll, attendance, projects, and leave.

The application is split into two experiences from a single codebase:

- **Admin Portal** — full administrative control over staff, departments, payroll, and projects
- **Employee Portal** — a self-service view scoped to each employee's own records, permissions, and responsibilities

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Features

### Administration
- **Staff Directory** — search and filter employees by name, role, or department; edit profiles, assign roles, manage system access permissions
- **Departments Management** — organize the org chart by department, assign department heads, view headcounts and role breakdowns
- **Former Staff Archive** — retained history of employees no longer active, including last role and department

### Payroll
- Generate monthly payroll runs across all active employees
- Edit allowances, deductions, and custom columns per employee
- Automatic pension and tax calculations
- Export payroll data to CSV
- Submit monthly reports and retain historical payroll records

### Projects
- Track projects by assigned department and project lead
- Employee-submitted progress reports, reviewable by admins
- Status tracking (planned, in progress, completed)

### Attendance
- Calendar-based daily check-in / check-out tracking
- Lunch break logging
- Department and employee filtering
- Excel export for reporting

### Leave Management
- Employee-submitted leave requests with reason and duration
- Admin approval / rejection workflow
- Automatic handling of permanent leave (offboarding)

### Access Control
- Role-based routing between Admin and Employee portals at login
- Granular, per-employee access flags for payroll, department, and project management
- Employee self-service portal scoped strictly to their own data

## Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX 21 (FXML) |
| Backend | Spring Boot 3.2 |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Reporting | Apache POI (Excel export) |
| Build | Maven |
| Language | Java 21 |

## Architecture

The application runs Spring Boot's IoC container inside a JavaFX application shell — Spring manages services, repositories, and dependency injection, while JavaFX's `FXMLLoader` is wired to Spring's `ApplicationContext` so that every FXML-driven controller is a fully-fledged Spring bean with `@Autowired` access to the service layer.

```
JavaFX View (FXML)  ->  Controller (@Component)  ->  Service  ->  Repository (Spring Data JPA)  ->  PostgreSQL
```

## Getting Started

### Prerequisites

- **JDK 21** — required. Newer JDKs are not currently supported (see [Troubleshooting](#troubleshooting))
- **PostgreSQL**, running locally or remotely accessible
- **Git**

> Maven does not need to be installed separately — this project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`).

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/birukti325/gtb-employee-management-system.git
   cd gtb-employee-management-system
   ```

2. **Create your local environment file**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` with your PostgreSQL credentials:
   ```env
   DB_USERNAME=your_postgres_username
   DB_PASSWORD=your_postgres_password
   ```

3. **Create the database**
   ```sql
   CREATE DATABASE gtb_employee_db;
   ```
   Confirm the host and port in `src/main/resources/application.properties` match your local PostgreSQL instance.

4. **Build**
   ```bash
   ./mvnw clean install
   ```

5. **Run**
   ```bash
   ./mvnw spring-boot:run
   ```
   Alternatively, run `Launcher.java` directly from your IDE.
   

## Project Structure

```
src/main/java/com/sms/gtbemployeemanagementsystem/
├── Entity/            # JPA entities
├── Repository/         # Spring Data JPA repositories
├── Service/              # Business logic layer
├── Security/               # Authentication & session management
├── controller/               # JavaFX FXML controllers
├── Launcher.java                # JavaFX application entry point
└── AppRunner.java                  # Spring Boot bootstrap

src/main/resources/
├── application.properties     # Application configuration
└── com/sms/gtbemployeemanagementsystem/    # FXML view definitions
```

## Troubleshooting

<details>
<summary><strong>Build fails with <code>ExceptionInInitializerError: TypeTag :: UNKNOWN</code></strong></summary>

This indicates the active JDK is newer than Lombok supports. Install [JDK 21 (Eclipse Temurin)](https://adoptium.net/temurin/releases/?version=21) and set it as your project SDK:
`File → Project Structure → Project → SDK`, and confirm Maven's runner JRE matches under
`Settings → Build, Execution, Deployment → Build Tools → Maven → Runner`.
</details>

<details>
<summary><strong>App fails with <code>Unable to determine Dialect without JDBC metadata</code></strong></summary>

This means the app could not establish a database connection. Check that:
- PostgreSQL is running
- The port in `application.properties` matches your PostgreSQL instance
- `.env` exists at the project root (same level as `pom.xml`), not inside `src/`
- Your credentials in `.env` are correct
</details>

<details>
<summary><strong><code>.env</code> values aren't being picked up</strong></summary>

Confirm `.env` is located at the project root. Run a full clean build to clear any stale compiled configuration:
```bash
./mvnw clean install
```
</details>

## License

This project is proprietary software developed for internal use by GTB. All rights reserved.
