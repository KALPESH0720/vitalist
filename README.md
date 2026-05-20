# Hospital Management System

A full-stack desktop hospital management system with Role-Based Access Control (RBAC),
built with Spring Boot + MySQL backend and Java Swing frontend.
Integrates two AI features: **CaseTwin** (case matching) and **UniRad3s** (X-ray analysis)


---

## Features

| Module            | Admin | Doctor | Receptionist |
|-------------------|:-----:|:------:|:------------:|
| Dashboard         |  yes  |  yes   |     yes      |
| Patient Admission |  yes  |  yes   |     yes      |
| Appointments      |  yes  |  yes   |     yes      |
| Inventory (View)  |  yes  |  yes   |     yes      |
| Inventory (Write) |  yes  |  no    |     no       |
| AI: CaseTwin      |  yes  |  yes   |     no       |
| AI: UniRad3s      |  yes  |  yes   |     no       |
| User Management   |  yes  |  no    |     no       |

---

## Tech Stack

- **Backend:** Spring Boot 3.2, Spring Security (HTTP Basic), Spring Data JPA
- **Database:** MySQL 8
- **Frontend:** Java Swing + FlatLaf
- **AI:** HuggingFace Serverless Inference API (google/medgemma-4b-it)
- **HTTP Client:** OkHttp 4.12

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8 running on localhost:3306
- setup ollama on your pc

---

## Step 1 - Set Up MySQL

```sql
-- Option A: Run the schema script directly
mysql -u root -p < backend/src/main/resources/schema.sql

-- Option B: Just create the database (Spring Boot auto-creates tables)
CREATE DATABASE hospital_mgmt;
```

Update `backend/src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.username=root
spring.datasource.password=yourpassword
```

---

## Step 2 - Start the Backend

```powershell
cd backend
mvn spring-boot:run
```

Wait for: `Started HospitalApplication in X.XXX seconds`

Backend runs on **http://localhost:8081**

---

## Step 3 - Start the Client

Open a **second** PowerShell window:

```powershell
cd client
mvn compile exec:java "-Dexec.mainClass=com.hospital.client.HospitalClientApp"
```

---

## Default Login Credentials

| Username      | Password   | Role         |
|---------------|------------|--------------|
| admin         | password   | Admin        |
| dr.smith      | password   | Doctor       |
| receptionist  | password   | Receptionist |

---

## API Endpoints (Backend - port 8081)

### Auth
- `POST /api/auth/login`

### Dashboard
- `GET /api/dashboard/stats`

### Patients
- `GET    /api/patients`
- `GET    /api/patients?search=name`
- `POST   /api/patients`
- `PUT    /api/patients/{id}`
- `POST   /api/patients/{id}/discharge`
- `DELETE /api/patients/{id}`

### Appointments
- `GET    /api/appointments`
- `GET    /api/appointments?scope=today`
- `POST   /api/appointments`
- `PUT    /api/appointments/{id}`
- `PUT    /api/appointments/{id}/status`
- `DELETE /api/appointments/{id}`

### Inventory
- `GET    /api/inventory`
- `GET    /api/inventory?filter=low`
- `POST   /api/inventory`
- `PUT    /api/inventory/{id}`
- `DELETE /api/inventory/{id}`

### AI (Doctor/Admin only)
- `POST /api/ai/casetwin/extract-profile`
- `POST /api/ai/casetwin/annotate-cxr`
- `POST /api/ai/casetwin/compare-cases`
- `POST /api/ai/casetwin/generate-referral`
- `POST /api/ai/unirad3s/spot`
- `POST /api/ai/unirad3s/segment`
- `POST /api/ai/unirad3s/simplify/clinical`
- `POST /api/ai/unirad3s/simplify/patient`

### Users (Admin only)
- `GET    /api/users`
- `GET    /api/users/doctors`
- `POST   /api/users`
- `PUT    /api/users/{id}`
- `DELETE /api/users/{id}`

---
**to run the project**
run the schema.sql
go to resouces and then to application.properties and add your mysql username and password 
pull the project
to run the project change directory to backend  and run the command  **mvn spring-boot:run **
next in another terminal change directory to client and run the command **** mvn compile exec:java "-Dexec.mainClass=com.hospital.client.HospitalClientApp"****


