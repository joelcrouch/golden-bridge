# Updated Architecture and Sprint Plan

## 1. Overview

This document details the updated architecture of the Golden Bridge project. The original sprint plans (found in `garmin_gc_sprint_plan.md` and `standalone_sprint_plan.md`) have been revised to reflect a more robust and scalable microservice-based architecture.

The core components of the system remain the same:

*   **Java Spring Boot Application:** The main backend application, handling user authentication, data storage, and business logic.
*   **Python Service:** A service for integrating with the Garmin Connect API.
*   **PostgreSQL Database:** The primary data store for the application.
*   **Angular Frontend:** The user interface for the application.

However, the interaction between the Java application and the Python service has been significantly changed.

## 2. Architectural Changes

### Original Architecture: Python Script Execution

The original architecture proposed using Java's `ProcessBuilder` to execute individual Python scripts for each Garmin-related operation (e.g., login, fetch activities).

```
Java Spring Boot App ↔ ProcessBuilder ↔ Python Scripts ↔ python-garminconnect ↔ Garmin Connect
```

**Limitations of this approach:**

*   **Performance:** Starting a new Python process for each operation can be slow and resource-intensive.
*   **State Management:** Maintaining the Garmin Connect session state across multiple script executions is complex.
*   **Error Handling:** Handling errors across the process boundary is cumbersome.
*   **Scalability:** Scaling the Python part of the system is difficult.

### Updated Architecture: Python Microservice

The current architecture replaces the direct script execution with a Python microservice built with **Flask**.

```
Java Spring Boot App ↔ HTTP (REST API) ↔ Python Flask Service ↔ python-garminconnect ↔ Garmin Connect
```

**Key changes:**

*   **Python as a Microservice:** The Python code now runs as a standalone web server, exposing a REST API for Garmin integration. This is evident from the `python-scripts/garmin_api.py` file, which uses the Flask framework to define API endpoints.
*   **HTTP-based Communication:** The Java application communicates with the Python service using HTTP requests, as seen in the `GarminIntegrationService.java` which uses `RestTemplate` to call the Python API. The `python.service.base-url` property in the `application.yml` file configures the address of the Python service.
*   **Centralized Garmin Integration:** All Garmin-related logic is now encapsulated within the Python microservice, which handles authentication, session management, and data retrieval.
*   **No More `ProcessBuilder`:** The `ProcessBuilder` approach has been completely removed from the codebase. The `PythonScriptService` now acts as a client to the Python microservice.

**Advantages of the new architecture:**

*   **Improved Performance:** The Python service is always running, eliminating the overhead of process creation.
*   **Simplified State Management:** The Python service can maintain the Garmin Connect session in memory, simplifying the authentication and session handling logic.
*   **Better Error Handling:** Errors can be communicated between the services using standard HTTP status codes and JSON responses.
*   **Improved Scalability:** The Java and Python services can be scaled independently.
*   **Clear Separation of Concerns:** The new architecture provides a cleaner separation between the core application logic in the Java service and the Garmin integration logic in the Python service.

## 3. Current Implementation Details

*   **Python Service:**
    *   Built with Flask.
    *   Exposes endpoints for `/garmin/login`, `/garmin/status`, `/garmin/logout`, `/garmin/activities`, and `/garmin/activity/<activity_id>/download`.
    *   Uses the `garminconnect` library to interact with the Garmin Connect API.
    *   Runs on port 5001.
*   **Java Service:**
    *   Uses `RestTemplate` to communicate with the Python service.
    *   The `GarminIntegrationService` encapsulates the logic for calling the Python API.
    *   The `python.service.base-url` property is used to configure the address of the Python service.

## 4. Updated Sprint Plan

The original sprint plan remains largely relevant, but the implementation details for the Garmin integration part should be updated to reflect the new microservice architecture.

### Week 1: Backend Foundation & Garmin Integration

*   **Day 1-2: Project Setup & Authentication:** No changes.
*   **Day 3-4: Garmin API Integration:**
    *   **Original:** Implement Garmin integration using `ProcessBuilder` and Python scripts.
    *   **Updated:**
        *   Develop the Python Flask microservice with the necessary API endpoints for Garmin integration.
        *   In the Java application, implement the `GarminIntegrationService` to communicate with the Python microservice using `RestTemplate`.
*   **Day 5-7: Data Processing & Golden Cheetah Integration:** No changes.

### Week 2: Frontend, Scheduling & Production Polish

No major changes are needed for the second week of the sprint. The frontend will interact with the Java backend as originally planned, and the scheduling and other production-readiness tasks are not affected by the change in the backend architecture.

## 5. Conclusion

The shift from direct Python script execution to a microservice-based architecture is a significant improvement to the project. It results in a more performant, scalable, and maintainable system. The updated architecture is a better demonstration of enterprise software development best practices.
