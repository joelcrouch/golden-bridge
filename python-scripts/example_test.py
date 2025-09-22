print("Hello from example_test.py")
import datetime
import json
import logging
import os
import sys

import requests

from garminconnect import (
    Garmin,
    GarminConnectAuthenticationError,
    GarminConnectConnectionError,
    GarminConnectTooManyRequestsError,
)

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Load credentials from environment variables
email = os.getenv("EMAIL")
password = os.getenv("PASSWORD")
api = None

def init_api(email, password):
    """Initialize Garmin API with your credentials."""
    try:
        print("Attempting to log in to Garmin Connect with provided credentials...")
        api = Garmin(email, password)
        api.login()
        print("Garmin Connect login successful!")
        return api
    except (
        GarminConnectConnectionError,
        GarminConnectAuthenticationError,
        GarminConnectTooManyRequestsError,
        requests.exceptions.HTTPError,
    ) as err:
        logger.error("Error occurred during Garmin Connect communication: %s", err)
        return None

if __name__ == '__main__':
    if not email or not password:
        print("Error: EMAIL and PASSWORD environment variables must be set.")
        sys.exit(1)

    api = init_api(email, password)

    if api:
        try:
            # Attempt to fetch some basic data to confirm login
            print("\n--- Fetching Full Name ---")
            full_name = api.get_full_name()
            print(f"Full Name: {full_name}")

            print("\n--- Fetching Last Activity ---")
            last_activity = api.get_last_activity()
            print(f"Last Activity: {json.dumps(last_activity, indent=4)}")

            print("\n--- Logging out ---")
            api.logout()
            print("Logout successful.")

        except (
            GarminConnectConnectionError,
            GarminConnectAuthenticationError,
            GarminConnectTooManyRequestsError,
            requests.exceptions.HTTPError,
        ) as err:
            logger.error("Error occurred during data fetch or logout: %s", err)
    else:
        print("API initialization failed. Cannot proceed with data fetch.")
    sys.exit(0)
