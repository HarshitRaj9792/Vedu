<div align="center">

<img src="https://img.shields.io/badge/Vedu-Virtual%20Classroom-4f6ef7?style=for-the-badge&logo=googlemeet&logoColor=white" />
<br/><br/>

# 🎓 Vedu — Virtual Classroom Platform

**A production-ready, full-stack virtual classroom system built with Spring Boot, LiveKit WebRTC, and a real-time admin dashboard.**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.2-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![LiveKit](https://img.shields.io/badge/LiveKit-WebRTC-00AFFE?style=flat-square&logo=webrtc&logoColor=white)](https://livekit.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791?style=flat-square&logo=postgresql&logoColor=white)](https://supabase.com)
[![MinIO](https://img.shields.io/badge/MinIO-S3%20Storage-C72E49?style=flat-square&logo=minio&logoColor=white)](https://min.io)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

</div>

---

## 👨‍💻 About the Developer

<table>
<tr>
<td>

**Harshit Raj** is a passionate full-stack developer from India, building real-world applications with modern Java and cloud-native technologies. Vedu is his flagship project — a complete virtual classroom infrastructure built entirely from scratch, containerised, and deployed on Azure.

- 🌐 Domain: [vedulive.net2coder.in](https://vedulive.net2coder.in)
- 🧑‍💻 GitHub: [@harshitraj](https://github.com)
- 📧 Email: dm878919@gmail.com
- 🏫 Project: BCA Final Year Project — *Virtual Classroom with WebRTC, Egress Recording & Role-Based Access*

</td>
</tr>
</table>

---

## 📖 What is Vedu?

**Vedu** is a fully self-hosted virtual classroom platform that enables teachers to:
- Schedule, host, and record live video classes
- Manage students by course and batch
- View a real-time admin dashboard for all sessions

And students to:
- View their personalised class schedule
- Join live sessions via browser (no app install needed)
- Access recorded sessions after class ends

> Built with **zero dependency on paid services** — 100% self-hosted using open-source infrastructure.

---

## ✨ Features

### 🎥 Live Classroom
- Real-time video/audio via **LiveKit WebRTC** (SFU architecture)
- Built-in whiteboard tool — teachers can draw live, students can view
- Session timer displayed during class and captured in recordings
- Mobile-responsive classroom interface

### 📼 Recording System
- Automatic session recording via **LiveKit Egress** (headless browser)
- Recordings stored in **MinIO** (S3-compatible object storage)
- Egress state persisted in PostgreSQL — survives backend restarts
- Admin can view, watch, and delete recordings from the dashboard

### 🗓️ Schedule Management
| Role | Can Do |
|------|--------|
| Teacher | Create schedules, enter LIVE sessions, mark as completed, cancel UPCOMING classes |
| Student | View personalised schedule (filtered by course & batch), join LIVE sessions |
| Admin | View **all** schedules, force-change any status, delete any class |

**Auto-complete:** A Spring `@Scheduled` job runs every 30 minutes to mark sessions older than 3 hours as `COMPLETED`.

### 🔐 Authentication & Role System
- **Supabase Auth** — JWT-based login with roles: `admin`, `teacher`, `student`
- Email extracted directly from the signed JWT on the backend — cannot be spoofed
- Role-based page guards on every frontend page

### 👤 Profile Management
- Teachers: full name, specialisation, employee ID, bio
- Students: full name, course (auto-uppercased), class/batch, roll number
- Emails auto-synced from Supabase JWT on every profile save

### 📊 Admin Dashboard
- Real-time view of all active LiveKit rooms and online participants
- Service health indicators: LiveKit, MinIO, Redis, Egress, Ingress
- **🎬 Recordings Modal** — watch or delete any recording
- **📅 Schedules Modal** — view all classes, change status, delete any session
- Force-close any active classroom

### 📧 Email Notifications *(optional)*
- Async email dispatch when a teacher schedules a new class
- Notifies all enrolled students in the matching course + batch
- Disabled by default — enable via SMTP env vars

---

## 🏗️ Architecture

```
                        ┌─────────────────────────────────────┐
                        │         Caddy Reverse Proxy          │
                        │  (HTTPS + Cert Auto-renew + Proxy)   │
                        └───────────┬─────────────┬────────────┘
                                    │             │
                    ┌───────────────▼──┐    ┌─────▼──────────────┐
                    │  Spring Boot API  │    │  LiveKit SFU Server │
                    │  (Vedu Backend)   │    │  (WebRTC Media)     │
                    │  Port 8080        │    │  Ports 7880/7881    │
                    └──┬────────────┬──┘    └──────┬─────────────┘
                       │            │              │
              ┌────────▼──┐  ┌──────▼────┐  ┌─────▼──────────────┐
              │ PostgreSQL │  │   Redis   │  │  Egress (Recording) │
              │ (Supabase) │  │  (State)  │  │  + Ingress (RTMP)   │
              └────────────┘  └───────────┘  └──────┬─────────────┘
                                                     │
                                            ┌────────▼───────────┐
                                            │  MinIO S3 Storage   │
                                            │  (Recordings Bucket)│
                                            └────────────────────┘
```

---

## 🐳 Services (Docker Compose)

| Service | Image | Purpose |
|---------|-------|---------|
| `caddy` | `caddy:2-alpine` | Reverse proxy + automatic HTTPS |
| `vedu-backend` | Custom (Dockerfile) | Spring Boot API server |
| `livekit` | `livekit/livekit-server` | WebRTC SFU media server |
| `egress` | `livekit/egress` | Headless browser recording |
| `ingress` | `livekit/ingress` | RTMP stream input |
| `redis` | `redis:7-alpine` | LiveKit state store |
| `minio` | `minio/minio` | S3-compatible recording storage |
| `minio-init` | `minio/mc` | One-shot bucket initializer |

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Role |
|-----------|---------|------|
| Java | 17 | Language |
| Spring Boot | 3.1.2 | Web framework |
| Spring Data JPA | — | ORM / database layer |
| Spring Mail | — | Email notifications |
| Spring Scheduling | — | Auto-complete cron jobs |
| LiveKit Server SDK | 0.12.1 | Room management + webhooks |
| JJWT | 0.13.0 | JWT parsing (Supabase tokens) |
| MinIO Java SDK | 9.0.0 | S3 object deletion |
| Lombok | — | Boilerplate reduction |
| PostgreSQL Driver | — | Database connectivity |

### Frontend
- Vanilla **HTML + CSS + JavaScript** (no framework)
- Glassmorphism UI with dark theme
- Google Fonts (Inter)
- LiveKit JS Client SDK for WebRTC

### Infrastructure
- **Supabase** — Auth + PostgreSQL database
- **MinIO** — Self-hosted S3 storage
- **LiveKit** — Open-source WebRTC SFU
- **Caddy** — Zero-config HTTPS reverse proxy
- **Docker Compose** — Single-command deployment
- **Azure VM** — Production hosting

---

## 📁 Project Structure

```
Vedu/
├── src/
│   └── main/
│       ├── java/org/example/videocall/
│       │   ├── controller/
│       │   │   ├── AdminController.java       # Admin dashboard + recordings + schedules
│       │   │   ├── Authcontroller.java        # Login / token endpoints
│       │   │   ├── RecordingAccessController.java  # Teacher/student recording views
│       │   │   ├── RoomController.java        # LiveKit room + webhook handler
│       │   │   ├── StudentController.java     # Student profile + schedule
│       │   │   └── TeacherController.java     # Teacher profile + schedule CRUD
│       │   ├── model/
│       │   │   ├── Recording.java
│       │   │   ├── Schedules.java
│       │   │   ├── Student.java
│       │   │   └── Teacher.java
│       │   ├── repo/                          # Spring Data JPA repositories
│       │   ├── service/
│       │   │   ├── EmailService.java
│       │   │   ├── RecordingService.java
│       │   │   ├── ScheduleStatusService.java # @Scheduled auto-complete
│       │   │   ├── StudentService.java
│       │   │   ├── TeacherService.java
│       │   │   └── TokenService.java          # JWT claim extraction
│       │   └── VideocallApplication.java
│       └── resources/
│           ├── static/
│           │   ├── admin.html                 # Admin dashboard
│           │   ├── login.html / Signup.html
│           │   ├── room.html                  # Live classroom + whiteboard
│           │   ├── lobby.html
│           │   ├── teacher/
│           │   │   ├── index.html             # Teacher dashboard
│           │   │   ├── tec_course.html        # Schedule management
│           │   │   └── recordings.html
│           │   └── student/
│           │       ├── index.html             # Student dashboard
│           │       ├── schedulepage.html      # Class schedule viewer
│           │       └── recordings.html
│           └── application.properties
├── docker-compose.yaml
├── Dockerfile
├── Caddyfile
├── livekit.yaml
├── egress.yaml
├── ingress.yaml
└── vm-setup.sh
```

---

## 🚀 Quick Start (Self-Hosted)

### Prerequisites
- Docker & Docker Compose
- A domain name pointed to your server
- Supabase project (for Auth + PostgreSQL)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/vedu.git
cd vedu
```

### 2. Configure environment variables
```bash
cp .env.example .env
```

Edit `.env` with your values:
```env
# Supabase
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_JWT_SECRET=your-supabase-jwt-secret
DATABASE_URL=jdbc:postgresql://db.your-project.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-db-password

# LiveKit
LIVEKIT_API_KEY=your-livekit-key
LIVEKIT_API_SECRET=your-livekit-secret
LIVEKIT_WS_URL=wss://your-domain.com

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=change-me-in-production
MINIO_BUCKET=recordings
MINIO_PUBLIC_URL=https://your-domain.com/storage

# Optional SMTP (email notifications)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your@email.com
SMTP_PASSWORD=your-app-password
```

### 3. Update Caddyfile with your domain
```
your-domain.com {
    reverse_proxy vedu-backend:8080
}
```

### 4. Deploy
```bash
docker compose up --build -d
```

That's it! Vedu is now live at `https://your-domain.com` 🎉

---

## 📡 API Overview

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Login → returns JWT |

### Teacher
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/teachers/profile` | Upsert profile |
| `GET` | `/api/teachers/me` | Get own profile |
| `POST` | `/api/teachers/saveSchedule` | Create a class schedule |
| `GET` | `/api/teachers/my-Schedule` | List own schedules |
| `PATCH` | `/api/teachers/schedules/{id}/status` | Change class status |
| `DELETE` | `/api/teachers/schedules/{id}` | Delete UPCOMING class only |

### Student
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/student/profile` | Upsert profile |
| `GET` | `/api/student/me` | Get own profile |
| `GET` | `/api/student/my-schedules` | Get schedules for my course+batch |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | LiveKit rooms + stats |
| `DELETE` | `/api/admin/rooms/{name}` | Force-close a room |
| `GET` | `/api/admin/recordings` | All recordings |
| `DELETE` | `/api/admin/recordings/{id}` | Delete recording + MinIO file |
| `GET` | `/api/admin/schedules` | All schedules |
| `PATCH` | `/api/admin/schedules/{id}/status` | Force any status |
| `DELETE` | `/api/admin/schedules/{id}` | Delete any class |

### Room
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/room/token` | Generate LiveKit access token |
| `POST` | `/api/room/start-recording` | Start Egress session |
| `POST` | `/api/room/webhook` | LiveKit webhook (egress events) |

---

## 🎯 Schedule Status Lifecycle

```
CREATE → UPCOMING ──(30 min window opens)──► Teacher enters → LIVE
                                                    │
                                         Teacher clicks "Done"
                                          → COMPLETED (manual)
                                                    │
                                    OR: @Scheduled job (every 30 min)
                                         topicTime + 3h → COMPLETED
```

### What each user sees per status:

| Status | Teacher Button | Student Action |
|--------|---------------|----------------|
| UPCOMING | ⏳ Not Started (disabled) + countdown | Info only |
| LIVE | 🚀 Enter Classroom | 🚀 Join Classroom Now |
| COMPLETED | 🔒 Class Ended | 📼 View Recordings |

---

## 🔒 Security

- JWT tokens signed and verified using Supabase's HMAC-SHA256 secret
- Email and user ID extracted **server-side** from verified JWT (cannot be forged by client)
- Admin panel guards: non-admin tokens are rejected before any page loads
- Teacher endpoints verify resource ownership before any edit/delete
- MinIO is not publicly exposed — recordings served through Caddy proxy only

---

## 🌐 Live Demo

> **Production URL:** [https://vedulive.net2coder.in](https://vedulive.net2coder.in)

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

---

## 📄 License

This project is open-source under the [MIT License](LICENSE).

---

<div align="center">

**Built with ❤️ by Harshit Raj**

*From India 🇮🇳 — BCA Student, Full-Stack Developer*

</div>
