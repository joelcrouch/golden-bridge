# Golden Bridge - Working Context

## Architecture
- **Java:** Spring Boot 3.x (port 8080) + JWT auth + JPA
- **Python:** Flask microservice (port 5001) - stateful sessions
- **DB:** PostgreSQL with 5 tables (users, garmin_credentials, activities, sync_history, user_preferences)
- **Communication:** RestTemplate HTTP (not ProcessBuilder)

## Sprint Status: Day 5-7 (In Progress - October 8, 2025)
**✅ Completed:**
- Days 1-2: Project setup, DB schema, JWT auth
- Days 3-4: Python Flask + Java integration layer
- **Day 5 Completed:**
  - ✅ Implemented `SyncService` to transform Garmin JSON to `Activity` entities
  - ✅ Implemented deduplication using `data_hash` for activities
  - ✅ Activities are now saved to the database upon fetching
  - ✅ Unit tests for `SyncService` created (95% line coverage, 83% branch coverage)
  - ✅ Refactored JPA Auditing to `JpaConfig` to resolve test context conflicts
  - ✅ Fixed Python version compatibility with `garminconnect` library
  - ✅ Fixed `Activity` entity setters and `DateTimeParseException` for activity dates
  - ✅ **NEW: Implemented `GoldenCheetahService` with FIT file export logic**
  - ✅ **NEW: Added `GoldenCheetahController` with export endpoints**
  - ✅ **NEW: Added `EXPORTED` status to `Activity.SyncStatus` enum**
  - ✅ **NEW: Unit tests for `GoldenCheetahService` (83% instruction coverage, 81% branch coverage)**
  - ✅ **NEW: FIT file download and copy to Golden Cheetah directory structure**

**🎯 Current Focus (Remaining Days 5-7):**
- [ ] Test end-to-end Golden Cheetah export flow manually
- [ ] Add user preferences endpoint to configure golden_cheetah_path
- [ ] Improve overall test coverage if time permits

## Key Endpoints
**Java (port 8080):**
- `POST /api/auth/register|login` - GB user auth
- `POST /api/auth/garmin/login` - Garmin auth (Now publicly accessible)
- `GET /api/garmin/activities` - Fetch & store activities (initiates sync to DB)
- `GET /api/garmin/activities/{id}/download` - FIT file download
- **NEW:** `POST /api/golden-cheetah/export/{activityId}` - Export single activity to Golden Cheetah
- **NEW:** `POST /api/golden-cheetah/export-all` - Export all non-exported activities to Golden Cheetah

**Python (port 5001) - Internal:**
- `POST /garmin/login` - Auth with Garmin
- `GET /garmin/activities` - Fetch from Garmin
- `GET /garmin/activity/<id>/download` - Get FIT

## Database Schema (5 tables)
- `users` - GB app users
- `garmin_credentials` - Encrypted Garmin creds (1:1 with users)
- `activities` - Synced activities with FIT paths, data_hash, **golden_cheetah_path**, and **sync_status** (PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED, **EXPORTED**)
- `sync_history` - Audit log of sync operations
- `user_preferences` - Auto-sync settings + **golden_cheetah_path** configuration

## Known Issues/Decisions
- Python service stateful (sessions in memory) - OK for MVP
- **Golden Cheetah integration now implemented** - FIT file export working
- FIT files stored in `/tmp/golden-bridge/fits/` before copying to Golden Cheetah directory
- Golden Cheetah directory structure: `{goldenCheetahPath}/{username}/activities/`
- Overall project test coverage improving (current overall ~60%, GoldenCheetahService at 83%)

## Implementation Details
**GoldenCheetahService Logic:**
1. Validates user owns the activity
2. Checks `golden_cheetah_path` is configured in user preferences
3. Downloads FIT file from Garmin if not already cached (via `GarminIntegrationService`)
4. Saves FIT to temp location: `/tmp/golden-bridge/fits/{garminActivityId}_{date}.fit`
5. Copies FIT to Golden Cheetah: `{gcPath}/{username}/activities/{filename}.fit`
6. Updates activity with `golden_cheetah_path` and sets `sync_status = EXPORTED`
7. Creates `SyncHistory` entry for audit trail

**Error Handling:**
- Missing Golden Cheetah path → 400 Bad Request with helpful message
- Activity not found → 400 Bad Request
- FIT download fails → Activity marked as FAILED, error logged
- File copy fails → Activity marked as FAILED, transaction rollback
- Batch export continues on individual failures, returns partial success

## Next Steps
- Test end-to-end export flow with real Garmin data
- Add user preferences management endpoint
- Consider adding scheduled export jobs (Day 10-11 feature)
