# README

# Exam Portal System

## Spring Boot Online Examination Management System

A full-stack web application developed using **Spring Boot**, **Spring MVC**, **Thymeleaf**, **MySQL**, and **Bootstrap** for conducting and managing online examinations.

The system supports multiple user roles including:

* Admin
* Faculty
* Student

It provides subject management, question management, exam handling, dashboards, and student examination workflows.

---

# Quick Start

## Clone Repository

```bash
git clone https://github.com/devivittanala/Exam_Portal.git
```

## Open Project

Import into:

```txt
Spring Tool Suite (STS)
OR
Eclipse IDE
```

As:

```txt
Existing Maven Project
```

---

## Configure MySQL

Create database:

```sql
CREATE DATABASE question_bank;
```

Update database configuration inside:

```txt
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/question_bank
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

---

## Run Application

Run:

```txt
QuestionBankApplication.java
```

OR use:

```bash
mvn spring-boot:run
```

Open:

```txt
http://localhost:8082
```

---

# Project Overview

The Exam Portal System is designed to automate and simplify online examination processes in educational institutions.

The application allows:

* Faculty to create subjects and questions
* Students to attend exams online
* Admins to monitor system activities
* Secure database-based question storage
* Dynamic dashboard management
* Subject-wise examination handling

---

# Technologies Used

| Technology      | Purpose                  |
| --------------- | ------------------------ |
| Java            | Backend programming      |
| Spring Boot     | Application framework    |
| Spring MVC      | MVC architecture         |
| Thymeleaf       | Frontend template engine |
| MySQL           | Database                 |
| Hibernate / JPA | ORM framework            |
| Bootstrap 5     | Responsive UI            |
| Maven           | Dependency management    |
| HTML/CSS        | Frontend design          |

---

# Features

## Admin Module

* Admin dashboard
* Manage students
* Manage faculty
* Monitor system activities
* Database integration
* User management

## Faculty Module

* Faculty login
* Add subjects
* Add questions
* View questions
* Manage examination content
* Subject-wise question handling

## Student Module

* Student registration/login
* View subjects
* Attend online exams
* Student dashboard access
* View examination content

## Question Management

* Add questions dynamically
* Subject-wise questions
* Database storage
* Question viewing functionality
* Question management system

## Dashboard System

* Admin dashboard
* Faculty dashboard
* Student dashboard
* Responsive Bootstrap interface
* Navigation-based UI

---

# Project Structure

```txt
QuestionBank/
├── src/main/java/
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── QuestionBankApplication.java
│
├── src/main/resources/
│   ├── templates/
│   │   ├── admin-dashboard.html
│   │   ├── faculty-dashboard.html
│   │   ├── student-dashboard.html
│   │   ├── login.html
│   │   └── index.html
│   │
│   ├── static/
│   ├── application.properties
│   └── schema.sql
│
├── pom.xml
├── mvnw
└── README.md
```

---

# Modules Description

## Authentication Module

Handles:

* Login
* Registration
* User authentication
* Session management
* Role handling

## Subject Module

Allows faculty to:

* Add subjects
* View subjects
* Manage subjects
* Organize exam categories

## Question Module

Allows:

* Adding questions
* Viewing questions
* Subject-wise organization
* Dynamic database storage
* Examination preparation

## Examination Module

Students can:

* Attend exams
* View subjects
* Submit answers
* Access examination workflows

---

# Frontend Features

* Responsive Bootstrap UI
* Navigation dashboards
* Dynamic tables
* Form validation
* User-friendly interface
* Dashboard-based design

---

# Backend Features

* Spring MVC Controllers
* Service Layer Architecture
* Repository Layer using JPA
* MySQL Integration
* Hibernate ORM
* MVC Pattern Implementation

---

# Application URLs

## Home Page

```txt
http://localhost:8082
```

## Admin Dashboard

```txt
http://localhost:8082/admin
```

## Faculty Dashboard

```txt
http://localhost:8082/faculty
```

## Student Dashboard

```txt
http://localhost:8082/student
```

---

# Screenshots

Add project screenshots here.

## Home Page

```txt
Insert home page screenshot
```

## Faculty Dashboard

```txt
Insert faculty dashboard screenshot
```

## Student Dashboard

```txt
Insert student dashboard screenshot
```

---

# Future Enhancements

* Timer-based examinations
* Automatic result generation
* Email notifications
* AI-based question generation
* Student analytics dashboard
* PDF report generation
* JWT Authentication
* Role-based authorization
* Online result publishing

---

# GitHub Repository

```txt
https://github.com/devivittanala/Exam_Portal
```

---

# Author

## E M SHALINI PRIYA

GitHub:

```txt
https://github.com/devivittanala
```

---

# License

This project is developed for:

* Educational purposes
* Academic learning
* Spring Boot practice
* Online examination management

---

# Contribution

Contributions are welcome.

Steps:

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push branch
5. Create pull request

---

# Conclusion

The Exam Portal System is a complete Spring Boot web application demonstrating:

* MVC architecture
* Database integration
* Online examination workflows
* Dashboard-based management
* Modern frontend design
* Full-stack Java development

This project is suitable for:

* College mini projects
* Major projects
* Portfolio projects
* Spring Boot learning
* Academic demonstrations
