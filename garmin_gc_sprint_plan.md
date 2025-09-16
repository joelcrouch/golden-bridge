# Garmin to Golden Cheetah Bridge - Complete Project Brief

## **Project Context & Goals**

### **Why This Project?**
This Java application serves as a portfolio project to demonstrate enterprise-level software development skills for a **Java Developer position at the Oregon Department of Justice (Child Support Division)**. The position requires expertise in Java Spring Boot, Angular, database integration, external API integration, authentication, testing, and enterprise patterns.

### **Business Problem Being Solved**
Currently, cyclists using Garmin devices face a manual, error-prone process to import their training data into Golden Cheetah (popular cycling analytics software). This application automates that process with enterprise-grade reliability, authentication, and error handling.

### **Technical Requirements to Demonstrate**
Based on the DOJ job description, this project must showcase:
- ✅ Java Spring Boot enterprise development
- ✅ Angular frontend integration
- ✅ Database design and operations (PostgreSQL)
- ✅ External API integration (OAuth, REST APIs)
- ✅ Authentication and security patterns
- ✅ Batch processing and scheduling
- ✅ **Comprehensive testing strategy (Unit, Integration, API, Frontend)**
- ✅ Error handling and logging
- ✅ Production-ready architecture patterns

### **Testing Requirements (Critical for DOJ Position)**
The DOJ job specifically mentions "unit/regression testing, deployment, and maintenance" as core SDLC activities. This project must demonstrate:
- **80%+ Code Coverage** - JUnit 5 unit tests for all service classes
- **Integration Testing** - TestContainers for database operations
- **API Testing** - MockWebServer for external API interactions
- **Frontend Testing** - Angular unit tests with Jasmine/Karma
- **End-to-End Testing** - Cypress or Selenium for complete user workflows
- **Performance Testing** - Load testing for sync operations
- **Security Testing** - Authentication and authorization validation

---

# Garmin to Golden Cheetah Bridge - Sprint Plan & API Analysis

## API Research Summary

### **Garmin Integration Options (Updated)**

**Primary Choice: python-garminconnect (GitHub: cyberjunky/python-garminconnect)**
- **Advantages:** No business approval required, extensive documentation, active maintenance
- **Capabilities:** Complete access to activities, health metrics, device data, user profile
- **Authentication:** Username/password login (simpler than OAuth for development)
- **Data Access:** FIT files, GPX, TCX, JSON formats with full activity details
- **Community Support:** Well-documented with examples, regular updates

**Implementation Strategy:**
- Use Python wrapper for Garmin Connect authentication and data retrieval
- Create Java REST API that calls Python scripts or use process execution
- Alternative: Port key functionality to Java using HTTP requests (reverse engineering the API calls)

**Note:** While unofficial, this library provides more comprehensive access than waiting for official API approval and is actively maintained with good documentation.

### Official Garmin APIs (Future Enhancement)
**Garmin Connect Developer Program** - Can be added later for production deployment:
- Requires business approval process
- OAuth 2.0 authentication
- Official support and rate limiting

### Golden Cheetah Integration Options
**Golden Cheetah REST API** - Available via GitHub documentation
- Native JSON format for activity data storage
- Supports FIT file imports directly
- REST endpoints for data manipulation

**Data Formats:** Golden Cheetah uses native JSON format but maintains original files (FIT, GPX, TCX) for compatibility

---

## **Project Architecture Overview**

### **Architecture Implementation: Python Script Execution (Option 1)**

**Why This Approach is Perfect for Portfolio:**
- **Enterprise Integration Skills** - Shows ability to integrate existing tools and libraries
- **Polyglot Programming** - Demonstrates Java + Python competency
- **Process Management** - Enterprise-level inter-process communication
- **Real-World Problem Solving** - Uses practical approach developers actually employ
- **Error Handling Complexity** - Managing errors across technology boundaries

**Technical Implementation:**
```
Java Spring Boot App ↔ ProcessBuilder ↔ Python Scripts ↔ python-garminconnect ↔ Garmin Connect
```

**Key Components:**
1. **Java GarminIntegrationService** - Uses ProcessBuilder to execute Python scripts
2. **Python Scripts** - Individual scripts for login, activities, downloads using garminconnect
3. **JSON Communication** - Data exchange between Java and Python via JSON files/stdout
4. **Error Handling** - Comprehensive error management across process boundaries
5. **Session Management** - Python handles Garmin authentication, Java manages application sessions

