# 🎬 Cinebook — Microservices Movie Booking App

> A scalable, role-based movie booking platform built with Spring Boot, React, and Docker. Migrated from monolithic architecture to microservices for better modularity, fault isolation, and deployment flexibility.

---

## 🚀 Features

- 🎟️ **User Panel**: Browse movies, book tickets, view booking history  
- 🛠️ **Admin Panel**: Add/edit movies, manage shows and screens  
- 👑 **Super Admin**: Manage admins, view system analytics  
- 🔐 **Role-Based Access**: JWT + Spring Security for secure endpoints  
- 🧩 **Microservices Architecture**: Independent services for booking 
---

## 🧱 Tech Stack

| Layer        | Technologies Used |
|--------------|-------------------|
| Frontend     | React, Tailwind CSS, Axios |
| Backend      | Spring Boot, Spring Security, Feign Client |
| Auth         | JWT|
| Database     | MySQL (per service), JPA |
| DevOps       | Docker, Docker Compose |
| Tools        | Postman, Git, VS Code , Intellij |

---

## 🗂️ Microservices Breakdown

| Service          | Description |
|------------------|-------------|
| `auth-service`   | Handles login, registration, JWT generation |
| `user-service`   | Manages user profiles and booking history |
| `movie-service`  | CRUD for movies, shows, screens |
| `booking-service`| Ticket booking logic, seat availability |
| `gateway`        | API gateway for routing requests |
| `config-server`  | Centralized config management (optional) |
| `eureka-server`  | Service discovery for dynamic routing |

---

## 🛠️ Setup Instructions

```bash
# Clone the repo
git clone https://github.com/tribhuvan-web/cinebook-microservices.git

# Navigate to project
cd cinebook-microservices

# Start all services using Docker Compose
docker-compose up --build
