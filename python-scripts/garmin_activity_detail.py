
from garminconnect import Garmin

def download_fit_file(garmin: Garmin) -> bytes:
    """
    Downloads the FIT file for a given activity ID.

    Args:
        garmin: The authenticated Garmin client.
        activity_id: The ID of the activity to download.

    Returns:
        The FIT file data as bytes, or None if an error occurs.
    """
    try:
        print(f"Attempting to download FIT file for activity {activity_id}...")
        fit_data = garmin.download_activity(
            activity_id, dl_fmt=Garmin.ActivityDownloadFormat.FIT
        )
        print(f"Successfully downloaded FIT file for activity {activity_id}.")
        return fit_data
    except Exception as e:
        print(f"Error downloading FIT file for activity {activity_id}: {e}")
        return None
