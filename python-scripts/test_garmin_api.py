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
docker logs golden-bridge-python
 * Serving Flask app 'garmin_api'
 * Debug mode: off
WARNING: This is a development server. Do not use it in a production deployment. Use a production WSGI server instead.
 * Running on all addresses (0.0.0.0)
 * Running on http://127.0.0.1:5001
 * Running on http://172.18.0.2:5001
Press CTRL+C to quit
devdell2tb@devdell2tb-Precision-3591:~/Projects/golden-bridge$ ./rebuild_docker.sh 
---Bringing down docker containers---
WARN[0000] /home/devdell2tb/Projects/golden-bridge/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion 
[+] Running 3/3
 ✔ Container golden-bridge-postgres  Removed                                                                                            0.3s 
 ✔ Container golden-bridge-python    Removed                                                                                           10.2s 
 ✔ Network golden-bridge_default     Removed                                                                                            0.3s 
---Building and starting containers--
WARN[0000] /home/devdell2tb/Projects/golden-bridge/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion 
WARN[0000] Docker Compose is configured to build using Bake, but buildx isn't installed 
[+] Building 0.3s (13/13) FINISHED                                                                                            docker:default
 => [python-env internal] load build definition from Dockerfile                                                                         0.0s
 => => transferring dockerfile: 382B                                                                                                    0.0s
 => [python-env internal] load metadata for docker.io/library/python:3.11-slim                                                          0.0s
 => [python-env internal] load .dockerignore                                                                                            0.0s
 => => transferring context: 2B                                                                                                         0.0s
 => [python-env 1/7] FROM docker.io/library/python:3.11-slim                                                                            0.0s
 => [python-env internal] load build context                                                                                            0.0s
 => => transferring context: 17.20kB                                                                                                    0.0s
 => CACHED [python-env 2/7] WORKDIR /app                                                                                                0.0s
 => CACHED [python-env 3/7] RUN apt-get update && apt-get install -y     gcc     g++     && rm -rf /var/lib/apt/lists/*                 0.0s
 => CACHED [python-env 4/7] COPY docker/python/requirements.txt .                                                                       0.0s
 => CACHED [python-env 5/7] RUN pip install --no-cache-dir -r requirements.txt                                                          0.0s
 => CACHED [python-env 6/7] RUN mkdir -p /app/python-scripts /app/data                                                                  0.0s
 => [python-env 7/7] COPY python-scripts/ /app/python-scripts/                                                                          0.1s
 => [python-env] exporting to image                                                                                                     0.1s
 => => exporting layers                                                                                                                 0.0s
 => => writing image sha256:fee9665595865d2d81d04cddb111d2814cfd95238652069c15d45a4ea0f1c175                                            0.0s
 => => naming to docker.io/library/golden-bridge-python-env                                                                             0.0s
 => [python-env] resolving provenance for metadata file                                                                                 0.0s
[+] Running 4/4
 ✔ python-env                        Built                                                                                              0.0s 
 ✔ Network golden-bridge_default     Created                                                                                            0.1s 
 ✔ Container golden-bridge-python    Started                                                                                            0.4s 
 ✔ Container golden-bridge-postgres  Started                                                                                            0.4s 
---Docker Operations complete---
devdell2tb@devdell2tb-Precision-3591:~/Projects/golden-bridge$ docker ps 
CONTAINER ID   IMAGE                      COMMAND                  CREATED         STATUS                            PORTS                                       NAMES
3beb916d359e   postgres:14                "docker-entrypoint.s…"   4 seconds ago   Up 4 seconds (health: starting)   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp   golden-bridge-postgres
dd2497c1ec49   golden-bridge-python-env   "python3 /app/python…"   4 seconds ago   Up 4 seconds                      0.0.0.0:5001->5001/tcp, :::5001->5001/tcp   golden-bridge-python
devdell2tb@devdell2tb-Precision-3591:~/Projects/golden-bridge$ docker exec golden-bridge-python pytest /app/python-scripts/test_garmin_api.py
============================= test session starts ==============================
platform linux -- Python 3.11.13, pytest-8.4.2, pluggy-1.6.0
rootdir: /app
collected 11 items

python-scripts/test_garmin_api.py ...........                            [100%]

============================== 11 passed in 0.37s ==============================
devdell2tb@devdell2tb-Precision-3591:~/Projects/golden-bridge$ docker logs golden-bridge-python
 * Serving Flask app 'garmin_api'
 * Debug mode: off
WARNING: This is a development server. Do not use it in a production deployment. Use a production WSGI server instead.
 * Running on all addresses (0.0.0.0)
 * Running on http://127.0.0.1:5001
 * Running on http://172.18.0.2:5001
Press CTRL+C to quit
devdell2tb@devdell2tb-Precision-3591:~/Projects/golden-bridge$ 


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

if __name__ == '__main__':
    unittest.main()
