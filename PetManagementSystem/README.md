# Pet Management API

A Spring Boot 3 application to manage users and their pets, built with Java 21, H2 database, and REST APIs. This project fulfills the requirements of Assignment 1, providing endpoints to manage users, pets, and their relationships, with full test coverage and Swagger documentation.

## Table of Contents
- [Overview](#overview)
- [API Endpoints](#api-endpoints)
- [MCD Design](#mcd-design)
- [Technology Choices](#technology-choices)
- [Challenges and Solutions](#challenges-and-solutions)
- [Setup Instructions](#setup-instructions)
- [Future Enhancement](#future-enhancement)
- [Swagger Documentation](#swagger-documentation)
- [H2 Database](#h2-database)

## Overview
This application manages users and their pets, with support for:
- Creating and updating users and pets.
- Assigning pets to users with address-based constraints.
- Retrieving pets by user, city, pet type, or owner gender.
- Handling homonyms (users with identical names) via unique IDs.
- Marking users or pets as deceased.
  -  `Making a soft delete will keep the record(just for this POC).But design decision can be made appropriately by discussing with stake holders`.
  

## API Endpoints
The following REST endpoints were implemented:
## User specific endpoints
- `POST /api/users` - Create a user (201 Created).
- `PUT /api/users/{id}` - Update user (200 OK).
- `PATCH /api/users/{id}/decease` - Mark user as deceased (200 OK).
- `POST /api/users/{userId}/pets/{petId}` - Assign a pet to a user (200 OK).
- `DELETE /api/users/{userId}/pets/{petId}` - Marks the pet as deceased rather than physically deleting. Maintains referential integrity while logically removing the pet.
- `GET /api/users?petType={type}&city={city}` - Get users by pet type and city (200 OK).
- 
## Pet specific endpoints
- `POST /api/PETS` - Create a Pet (201 Created).
- `PUT /api/pets/{id}/deceased` - Mark pet as deceased (200 OK).
- `GET /api/users/{id}/pets` - Get pets for a user (200 OK).
- `PATCH /api/pets/{id}` - Update pet (200 OK).
- `GET /api/pets?by-city={city}` - Get pets by city (200 OK).
- `GET /api/pets/by-women-in-city?city={city}}&page=0&size=10` - Get pets by owner gender and city (200 OK).



**Justification**: The endpoints cover all required operations, with separate `POST` for creation, `GET` for retrieval, `PUT` for updates, and `PATCH` for partial updates (e.g., marking as deceased). 
**This aligns with REST best practices, using `201 Created` for resource creation and `200 OK` for successful retrievals/updates.

## MCD Design
The entity-relationship model includes:
- **User**: Fields (`id`, `name`, `firstName`, `age`, `gender`, `isAlive`, `address`), with a many-to-one relationship to `Address` and many-to-many with `Pet`.
- **Pet**: Fields (`id`, `name`, `age`, `type`, `isAlive`, `address`), with a many-to-one relationship to `Address` and many-to-many with `User`.
- **User_Pet**: Fields (`user_id`,`pet_id`), resultant table of User<->Pet manyToMany relationship
- **Address**: Fields (`id`, `city`, `type`, `addressName`, `number`), with a unique constraint on fields to ensure distinct addresses.

**Justification**:
- A many-to-many relationship between `User` and `Pet` allows multiple owners per pet, with a business rule ensuring all owners share the pet’s address.
- `Address` as a separate entity enables sharing across users and pets, reducing data duplication.
- `deceased` field handles "death" without deleting records, preserving historical data.This is to handle soft delete

## Technology Choices
- **Java 21**: Used records for DTOs (`UserDTO`, `PetDTO`) to reduce boilerplate and leverage modern features.(Tried to use as much as possible within time limit)
- **Spring Boot 3**: Provides robust REST and JPA support, with auto-configuration for H2.
- **H2 Database**: In-memory for testing, file-based for persistence if needed.
- **Lombok**: Reduces boilerplate for getters/setters in entities.
- **Springdoc OpenAPI**: Generates Swagger UI for API documentation (accessible at `/swagger-ui.html`).
- **JUnit 5 and MockMvc**: For unit and integration tests, ensuring full coverage.

**Justification**: These technologies enhance development efficiency and meet assignment requirements. Lombok and Springdoc were added for productivity and API usability, respectively.

## Challenges and Solutions
1. **Challenge**: Ownership management between User and Pet as entities and maintain N+1 issue.
   - **Solution**: Implemented explicit join table for flexible many-to-many relationships.
2. **Challenge**: Handle homonyms while creating user as well as removing pets underneath that user..
   - **Solution**: Implemented by comparing fetching of active user's address and address that passed from the Dto   
3. **Challenge**: `BeanDefinitionOverrideException` for `jpaAuditingHandler` during `mvn clean install`.
    - **Solution**: Added `spring.main.allow-bean-definition-overriding=true` in `application-test.properties` to allow bean overriding in tests. Disabled auditing (`spring.jpa.auditing.enabled=false`) as it wasn’t used.
4. **Challenge**: Test failures due to Spring Security auto-configuration.
    - **Solution**: Excluded security auto-configurations in tests using `@TestPropertySource` with `spring.autoconfigure.exclude`.
5. **Challenge**: Tests passed in IDE but failed in Maven build.
    - **Solution**: Used `@DirtiesContext` to reset the application context and ensured consistent H2 configuration (`create-drop` mode).

## Future Enhancement
   - **Add spring security feature for authentication and authorization to endpoints.Example: User can be created,Update by user with Admin rights**
   - **More validation scenarios can be added at each layers**
   - **Entity relationship diagrams,UML,sequence diagrams,class diagrams**
   - **Efficient queries**
   - **Conflict resolution.Meaning only one user can perform edit,delete pet,user.**
     - *This can be implemented by adding locking level strategies at column levels*
   - **To achieve scalability i.e. efficiency in bulk data retrival,persistence,removal**
     - *Can be achieve by implementing proper indexing,partitioning ,add pagination at UserService as well *
   - **Domain-Specific exception.**
     - *Example: For DAO/Repo level we can throw <ClassName>DAOException. *
     - *Example: For Service level we can throw <ClassName>ServiceException. *
   - **Improved swagger** documentation by adding more details about each and each attribute of method,class
   - **Cloud deployment** We can include changes for cloud deployment on AKS or Azure webapp.
   - **AI-Driven Endpoint Detection:** Explore AI-based tools to automatically detect and document endpoints,requires further research.


## Swagger Documentation:
    - Once system is up swagger can be found here http://localhost:8080/swagger-ui/index.html#/

## H2 Database
    - http://localhost:8080/h2-console
