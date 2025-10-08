# Testing Commands for Golden Bridge

This file contains the `curl` commands for testing the Golden Bridge application.

## 1. Register a New User

If you are starting with an empty database, you need to register a new user for the application.

```bash
curl -X POST -H "Content-Type: application/json" -d '{"username": "testuser", "password": "password", "email": "testuser@example.com"}' http://localhost:8080/api/auth/register
```

## 2. Log in and Get a JWT Token

Log in with your application user to get a JWT token. This command will automatically save the token to a shell variable named `TOKEN`.

```bash
TOKEN=$(curl -X POST -H "Content-Type: application/json" -d '{"username": "testuser", "password": "password"}' http://localhost:8080/api/auth/login | jq -r .accessToken)

# You can verify the token with:
# echo $TOKEN
```
*(Note: This command uses the `jq` utility. If you don't have `jq`, you can run the `curl` command by itself and then manually copy the `accessToken` value.)*

## 3. Log in to Garmin

Use the token from the previous step to log in to your Garmin account.

**Remember to replace `"your-garmin-username"` and `"your-garmin-password"` with your actual Garmin credentials.**

```bash
curl -X POST -H "Content-Type: application/json" -d '{"username": "your-garmin-username", "password": "your-garmin-password"}' http://localhost:8080/api/auth/garmin/login
```

## 4. Fetch Garmin Activities

Use your application token to fetch your recent activities from Garmin.

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/garmin/activities
```

---

## 5. Checking the Database

These commands are useful for verifying that the API calls are correctly writing data to the database.

### Step 1: Access the PostgreSQL Container

Get a command-line shell inside the running PostgreSQL Docker container.

```bash
docker exec -it golden-bridge-postgres bash
```

### Step 2: Connect to the Database

Once inside the container, use the `psql` command-line tool to connect to the database. The password is `golden_bridge_pass`.

```bash
psql -U golden_bridge_user -d golden_bridge
```

### Step 3: Query the Database

You can now run SQL queries to inspect the tables.

**Check for users:**
```sql
SELECT id, username, email, created_at FROM users;
```

**Check for activities:**
```sql
SELECT garmin_activity_id, activity_name, activity_date FROM activities;
```

### Step 4: Exit

*   To exit `psql`, type `\q` and press Enter.
*   To exit the container shell, type `exit` and press Enter.