### **Technology Stack:**
- **Backend:** Java 17+ Spring Boot 3.x, Spring Security, Spring Data JPA
- **Garmin Integration:** Python garminconnect library + Java Process execution OR Java HTTP client (reverse-engineered API calls)
- **Frontend:** Angular 15+, Angular Material, TypeScript
- **Database:** PostgreSQL 14+
- **Authentication:** JWT tokens for frontend, credential storage for Garmin auth
- **Testing:** 
  - **Backend:** JUnit 5, Mockito, TestContainers, WireMock
  - **Frontend:** Jasmine, Karma, Angular Testing Utilities
  - **Integration:** Spring Boot Test, MockMvc, WebTestClient
  - **API Testing:** RestAssured, MockWebServer
  - **E2E Testing:** Cypress or Playwright
  - **Performance:** JMeter, Spring Boot Actuator metrics
- **Build Tools:** Maven, Docker, Docker Compose
- **Documentation:** OpenAPI/Swagger

### **Key Design Patterns & Principles:**
- **Repository Pattern** - Data access abstraction
- **Strategy Pattern** - Multiple data format conversions
- **Circuit Breaker Pattern** - External API reliability
- **Observer Pattern** - Real-time sync status updates
- **SOLID Principles** - Clean, maintainable code architecture

---

## 2-Week Sprint Plan: Garmin to Golden Cheetah Bridge

### **Sprint Overview**
- **Goal:** Production-ready Java application demonstrating enterprise integration patterns
- **Duration:** 10 working days
- **AI Strategy:** Use Claude/Gemini for boilerplate generation, focus on integration logic and business rules
- **Target:** Complete, testable application suitable for technical interviews and portfolio showcase

---

### **Sprint 1: Backend Foundation & Garmin Integration (Week 1)**

#### **Day 1-2: Project Setup & Authentication**
**Goals:**
- Initialize Spring Boot project with security
- Set up Garmin authentication using python-garminconnect
- Database schema design

**Key Deliverables:**
- Spring Boot application structure with proper layering (controller, service, repository)
- Garmin authentication service using python-garminconnect library
- PostgreSQL database with entities: User, GarminCredentials, Activity, SyncHistory, UserPreferences
- Basic security configuration with JWT token management
- Docker Compose setup for local development (including Python environment)

**AI Prompts for Implementation:**
```
"Create a Spring Boot 3.x application that integrates with the python-garminconnect library. Include JPA entities for User, GarminCredentials, Activity, and SyncHistory with proper relationships."

"Generate a Java service that executes Python scripts using the garminconnect library for authentication and data retrieval, with proper error handling and credential management."

"Create a secure credential storage system for Garmin username/password with encryption, and implement JWT-based authentication for the frontend application."
```

#### **Day 3-4: Garmin API Integration**
**Goals:**
- Implement Garmin integration using python-garminconnect library
- Activity data fetching and processing
- Error handling and retry logic

**Key Deliverables:**
- GarminIntegrationService using ProcessBuilder for Python script execution
- Python scripts suite:
  - `garmin_auth.py` - Authentication and session management
  - `garmin_activities.py` - Fetch activity list with date ranges
  - `garmin_activity_detail.py` - Download specific activity with FIT files
  - `garmin_health.py` - Retrieve health metrics and summaries
- JSON-based communication protocol between Java and Python
- Process management with timeout handling and resource cleanup
- Comprehensive error handling with custom exceptions for both Java and Python failures
- **Unit tests for all service methods with 90%+ coverage**
- **Integration tests using mock Python processes and file system operations**

**AI Prompts for Implementation:**
```
"Create a Java service that uses ProcessBuilder to execute Python scripts with the garminconnect library. Handle JSON communication, process timeouts, and error management across language boundaries."

"Generate Python scripts using the garminconnect library for: authentication, activity list retrieval, activity detail fetching, and health data. Output results as JSON for Java consumption."

"Implement comprehensive JUnit 5 tests for the Java-Python integration including unit tests that mock ProcessBuilder and integration tests with actual Python script execution."

"Create robust error handling that manages both Java exceptions and Python script failures, including timeout scenarios, authentication failures, and network issues."
```

#### **Day 5-7: Data Processing & Golden Cheetah Integration**
**Goals:**
- Data transformation pipeline from Garmin to Golden Cheetah formats
- Golden Cheetah API integration
- Initial sync logic with deduplication

