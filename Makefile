DOCKER_COMPOSE = docker-compose

DOCKERFILE_DEV = Dockerfile.dev
DOCKERFILE_PROD = Dockerfile.prod
DOCKERFILE_TEST = Dockerfile.test

SPRING_PROFILE_DEV = dev
SPRING_PROFILE_PROD = prod

all: dev

dev:
	DOCKERFILE=$(DOCKERFILE_DEV) SPRING_PROFILE=$(SPRING_PROFILE_DEV) $(DOCKER_COMPOSE) up --build

prod:
	DOCKERFILE=$(DOCKERFILE_PROD) SPRING_PROFILE=$(SPRING_PROFILE_PROD) $(DOCKER_COMPOSE) up --build

test:
	DOCKERFILE=$(DOCKERFILE_TEST) $(DOCKER_COMPOSE) up --build --abort-on-container-exit
	$(DOCKER_COMPOSE) down -v --remove-orphans

stop:
	$(DOCKER_COMPOSE) down

clean:
	$(DOCKER_COMPOSE) down -v --rmi all --remove-orphans

help:
	@echo "Available commands:"
	@echo "  make dev	- Run the development environment"
	@echo "  make prod   - Run the production environment"
	@echo "  make test   - Run the test environment"
	@echo "  make stop   - Stop all containers"
	@echo "  make clean  - Clean up Docker resources"

.PHONY: all dev prod test stop clean help
