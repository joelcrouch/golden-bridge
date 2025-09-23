from functools import wraps
from flask import jsonify

def garmin_api_wrapper(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Access api_client from the global scope of the module where the decorated function is defined
        # This assumes api_client is a global variable in garmin_api.py
        global_api_client = f.__globals__.get('api_client')

        if not global_api_client or not global_api_client.username:
            return jsonify({'status': 'error', 'message': 'Not logged in'}), 401
        try:
            return f(*args, **kwargs)
        except Exception as e:
            print(f"Error in {f.__name__}: {e}")
            return jsonify({'status': 'error', 'message': str(e)}), 500
    return decorated_function
