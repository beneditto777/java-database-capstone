# Smart Clinic Management System Architecture

## Section 1: Architecture Summary
The Smart Clinic Management System is built on a robust three-tier Spring Boot architecture. For the presentation layer, it employs a hybrid approach: Spring MVC with Thymeleaf templates renders server-side HTML for the Admin and Doctor dashboards, while RESTful APIs handle client-server communication for other modules like appointments and patient records. 

At the data tier, the application leverages a dual-database strategy. It connects to a MySQL database to manage structured, relational data (such as patients, doctors, appointments, and admins) using JPA entities. Simultaneously, it utilizes MongoDB for flexible, unstructured document-based data (such as prescriptions). All incoming requests from the controllers are centralized through a unified Service layer. This layer enforces business logic and validations before delegating data operations to the corresponding MySQL or MongoDB repositories.

## Section 2: Flow of Data and Control
1. **User Interaction:** A user interacts with the application via a web browser (accessing Thymeleaf-rendered dashboards) or a client application (consuming REST APIs for appointments or patient data).
2. **Controller Routing:** The HTTP request is received and routed by the backend. Thymeleaf Controllers handle requests for HTML views, while REST Controllers process API inputs and prepare JSON responses.
3. **Service Layer Delegation:** The controller passes the request to the central Service Layer, where all business rules, validations, and application logic are executed.
4. **Repository Communication:** To read or write data, the Service Layer calls the Repository Layer, abstracting away the direct database connection details.
5. **Database Execution:** The repositories interface directly with the database engines. Relational data requests are sent to MySQL, while document-based requests are routed to MongoDB.
6. **Model Binding:** Data retrieved from the databases is mapped into Java model classes. MySQL data is converted into JPA `@Entity` objects, and MongoDB data is loaded into `@Document` objects.
7. **Response Generation:** The bound models are returned to the presentation layer. For MVC flows, the data populates Thymeleaf templates to render dynamic HTML. For REST flows, the models are serialized into JSON and sent back to the client as an HTTP response.
