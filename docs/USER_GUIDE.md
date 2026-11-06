# CampusExchange setup and user guide

## Run the existing workspace installation

Open PowerShell in the CampusExchange project folder and run:

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

Wait for `Started CampusExchangeApplication`, then open `http://localhost:8080`. Keep the terminal open. Press Ctrl+C to stop the application. The local database can be stopped separately with `stop-mysql.ps1`. The launcher supports Windows PowerShell 5.1, including the case where MySQL is initially stopped.

If port 8080 is already in use, stop the earlier CampusExchange terminal with Ctrl+C. To deliberately use another port, run `start.ps1 --server.port=8082` and open that port. Avoid running different versions against the same database while demonstrating changes.

## Install from the submission archive on another PC

The archive contains source, a runnable JAR, documentation and SQL. It does not contain the original PC's MySQL installation, database contents, passwords or `.local` configuration.

1. Install a JDK compatible with the project (Java 17–25 recommended) and MySQL 8.4.
2. Execute `database/schema.sql` in MySQL using an administrative connection.
3. Create an application database account with access to `campusexchange`.
4. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the terminal. Use your actual credentials locally; do not paste them into a report or commit them.
5. Run the JAR with the MySQL profile:

```powershell
java -jar .\CampusExchange-email.jar --spring.profiles.active=mysql
```

For an ephemeral demonstration without MySQL, run the same JAR without the profile. That uses in-memory H2 and resets when stopped. The workspace's start.ps1 chooses MySQL only when its private `.local` configuration is available.

## Build and test

With Maven installed:

```powershell
mvn package
mvn test
```

The default tests use H2. MySQL verification requires a separate `campusexchange_test` schema created from the supplied schema with its database name changed to that test name, and a test connection configured using the environment variables. Run `mvn test -Dspring.profiles.active=mysql`. Workflow tests create accounts and trades: always point them at the test schema.

Copy `target/campusexchange-0.1.0.jar` to `CampusExchange-email.jar` after rebuilding. Existing MySQL installations apply migrations once in this order: 002 reviews, 003 photos, 004 profiles/status. A fresh schema.sql already includes those structures.

## Student walkthrough

1. Register using your name, email and matching password entries. Sign in.
2. Open Profile to update your name, phone and hostel, with department optional.
3. Choose Sell an item and complete title, category, condition, price and description.
4. Open the listing's details to add or replace its photo. Use JPEG/PNG up to 3 MB and the displayed dimension limits.
5. Browse using keyword, category, price and condition. Reset clears filters.
6. As a different account, open an available listing and send a purchase request.
7. Use Requests to cancel your own pending requests or to accept/reject requests for your listings.
8. Acceptance completes the app's sale immediately. Coordinate payment/handover offline.
9. Open History. The recorded buyer can leave one seller review per trade.

My listings contains edit/remove actions. Pending requests must be resolved before editing. Removal hides a listing and rejects pending requests; sold items remain in history.

## Administrator walkthrough

For the original local installation, register your account first, then run `make-admin.ps1` and enter its email. Sign out and back in to refresh the role. On another PC, an installation owner can assign the ADMIN role through their own secure database administration connection.

Admin lets you remove available listings, add categories, delete unused categories, and suspend/restore students. Admin accounts are protected from dashboard suspension. Suspension hides a seller's listings and blocks their account; restoration makes their available listings visible again.

## Common problems

| Symptom | Action |
|---|---|
| Cannot connect to local MySQL | Use the updated start-mysql.ps1; inspect work/mysql/mysql.log if startup still fails. |
| Application reports port 8080 in use | Stop the older terminal or deliberately choose another port. |
| Schema validation fails after an update | Apply the missing supplied migration once. Do not drop existing tables to fix it. |
| New controls do not appear | Stop and restart using start.ps1, then refresh the browser. |
| Admin link is absent after role assignment | Sign out and sign in again. |
| Photo upload fails | Use a valid JPEG/PNG within the byte/dimension limits. |
| Listing cannot be edited | Resolve pending requests; sold/withdrawn listings cannot be edited. |
| Account is suspended | Ask the local administrator to restore it. |

## Data and backups

MySQL data survives app restarts. The original workspace uses `work/mysql/data`; private connection settings live in `.local`. Do not copy a running MySQL data directory as a backup. Use the MySQL backup tools and keep credentials private. The submission archive intentionally excludes live database files and local secrets.




