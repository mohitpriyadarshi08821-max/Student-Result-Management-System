# Student Result Management System

A Java Desktop Application built with **Java Swing**, **JDBC**, **MySQL**, and managed using **Maven**.

---

## 🏗️ Architecture & Layered Package Structure

The application adopts a clean, layered architectural pattern ensuring separation of concerns:

```
src/main/java/result/
├── database/        # Database connectivity & initialization
├── model/           # Data models / POJOs representing database entities
├── dao/             # Data Access Objects (CRUD operations via JDBC)
├── service/         # Business logic layer (calculations, validations, authentication)
├── exception/       # Domain-specific custom exceptions
└── ui/              # Java Swing GUI components and application entry point
```

---

## 📦 Package & Class Breakdown

### 1. `result.model`
Contains POJO classes mapping directly to the database entities and report transfer objects.
- **`Student.java`**: Holds student demographic and academic profile (roll number, names, course, semester, email, dob, gender).
- **`Subject.java`**: Holds subject details (subject code, subject name, max marks, passing marks).
- **`Result.java`**: Represents marks obtained by a student in a specific subject along with individual grade.
- **`StudentResultSummary.java`**: Composite report model consolidating a student's marks across all subjects, overall percentage, grade, and pass/fail status.
- **`User.java`**: Represents administrator / teacher credentials for system login.

### 2. `result.database`
Manages JDBC connections and automated schema initialization.
- **`DatabaseConnection.java`**: Thread-safe Singleton managing the MySQL JDBC `Connection` lifecycle.
- **`DatabaseInitializer.java`**: Bootstraps the MySQL database schema and default tables on startup.

### 3. `result.dao`
Data Access Object interface-implementation pairs decoupling persistence logic from business logic.
- **`StudentDAO.java` / `StudentDAOImpl.java`**: CRUD operations and search methods for students.
- **`SubjectDAO.java` / `SubjectDAOImpl.java`**: CRUD operations for academic courses/subjects.
- **`ResultDAO.java` / `ResultDAOImpl.java`**: Persistence and retrieval of subject-wise scores and mark sheets.
- **`UserDAO.java` / `UserDAOImpl.java`**: Retrieval and credential authentication for administrative accounts.

### 4. `result.service`
Encapsulates business rules, input validations, calculations, and security.
- **`StudentService.java`**: Student admission logic, unique roll number checks, profile updates.
- **`ResultService.java`**: Percentage computation, letter grade evaluation, pass/fail decision rules, and marksheet generation.
- **`AuthService.java`**: User login authentication, password verification, and active session management.

### 5. `result.exception`
Specialized runtime and checked exceptions for consistent error handling across the application.
- **`DatabaseException.java`**: Runtime exception wrapping underlying `SQLException`s.
- **`ValidationException.java`**: Checked exception thrown when inputs violate business constraints.
- **`RecordNotFoundException.java`**: Thrown when a queried entity (student/subject/result) is not found.
- **`DuplicateRecordException.java`**: Thrown upon attempting to insert duplicate roll numbers or codes.

### 6. `result.ui`
Java Swing desktop interface components and layout.
- **`MainApplication.java`**: Application launcher configuring the System Look-and-Feel and opening the Login window on the Swing Event Dispatch Thread (EDT).
- **`LoginFrame.java`**: Authentication dialog for administrative access.
- **`DashboardFrame.java`**: Primary desktop window containing tabbed navigation for managing students, entering marks, and viewing results.
- **`StudentManagementPanel.java`**: View/form for adding, editing, deleting, and searching student records.
- **`ResultEntryPanel.java`**: Form for entering marks for enrolled subjects.
- **`ResultViewPanel.java`**: Searchable marksheet viewer displaying formatted results and printable reports.
- **`UIConstants.java`**: Centralized color palette, fonts, and dimensions for UI consistency.

---

## ⚙️ Maven Build & Compilation

To build and compile the project using Maven:
```bash
mvn clean compile
```

To package into an executable JAR:
```bash
mvn clean package
```

