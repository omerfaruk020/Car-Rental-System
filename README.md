# Car Rental System

A console-based car rental management system written in plain Java, with no external dependencies. Data is stored in CSV files and survives between runs.

The project was built to practise object-oriented design — inheritance, abstraction, polymorphism, encapsulation — and a layered architecture.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Demo Accounts](#demo-accounts)
- [Business Rules](#business-rules)
- [State Machines](#state-machines)
- [Data Files](#data-files)
- [Design Patterns Used](#design-patterns-used)
- [Known Limitations](#known-limitations)

---

## Features

### Administrator

| # | Action | Notes |
|---|--------|-------|
| 1 | List all vehicles | Whole fleet with current status |
| 2 | Add a vehicle | Rejects duplicate license plates |
| 3 | Update vehicle details | Brand, model, year, segment, daily price |
| 4 | Remove a vehicle | Only vehicles in `AVAILABLE` status |
| 5 | View all reservations | Every record in the system |
| 6 | Change vehicle status | Warns and asks for confirmation if an open reservation exists |
| 7 | List all users | Registered users and their roles |

### Customer

| # | Action | Notes |
|---|--------|-------|
| 1 | List available vehicles | Only vehicles that can be rented right now |
| 2 | Rent a vehicle | Price calculation, discounts, payment, reservation record |
| 3 | My reservations | The signed-in customer's own records |
| 4 | Cancel a reservation | Only before the vehicle has been picked up |
| 5 | Pick up a vehicle | Moves the reservation to `ACTIVE` |
| 6 | Return a vehicle | Calculates late/damage penalties and prints an invoice |

### Shared

- Individual and corporate customer registration
- Email + password authentication
- Role-based menu routing (Admin / Customer)
- Every change is written back to CSV immediately

---

## Architecture

The project follows a layered architecture. Each layer depends only on the one below it:

```
┌──────────────────────────────────────────────┐
│  main         Presentation layer             │
│               Main, AdminMenu, CustomerMenu  │
├──────────────────────────────────────────────┤
│  service      Business logic layer           │
│               FleetManager, UserManager,     │
│               ReservationManager             │
├──────────────────────────────────────────────┤
│  repository   Data access layer              │
│               Vehicle/User/Reservation repos │
├──────────────────────────────────────────────┤
│  domain       Entity layer                   │
│               User, Vehicle, Reservation...  │
└──────────────────────────────────────────────┘
        util  ·  exception   (cross-cutting)
```

**Responsibilities**

- **domain** — Data models and enums. No business logic.
- **repository** — CSV read/write, in-memory collections, lookups.
- **service** — Business rules: pricing, availability, state transitions, validation.
- **main** — User interaction. Menus, input parsing, error display.
- **util** — CSV helpers plus mock notification and payment services.
- **exception** — Domain-specific exceptions.

---

## Project Structure

```
Car-Rental-System/
├── RUN.bat                      Compile and run without an IDE (Windows)
├── README.md
└── car-rental/
    └── src/
        ├── vehicles.csv          Vehicle data
        ├── users.csv             User data
        ├── reservations.csv      Reservation data
        └── com/carrental/
            ├── domain/
            │   ├── User.java                 (abstract)
            │   ├── Admin.java
            │   ├── Customer.java
            │   ├── Vehicle.java
            │   ├── Reservation.java
            │   ├── UserRole.java             (enum)
            │   ├── VehicleSegment.java       (enum)
            │   ├── VehicleStatus.java        (enum)
            │   └── ReservationStatus.java    (enum)
            ├── repository/
            │   ├── VehicleRepository.java
            │   ├── UserRepository.java
            │   └── ReservationRepository.java
            ├── service/
            │   ├── FleetManager.java
            │   ├── UserManager.java
            │   └── ReservationManager.java
            ├── main/
            │   ├── Main.java
            │   ├── AdminMenu.java
            │   └── CustomerMenu.java
            ├── util/
            │   ├── CsvUtil.java
            │   ├── NotificationService.java
            │   └── PaymentGateway.java
            └── exception/
                ├── AuthenticationException.java
                ├── DuplicateLicensePlateException.java
                ├── ReservationNotFoundException.java
                └── VehicleUnavailableException.java
```

---

## Getting Started

### Requirements

**JDK 17 or newer.** The code uses switch expressions (Java 14+) and `instanceof` pattern matching (Java 16+). A Java 8 JRE will neither compile nor run it.

If you do not have a JDK: [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) — tick *Set JAVA_HOME variable* during installation.

Check what you have:

```bash
javac -version
```

### Windows — one click

Double-click `RUN.bat`. It locates the JDK, compiles the sources and starts the application.

### Command line (any platform)

```bash
cd car-rental

# Compile
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")

# Run  (the working directory must be car-rental: CSV paths are relative)
java -Dfile.encoding=UTF-8 -cp out com.carrental.main.Main
```

On Windows CMD:

```bat
cd car-rental
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -Dfile.encoding=UTF-8 -cp out com.carrental.main.Main
```

> **Note:** If the box-drawing characters (`╔`, `✓`) look garbled on Windows, run `chcp 65001` first. `RUN.bat` already does this.

---

## Demo Accounts

Shipped in `users.csv`:

| Role | Email | Password | Type |
|------|-------|----------|------|
| Admin | `admin@carrental.com` | `admin123` | — |
| Customer | `alice@example.com` | `pass123` | Individual |
| Customer | `bob@example.com` | `pass123` | Individual |
| Customer | `carol@example.com` | `pass123` | Individual |
| Customer | `contact@acme-logistics.example` | `corp123` | Corporate |
| Customer | `fleet@northwind.example` | `corp123` | Corporate |

### What the seed data shows

The bundled fleet has 12 vehicles and the 8 seed reservations cover every state the system can reach, so each feature can be tried without setting anything up first:

| Reservation | Status | Demonstrates |
|-------------|--------|--------------|
| #1 | `COMPLETED` | Plain rental, returned on time |
| #2 | `COMPLETED` | Corporate customer, 7-day duration discount |
| #3 | `COMPLETED` | Late return — 1,200 TL penalty |
| #4 | `COMPLETED` | Damaged return — vehicle left in `MAINTENANCE` |
| #5 | `CANCELLED` | Cancellation, vehicle released back to `AVAILABLE` |
| #6 | `ACTIVE` | Corporate rental in progress, 10-day discount |
| #7 | `ACTIVE` | Individual rental in progress, 14-day discount |
| #8 | `CONFIRMED` | Booked but not picked up — vehicle is `RESERVED` |

Vehicle statuses match the reservations: `34MNO678` and `35BCD123` are `IN_USE`, `07JKL345` is `RESERVED`, and `35PQR901` sits in `MAINTENANCE` because of the damaged return in #4.

Sign in as `carol@example.com` to pick up reservation #8, or as `bob@example.com` to return #7 and watch the late-fee calculation.

---

## Business Rules

### Rental cost

```
base = number of days × daily price
```

Duration discount:

| Duration | Discount |
|----------|----------|
| 14 days or more | 15% |
| 7–13 days | 10% |
| Under 7 days | none |

Corporate customers get a further **15%** off, applied *after* the duration discount.

> **Example:** a 450 TL/day vehicle, corporate customer, 7 days
> `7 × 450 = 3150` → `10% off` → `2835` → `15% corporate` → **2,409.75 TL**

### Penalties

| Case | Formula |
|------|---------|
| Late return | `days late × daily price × 1.5` |
| Damage | Flat 500 TL |

Penalties are charged at return time and added to the reservation total. A vehicle returned with damage is moved to `MAINTENANCE` automatically.

### Validation

- The start date cannot be in the past
- The end date must be after the start date
- Only an `AVAILABLE` vehicle can be rented
- The vehicle is held **before** payment is taken; if any step fails, the hold is released
- A customer can only act on their own reservations
- A corporate invoice is issued only to a corporate customer

---

## State Machines

### Reservation

```
        makeReservation()
              │
              ▼
         CONFIRMED ──── cancelReservation() ───► CANCELLED
              │
              │ pickUpVehicle()
              ▼
           ACTIVE
              │
              │ returnVehicle()
              ▼
         COMPLETED
```

### Vehicle

```
   AVAILABLE ──── reserved ────────► RESERVED
       ▲                                │
       │                                │ picked up
       │                                ▼
       │                             IN_USE
       │                                │
       ├──── returned, no damage ───────┤
       │                                │
   MAINTENANCE ◄──── returned damaged ──┘
```

---

## Data Files

Data lives in `car-rental/src/` as CSV and is flushed to disk on every change. Values containing a comma, a quote or a line break are quoted and escaped per RFC 4180.

**vehicles.csv**
```csv
licensePlate,brand,model,year,segment,dailyPrice,status
34ABC123,Toyota,Corolla,2022,SEDAN,450.0,AVAILABLE
```

**users.csv**
```csv
userId,email,password,role,phoneNumber,isCorporate,taxId
5,contact@acme-logistics.example,corp123,CUSTOMER,5554567890,true,TR1234567890
```

**reservations.csv**
```csv
reservationId,customerId,licensePlate,startDate,endDate,totalCost,status,isCorporateInvoice,penaltyAmount
3,3,06DEF789,2026-07-20,2026-07-25,3200.0,COMPLETED,false,1200.0
```

A malformed row is skipped with a warning on the console; the application keeps running.

---

## Design Patterns Used

| Pattern / Concept | Where |
|-------------------|-------|
| **Inheritance & abstraction** | `User` (abstract) → `Admin`, `Customer` |
| **Polymorphism** | `getDisplayInfo()` behaves differently per subclass |
| **Encapsulation** | All fields `private`, exposed through accessors |
| **Repository pattern** | Data access separated from business logic |
| **Singleton** | `FleetManager`, `NotificationService`, `PaymentGateway` |
| **Custom exceptions** | Four domain-specific `RuntimeException` subclasses |
| **Enums** | Type-safe roles, segments and statuses |
| **Optional** | `findById`, `findByPlate` — instead of returning `null` |
| **Stream API** | Filtering and lookups |
| **ReadWriteLock** | Concurrent access guard in `VehicleRepository` |

---

## Known Limitations

This is a learning project, not production software.

- **Passwords are stored in plain text.** A real system would hash them with bcrypt or argon2.
- **Future-dated reservations are not supported.** Availability is decided from the vehicle's current status rather than from overlapping date ranges, so a car that is out on rental today cannot be booked for a later date.
- **No refund on early return.** A customer who ends a 10-day rental on day 2 still pays the full amount.
- **Payment and notification services are mocks.** `PaymentGateway` always approves; `NotificationService` only prints to the console.
- **CSV instead of a database.** Not suitable for concurrent multi-user access.
- **Relative file paths.** The application must be started from the `car-rental` directory, otherwise the CSV files are not found.

---

## License

Built for educational purposes.
