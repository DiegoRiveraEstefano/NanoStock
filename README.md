# NanoStock

Lightweight household inventory, pantry management, and grocery planning system built with Spring Boot and a modular architecture.

## Features

- **Product Catalog**: Canonical product registry, categories, and optical barcode scanner support.
- **Inventory & FEFO Tracking**: Batch tracking, First-Expired-First-Out consumption, stock-level thresholds, waste logging, and audit history.
- **Shopping & Purchases**: Smart replenishment lists, touch-friendly mobile supermarket mode, and receipt capture.
- **Recipe Engine**: "What can I cook?" feasibility matcher, dynamic ingredient scaling, and automated stock deduction.
- **Analytics & Notifications**: Stockout predictions, autonomy estimation, waste cost tracking, and expiry alerts.
- **Data Management**: Bulk import and export supporting CSV, Excel (`.xlsx`), and JSON with dry-run validation.

## Tech Stack

- **Backend**: Java 21, Spring Boot 4, Spring Modulith, Spring Security, Spring Batch, Spring Shell.
- **Database & Persistence**: SQLite, Spring Data JPA, Flyway.
- **Frontend**: Java Template Engine (JTE), Bulma CSS, HTMX, Native Web Components (Vanilla JS).
- **Observability**: OpenTelemetry, Micrometer, Grafana LGTM stack (`compose.yaml`).

## Getting Started

### Prerequisites

- Java 21 SDK
- Docker & Docker Compose *(optional, for OpenTelemetry / Grafana)*

### Running the Application

1. Start the application using the Gradle wrapper:
   ```bash
   ./gradlew bootRun
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

### Running Tests

Execute the automated test suite:
```bash
./gradlew test
```

## Architecture

NanoStock is structured as a modular monolith using **Spring Modulith**:
- `iam`: Authentication, authorization, and user profiles.
- `catalog`: Master products and barcode definitions.
- `inventory`: Physical batches, storage locations, stock movements, and FEFO rules.
- `purchases`: Stores, receipts, and shopping lists.
- `recipes`: Recipes, meal plans, and inventory deduction.
- `analytics`: Consumption metrics, stockout forecasting, and waste financial summaries.
- `notifications`: Expiry warnings and threshold alerts.
- `dataimport` / `dataexport`: Batch file ingestion and report generation.

## License

This project is licensed under the [MIT License](LICENSE).
