#!/bin/bash
cd "$(dirname "$0")"
docker-compose up -d
cd tb-automation
./mvnw spring-boot:run
