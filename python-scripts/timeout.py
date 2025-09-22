import time
import sys

# Sleep for a number of seconds passed as an argument, or 5 by default
sleep_time = int(sys.argv[1]) if len(sys.argv) > 1 else 5
time.sleep(sleep_time)
