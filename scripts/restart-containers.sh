#!/bin/bash

# Script để xóa hết containers và chạy lại

echo "=== Stopping all containers ==="
docker-compose -f docker-compose.prod.yml down

echo "=== Removing all containers ==="
docker-compose -f docker-compose.prod.yml rm -f

echo "=== Removing stopped containers ==="
docker container prune -f

echo "=== Starting all services ==="
docker-compose -f docker-compose.prod.yml up -d

echo "=== Checking status ==="
docker-compose -f docker-compose.prod.yml ps

echo "=== Done! ==="


