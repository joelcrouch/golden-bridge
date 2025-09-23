import unittest
from unittest.mock import MagicMock, patch
import json

import garmin_api

# Assuming garmin_api.py is in the same directory or importable

class MockGarmin:
    """Mock class for garminconnect.Garmin."""
    def __init__(self, *args, **kwargs):
        pass

    def login(self):
        pass

    @property
    def username(self):
        return "mockuser"

    def get_activities(self, start, limit):
        # Return dummy activity data
        return [
            {"activityId": 1, "activityName": "Mock Activity 1"},
            {"activityId": 2, "activityName": "Mock Activity 2"}
        ]

    def get_activity_details(self, activity_id):
        # Return dummy activity detail data
        return {"activityId": activity_id, "detail": "Mock details for " + str(activity_id)}

    def download_activity(self, activity_id, dl_fmt):
        # Return dummy binary data for FIT file
        return b"mock_fit_data_for_" + str(activity_id).encode()

    def logout(self):
        pass

    def get_user_summary(self, cdate):
        return {"cdate": cdate, "summary": "Mock health summary for " + cdate}

    class ActivityDownloadFormat:
        FIT = "fit" # Or whatever value is expected by the download_activity mock


class GarminApiTest(unittest.TestCase):

    def setUp(self):
        self.app = garmin_api.app.test_client()
        self.app.testing = True
        # Patch the Garmin class in garmin_api.py
        self.garmin_patcher = patch('garmin_api.Garmin', new=MockGarmin)
        self.mock_garmin_class = self.garmin_patcher.start()
        # Patch the global api_client instance directly
        self.api_client_patcher = patch('garmin_api.api_client', new=self.mock_garmin_class())
        self.mock_api_client_instance = self.api_client_patcher.start()

    def tearDown(self):
        self.garmin_patcher.stop()
        self.api_client_patcher.stop()
        garmin_api.api_client = None # Clear api_client after tests

    def test_garmin_login_success(self):
        response = self.app.post('/garmin/login', data=json.dumps({
            'username': 'test@example.com',
            'password': 'password'
        }), content_type='application/json')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(json.loads(response.data)['status'], 'success')

    def test_garmin_login_missing_credentials(self):
        response = self.app.post('/garmin/login', data=json.dumps({
            'username': 'test@example.com'
        }), content_type='application/json')
        self.assertEqual(response.status_code, 400)
        self.assertEqual(json.loads(response.data)['status'], 'error')

    def test_garmin_status_logged_in(self):
        # api_client is set in setUp
        response = self.app.get('/garmin/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(json.loads(response.data)['status'], 'logged_in')
        self.assertEqual(json.loads(response.data)['username'], 'mockuser')

    def test_garmin_status_logged_out(self):
        garmin_api.api_client = None # Ensure logged out state
        response = self.app.get('/garmin/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(json.loads(response.data)['status'], 'logged_out')

    def test_garmin_logout(self):
        # Ensure logged in state for logout test
        garmin_api.api_client = self.mock_garmin_class()
        response = self.app.post('/garmin/logout')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(json.loads(response.data)['status'], 'success')
        self.assertIsNone(garmin_api.api_client) # Check if api_client is cleared

    def test_garmin_activities_success(self):
        response = self.app.get('/garmin/activities?start=0&limit=2')
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(len(data), 2)
        self.assertEqual(data[0]['activityId'], 1)

    def test_garmin_activities_not_logged_in(self):
        garmin_api.api_client = None # Ensure logged out state
        response = self.app.get('/garmin/activities')
        self.assertEqual(response.status_code, 401)
        self.assertEqual(json.loads(response.data)['status'], 'error')



    def test_garmin_activity_detail_success(self):
        response = self.app.get('/garmin/activity_detail/123')
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(data['activityId'], 123)
        self.assertIn('Mock details', data['detail'])

    def test_garmin_activity_detail_not_logged_in(self):
        garmin_api.api_client = None # Ensure logged out state
        response = self.app.get('/garmin/activity_detail/123')
        self.assertEqual(response.status_code, 401)
        self.assertEqual(json.loads(response.data)['status'], 'error')

    def test_garmin_activity_download_success(self):
        response = self.app.get('/garmin/activity_download/456')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.mimetype, 'application/octet-stream')
        self.assertEqual(response.headers['Content-Disposition'], 'attachment; filename=activity_456.fit')
        self.assertEqual(response.data, b'mock_fit_data_for_456')

    def test_garmin_activity_download_not_logged_in(self):
        garmin_api.api_client = None # Ensure logged out state
        response = self.app.get('/garmin/activity_download/456')
        self.assertEqual(response.status_code, 401)
        self.assertEqual(json.loads(response.data)['status'], 'error')

    def test_garmin_health_summary_success(self):
        response = self.app.get('/garmin/health?cdate=2025-09-22')
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(data['cdate'], '2025-09-22')
        self.assertIn('Mock health summary', data['summary'])

    def test_garmin_health_summary_not_logged_in(self):
        garmin_api.api_client = None # Ensure logged out state
        response = self.app.get('/garmin/health')
        self.assertEqual(response.status_code, 401)
        self.assertEqual(json.loads(response.data)['status'], 'error')

if __name__ == '__main__':
    unittest.main()
