# Setup Guide for School Management System

## Quick Start

1. **Extract the ZIP file** to your desired location

2. **Database Setup**
   - Install MySQL 8.0 or higher
   - Create a database named 'school_management_system'
   - Run the SQL script: src/main/resources/database/schema.sql

3. **Configure Database Connection**
   - Edit src/main/resources/application.properties
   - Update database URL, username, and password

4. **Import into IntelliJ IDEA**
   - Open IntelliJ IDEA
   - File → Open → Select the extracted project folder
   - Wait for Maven to import dependencies

5. **Run the Application**
   - Navigate to src/main/java/com/schoolmanagementsystem/Main.java
   - Right-click and select "Run 'Main'"

## Default Database Configuration

```
Database Name: school_management_system
Default URL: jdbc:mysql://localhost:3306/school_management_system
Default Username: root
Default Password: password
```

## Features Included

✅ Student Management with full CRUD operations
✅ Advanced Scheduling using Greedy Algorithm
✅ Student-Course Graph Analysis
✅ MySQL Database Integration with Connection Pooling
✅ Professional Swing GUI Interface
✅ Comprehensive Logging
✅ Unit Testing Framework

## System Requirements

- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- 4GB RAM minimum
- IntelliJ IDEA (recommended)

## Troubleshooting

If you encounter any issues:
1. Verify Java version: java -version
2. Check MySQL service is running
3. Confirm database credentials in application.properties
4. Review application logs for error details

Happy coding! 🎓
