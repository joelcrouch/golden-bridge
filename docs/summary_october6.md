# Golden Bridge Project Summary and Status Report

## 1. Project Description

**Project Goal:** The "Golden Bridge" is a Java-based enterprise application designed to automate the transfer of cycling data from Garmin Connect to the Golden Cheetah analytics software. This project serves as a portfolio piece to demonstrate enterprise-level software development skills, including backend services, frontend integration, database management, and external API integration.

**Problem Solved:** It addresses the manual and error-prone process cyclists face when transferring their training data, aiming to provide a reliable, authenticated, and automated solution.

**Technology Stack:**
*   **Backend:** Java 17+ with Spring Boot 3.x (Spring Security, Spring Data JPA)
*   **Frontend:** Angular 15+ with Material Design
*   **Database:** PostgreSQL 14+
*   **Integration:** A Python Flask API wrapping the `python-garminconnect` library.
*   **DevOps:** Docker, Docker Compose, Maven

## 2. Architecture and Data Flow

The application's architecture was refactored from a direct process execution model to a more robust and scalable service-based (microservice) architecture.

### API and Service Description

*   **Java Spring Boot Application:** The primary application that orchestrates the entire process. It exposes a REST API for the frontend, manages user authentication (for the app itself), and communicates with the Python service.
*   **Python Flask Service:** A dedicated microservice that acts as a bridge to the Garmin Connect platform. It exposes a simple REST API with endpoints for logging into Garmin, checking status, logging out, and fetching activity data. This service encapsulates the `python-garminconnect` library, hiding the complexity of web scraping from the Java application.

### Data Flow

The data moves through the system in a clear, service-oriented path:

1.  **User Interaction (Frontend):** The user initiates a login or data sync request from the Angular web interface.
2.  **Java Backend (Controller):** The request hits a `GarminController` endpoint in the Spring Boot application.
3.  **Java to Python (HTTP Request):** The `GarminIntegrationService` in the Java application makes a REST API call (using `RestTemplate`) to the appropriate endpoint on the Python Flask service (e.g., `POST /garmin/login`).
4.  **Python to Garmin (Web Scraping):** The Python service receives the request and uses the `python-garminconnect` library to interact with Garmin Connect's web interface, performing actions like logging in or fetching activity data.
5.  **Garmin to Python:** Garmin Connect returns the data (e.g., a list of activities) to the Python service.
6.  **Python to Java (HTTP Response):** The Python service packages the data into a JSON response and sends it back to the `GarminIntegrationService`.
7.  **Java Backend (Processing):** The Java application receives the JSON data. The plan is to have it process this data, transform it into the Golden Cheetah format, and store it in the PostgreSQL database.
8.  **Data to User (Frontend):** The Java backend sends a final status response to the frontend, which updates the user interface.

```
+-----------------+      +----------------------+      +------------------------+      +-----------------+
| Angular         |----->| Java Spring Boot App |----->| Python Flask Service   |----->| Garmin Connect  |
| (User Interface)|      | (Orchestrator)       |      | (Garmin API Wrapper)   |      | (Web Platform)  |
+-----------------+      +----------------------+      +------------------------+      +-----------------+
        ^                        |                                |                             |
        |                        |                                |<----------------------------+
        |                        |                                |      (Data returned)
        |                        |<-------------------------------+
        |                        |      (JSON data returned)
        |                        |
        +------------------------+
             (Status update)
```

## 3. Sprint Plan Overview

The project is structured around an intensive 2-week sprint plan.

*   **Sprint 1 (Week 1): Backend Foundation & Garmin Integration**
    *   **Days 1-2:** Project setup, database schema design, and core application security (JWT).
    *   **Days 3-4:** Implement the Garmin integration layer for authentication and data retrieval.
    *   **Days 5-7:** Develop the data transformation pipeline (Garmin to Golden Cheetah) and the initial sync logic with deduplication.

*   **Sprint 2 (Week 2): Frontend, Scheduling & Production Polish**
    *   **Days 8-9:** Develop the Angular frontend for user interaction.
    *   **Days 10-11:** Implement automated/scheduled sync jobs and advanced error handling.
    *   **Days 12-14:** Focus on comprehensive testing (unit, integration, E2E), documentation, and preparing for deployment.

## 4. Current Position in the Sprint Plan

As of the latest log (**September 22, 2025**), the project is in the middle of **Sprint 1** and is slightly behind the original schedule, primarily due to the necessary architectural refactor.

*   **Achievements:**
    *   **Completed:** Most of the "Day 1-2" and "Day 3-4" tasks are complete.
    *   **Architectural Refactor:** Successfully pivoted from a brittle `ProcessBuilder` approach to a stable service-based API architecture.
    *   **Garmin Authentication:** Overcame a major hurdle by successfully implementing and debugging the Garmin login process. The end-to-end flow for authentication and fetching the activity list is functional.
    *   **Testing:** A solid foundation for testing has been established, including repository tests and a testing strategy for the new service-based architecture.

*   **Current Status:** The team is currently working on tasks originally planned for **Day 3-4**.

*   **Immediate Next Steps (Outstanding from Day 3-4 Plan):**
    *   Implement fetching of individual activity details, including FIT files (`garmin_activity_detail.py`).
    *   Implement fetching of health metrics (`garmin_health.py`).
    *   Standardize the JSON communication protocol and error handling between the Java and Python services.
    *   Implement secure credential encryption and storage.
