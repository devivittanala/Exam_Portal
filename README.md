# 🎓 Exam_Portal

A full-stack **Online Examination Portal** built using **Spring Boot**, designed to streamline the process of managing online examinations. The application allows administrators to create and manage exams, questions, and users, while students can securely take exams and view their results.

---

## 🚀 Features

### Admin
- User authentication and authorization
- Create, update, and delete exams
- Manage question bank
- Add, edit, and remove questions
- Assign questions to exams
- View student results

### Student
- Secure login
- View available exams
- Attend online examinations
- Submit answers
- View scores and performance

---

## 🛠️ Tech Stack

**Backend**
- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- Hibernate

**Database**
- MySQL

**Build Tool**
- Maven

**Tools**
- Lombok
- Postman
- Git
- GitHub

---

## 📂 Project Structure

```text
Exam_Portal
│
├── src
│   ├── main
│   ├── test
│   └── resources
├── pom.xml
├── README.md
└── mvnw
```

---

## ⚙️ Prerequisites

- Java 17 or later
- Maven
- MySQL
- IntelliJ IDEA / Eclipse / VS Code

---

## 📥 Installation

Clone the repository:

```bash
git clone https://github.com/devivittanala/Exam_Portal.git
```

Navigate into the project:

```bash
cd Exam_Portal
```

---

## 🗄️ Database Configuration

Update your `application.properties` file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/exam_portal
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
```

---

## ▶️ Running the Application

```bash
mvn spring-boot:run
```

or

```bash
mvn clean install
java -jar target/Exam_Portal-0.0.1-SNAPSHOT.jar
```

---

## 🌐 Application URL

```
http://localhost:8080
```

---

## 🔐 Security Features

- Spring Security Authentication
- Role-Based Authorization
- Secure Password Encryption

---

## 🚀 Future Enhancements

- JWT Authentication
- Email Verification
- Timer-Based Exams
- Random Question Generation
- Performance Analytics
- Leaderboard
- PDF Result Download
- Responsive UI

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch

```bash
git checkout -b feature-name
```

3. Commit your changes

```bash
git commit -m "Added new feature"
```

4. Push your branch

```bash
git push origin feature-name
```

5. Open a Pull Request

---

## 👩‍💻 Author

**Vittanala Devi Prasanna**

GitHub: https://github.com/devivittanala

---

## 📄 License

This project is licensed under the MIT License.

---

## ⭐ Support

If you like this project, don't forget to ⭐ the repository!
