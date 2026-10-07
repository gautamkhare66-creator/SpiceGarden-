# SpiceGarden

SpiceGarden is a full-stack restaurant ordering and table reservation application. Customers can browse the menu, place an order, book a table, and create an account. The project uses a React frontend and a Java Spring Boot backend.

## Features

- Responsive restaurant website with menu items, categories, availability, and dish images.
- Customer registration, login, and logout using session-based authentication.
- Password storage using Spring Security's BCrypt password encoder.
- Menu browsing and a shopping cart with quantity selection and price totals.
- Order submission with server-calculated totals and validation of menu item availability.
- Table reservations with date, party-size, and guest-detail validation.
- Confirmation views and user-friendly validation and API error messages.
- Sample menu items seeded automatically when the database is empty.

## Technology stack

- **Frontend:** React 18, Vite, and CSS.
- **Backend:** Java 17, Spring Boot 3, Spring MVC, and Spring Security.
- **Persistence:** Spring Data JPA with an H2 file-backed database.
- **Templates:** Thymeleaf for server-rendered pages and confirmations.
- **Build and tests:** Maven, JUnit 5, and Mockito.

## Requirements

- Java 17 or later.
- Maven 3.6 or later (or use the Maven wrapper if available).
- Node.js and npm.

## Run locally

1. Start the backend from the repository root:

   ```sh
   mvn spring-boot:run
   ```

   The Spring Boot server starts at `http://localhost:8080`. On startup, Hibernate creates or updates the database schema and the application seeds the menu if it is empty.

2. In a second terminal, install frontend dependencies and start the Vite development server:

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

3. Open `http://localhost:5173`. Vite proxies `/api` requests to the backend at `http://localhost:8080`.

The H2 database is stored under `data/` in the backend's working directory. Database settings, including the server port, can be changed in [application.properties](src/main/resources/application.properties).

## API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/menu` | List available menu items. |
| `POST` | `/api/orders` | Create an order and its order items. |
| `POST` | `/api/reservations` | Create a table reservation. |
| `GET` | `/api/csrf` | Obtain the CSRF token used by protected state-changing requests. |
| `GET` | `/api/auth/me` | Get the current account or guest status. |
| `POST` | `/api/auth/register` | Register and sign in a customer. |
| `POST` | `/api/auth/login` | Sign in a customer. |
| `POST` | `/api/auth/logout` | Sign out and invalidate the session. |

Orders and reservations are created with status `CONFIRMED`. Invalid requests return a `400` response with a JSON error message; invalid login credentials return `401`. Authentication uses an HTTP session, and the API enforces CSRF protection for state-changing requests.

## Build and test

Build the frontend:

```sh
cd frontend
npm run build
```

Run backend tests and package the application from the repository root:

```sh
mvn test
mvn package
```

## Project structure

```text
.
├── frontend/                 # React and Vite application
├── src/main/java/            # Spring Boot controllers, services, entities, and repositories
├── src/main/resources/       # Application configuration and Thymeleaf templates
└── src/test/java/            # Backend service tests
```

## Current scope and future work

The current implementation supports customer accounts, menu browsing, ordering, and reservations. It does not yet include an administrator dashboard, admin-specific authorization, payment processing, customer order history, or reservation and order management screens. These could be added in future iterations, along with email or phone notifications, inventory management, and additional integration tests.