# --- Variables Globales ---
.DEFAULT_GOAL := help

# --- Colores y Formato para la ayuda ---
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
WHITE  := $(shell tput -Txterm setaf 7)
CYAN   := $(shell tput -Txterm setaf 6)
RESET  := $(shell tput -Txterm sgr0)

.PHONY: all help install dev test db-migrate lint clean

##@ General

help:  ## Muestra este mensaje de ayuda
	@echo ''
	@echo 'Usage:'
	@echo '  ${YELLOW}make${RESET} ${GREEN}<target>${RESET}'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*##"; printf "\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  ${YELLOW}%-20s${GREEN}%s${RESET}\n", $$1, $$2 } \
		/^##@/ { printf "\n${CYAN}%s${RESET}\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Instalación

install: install-dev install-test install-prod ## Instala todas las dependencias (Prod, Dev y Test)

install-prod: ## Instala solo dependencias de producción
	pip install -e .

install-dev: ## Instala dependencias de desarrollo
	pip install -e ".[dev]"

install-test: ## Instala dependencias de test
	pip install -e ".[test]"

##@ Desarrollo

dev: ## Levanta el servidor de desarrollo con hot-reload
	uvicorn src.main:app --reload

formatter: ## Ejecuta el linter (Ruff) y el formateador.
	ruff check . --fix
	ruff format .

typecheck: ## Verifica tipos con mypy y pyrefly
	pyrefly src
	mypy src

clean: ## Limpia archivos caché, builds y artefactos de Python
	find . -type f -name '*.pyc' -delete
	find . -type d -name '__pycache__' -delete
	rm -rf build/
	rm -rf dist/
	rm -rf .pytest_cache/
	rm -rf *.egg-info

##@ Base de Datos

seed-db: ## Pobla la base de datos con scripts de semilla y permisos
	python -m src.scripts.seed
	python -m src.scripts.seed_permissions

migrate: ## Ejecuta las migraciones pendientes (Alembic upgrade head)
	alembic upgrade head

createmigration: ## Crea una nueva migración. Uso: make createmigration arg1="mensaje"
	@if [ -z "$(arg1)" ]; then echo "${YELLOW}Error: Falta el mensaje. Uso: make createmigration arg1=\"mensaje\"${RESET}"; exit 1; fi
	alembic revision --autogenerate -m "$(arg1)"

##@ Testing & Deploy

test: ## Ejecuta los tests con Pytest
	pytest

docker-build: ## Construye la imagen de Docker (medbot:latest)
	docker build -t medbot:latest .
