-- Create development database
CREATE DATABASE golden_bridge_dev;

-- Create test database  
CREATE DATABASE golden_bridge_test;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE golden_bridge TO golden_bridge_user;
GRANT ALL PRIVILEGES ON DATABASE golden_bridge_dev TO golden_bridge_user;
GRANT ALL PRIVILEGES ON DATABASE golden_bridge_test TO golden_bridge_user;