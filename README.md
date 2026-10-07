# SpiceGarden-
Online Restaurant Ordering and Reservation System
# Spice Garden

Restaurant ordering and table reservations, with a React frontend and a Spring Boot backend.

## Stack

- React 18 and Vite for the responsive customer interface.
- Spring Boot 3, Spring MVC, and Spring Data JPA for the backend and business rules.
- File-backed H2 database by default. MySQL is not configured yet.

## Run locally

Start the backend from the project root:

```sh
mvn spring-boot:run
```

In a second terminal, start the React frontend:

```sh
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite forwards `/api` requests to the Spring Boot server at `http://localhost:8080`.

## API

- `GET /api/menu` returns available menu items.
- `POST /api/orders` creates an order.
- `POST /api/reservations` creates a reservation.

The order and reservation endpoints return `400` with a JSON error message for invalid requests.

## Build

```sh
cd frontend && npm run build
cd .. && mvn clean package
```
