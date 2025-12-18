## Design Decisions for Payment Gateway

### Layered Architecture

The backend follows a **traditional layered architecture**, providing clear separation of concerns:

* **Controller layer**: Handles HTTP requests, maps inputs and outputs, and manages response codes.
* **Service layer**: Implements business logic, orchestrates validation and external communication, and determines outcomes.
* **Repository layer**: Manages persistence independently of API models, ensuring decoupling and flexibility.

This design makes the system easier to maintain, extend, and test.

---

### Model Layer

Data transfer objects (DTOs) are implemented as **Java `record`s**:

* Reduces boilerplate by automatically providing constructors, `toString()`, `equals()`, and `hashCode()`.
* Enforces immutability, which is suitable for payment data where consistency and thread-safety are important.

---

### Validation Strategy

* **Business validation** is implemented in a dedicated `PaymentRequestValidator` class:

  * Returns a **list of errors**, allowing the client to see all reasons a request failed.
  * Avoids throwing exceptions for validation errors because the specification requires a “rejected” response.
  * Safe to expose to merchants, as validation errors do not contain sensitive information.

This approach ensures deterministic and user-friendly feedback.

---

### Bank Communication

The `BankClient` handles communication with an external bank simulator:

* Uses **Spring `RestClient`** for synchronous HTTP requests.
* Exceptions for technical failures (e.g., simulator unavailable) propagate to a **global exception handler**.
* Decoupling via interfaces makes testing easier and allows future replacement of the HTTP client with a reactive or asynchronous implementation if scalability requirements change.

---

### Dependency Injection

* **Constructor-based dependency injection** is used throughout, ensuring immutability and simplifying testing.
* Interfaces are defined for key components like the `BankClient` to facilitate mocking and unit testing.

---

### Repository Layer

* The repository stores internal payment representations (`PaymentResponse`) rather than API DTOs.
* This ensures the persistence model is independent of external API contracts, improving maintainability and future flexibility.

---

### Controller Endpoints

* All endpoints are versioned under `/api/v1` to allow backward-compatible changes in the future.
* Controllers delegate validation and business logic to services and validators, adhering to **single responsibility principles**.

---

### Testing Strategy

* **Unit tests** cover validation, service logic, repository operations, and controller behavior.
* **Integration tests** verify end-to-end functionality against a running simulator, ensuring the API, validation, service orchestration, and responses work as expected.

---

### Summary of Design Philosophy

* **Single Responsibility Principle**: each layer and class has a clear, isolated responsibility.
* **Clean, minimal boilerplate**: using `record`s, constructor DI, and Spring features reduces repetitive code.
* **Explicit error handling**: lists for validation errors and exceptions for technical failures provide clarity.
* **Testability and maintainability**: interfaces and layered design simplify testing and future development.
* **Future-proof foundation**: while synchronous handling suffices for current requirements, the architecture allows easy adoption of reactive or asynchronous processing later.
