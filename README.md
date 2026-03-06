# Finance App

This is an Android stock and sales management app for a single shop or tavern. It works offline on one device, stores its data locally in SQLite, and uses local authentication with role-based access control.

## What The App Does

The app helps staff:

- keep stock records
- record item sales
- view daily sales totals
- see the most sold item for the day
- watch for low stock
- manage staff accounts based on role
- send daily email reports to business recipients

The app is designed for one device in one business location. It does not currently sync users or data between multiple devices.

## Main Features

- local login and registration
- first-user Owner setup
- three roles: `Owner`, `Manager`, `Cashier`
- required email capture for `Owner` and `Manager`
- stock item and size management
- stock quantity, pricing, and received-stock baseline tracking
- sales recording from the dashboard
- user management for the Owner
- manual report sending to all active `Owner` and `Manager` emails
- store-close report sending
- automatic near-midnight daily report fallback
- global low-stock threshold or per-item 10% fallback logic

## How Authentication Works

Authentication is fully local.

- User accounts are stored in SQLite in the `users` table.
- Passwords are not stored as plain text.
- Passwords are salted and hashed before being saved.
- Logged-in session details are stored in local preferences.
- The session stores metadata like user id, display name, and role.
- The session does not store the password.
- `Owner` and `Manager` accounts store email addresses for reporting.

### First Launch

When the app is launched for the first time:

1. The app opens `MainActivity`.
2. If tavern details are missing, the user must enter the tavern name and barman name.
3. The app checks whether any user exists in the database.
4. If no users exist, it opens the Owner registration screen.
5. The first account created automatically becomes the `Owner`.
6. The Owner is logged in immediately and sent into the main app.

### Later Launches

After the first setup:

1. `MainActivity` checks whether a valid session exists.
2. If a valid active user session exists, the app opens `ManagementActivity`.
3. If no session exists, the app opens `LoginActivity`.
4. If the saved session points to a disabled or missing user, the app clears the session and sends the user to login.

## User Roles

The app has three user roles.

### Owner

The `Owner` has full access.

The Owner can:

- log in and use all app features
- manage stock
- sell items
- send reports
- receive daily and manual report emails
- edit tavern details
- create staff accounts
- enable or disable staff accounts
- switch non-owner users between `Manager` and `Cashier`
- edit user email addresses
- configure the global low-stock threshold
- reset stock and sales data

### Manager

The `Manager` can work with stock and reports, but cannot manage users or wipe data.

The Manager can:

- log in
- view the dashboard
- sell items
- manage stock
- add items and item sizes
- add or update stock quantities and prices
- send reports
- receive daily and manual report emails

The Manager cannot:

- create users
- change user roles
- disable users
- reset stock and sales data
- edit tavern details through the Owner menu
- change low-stock settings

### Cashier

The `Cashier` is limited to day-to-day selling.

The Cashier can:

- log in
- view the dashboard
- sell items

The Cashier cannot:

- manage stock
- add items
- add sizes
- send reports
- receive automated role-based reports
- manage users
- reset stock and sales data

## Main Screens

### `MainActivity`

This is the app entry point. It does not act as the business screen anymore. It now:

- checks tavern details
- checks whether users exist
- checks whether a session exists
- schedules the daily report worker
- routes the user to registration, login, or the management screen

### `RegisterActivity`

This screen is used in two cases:

- first setup, where it creates the very first `Owner`
- Owner-managed account creation, where the Owner can create a `Manager` or `Cashier`

Validation rules:

- all fields are required except email for `Cashier`
- passwords must match
- password must be at least 4 characters
- usernames must be unique
- `Owner` and `Manager` accounts must have a valid email address

### `LoginActivity`

This screen is used by existing users.

Login behavior:

- the username is normalized
- the password is checked against the stored salted hash
- if valid, the session is saved
- the user is sent to `ManagementActivity`

### `ManagementActivity`

This is the main working screen.

It includes:

- a dashboard
- daily totals
- most sold item summary
- category filtering
- stock list
- sell-item action by tapping a stock row
- stock management form for allowed roles
- low-stock summary banner
- role-aware menu actions

The screen changes behavior based on the current role.

### `UserManagementActivity`

This is an Owner-only screen.

The Owner can:

- view all users
- create new staff accounts
- enable or disable non-owner users
- switch non-owner roles between `Manager` and `Cashier`
- view and update stored user emails

The Owner account cannot be disabled or changed from this screen.

## Stock And Sales Flow

### Managing Stock

Only `Owner` and `Manager` can manage stock.

They can:

- add item names
- add item sizes
- enter cost price
- enter selling price
- enter quantity
- enter category

If the selected item and size do not already exist in stock:

- a new stock row is created
- the received quantity baseline is initialized from the entered quantity

If the selected item and size already exist:

- the quantity is increased
- pricing values are updated
- the received quantity baseline is also increased

This received baseline is used by the per-item low-stock fallback rule.

### Selling Items

