# Smart Crop Advisory — Development Instructions

## Project

We are building:

**PS-02 — Smart Crop Advisory & Farmer Distress Early-Warning System**

The goal is to build a hackathon-ready but properly engineered MVP.

The system provides:

1. Personalized crop advisory
2. Weather information
3. Mandi/market price comparison
4. Farmer distress-risk scoring
5. High-risk farmer alerts
6. Officer intervention tracking
7. Regional-language text/voice support
8. Farmer, Officer and Admin dashboards

---

# Architecture

Use this architecture unless explicitly instructed otherwise:

Frontend:
React + TypeScript + Tailwind CSS

Backend:
Java 21 + Spring Boot

Database:
PostgreSQL + PostGIS

ML:
Optional Python + FastAPI service

External integrations:
Weather API
Market/Mandi API
Translation API
TTS/STT
SMS/Notification APIs

Architecture style:
MODULAR MONOLITH

Do NOT convert the backend into microservices unless explicitly requested.

---

# Repository Structure

smart-crop-advisory/

frontend/
backend/
ml-service/
database/
infrastructure/
docs/
README.md
CLAUDE.md

Backend:

backend/src/main/java/com/smartcrop/

auth/
farmer/
crop/
weather/
market/
advisory/
distress/
risk/
intervention/
notification/
officer/
admin/
config/

Each backend module should normally follow:

module/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
└── mapper/

---

# Backend Rules

Use:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Bean Validation

Follow:

Controller
    ↓
Service
    ↓
Repository
    ↓
Database

Controllers must not contain business logic.

Services contain business logic.

Repositories handle persistence.

DTOs should be used for API requests/responses instead of exposing entities directly.

---

# Security

Roles:

FARMER
OFFICER
ADMIN

Use JWT authentication.

Use role-based authorization.

A farmer must never be able to access another farmer's private information.

Never hard-code:

- passwords
- API keys
- JWT secrets
- database credentials
- SMS credentials
- external API credentials

Use environment variables.

---

# Distress Risk Engine

The first implementation should be rule-based and explainable.

Possible inputs:

- rainfall deviation
- market price decline
- loan due proximity
- crop condition

Output:

- risk score 0–100
- risk level
- contributing factors

Example:

0–30 LOW
31–50 MODERATE
51–75 HIGH
76–100 CRITICAL

Do NOT make an LLM responsible for the final risk decision.

A Python ML service may be introduced later if it provides a genuine benefit.

---

# Advisory Engine

The advisory engine should combine:

Crop
+
Crop stage
+
Weather
+
Soil
+
Market

The engine should first produce structured recommendations.

Example:

{
  "category": "IRRIGATION",
  "severity": "WARNING",
  "recommendation": "Delay irrigation",
  "reason": "Heavy rainfall expected"
}

Translation and voice generation should happen after the structured recommendation is produced.

---

# Development Rules

IMPORTANT:

Do not generate the entire application at once.

Implement one feature/module at a time.

Before changing code:

1. Inspect the existing project.
2. Understand the current architecture.
3. Identify affected files.
4. Explain what you intend to change.
5. Make the smallest clean change.
6. Run/build/test the affected module.
7. Report what changed.

Do NOT modify unrelated files.

Do NOT create duplicate classes.

Do NOT introduce new libraries without explaining why they are needed.

Do NOT replace the existing architecture without explicit approval.

---

# Code Quality

Prefer:

- Clean code
- SOLID principles
- Small focused classes
- Meaningful names
- Constructor injection
- DTOs
- Centralized exception handling
- Validation
- Logging
- Unit tests for important business logic

Avoid:

- God classes
- Huge controllers
- Business logic in controllers
- Hard-coded data
- Duplicate logic
- Unnecessary abstractions
- Premature microservices

---

# API Design

Use REST APIs.

Example:

POST /api/auth/register
POST /api/auth/login

GET /api/farmers/me

GET /api/weather/current
GET /api/weather/forecast

GET /api/market/prices
GET /api/market/compare

GET /api/advisories
POST /api/advisories/generate

GET /api/risk/current
GET /api/risk/history

GET /api/officer/high-risk-farmers
POST /api/officer/interventions

---

# Database

Use PostgreSQL.

Important entities:

User
Farmer
Officer
Crop
SoilData
WeatherData
MarketPrice
Advisory
RiskAssessment
RiskFactor
Intervention
Notification
AuditLog

Use proper relationships and constraints.

Do not duplicate information unnecessarily.

---

# External APIs

External APIs should be accessed through dedicated service/provider classes.

Do not put API calls directly inside controllers.

Prefer interfaces where multiple providers may be used.

Example:

WeatherProvider
MarketDataProvider
NotificationProvider
TranslationProvider

This allows mock/sample data during development and hackathon demos.

---

# Testing

Whenever implementing meaningful business logic:

- Add unit tests.
- Test validation.
- Test important service logic.
- Test risk calculation.
- Test authorization.

Run tests before declaring a feature complete.

---

# Git

Keep commits small and meaningful.

Examples:

feat: add farmer registration
feat: add crop management
feat: add weather service
feat: add advisory engine
feat: add distress risk calculation
fix: correct farmer authorization
test: add risk engine tests

Do not create one giant commit containing the entire project.

---

# IMPORTANT

This is a hackathon project, so prioritize:

1. Working MVP
2. Clean architecture
3. Reliability
4. Demonstrable features
5. Explainability
6. Security

Do not over-engineer features that are not required for the MVP.

When uncertain about an architectural decision, explain the trade-off and ask before making a major change.