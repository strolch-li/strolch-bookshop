# Strolch Bookshop

Bookshop built on Strolch. This project is the implementation of the [Strolch Tutorial](https://strolch.li/tutorial/).

## Features

In this tutorial project we build a book store using Strolch. The book store has the following features:

*   The store owner can add, update and remove books
*   The store owner can edit the stock quantity
*   Users can view a list of books
*   Users can add books to a virtual cart
*   Users can create and verify an account using an e-mail address
*   Users can submit an order for the books in their cart
*   The store owner can see the orders by state (pending, preparing, sent)
*   The store owner can update the state of an order (preparing, sent)
*   Notify the user when the order is sent

## Getting Started

### Prerequisites

*   Java (JDK 24 recommended)
*   Maven 3.6+

### Build Instructions

To build the project:

```bash
mvn clean install
```

### Running Tests

To run the tests:

```bash
mvn test
```

## Running the Application

The Bookshop is a Strolch-based web application tutorial. It is packaged as a WAR file and can be deployed to any Jakarta EE compatible servlet container (e.g., Tomcat, Jetty).

For development, the project is configured with a local development profile that uses the runtime configuration in the `runtime` directory.

### 🐳 Running with Docker

To run the Bookshop using Docker, follow these steps:

1.  Build the project from the root:
    ```bash
    mvn clean install -DskipTests
    ```
2.  Start the application using Docker Compose:
    ```bash
    docker compose up --build
    ```
3.  The application will be available at `http://localhost:8080/bookshop`.

The Docker configuration uses the `runtime` directory for configuration and data. Any changes made to the `runtime` directory on your host will be reflected in the container.

## Documentation

For more information, see the [Strolch Tutorial](https://strolch.li/tutorial/).