**Key Deliverables:**
- DataTransformationService for converting Garmin data to Golden Cheetah JSON
- GoldenCheetahService for API interactions
- SyncService with deduplication logic (avoiding duplicate imports)
- **Comprehensive unit tests for all transformation logic**
- **Integration tests with TestContainers for database operations**
- Database migration scripts

**AI Prompts for Implementation:**
```
"Create a data transformation service that converts Garmin activity data (FIT format) to Golden Cheetah JSON format, preserving all power, heart rate, GPS, and performance data."

"Generate comprehensive JUnit 5 tests for the data transformation service including edge cases, error scenarios, and validation of complex data transformations."

"Implement integration tests using TestContainers for PostgreSQL to verify complete sync workflows from Garmin API through database storage."
```

---

### **Sprint 2: Frontend, Scheduling & Production Polish (Week 2)**

#### **Day 8-9: Angular Frontend Development**
**Goals:**
- User interface for authentication and sync management
- Real-time monitoring dashboard
- Configuration management

**Key Deliverables:**
- Angular application with Material Design components
- OAuth login flow with Garmin Connect
- Dashboard showing sync history, status, and statistics
- Configuration forms for sync preferences and scheduling
- Responsive design working on desktop and mobile
- Real-time updates using WebSocket or Server-Sent Events
- **Angular unit tests with Jasmine/Karma for all components**
- **Frontend integration tests for user workflows**

**AI Prompts for Implementation:**
```
"Create an Angular 15+ application with Angular Material for Garmin to Golden Cheetah sync management. Include OAuth login, sync history table with filtering, and configuration forms."

"Generate comprehensive Angular unit tests using Jasmine and Karma for all components, services, and guards including mock HTTP interceptors and routing tests."

"Build Angular integration tests for complete user workflows including OAuth login, sync configuration, and real-time status monitoring."
```

#### **Day 10-11: Automated Sync & Advanced Features**
**Goals:**
- Background scheduling system
- Advanced error handling and recovery
- Performance optimization and monitoring

**Key Deliverables:**
- Spring @Scheduled jobs for automatic syncing (daily, weekly, custom intervals)
- Exponential backoff retry logic for failed operations
- Circuit breaker implementation for external API calls
- Sync queue management for handling large data volumes
- Performance metrics and health check endpoints
- Email/notification system for sync status updates
- **Performance tests using JMeter for sync operations under load**
- **Security tests for authentication and authorization flows**

**AI Prompts for Implementation:**
```
"Implement Spring @Scheduled jobs for automatic Garmin data sync with configurable intervals, exponential backoff retry logic, and comprehensive error handling with user notifications."

"Create JMeter test plans for load testing the sync operations, including concurrent user scenarios and API rate limit validation."

"Generate security tests using Spring Security Test to validate OAuth flows, JWT token handling, and authorization rules for different user roles."
```

#### **Day 12-14: Comprehensive Testing & Documentation**
**Goals:**
- Multi-layered testing strategy implementation
- Performance and security validation
- Production-ready documentation

**Key Deliverables:**
- **Backend Testing Suite:**
  - JUnit 5 unit tests with 85%+ coverage using Mockito
  - Integration tests with TestContainers for database operations
  - API integration tests with WireMock for external service mocking
  - Spring Boot Test slices (@WebMvcTest, @DataJpaTest, @JsonTest)
- **Frontend Testing Suite:**
  - Angular unit tests with Jasmine/Karma for components and services
  - Angular integration tests for user workflows and routing
  - Mock HTTP interceptors for API testing
- **End-to-End Testing:**
  - Cypress tests for complete user journeys (OAuth → Sync → Dashboard)
  - Cross-browser testing scenarios
- **Performance & Security Testing:**
  - JMeter load tests for sync operations and API endpoints
  - Security tests for authentication, authorization, and input validation
  - Memory and performance profiling
- **Documentation & Deployment:**
  - OpenAPI/Swagger documentation for all REST endpoints
  - Docker containerization with multi-stage builds
  - README with setup, testing, and usage instructions
  - Test coverage reports and performance benchmarks

**AI Prompts for Implementation:**
```
"Generate a comprehensive testing strategy for the Garmin to Golden Cheetah sync application including JUnit 5 unit tests with Mockito, integration tests with TestContainers, and Spring Boot test slices."

"Create Cypress end-to-end tests covering the complete user workflow from OAuth login through sync configuration to real-time monitoring dashboard."

"Implement JMeter performance tests for the sync operations including concurrent user scenarios, API rate limiting validation, and database performance under load."

"Generate security tests using Spring Security Test to validate OAuth 2.0 flows, JWT token handling, CSRF protection, and role-based access control."
```

