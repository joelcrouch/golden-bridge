#!/bin/bash

echo "---Bringing down docker containers---"
docker compose down

echo "---Building and starting containers--"
docker compose up -d --build

echo "---Docker Operations complete---"
