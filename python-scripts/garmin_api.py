from flask import Flask, request, jsonify, send_file
from garminconnect import (
    Garmin,
    GarminConnectAuthenticationError,
    GarminConnectConnectionError,
    GarminConnectTooManyRequestsError
)

import io

app = Flask(__name__)

# This is a simple, in-memory way to store the API client.
# In a production app, you might use a more robust session management solution.
api_client = None

@app.route('/garmin/login', methods=['POST'])
def login():
    global api_client
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'status': 'error', 'message': 'Username and password required'}), 400

    username = data['username']
    password = data['password']

    try:
        # Initialize the Garmin client
        api_client = Garmin(username, password, user_agent="com.garmin.android.apps.connectmobile")
        # Attempt to log in
        api_client.login()

        return jsonify({'status': 'success', 'message': 'Garmin login successful'})

    except Exception as e:
        api_client = None
        return jsonify({'status': 'error', 'message': str(e)}), 401

@app.route('/garmin/status', methods=['GET'])
def status():
    if api_client and api_client.username:
        return jsonify({'status': 'logged_in', 'username': api_client.username})
    else:
        return jsonify({'status': 'logged_out'})

@app.route('/garmin/logout', methods=['POST'])
def logout():
    global api_client
    if api_client:
        api_client.logout()
        api_client = None
    return jsonify({'status': 'success', 'message': 'Garmin logout successful'})



@app.route('/hello', methods=['GET'])
def hello():
    name = request.args.get('name', 'World')
    return f"Hello, {name} from Python!"



@app.route('/garmin/activities', methods=['GET'])
def get_activities():
    if not api_client or not api_client.username:
        return jsonify({'status': 'error', 'message': 'Not logged in'}), 401

    try:
        start = request.args.get('start', 0, type=int)
        limit = request.args.get('limit', 10, type=int)

        activities = api_client.get_activities(start, limit)

        return jsonify(activities)

    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/garmin/activity_detail/<int:activity_id>', methods=['GET'])
def get_activity_detail(activity_id):
    if not api_client or not api_client.username:
        return jsonify({'status': 'error', 'message': 'Not logged in'}), 401

    try:
        activity_details = api_client.get_activity_details(activity_id)
        return jsonify(activity_details)
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/garmin/activity_download/<int:activity_id>', methods=['GET'])
def download_activity(activity_id):
    if not api_client or not api_client.username:
        return jsonify({'status': 'error', 'message': 'Not logged in'}), 401

    try:
        fit_data = api_client.download_activity(activity_id, dl_fmt=api_client.ActivityDownloadFormat.FIT)
        return send_file(
            io.BytesIO(fit_data),
            mimetype='application/octet-stream',
            as_attachment=True,
            download_name=f'activity_{activity_id}.fit'
        )
    except Exception as e:
        print(f"Error in download_activity: {e}")
        return jsonify({'status': 'error', 'message': str(e)}), 500




if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)
