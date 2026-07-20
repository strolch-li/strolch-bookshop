# Strolch Bookshop Web UI

This document provides a technical overview of the vanilla JavaScript web UI for the Strolch Bookshop.

## Overview

The Web UI is a single-page application (SPA) built using standard HTML5, CSS3, and vanilla JavaScript (ES6+). It interacts with the Strolch Bookshop REST API to provide book management capabilities.

## Architecture

- **Frontend**: Standard web technologies (HTML/CSS/JS). No external frameworks or libraries are used to keep it simple and lightweight.
- **Backend**: Strolch-based Java REST API.

## Key Components

### 1. Authentication
Authentication is handled via the standard Strolch authentication resource.
- **Endpoint**: `POST rest/strolch/authentication`
- **Mechanism**: The user provides a username and password. The password is Base64 encoded before being sent in the JSON payload.
- **Token Management**: On successful login, an `authToken` is returned and stored in the browser's `localStorage`. This token is sent in the `Authorization` header for all subsequent API calls.

### 2. Book Management
The UI provides CRUD operations for books.
- **Listing**: Fetches books from `GET rest/books`. Supports search queries and pagination (`offset` and `limit`).
- **Creation**: Sends a `POST` request to `rest/books` with the book data in flat JSON format.
- **Updating**: Sends a `PUT` request to `rest/books/{id}` with the updated book data.
- **Deletion**: Sends a `DELETE` request to `rest/books/{id}`.

### 3. UI Structure
- `index.html`: Defines the layout, including login, book list, and book form sections.
- `style.css`: Contains all styling for the application, using a modern, responsive design based on a clean Indigo color palette and card-based layout.
- `app.js`: Contains all the logic for API interaction, DOM manipulation, and state management.
- `assets/`: Contains image assets like the Strolch logo and mascot.
- `favicon.ico`: The browser favicon for the application.

## File Locations
All web resources are located in the `src/main/webapp` directory of the `strolch-bookshop` module:
- `strolch-bookshop/src/main/webapp/index.html`
- `strolch-bookshop/src/main/webapp/style.css`
- `strolch-bookshop/src/main/webapp/app.js`
- `strolch-bookshop/src/main/webapp/assets/`
- `strolch-bookshop/src/main/webapp/favicon.ico`

## How to use
Once the application is running, the UI is available at the root context:
`http://localhost:8080/bookshop/index.html` (or simply `http://localhost:8080/bookshop/` if the server is configured to serve `index.html` as a welcome file).
