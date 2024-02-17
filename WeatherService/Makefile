# Include environment variables from .env.test
include .env.test
export

# Define variables for Docker compose and Docker image name
DOCKER_COMPOSE = docker-compose
APP_NAME = my-weather-service

phony: test build-no-cache run build-and-run

# Target to build the Docker image without cache
build-no-cache:
	docker build --no-cache -t $(APP_NAME) .

# Target to run the application
run:
	$(DOCKER_COMPOSE) up --build -d

# Target to cleanup: stop and remove containers, networks, and volumes
cleanup:
	$(DOCKER_COMPOSE) down -v

# Combine both targets to build (without cache) and run
build-and-run: build-no-cache run

test: ## Test all packages
	@go test -v -cover -count=1 -race -shuffle=on -timeout 60s  ./...