**Testing Coverage Goals:**
- **Unit Tests:** 85%+ code coverage for all service and utility classes
- **Integration Tests:** All database operations and external API interactions
- **Frontend Tests:** All Angular components, services, guards, and interceptors
- **E2E Tests:** Complete user workflows and error scenarios
- **Performance Tests:** Load testing with 100+ concurrent users
- **Security Tests:** Authentication, authorization, and input validation

---

## **Success Criteria & Portfolio Impact**

### **Technical Demonstration Goals:**
1. **Enterprise Java Skills** - Spring Boot, Spring Security, JPA, proper layering
2. **Frontend Integration** - Angular with TypeScript, Material Design, responsive UI
3. **API Integration Mastery** - OAuth flows, REST client implementation, error handling
4. **Database Design** - Proper normalization, relationships, migration scripts
5. **Testing Excellence** - Multi-layered testing strategy with high coverage
6. **Performance Engineering** - Load testing, optimization, monitoring
7. **Security Implementation** - Authentication, authorization, input validation
8. **Production Readiness** - Docker, monitoring, logging, configuration management

### **Testing Strategy Highlights for Portfolio:**
- **85%+ Code Coverage** - Demonstrating thorough unit testing practices
- **Integration Testing** - TestContainers for realistic database testing
- **API Testing** - WireMock for reliable external service mocking
- **Frontend Testing** - Complete Angular testing with Jasmine/Karma
- **E2E Testing** - Cypress for user workflow validation
- **Performance Testing** - JMeter load testing with metrics
- **Security Testing** - OAuth, JWT, and authorization validation

### **Portfolio Presentation Points:**
- **Problem Complexity** - Real-world integration challenge, not a tutorial project
- **Technical Depth** - Shows advanced Java patterns and enterprise architecture
- **Full-Stack Capability** - Backend services + modern frontend
- **Production Quality** - Comprehensive testing, error handling, monitoring
- **Documentation** - Professional API docs and setup guides

### **Interview Discussion Topics:**
- **Process Management** - How to handle cross-language integration with ProcessBuilder
- **Error Handling Strategies** - Managing failures across Java and Python boundaries
- **JSON Communication Protocols** - Designing data exchange between different technologies
- **Session Management** - Handling Garmin authentication across process boundaries
- **Testing Strategies** - Unit testing process execution and mocking external dependencies
- **Performance Considerations** - Process startup overhead vs. persistent Python services
- **Security Implementation** - Secure credential handling across multiple processes
- **Scalability Considerations** - When to evolve from process execution to microservices

This project directly addresses the DOJ position requirements while solving a genuine problem in the cycling community, making it both technically impressive and practically useful.

---

### **Sprint 1: Backend Foundation & Garmin Integration (Week 1)**

#### **Day 1-2: Project Setup & Authentication**
**Goals:**
- Initialize Spring Boot project with security
- Set up Garmin OAuth 2.0 flow
- Database schema design

**AI Prompts:**
```
"Create a Spring Boot application with Spring Security for OAuth 2.0 integration with Garmin Connect API. Include entities for User, GarminToken, Activity, and SyncHistory."

"Generate OAuth 2.0 configuration for Garmin Connect API with proper token refresh handling and error management."
```

**Deliverables:**
- Basic Spring Boot app structure
- OAuth flow working with Garmin (test environment)
- PostgreSQL database with core entities
- User authentication endpoints

#### **Day 3-4: Garmin API Integration**
**Goals:**
- Implement Garmin API client
- Activity data fetching and parsing
- FIT file handling

**AI Prompts:**
```
"Create a Java service class that integrates with Garmin Connect API to fetch user activities, handle FIT file downloads, and parse activity data with proper error handling."

"Implement FIT file parsing in Java using the Garmin FIT SDK to extract activity metrics like power, heart rate, GPS coordinates."
```

**Deliverables:**
- Garmin API service with full CRUD operations
- FIT file parsing utilities
- Activity data models matching Garmin schema
- Comprehensive error handling and logging

#### **Day 5-7: Data Processing & Golden Cheetah Integration**
**Goals:**
- Data transformation layer
- Golden Cheetah format conversion
- Initial sync logic

