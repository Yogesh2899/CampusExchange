# Listing confirmation emails

When enabled, publishing a new listing queues a confirmation email to the seller's registered @thapar.edu address. It includes the item title, price, category, condition and listing ID. Editing a listing does not send another publication email. Purchase/request/review emails are not included in this first notification feature.

## Gmail or Google Workspace setup on this PC

1. Choose a Gmail/Google Workspace account that you control as the sender. It can differ from the seller's college address.
2. Follow Google's app-password instructions: https://support.google.com/accounts/answer/185833. App passwords require 2-Step Verification. A school/organization may restrict availability, in which case consult its administrator or use a sender account where app passwords are permitted.
3. In the project folder, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-email.ps1
```

4. Enter the sender email and Google app password locally when prompted. Do not use your normal Google password or send credentials in chat.
5. Stop the old app with Ctrl+C and restart using `start.ps1`.
6. Sign in with your college account and publish a new listing. Allow approximately 10–20 seconds for the queue worker. Check Inbox and Spam.

No email is sent by the setup script. Actual delivery has not been verified until a real sender is configured and a confirmation arrives.

The app uses smtp.gmail.com on port 587 with authentication and required STARTTLS. See Google's settings: https://support.google.com/mail/answer/7104828. Sender settings are stored under .local/mail-settings.json; the password uses Windows user encryption and can be decrypted only in the appropriate Windows user context. The launcher decrypts it into the Java process environment. The file is excluded from Git and the submission archive. Re-run setup when changing Windows accounts or sender credentials.

## Queue and failures

The notification row commits with the listing. SMTP delivery happens afterwards, outside the listing transaction. A failed delivery does not undo the saved listing. Pending rows survive app restarts when using MySQL. H2 demo rows do not survive a restart.

The worker checks periodically, retries failures with increasing delays, and stops after five failed attempts. Logs show the notification ID/attempt only, without printing credentials or full email content. The listing page does not claim that an email has arrived.

After correcting credentials, an installation administrator can inspect status with:

```sql
SELECT id, item_id, status, attempts, next_attempt_at, sent_at
FROM email_notifications ORDER BY id DESC;
```

Failed rows can be requeued deliberately after resolving the problem:

```sql
UPDATE email_notifications
SET status='PENDING', attempts=0, next_attempt_at=CURRENT_TIMESTAMP(6)
WHERE id=YOUR_NOTIFICATION_ID AND status='FAILED';
```

SMTP cannot guarantee exactly-once delivery. A connection failure after Gmail accepts a message, or a process crash before recording success, can cause a retry and duplicate email. This notification confirms publication, not payment or physical handover.

Notifications are disabled by default. Enabling them affects new listings only; previous listings do not generate retrospective emails. To disable them in the original installation, rename the private mail-settings.json file before restarting, and unset EMAIL_NOTIFICATIONS_ENABLED if you configured it manually.

## Other installations

Apply database/005_email_notifications.sql once (new schema.sql already includes it). Set EMAIL_NOTIFICATIONS_ENABLED=true, SMTP_HOST, SMTP_PORT, SMTP_USERNAME and SMTP_PASSWORD as environment variables if not using the local Windows scripts. Do not publish these settings.

The new email_notifications table is an operational outbox alongside the seven marketplace business tables shown in the earlier ER diagram.

