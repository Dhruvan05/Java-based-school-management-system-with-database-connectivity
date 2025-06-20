# 🎓 School Management System

A comprehensive **Java-based desktop application** for managing all aspects of a school — including students, teachers, scheduling, course graphs, and more — powered by **MySQL** and a professional Swing GUI.

---

## 🚀 Features

* 👨‍🎓 **Student Management:** Add, update, delete, and list student records.
* 👩‍🏫 **Teacher Management:** Assign teachers to courses with full CRUD functionality.
* 📅 **Smart Scheduling:** Efficient course scheduling using **Greedy algorithms**.
* 📊 **Course Graph Analysis:** Visualize student-course relationships using graphs.
* 🛢️ **MySQL Integration:** Robust JDBC connectivity with **HikariCP** pooling.
* 🖥️ **Sleek Desktop GUI:** Built with **Java Swing** for a rich user interface.
* 🧪 **Unit Testing:** Structured with **JUnit 5** for quality assurance.
* 📝 **Logging:** Integrated with **SLF4J + Logback** for detailed logs.

---

## 🗂️ Project Structure

```
school-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/schoolmanagementsystem/
│   │   │       ├── Main.java
│   │   │       ├── algorithm/      # Scheduling and graph logic
│   │   │       ├── dao/            # Database access
│   │   │       ├── model/          # Data models (Student, Course, Teacher)
│   │   │       ├── service/        # Business logic
│   │   │       ├── util/           # Utility classes (DB, Logger, Validator)
│   │   │       └── view/           # Java Swing GUI panels
│   ├── resources/
│   │   ├── application.properties  # DB config
│   │   └── school_management_schema.sql  # Full DB schema
│   └── test/java/                  # JUnit tests
├── pom.xml                         # Maven project file
├── SETUP.md                        # Setup instructions
└── target/                         # Build artifacts
```

---

## 🛠️ Getting Started

### 1️⃣ Database Setup

* Install **MySQL 8.0+**
* Create the database: `school_management_system`
* Import the schema:

  ```bash
  mysql -u root -p < src/main/resources/school_management_schema.sql
  ```

### 2️⃣ Application Configuration

Update your DB settings in `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/"db_name"
db.username="db_uname"
db.password="db_pwd"
db.driver=com.mysql.cj.jdbc.Driver
```

### 3️⃣ Run the Project

* Open the project in **IntelliJ IDEA** or **Eclipse**
* Use Maven to import dependencies
* Run `Main.java` to launch the app

---

## 📦 Dependencies

* Java 11+
* MySQL 8.0+
* Maven 3.6+
* HikariCP – Fast JDBC connection pooling
* Apache Commons Lang – Utility functions
* Jackson – JSON handling
* SLF4J + Logback – Logging
* JUnit 5 – Testing framework
* Java Swing – GUI

---

## 🧩 Key Modules

| Module       | Description                            |
| ------------ | -------------------------------------- |
| `dao/`       | All DB interactions (CRUD)             |
| `model/`     | POJO representations of DB entities    |
| `view/`      | GUI forms and visualizations           |
| `algorithm/` | Scheduling & graph logic               |
| `service/`   | Business logic and controller layer    |
| `util/`      | Common utilities: DB, validators, etc. |

---

## ✅ Sample UI

* 👁️ Course Form with dropdown for teacher assignment
* 🔗 Graphical student-course relationship view
* ⏱️ Schedule visualizer using Greedy algorithm

---

## ⚙️ System Requirements

* 🖥️ Java 11 or higher
* 🗃️ MySQL 8.0 or higher
* 📦 Maven 3.6+
* 🧠 Minimum 4GB RAM
* 💡 IntelliJ IDEA (recommended)

---

## 🧪 Testing & Debugging

* Run JUnit tests from `src/test/java`
* Enable logging in `logback.xml`
* Check DB connection via logs or console
* Use breakpoints for GUI event debugging

---

## 🐞 Troubleshooting

| Issue                   | Solution                                   |
| ----------------------- | ------------------------------------------ |
| Java not found          | Run `java -version` to verify installation |
| MySQL connection failed | Check credentials and DB running status    |
| GUI not launching       | Ensure all dependencies are resolved       |
| Schema errors           | Re-run the SQL script to reset the DB      |

---

## 📄 License

This project is licensed under the **MIT License**.
Feel free to use, modify, and distribute.

---

### ✨ Final Words

> “Education is the passport to the future, for tomorrow belongs to those who prepare for it today.” – Malcolm X

**🎉 Happy Coding & Learning!**
