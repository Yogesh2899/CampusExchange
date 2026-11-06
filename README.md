# CampusExchange

CampusExchange is a Java and Spring Boot marketplace for buying and selling pre-owned items within a campus community.

Registration and login require an email address belonging to the exact `@thapar.edu` domain. Students can act as both buyers and sellers.

## Features

### Student accounts

- College-domain registration and login
- BCrypt password hashing
- Show/hide controls for password fields
- Private profile editing
- Required phone and hostel when saving a profile
- Optional department field

### Marketplace

- Create item listings
- Edit eligible available listings
- Withdraw listings while preserving history
- Upload or replace one JPEG/PNG photograph per listing
- Search by keyword
- Filter by category, price range, and condition

### Purchase workflow

- Send purchase requests
- Cancel your own pending requests
- Accept or reject requests for your listings
- Record a trade when a request is accepted
- Mark the item sold
- Reject competing pending requests
- View trade history
- Submit one buyer review per trade
- Display seller rating averages

### Administration

- Moderate available listings
- Add categories
- Delete unused categories
- Suspend or restore student accounts
- Revoke access from suspended sessions

### Optional email notifications

Publishing a new listing can queue a confirmation email to its seller.

Notifications use a persistent outbox and retry failed delivery attempts. SMTP failures do not undo the saved listing.

This feature currently covers listing publication.

## Sale workflow

1. A seller publishes an available item.
2. A buyer submits a pending purchase request.
3. The seller accepts or rejects it.
4. Acceptance records the trade, marks the item sold, and rejects competing requests.
5. The buyer can review the seller.

Payment and physical handover happen offline. There is no separate reservation or handover-completion stage.

## Technology stack

- Java 17 source target
- Spring Boot 3.5.16
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA and Hibernate
- MySQL
- H2 for development and testing
- Spring Mail
- HTML, CSS, and JavaScript
- Maven
- JUnit, MockMvc, and Mockito

## Project structure

```text
src/
├── main/
│   ├── java/com/campusexchange/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── static/
│       │   ├── css/
│       │   └── js/
│       ├── templates/
│       ├── application.properties
│       └── application-mysql.properties
└── test/
    ├── java/
    └── javascript/

database/
docs/
pom.xml
```

## Requirements

For rebuilding from source:

- A compatible JDK, with Java 17 as the source target
- Maven 3.6.3 or later
- MySQL 8.4 for persistent operation

A packaged application requires Java but does not require Maven.

## Clone and build

Replace `YOUR_USERNAME` with the repository owner's GitHub username.

```powershell
git clone https://github.com/YOUR_USERNAME/CampusExchange.git
cd CampusExchange
mvn clean package -DskipTests
```

The executable JAR is created at:

```text
target/campusexchange-0.1.0.jar
```

Run the isolated tests described below before treating a changed build as verified.

## Quick demonstration using H2

```powershell
java -jar target/campusexchange-0.1.0.jar
```

Open:

```text
http://localhost:8080
```

The default configuration uses an in-memory H2 database.

**H2 demonstration data resets when the application stops.**

If you have previously selected the MySQL profile through environment variables, clear that selection before using the default H2 configuration.

## Run with MySQL

### 1. Create the schema

Open the MySQL client from the repository directory:

```powershell
mysql -u root -p
```

Execute:

```sql
SOURCE database/schema.sql;
```

For a new installation, this creates the current database structure.

### 2. Create an application database account

Run these statements through your administrative database connection.

Replace the password placeholder locally.

```sql
CREATE USER 'campus_app'@'localhost'
IDENTIFIED BY 'REPLACE_WITH_LOCAL_DB_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE
ON campusexchange.*
TO 'campus_app'@'localhost';
```

### 3. Configure the connection

Example for a MySQL server listening on port 3306:

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/campusexchange"
$env:DB_USER="campus_app"
$env:DB_PASSWORD="YOUR_LOCAL_DB_PASSWORD"
```

Use the actual port of your MySQL installation.

The original project workspace uses port 3307.

### 4. Start the application

```powershell
java -jar target/campusexchange-0.1.0.jar --spring.profiles.active=mysql
```

Open `http://localhost:8080`.

The MySQL profile validates the database schema at startup.

## Original Windows workspace launchers

The supplied helper scripts support the original local installation:

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

The MySQL helper scripts expect private installation settings under `.local/`.

Those settings and the original MySQL runtime are not included in a public GitHub checkout.

For a fresh checkout on another PC, follow the manual MySQL setup above.

## Administrator access

There is no public administrator-registration endpoint or default administrator password.

First register an eligible `@thapar.edu` account.

For the original local installation:

```powershell
powershell -ExecutionPolicy Bypass -File .\make-admin.ps1
```

Enter the registered email, then sign out and back in.

For another installation, the trusted database owner can assign the role through an administrative connection:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'your.registered.address@thapar.edu';
```

The role remains stored in MySQL.

## Email notification setup

Email notifications are disabled by default.

### Original Windows installation

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-email.ps1
```

Enter the sender email and Google app password locally, then restart using `start.ps1`.