**AI Prompts:**
```
"Create a data transformation service that converts Garmin activity data to Golden Cheetah JSON format, including all power, heart rate, and GPS data."

"Implement a Golden Cheetah REST API client in Java for uploading activities and managing athlete data."
```

**Deliverables:**
- Data transformation pipeline
- Golden Cheetah API integration
- Basic sync service with deduplication
- Unit tests for core services

---

### **Sprint 2: Frontend, Scheduling & Production Polish (Week 2)**

#### **Day 8-9: Angular Frontend**
**Goals:**
- User dashboard for sync configuration
- Real-time sync monitoring
- Authentication UI

**AI Prompts:**
```
"Create an Angular application with Material Design for Garmin to Golden Cheetah sync management. Include OAuth login, sync history table, and configuration forms."

"Build Angular components for displaying activity sync status with real-time updates using WebSocket or SSE."
```

**Deliverables:**
- Angular app with routing and authentication
- Dashboard showing sync history and status
- Configuration forms for sync preferences
- Responsive design with error handling

#### **Day 10-11: Automated Sync & Scheduling**
**Goals:**
- Background sync jobs
- Retry logic and error handling
- Performance optimization

**AI Prompts:**
```
"Implement Spring @Scheduled jobs for automatic Garmin data sync with exponential backoff retry logic and comprehensive error handling."

"Create a robust sync engine that handles partial failures, maintains sync state, and provides detailed logging for troubleshooting."
```

**Deliverables:**
- Automated daily/weekly sync jobs
- Advanced error handling with notifications
- Performance monitoring and metrics
- Admin interface for sync management

#### **Day 12-14: Testing & Documentation**
**Goals:**
- Comprehensive testing suite
- API documentation
- Deployment preparation

**AI Prompts:**
```
"Generate comprehensive JUnit tests for the Garmin to Golden Cheetah sync application including integration tests with TestContainers for PostgreSQL."

"Create OpenAPI/Swagger documentation for all REST endpoints with examples and authentication details."
```

**Deliverables:**
- 80%+ test coverage with JUnit/Mockito
- Integration tests with mock APIs
- Complete API documentation
- Docker containerization
- README with setup instructions

---

## **Technical Architecture**

### **Core Components:**
1. **Authentication Service** - OAuth 2.0 with Garmin, JWT for frontend
2. **Garmin Integration Service** - API client, FIT file processing
3. **Data Transformation Engine** - Format conversion, validation
4. **Golden Cheetah Service** - REST API client, file management
5. **Sync Engine** - Scheduling, deduplication, error handling
6. **Web Dashboard** - Angular frontend for configuration/monitoring

### **Data Flow:**
```
Garmin API → FIT File Download → Data Parsing → Format Conversion → 
Golden Cheetah Upload → Sync History Update → User Notification
```

### **Key Design Patterns:**
- **Strategy Pattern** for different data format conversions
- **Observer Pattern** for sync status updates
- **Circuit Breaker** for external API reliability
- **Repository Pattern** for data access abstraction

---

## **Success Metrics & Deliverables**

### **Technical Requirements Coverage:**
- ✅ **Java Spring Boot** - Core application framework
- ✅ **Angular Frontend** - User interface and configuration
- ✅ **Database Integration** - PostgreSQL for sync metadata
- ✅ **External API Integration** - Garmin Connect API + Golden Cheetah
- ✅ **Authentication & Security** - OAuth 2.0, JWT tokens
- ✅ **Scheduling/Batch Processing** - Automated sync jobs
- ✅ **Comprehensive Testing** - JUnit, integration tests
- ✅ **Error Handling & Logging** - Production-ready reliability
- ✅ **Documentation** - API docs, setup guides

### **Demo Scenarios:**
1. **OAuth Flow** - Login with Garmin account
2. **Manual Sync** - Trigger immediate data sync
3. **Automated Sync** - Show scheduled job execution
4. **Error Handling** - Demonstrate retry logic and error recovery
5. **Data Visualization** - Show sync history and activity metrics

### **Portfolio Value:**
- **Real-world Problem Solving** - Addresses genuine user pain point
- **Enterprise Integration** - Shows complex API orchestration
- **Production Considerations** - Authentication, error handling, scheduling
- **Full-Stack Capability** - Java backend + Angular frontend
- **Testing & Documentation** - Professional development practices

This project perfectly demonstrates the enterprise Java development skills required for the DOJ position while solving an actual problem in the cycling community!