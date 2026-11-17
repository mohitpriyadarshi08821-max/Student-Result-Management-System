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

---

## 🚀 Deployment & Web Browser Execution

This project is configured for **CheerpJ WebAssembly** and **jDeploy**, allowing you to run the Java Swing interface directly inside modern web browsers or deploy native desktop installers.

### 1. Run in Your Web Browser Locally (Instant)

Launch the complete Java Swing desktop interface inside Google Chrome, Edge, Firefox, Brave, or Safari:

- **Option A — Quick Launch (Windows):**  
  Double-click **`run-browser.bat`** in the project folder.

- **Option B — Command Line (npm / Node):**
  ```bash
  npm run web
  ```
  Then navigate to **`http://localhost:8080`**.

> **Demo Login Credentials:**  
> - Username: **`admin`**  
> - Password: **`admin123`**  
>  
> The application features a built-in offline demo mode with sample student profiles, courses, GPA calculations, and marksheet generation so all modules work smoothly inside the browser sandbox even without an external MySQL instance.

---

### 2. Cloud Deployment (Open in Any Browser Online)

The project includes pre-configured deployment manifests for the most popular free cloud hosting platforms:

#### A. Deploy to Vercel (1-Command Deploy)
The project includes a ready-to-use [`vercel.json`](file:///vercel.json) configured with the necessary HTTP Range request and Cross-Origin Isolation headers:
```bash
npx vercel
```
*Follow the interactive prompt and your live HTTPS browser link will be generated automatically.*

#### B. Deploy to Netlify
The project includes [`netlify.toml`](file:///netlify.toml) configured for static asset publishing with full streaming support:
```bash
npx netlify deploy --prod
```
*Or drag and drop the `web/` folder directly into your [Netlify App Dashboard](https://app.netlify.com).*

#### C. Deploy to GitHub Pages (Automated CI/CD)
The project includes an automated GitHub Actions workflow at [`.github/workflows/deploy-pages.yml`](file:///.github/workflows/deploy-pages.yml).  
1. Push your repository to GitHub:
   ```bash
   git add .
   git commit -m "Configure web deployment"
   git push origin main
   ```
2. In your GitHub repository: Go to **Settings** > **Pages** > **Build and deployment** > Source: **GitHub Actions**.
3. Every push to `main` automatically builds and deploys your web app to `https://<your-username>.github.io/<repo-name>/`.

---

### 3. Native Desktop Installers with jDeploy

jDeploy creates native platform installers (`.exe` / `.msi` for Windows, `.dmg` for macOS, `.deb` for Linux):

1. **Verify `package.json`:**  
   The project root includes `package.json` pointing to `target/student-result-management-system-1.0.0.jar`.

2. **Publish via jDeploy CLI:**
   ```bash
   npx jdeploy publish
   ```

3. **Automated CI/CD via GitHub Actions:**  
   The workflow at [`.github/workflows/jdeploy.yml`](file:///.github/workflows/jdeploy.yml) automatically compiles, tests, packages, and publishes releases whenever you push a version tag (e.g., `git tag v1.0.0 && git push --tags`).


