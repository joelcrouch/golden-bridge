# Golden Bridge - Working Context

## Architecture
- **Java:** Spring Boot 3.x (port 8080) + JWT auth + JPA
- **Python:** Flask microservice (port 5001) - stateful sessions
- **DB:** PostgreSQL with 5 tables (users, garmin_credentials, activities, sync_history, user_preferences)
- **Communication:** RestTemplate HTTP (not ProcessBuilder)

## Sprint Status: Day 4 → Starting Day 5
**✅ Completed:**
- Days 1-2: Project setup, DB schema, JWT auth
- Days 3-4: Python Flask + Java integration layer
- **New:** Implemented `SyncService` to transform Garmin JSON to `Activity` entities.
- **New:** Implemented deduplication using `data_hash` for activities.
- **New:** Activities are now saved to the database upon fetching.
- **New:** Unit tests for `SyncService` created (95% line coverage, 83% branch coverage).
- **New:** Refactored JPA Auditing to `JpaConfig` to resolve test context conflicts.
- **New:** Fixed Python version compatibility with `garminconnect` library.
- **New:** Fixed `Activity` entity setters and `DateTimeParseException` for activity dates.

**🎯 Current Focus (Days 5-7):**
- [ ] Golden Cheetah FIT file export (Next major task)
- [ ] Improve overall test coverage (especially branch coverage for `SyncService` to 85%+)

## Key Endpoints
**Java (port 8080):**
- `POST /api/auth/register|login` - GB user auth
- `POST /api/auth/garmin/login` - Garmin auth (Now publicly accessible)
- `GET /api/garmin/activities` - Fetch & store activities (initiates sync to DB)
- `GET /api/garmin/activities/{id}/download` - FIT file

**Python (port 5001) - Internal:**
- `POST /garmin/login` - Auth with Garmin
- `GET /garmin/activities` - Fetch from Garmin
- `GET /garmin/activity/<id>/download` - Get FIT

## Database Schema (5 tables)
- `users` - GB app users
- `garmin_credentials` - Encrypted Garmin creds (1:1 with users)
- `activities` - Synced activities with FIT paths & data_hash
- `sync_history` - Audit log of sync operations
- `user_preferences` - Auto-sync settings

## Known Issues/Decisions
- Python service stateful (sessions in memory) - OK for MVP
- No Golden Cheetah integration yet - next priority
- Overall project test coverage needs improvement (current overall 56%)

## Next Steps
- Implement Golden Cheetah integration (file export/API push).