`Owner`, `Manager`, and `Cashier` can sell items.

Selling flow:

1. Tap an item row on the dashboard.
2. Enter quantity to sell.
3. The app validates that the quantity is greater than zero.
4. The app validates that enough stock exists.
5. Stock quantity is reduced.
6. The sale is saved in the sales table.
7. Totals, summaries, and low-stock status are refreshed.

### Dashboard Data

The dashboard shows:

- current stock by category
- quantity remaining
- total amount sold for the day
- most sold item for the day
- low-stock summary information

## Low Stock Alerts

The app supports two low-stock modes.

### Global Threshold Mode

The `Owner` can set one global low-stock threshold for the whole store.

If this is set:

- every item is treated as low stock when `current quantity <= global threshold`
- the dashboard warning and report logic both use the same rule

### Fallback 10% Mode

If the global threshold is left empty, the app falls back to per-item monitoring.

For every stock item:

- the app stores a `received quantity` baseline
- the item is treated as low stock when `current quantity <= 10% of received quantity`
- each item is checked independently

This means every item can trigger its own low-stock alert based on its own received inventory history.

### Where Alerts Appear

Low-stock items appear in:

- the dashboard low-stock summary
- manual full reports
- store-close reports
- automatic near-midnight reports

## Tavern Details

The app stores tavern details locally:

- tavern name
- barman name

These are required before the app can be used.

The Owner can update these details later from the menu.

## Reports

The app can send reports by email, but only if SMTP settings are configured.

Reports are sent to all active `Owner` and `Manager` accounts that have email addresses.

### Report Triggers

The app supports three report paths:

- manual full report from the menu
- store-close report when the store is explicitly closed from the app
- automatic near-midnight fallback if the store was not closed

### Store Close Behavior

When an allowed user closes the store:

- the app can send the daily report immediately
- the day is recorded in report history
- the midnight worker skips sending a duplicate report for that same day

### Midnight Fallback

If the store was not closed through the app:

- a background worker runs near midnight
- it sends the previous day’s report
- it skips the send if a close-of-day report was already sent for that day

This background scheduling is best effort, not exact-to-the-second `00:00`.

### Report Content

Reports include:

- total sales amount
- best-selling item
- detailed sales lines for the date
- low-stock alerts
- trigger type such as manual, store close, or automatic fallback

Low-stock alerts are always included in report content.

### SMTP Configuration

To enable reporting, set these values in:

- `app/src/main/res/values/strings.xml`

Required values:

- `email_sender`
- `app_email_password`
- `smtp_host`
- `smtp_port`

Without those values, the app will show that email reporting is not configured.

## Database Overview

The app uses a local SQLite database managed by `StockDatabaseHelper`.

Important tables:

- `users`
- `stock_table`
- `sales_table`
- `items_in_store`
- `items_sizes_in_store`
- `app_settings`
- `report_history`

### `users`

Stores:

- id
- full name
- username
- email
- password hash
- role
- active flag
- created timestamp

### `stock_table`

Stores stock records:

- item name
- cost price
- selling price
- quantity
- received quantity
- description
- category
- size

### `sales_table`

Stores recorded sales:

- item name
- cost price
- selling price
- quantity sold
- description
- size
- date
- category

### `app_settings`

Stores application-level settings such as:

- global low-stock threshold

### `report_history`

Stores sent report history so the app can:

- detect already sent store-close reports
- avoid duplicate midnight sends for the same business day

## Security Improvements Included

The current version fixes several important issues from the older app flow:

- removed the hardcoded password gate
- removed the old fake admin password flow
- removed the dangerous delete-before-auth logic
- made internal activities non-exported
- fixed the receiver class path in the manifest
- removed hardcoded email credentials from source/resources
- added role checks in both UI behavior and app logic
- stores passwords as salted hashes instead of plain text

## Running The App

### Build

From the project root:

```powershell
./gradlew.bat assembleDebug
```

### Install On A Connected Device

```powershell
./gradlew.bat installDebug
```

### Launch With ADB

```powershell
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" shell monkey -p com.example.finance -c android.intent.category.LAUNCHER 1
```

## Current Limitations

- accounts are local to one device
- there is no cloud sync
- there is no backend API for auth
- email reporting needs manual SMTP configuration
- background midnight reporting depends on the device being on and allowed to run background work
- the app is intended for offline single-shop use

## Suggested Test Flow

1. Launch the app on a clean install.
2. Enter tavern details.
3. Create the first Owner account with an email address.
4. Add stock as Owner.
5. Create one Manager and one Cashier user.
6. Verify the Manager must have an email and the Cashier can be created without one.
7. Log in as Manager and verify stock management works.
8. Log in as Cashier and verify stock management is blocked.
9. Sell items until at least one item reaches 10% of its received quantity.
10. Verify that the item appears in the low-stock summary and report output.
11. Send a manual full report and confirm it targets active Owner and Manager emails.
12. Use the store-close action and verify the day is treated as already reported.
