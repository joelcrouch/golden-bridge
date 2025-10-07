#!/bin/bash

echo "--- Bringing down Docker containers and removing volumes ---"
docker compose down -v

echo "--- Rebuilding Docker images without cache ---"
docker compose build --no-cache

echo "--- Bringing up Docker containers ---"
docker compose up -d

echo "--- Verifying running Docker containers ---"
docker ps

echo "--- Verifying all Docker Compose services status ---"
docker compose ps -a

echo "--- Running Maven tests and redirecting output to test_log.txt ---"
mvn -q test > test_log.txt 2>&1

echo "--- Test cycle complete. Check test_log.txt for results. ---"