The script stores the app password using Windows user encryption under `.local/`.

### Manual SMTP configuration

Example Gmail configuration:

```powershell
$env:EMAIL_NOTIFICATIONS_ENABLED="true"
$env:SMTP_HOST="smtp.gmail.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="your.sender@gmail.com"
$env:SMTP_PASSWORD="YOUR_GOOGLE_APP_PASSWORD"
```

Start the app with the appropriate database profile.

Google app-password instructions:

https://support.google.com/accounts/answer/185833

App-password availability depends on the account's security and organization settings.

Do not commit real credentials to GitHub.

### Delivery behavior

- A new listing queues an email to its seller.
- The notification commits with the listing transaction.
- Delivery occurs afterwards.
- Failed delivery attempts do not reverse the listing.
- The worker retries with increasing delays.
- Delivery stops after five failed attempts.
- MySQL preserves pending notifications across restarts.
- H2 does not preserve them after shutdown.

SMTP submission is not an inbox-placement guarantee. An ambiguous delivery failure can result in a duplicate email on retry.

See `docs/EMAIL_SETUP.md` for details.

## Database tables

| Table | Purpose |
|---|---|
| `users` | Accounts and profile information |
| `categories` | Listing categories |
| `items` | Marketplace listings |
| `purchase_requests` | Buyer requests and their status |
| `trades` | Recorded sales |
| `reviews` | Buyer ratings and comments |
| `item_photos` | Stored listing photographs |
| `email_notifications` | Publication-confirmation delivery queue |

### Existing database migrations

Apply only migrations that your existing installation has not already received:

- `002_reviews.sql`
- `003_item_photos.sql`
- `004_profiles_and_status.sql`
- `005_email_notifications.sql`

Fresh installations should use `schema.sql`, which already contains the current tables and columns.

## Tests

Tests create synthetic accounts, listings, and trade records.

**Always use an isolated test database. Never run workflow tests against the real application database.**

### Explicit isolated H2 test run

The following PowerShell command overrides inherited application database settings:

```powershell
$env:EMAIL_NOTIFICATIONS_ENABLED="false"

mvn test `
  "-Dspring.profiles.active=test" `
  "-Dspring.datasource.url=jdbc:h2:mem:campus_test;DB_CLOSE_DELAY=-1;MODE=MySQL" `
  "-Dspring.datasource.driver-class-name=org.h2.Driver" `
  "-Dspring.datasource.username=sa" `
  "-Dspring.datasource.password=" `
  "-Dspring.jpa.hibernate.ddl-auto=update"
```

The email-notification integration tests use a mocked mail sender.

### JavaScript password-control check

If Node.js is installed:

```powershell
node src/test/javascript/password-visibility.test.cjs
```

### MySQL tests

Create a separate test schema, such as `campusexchange_test`, using the supplied schema with the database name changed appropriately.

Configure credentials for that test database and explicitly point the test run at it:

```powershell
$env:EMAIL_NOTIFICATIONS_ENABLED="false"
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/campusexchange_test"
$env:DB_USER="YOUR_TEST_DB_USER"
$env:DB_PASSWORD="YOUR_TEST_DB_PASSWORD"

mvn test `
  "-Dspring.profiles.active=mysql" `
  "-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/campusexchange_test"
```

Change port 3306 if your test server uses another port.

### Recorded verification

The latest suite contains 12 test executions covering:

- Marketplace workflows
- College-domain restrictions
- Required profile fields
- Authorization and CSRF
- Escaping and invalid input
- Photo handling
- Administrator controls
- Email queue failure and rollback behavior
- Concurrent duplicate requests, sale acceptance, and reviews

The isolated H2 run and recorded MySQL run completed with zero test failures and errors.

The selected concurrency checks repeat five times. They are bounded race checks rather than a sustained load test.

## Security and business rules

- Only exact `@thapar.edu` addresses can register and sign in.
- Email ownership is not verified through OTP.
- Passwords use BCrypt.
- State-changing requests require CSRF protection.
- Listing actions check the authenticated seller.
- Profile updates cannot assign a role or target another account.
- Suspended users cannot authenticate or retain ordinary access.
- Admin routes and service methods require the ADMIN role.
- Marketplace transactions use row locking and READ_COMMITTED isolation.
- Only a recorded buyer can review a trade.
- Uploaded image names are not used as filesystem paths.

## Screenshots and documentation

The `docs/` folder contains:

- Project report
- Architecture and database diagrams
- Screenshot gallery
- User guide
- Demo script
- Test results
- Email setup instructions

Some screenshots show an earlier build. Registration and profile captures may predate the password eye buttons, college-domain wording, and required phone/hostel fields.

## Limitations

- No email ownership verification
- No online payment processing
- Offline payment and handover
- No password-reset email service
- No real-time chat or shipping integration
- No pagination
- Photos increase database size
- Real SMTP delivery requires valid configuration and manual verification
- No claim of production load capacity or complete security certification

## Future improvements

- Email verification and account recovery
- Pagination and improved search
- Image thumbnails
- Additional request and sale notifications
- Moderation audit records
- Backup and deployment tooling
