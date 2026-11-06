# Final test results

**Date:** 6 November 2026  
**Environment:** Windows, installed Oracle JDK 26.0.2, Maven 3.9.11, Spring Boot 3.5.16; Java source target 17.  
**Databases:** H2 (MySQL mode) and MySQL Community Server 8.4.13 using the separate campusexchange_test schema.

## Automated result

| Suite | H2 | MySQL |
|---|---|---|
| MarketplaceFlowTest | PASS | PASS |
| SecurityValidationTest | PASS | PASS |
| ConcurrencyTest, repeated five times | PASS (5/5) | PASS (5/5) |
| Total test executions | 7, zero failures/errors | 7, zero failures/errors |

The table below lists scenarios covered by those integration tests; each row is not a separate JUnit method.

| ID | Scenario | Expected / observed result | Status |
|---|---|---|---|
| T01 | Render home, registration, login and listing pages | Successful HTML responses | PASS |
| T02 | Register/login and create a listing | Account authenticates; listing recorded | PASS |
| T03 | Request own item | Rejected | PASS |
| T04 | Duplicate pending request | Rejected | PASS |
| T05 | Unauthorized request acceptance | Rejected | PASS |
| T06 | Seller accepts request | Sold item, one trade, competing request rejected | PASS |
| T07 | Accept an already resolved request | Rejected | PASS |
| T08 | Listing edit by another user | Rejected | PASS |
| T09 | Owner edits available listing | Saved values displayed | PASS |
| T10 | Edit with pending requests | Rejected | PASS |
| T11 | Buyer cancels / seller rejects | Correct terminal state | PASS |
| T12 | Repeat request after cancellation/rejection | New pending request allowed | PASS |
| T13 | Owner removes listing | Hidden; pending requests rejected; history retained | PASS |
| T14 | Buyer-only review and duplicate prevention | Wrong users and duplicates rejected | PASS |
| T15 | Review rating/comment limits | Out-of-range rating and long comments rejected | PASS |
| T16 | Combined category/price/condition search | Inclusive price bounds and correct exclusions | PASS |
| T17 | Valid owner photo upload | PNG response retrievable | PASS |
| T18 | Invalid, oversized or unauthorized photo | Rejected | PASS |
| T19 | Photo for withdrawn listing | 404 response | PASS |
| T20 | Student requests admin page/action | 403 response | PASS |
| T21 | Admin adds/deletes categories | Unused category removed; used category protected | PASS |
| T22 | Admin removes listing | Withdrawal and pending-request rejection | PASS |
| T23 | Profile update and validation | Own fields updated; invalid fields flagged | PASS |
| T24 | Submitted role/other account ID in profile | No privilege/ownership change | PASS |
| T25 | Suspend an already signed-in student | Session invalidated on next authorized request | PASS |
| T26 | Suspended login/listings and restoration | Login blocked/listings hidden; restored afterwards | PASS |
| T27 | Suspend admin through dashboard | Rejected | PASS |
| T28 | Missing CSRF token on changes | 403 response | PASS |
| T29 | User text containing script markup | Escaped HTML, not an executable script element | PASS |
| T30 | Invalid price/condition parameters | 400 and readable input-error page | PASS |
| T31 | Missing listing ID | 404 response | PASS |
| T32 | Concurrent duplicate requests | Exactly one succeeds, repeated five times | PASS |
| T33 | Concurrent acceptance for one item | One sale, one accepted and one rejected request, repeated five times | PASS |
| T34 | Concurrent duplicate reviews | Exactly one stored review, repeated five times | PASS |

## Bug found and corrected during this phase

The first MySQL concurrency run allowed two duplicate pending requests. Row locking alone was insufficient because the default repeatable-read snapshot could retain an older request view. Marketplace and review service transactions now explicitly use READ_COMMITTED isolation so reads after acquiring a waited lock observe committed state. The corrected suite passed five repetitions of each concurrent scenario on MySQL and H2.

## Other checks

- Earlier MySQL restart verification preserved a stored trade and review.
- Windows PowerShell 5.1 checks passed for a stopped server, an already-running server, and an unavailable-server probe under ErrorActionPreference=Stop.
- Architecture and ER SVG assets are well-formed XML and match the implemented table relationships.
- Java sources were formatted before packaging.

## Practical limits

After the final desktop restart, a live browser walkthrough passed for registration feedback, sign-in/out, item/photo creation, profile saving, buyer requests, sale acceptance, review publication and admin suspension/restoration controls. The 390-pixel mobile layout was visually checked. Eleven real browser screenshots were saved; temporary demo records were then removed. This is not a completed penetration test, sustained load test, or public deployment audit. Five concurrent pairs validate the selected races; they do not establish unlimited concurrency capacity. Backup restoration and email delivery are outside this verification.

## Reproduce

Run `mvn package` for H2. For MySQL, configure a separate test schema and DB_URL/DB_USER/DB_PASSWORD, then run `mvn test -Dspring.profiles.active=mysql`. Do not point these workflow tests at the real application database.



## Password visibility update

Show/hide eye buttons were added to the login password and both registration password fields. A Node check passed for independent toggles, accessible labels, unchanged password values and safe missing-field handling. The selected SecurityValidationTest passed, including anonymous script access and rendered controls; Maven packaging succeeded. This small update did not repeat the database concurrency suites. The existing screenshot gallery predates these eye buttons.

Reproduce the JavaScript check with: node src/test/javascript/password-visibility.test.cjs



## College email policy update

The complete H2 build passed eight test executions with zero failures/errors. CollegeEmailTest verifies normalized college registration/login, rejection of Gmail/lookalike/subdomain/malformed addresses, registration error feedback, denial of legacy non-college logins, and revocation of an existing non-college principal. Existing account records remain stored. MySQL was not rerun for this pure Java authentication/validation update; the schema is unchanged. Historical screenshots show now-ineligible fictional example.edu demo accounts, which were removed after capture.



## Required profile fields update

The full H2 package build passed nine test executions. ProfileRequiredFieldsTest covers omitted and whitespace-only phone/hostel, valid submission with an empty department, attempts to clear each required field, direct service rejection of null values, and preservation of previously saved data. MySQL schema is unchanged.



## Listing email notifications update

The complete suite passed twelve test executions on H2 and twelve on MySQL. EmailNotificationTest uses a mocked SMTP sender to verify recipient/content, persistence of a listing after delivery failure, delayed retry followed by success, no repeat send after success, rollback of notification rows with their listing transaction, and a five-failure limit. No real emails were sent during automated tests. Windows PowerShell 5.1 encrypted credential loading and script syntax checks also passed. Real Gmail delivery is pending local sender configuration.

