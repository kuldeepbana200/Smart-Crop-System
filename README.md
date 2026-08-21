# 🌾 Smart Crop Advisory & Farmer Distress Early-Warning System

A multilingual, low-bandwidth platform designed to provide **localized crop advisory**, **market/mandi intelligence**, and **early detection of farmer distress risk** using weather, crop, soil, market, and financial-risk indicators.

The system is designed around the **PS-02: Smart Crop Advisory & Farmer Distress Early-Warning System** problem statement.

---

## 📌 Problem Statement

Farmers often lack timely and localized information about:

- Crop health and recommended actions
- Weather-related risks
- Soil and crop conditions
- Local mandi/market prices
- Price crashes
- Potential financial distress
- Relevant intervention or support

The platform addresses this through two connected capabilities:

1. **Advisory Engine** — converts crop, soil, weather, and market information into actionable recommendations.
2. **Distress Early-Warning Engine** — combines signals such as rainfall deviation, market-price decline, and loan-due proximity to calculate a farmer distress-risk score.

The system also provides **market/mandi comparison**, **regional-language text and voice support**, and an **officer intervention workflow** for high-risk cases.

---

# 🎯 Objectives

### Primary Objectives

- Provide personalized crop advisory.
- Provide localized weather information.
- Compare mandi/market prices.
- Detect potential farmer distress at an early stage.
- Generate explainable risk scores.
- Alert authorized agriculture officers/NGO support personnel for high-risk cases.
- Provide information in regional languages.
- Work effectively on basic smartphones and low-bandwidth connections.

### MVP Goal

The MVP should demonstrate this complete flow:

```text
Farmer Registration
        ↓
Crop + Location Selection
        ↓
Weather + Market Data
        ↓
Personalized Crop Advisory
        ↓
Distress Risk Calculation
        ↓
LOW / MEDIUM / HIGH / CRITICAL
        ↓
High-Risk Alert
        ↓
Officer Intervention
        ↓
Farmer Regional-Language Alert
```

---

# 👥 User Roles

## 1. Farmer

The farmer can:

- Register/login.
- Create a farmer profile.
- Add crop information.
- Provide location/district.
- View weather.
- View crop advisory.
- View mandi/market prices.
- Compare nearby markets.
- View distress-risk score.
- Receive alerts.
- Access recommendations in regional languages.
- Use text/voice assistance.

## 2. Officer

The officer represents an authorized agriculture-extension, government, NGO, or other designated support worker.

The officer can:

- View high-risk farmers.
- View risk factors.
- Review farmer details.
- Receive distress alerts.
- Prioritize cases.
- Record interventions.
- Track intervention status.
- Add notes and follow-up information.
- View district-level analytics.

## 3. Admin

The administrator can:

- Manage users.
- Manage officers.
- Manage master data.
- Configure system settings.
- Monitor integrations.
- Review audit logs.
- Manage supported crops, regions, and configuration.

---

# 🏗️ System Architecture

```text
                         SMART CROP PLATFORM
                                  │
             ┌────────────────────┼────────────────────┐
             ▼                    ▼                    ▼
      FARMER WEB/PWA       OFFICER WEB APP       ADMIN WEB APP
       React + TS            React + TS            React + TS
             │                    │                    │
             └────────────────────┼────────────────────┘
                                  │
                              REST APIs
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │     SPRING BOOT         │
                    │      BACKEND             │
                    │       Java               │
                    └────────────┬────────────┘
                                 │
       ┌────────────┬────────────┼────────────┬────────────┐
       ▼            ▼            ▼            ▼            ▼
   Advisory      Distress      Risk         Market     Intervention
   Service       Service      Engine        Service      Service
                                 │
                                 ▼
                         Optional ML Service
                           Python + FastAPI
                                 │
                                 ▼
                            ML Model
                                 │
                                 ▼
                       Risk Probability /
                       Feature Importance

                                 │
                                 ▼
                       PostgreSQL Database
                                 │
        ┌────────────────────────┼─────────────────────────┐
        ▼                        ▼                         ▼
   Weather API              Market API              Other APIs
   / Weather Data            / Mandi Data       Translation / TTS
                                                     SMS / etc.
```

---

# 🧩 Architecture Philosophy

The backend follows a **modular monolith** architecture.

We intentionally avoid starting with multiple microservices because the project is an MVP/hackathon system. A modular monolith gives us:

- Faster development
- Easier debugging
- Simpler deployment
- Clear separation of responsibilities
- Easier future migration to microservices if required

The ML service is separated because ML experimentation and model deployment have different requirements from the main application backend.

---

# 🛠️ Technology Stack

## Frontend

| Technology | Purpose |
|---|---|
| React | UI |
| TypeScript | Type safety |
| Tailwind CSS | Styling |
| React Router | Routing |
| Axios | API communication |
| Leaflet | Maps |
| OpenStreetMap | Map data |
| Recharts | Analytics/charts |

## Backend

| Technology | Purpose |
|---|---|
| Java | Backend language |
| Spring Boot | Backend framework |
| Spring Web | REST APIs |
| Spring Security | Authentication/security |
| JWT | Stateless authentication |
| Spring Data JPA | Persistence |
| Hibernate | ORM |
| Bean Validation | Request validation |

## Database

| Technology | Purpose |
|---|---|
| PostgreSQL | Primary database |
| PostGIS | Geographic/spatial queries |

## ML / Risk Engine

Initial implementation:

- Java-based weighted/rule-based risk engine

Optional ML implementation:

- Python
- FastAPI
- scikit-learn
- Pandas
- NumPy
- XGBoost / Random Forest if justified

The ML service should remain optional until the core application is stable.

## External Integrations

Potential integrations:

- Weather API
- Mandi/market-price API
- Translation API
- Text-to-Speech API
- Speech-to-Text API
- SMS provider
- Push notification provider

For the MVP, simulated/sample data may be used where live government data is unavailable.

## DevOps

- Git
- GitHub
- Docker
- Docker Compose
- Postman
- Swagger/OpenAPI

---

# 📁 Repository Structure

```text
smart-crop-advisory/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   │   ├── farmer/
│   │   │   ├── officer/
│   │   │   └── admin/
│   │   ├── layouts/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── context/
│   │   ├── types/
│   │   └── utils/
│   └── package.json
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/smartcrop/
│   │   │   │       ├── auth/
│   │   │   │       ├── farmer/
│   │   │   │       ├── crop/
│   │   │   │       ├── weather/
│   │   │   │       ├── market/
│   │   │   │       ├── advisory/
│   │   │   │       ├── distress/
│   │   │   │       ├── risk/
│   │   │   │       ├── intervention/
│   │   │   │       ├── notification/
│   │   │   │       ├── officer/
│   │   │   │       ├── admin/
│   │   │   │       └── config/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
│
├── ml-service/
│   ├── app/
│   │   ├── models/
│   │   ├── services/
│   │   ├── schemas/
│   │   └── main.py
│   └── requirements.txt
│
├── database/
│   ├── migrations/
│   └── seed/
│
├── infrastructure/
│   ├── docker/
│   └── docker-compose.yml
│
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── database/
│   └── decisions/
│
├── .gitignore
├── README.md
└── LICENSE
```

---

# 🔄 Core Functional Flow

## Farmer Advisory Flow

```text
Farmer
  ↓
Login
  ↓
Select Crop + Location
  ↓
Backend
  ↓
Fetch Weather + Crop + Soil + Market Data
  ↓
Advisory Engine
  ↓
Personalized Recommendation
  ↓
Translate / TTS
  ↓
Farmer
```

---

# 🚨 Distress Early-Warning Flow

```text
Weather Data
      +
Market Price Data
      +
Loan-Due / Financial Signal
      ↓
Feature Preparation
      ↓
Risk Engine
      ↓
Risk Score (0–100)
      ↓
Risk Level
      ↓
┌─────────┬──────────┬─────────┬──────────┐
│   LOW   │ MODERATE │  HIGH   │ CRITICAL │
└─────────┴──────────┴─────────┴──────────┘
                         │
                         ▼
                Officer Notification
                         │
                         ▼
                 Intervention Tracking
```

---

# 🧠 Distress Risk Engine

The first MVP implementation should use a transparent weighted model.

Example:

```text
Risk Score =
    40% Rainfall Risk
  + 35% Market Price Risk
  + 25% Loan-Due Risk
```

Example:

```text
Rainfall Risk       = 75
Market Price Risk   = 60
Loan-Due Risk       = 80

Risk Score =
(0.40 × 75) +
(0.35 × 60) +
(0.25 × 80)

= 70
```

Example classification:

```text
0–30      LOW
31–50     MODERATE
51–75     HIGH
76–100    CRITICAL
```

These thresholds are configurable and should not be treated as validated real-world financial or agricultural risk thresholds.

The system should always show **why** a score was generated.

Example:

```text
Risk Score: 82 — CRITICAL

Contributing Factors:
• Rainfall deviation: High
• Market price decline: High
• Loan due proximity: Medium
```

---

# 🤖 Optional Machine Learning Architecture

After the rule-based engine is stable, the risk engine can be upgraded:

```text
Spring Boot
     │
     │ REST
     ▼
Python FastAPI
     │
     ▼
Feature Processing
     │
     ▼
ML Model
     │
     ├── Risk Probability
     └── Feature Importance
     │
     ▼
Spring Boot
     │
     ▼
Farmer / Officer Dashboard
```

Possible models:

- Logistic Regression
- Random Forest
- XGBoost

The model should only be introduced if it improves the prototype and can be explained clearly.

---

# 🌦️ Weather Module

The weather module collects:

- Temperature
- Rainfall
- Rainfall forecast
- Humidity
- Wind
- Weather alerts

The system uses location/district to provide localized information.

Example:

```text
Location: District X
Crop: Wheat

Rain Probability: 82%
Expected Rainfall: 45mm

Advisory:
"Heavy rainfall expected. Delay irrigation."
```

---

# 📈 Market / Mandi Module

The market module provides:

- Crop price
- Mandi
- District
- Date
- Historical price
- Nearby mandi comparison

Example:

```text
Crop: Wheat

Mandi          Price
----------------------
Sehore         ₹2,250
Bhopal         ₹2,310
Indore         ₹2,390
Ujjain         ₹2,340
```

The system can calculate:

```text
Best Market
Price Difference
Transport Cost
Estimated Net Realization
```

The MVP can start with sample market data and later integrate a live source.

---

# 🌱 Advisory Engine

The advisory engine combines:

```text
Crop
+
Crop Stage
+
Weather
+
Soil
+
Market
```

Example rule:

```text
IF
crop = wheat
AND
rain_probability > 70%
AND
crop_stage = irrigation

THEN
recommend delaying irrigation.
```

Another example:

```text
IF
temperature > crop_threshold
AND
soil_moisture < threshold

THEN
recommend increased irrigation.
```

The engine should return structured recommendations first:

```json
{
  "category": "IRRIGATION",
  "severity": "WARNING",
  "recommendation": "Delay irrigation",
  "reason": "Heavy rainfall expected"
}
```

The presentation layer can then translate the recommendation.

---

# 🌐 Multilingual Support

The platform is intended to support regional-language text and voice.

Initial implementation can support a small number of languages rather than trying to support every Indian language.

Flow:

```text
Structured Advisory
        ↓
Translation Service
        ↓
Regional Language
        ↓
Text + Voice
```

The system should keep the underlying advisory structured so that translation does not change the actual recommendation.

---

# 👨‍🌾 Officer Intervention

An officer is involved only when the system detects a case requiring human attention.

Example:

```text
Farmer A
Risk Score: 87
Risk Level: CRITICAL

Factors:
• Major rainfall deviation
• 30% market-price decline
• Loan due soon
```

Officer dashboard:

```text
[HIGH PRIORITY]

Farmer A
Crop: Wheat
District: X

[Contact Farmer]
[Assign Intervention]
[Mark Resolved]
```

The officer is not intended to replace the advisory system. The officer provides a **human escalation path** for high-risk cases.

---

# 🔔 Notification System

Possible channels:

- In-app notifications
- SMS
- Push notifications
- Email

Example:

```text
Risk >= HIGH
      ↓
Notification Service
      ↓
Farmer + Officer
```

Notifications should contain actionable information rather than simply saying "High risk."

---

# 🔐 Security

Security requirements include:

- JWT authentication
- Role-based access control
- Password hashing
- Input validation
- API authorization
- Rate limiting
- Audit logging
- Secure configuration
- Environment variables for secrets

Roles:

```text
FARMER
OFFICER
ADMIN
```

A farmer must never be able to access another farmer's private data.

---

# 🗄️ Core Database Entities

Initial entities:

```text
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
```

Relationships:

```text
User
 │
 ├── Farmer
 │      │
 │      ├── Crops
 │      ├── Soil Data
 │      ├── Risk Assessments
 │      ├── Advisories
 │      └── Interventions
 │
 └── Officer
        │
        └── Interventions
```

---

# 🔌 Initial API Design

## Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
```

## Farmer

```http
GET  /api/farmers/me
PUT  /api/farmers/me
GET  /api/farmers/me/crops
POST /api/farmers/me/crops
```

## Weather

```http
GET /api/weather/current
GET /api/weather/forecast
```

## Market

```http
GET /api/market/prices
GET /api/market/compare
```

## Advisory

```http
GET  /api/advisories
POST /api/advisories/generate
```

## Risk

```http
GET /api/risk/current
GET /api/risk/history
POST /api/risk/calculate
```

## Officer

```http
GET  /api/officer/high-risk-farmers
GET  /api/officer/farmers/{id}
POST /api/officer/interventions
PUT  /api/officer/interventions/{id}
```

## Notifications

```http
GET  /api/notifications
POST /api/notifications/send
```

---

# 🧪 Testing Strategy

## Backend

- Unit tests — JUnit
- Mockito
- Spring Boot integration tests

## API

- Postman
- Swagger/OpenAPI

## Frontend

- Component testing
- Form validation
- Role-based route testing

## ML

- Dataset validation
- Precision/recall
- Confusion matrix
- Feature importance

---

# 🐳 Docker

The project should eventually run with:

```text
Docker Compose
│
├── frontend
├── backend
├── postgres
├── ml-service
└── optional redis
```

The goal is to make the development environment reproducible for every team member.

---

# 📊 Dashboard Requirements

## Farmer Dashboard

```text
Weather
Crop Advisory
Market Prices
Risk Score
Alerts
Recommendations
```

## Officer Dashboard

```text
Total Farmers
High-Risk Farmers
Critical Cases
Risk Distribution
District Map
Intervention Queue
```

## Admin Dashboard

```text
Users
Officers
Master Data
API Status
System Configuration
Audit Logs
```

---

# 🚀 Development Roadmap

## Phase 1 — Project Foundation

- [ ] Repository setup
- [ ] Folder structure
- [ ] React setup
- [ ] Spring Boot setup
- [ ] PostgreSQL setup
- [ ] Docker setup
- [ ] Git workflow

## Phase 2 — Authentication

- [ ] Registration
- [ ] Login
- [ ] JWT
- [ ] Role-based access
- [ ] Farmer role
- [ ] Officer role
- [ ] Admin role

## Phase 3 — Farmer Profile

- [ ] Farmer details
- [ ] Location
- [ ] Land information
- [ ] Crop selection
- [ ] Crop stage

## Phase 4 — Data Integration

- [ ] Weather API
- [ ] Market data
- [ ] Soil data
- [ ] Data normalization
- [ ] Caching where required

## Phase 5 — Advisory Engine

- [ ] Rule engine
- [ ] Crop-specific recommendations
- [ ] Weather-based recommendations
- [ ] Advisory history

## Phase 6 — Distress Engine

- [ ] Feature calculation
- [ ] Weighted risk score
- [ ] Risk classification
- [ ] Explainable risk factors
- [ ] Risk history

## Phase 7 — Officer System

- [ ] High-risk farmer list
- [ ] Risk details
- [ ] Intervention creation
- [ ] Intervention status
- [ ] Follow-up notes

## Phase 8 — Multilingual System

- [ ] Translation
- [ ] Regional-language UI/content
- [ ] Text-to-speech
- [ ] Optional speech-to-text

## Phase 9 — Notifications

- [ ] In-app alerts
- [ ] SMS
- [ ] Push notifications
- [ ] Officer alerts

## Phase 10 — ML Upgrade

- [ ] Dataset preparation
- [ ] Model training
- [ ] Model evaluation
- [ ] FastAPI inference
- [ ] Spring Boot integration
- [ ] Explainability

## Phase 11 — Final Product

- [ ] Mobile optimization
- [ ] Error handling
- [ ] Loading states
- [ ] Security review
- [ ] Performance testing
- [ ] Docker deployment
- [ ] Demo data
- [ ] Documentation
- [ ] PPT
- [ ] 3-minute demo

---

# 🎬 Target Hackathon Demo

The final 3-minute demo should tell one simple story:

### Step 1 — Farmer

```text
Farmer registers
        ↓
Selects:
Crop = Wheat
Location = District X
```

### Step 2 — Data

```text
Weather:
Heavy rainfall expected

Market:
Price falling

Crop:
Wheat
```

### Step 3 — Advisory

```text
⚠️ Delay irrigation.
Heavy rainfall is expected.
```

### Step 4 — Distress Detection

```text
Rainfall Risk:       78
Market Risk:         72
Loan Risk:           65

Overall Risk:        73
Status:              HIGH
```

### Step 5 — Intervention

```text
Officer Dashboard

🔴 HIGH-RISK FARMER

Reason:
• Rainfall deviation
• Market price decline
• Financial pressure
```

### Step 6 — Notification

```text
Farmer:
Regional-language warning

Officer:
New high-priority case
```

### Step 7 — Market Decision

```text
Nearby Mandis

Sehore     ₹2,250
Bhopal     ₹2,310
Indore     ₹2,390  ← Recommended
```

This demonstrates the three major PS-02 outcomes: **regional-language advisory, distress-risk detection/alert routing, and market comparison**.

---

# ⚠️ Important Development Principles

### 1. Don't start with ML

Build the rule-based risk engine first.

### 2. Don't build three separate frontends

Use one React application with role-based dashboards.

### 3. Don't use microservices everywhere

Use a modular Spring Boot monolith.

### 4. Don't depend entirely on live government APIs

Create a clean provider interface and allow sample/mock data for development.

### 5. Don't make the LLM the decision-maker

The risk score and agricultural rules should be deterministic or model-based and explainable.

### 6. Don't expose sensitive farmer information

Use proper authorization and audit logging.

### 7. Don't overbuild the MVP

The most important flow is:

```text
DATA
 ↓
ADVISORY
 ↓
RISK
 ↓
INTERVENTION
```

---

# 🏆 Project Success Criteria

The MVP is successful when a judge can see:

- A farmer registering.
- A crop and location being selected.
- Weather and market information being retrieved.
- A personalized advisory being generated.
- A transparent distress-risk score being calculated.
- A high-risk farmer appearing on the officer dashboard.
- An intervention being created.
- The farmer receiving a regional-language warning.
- Mandi prices being compared.

The goal is not to build every possible feature. The goal is to demonstrate a **credible end-to-end early-warning and advisory system**.

---

# 📌 Current Architecture Decision

| Layer | Decision |
|---|---|
| Frontend | React + TypeScript |
| Styling | Tailwind CSS |
| Maps | Leaflet + OpenStreetMap |
| Backend | Java + Spring Boot |
| Architecture | Modular Monolith |
| Database | PostgreSQL |
| Spatial Data | PostGIS |
| Risk Engine | Rule-based initially |
| ML | Python + FastAPI, optional |
| Market | External API / mock provider |
| Weather | External API / mock provider |
| Language | Translation + TTS |
| Notifications | SMS / Push / In-App |
| Deployment | Docker |
| Version Control | Git + GitHub |

---

# 📄 Project Status

**Status:** 🟡 Architecture / Initial Development

**Problem Statement:** PS-02

**Primary Goal:** Build a working MVP for the internal hackathon.

**Current Priority:**

```text
Repository
    ↓
Project Structure
    ↓
Backend + Frontend Setup
    ↓
Database
    ↓
Authentication
    ↓
Farmer Module
    ↓
Advisory
    ↓
Distress Risk
    ↓
Officer Intervention
```

---

## 🤝 Development Workflow

The project should be developed incrementally.

Each feature should be:

1. Designed
2. Implemented
3. Tested
4. Reviewed
5. Committed to Git
6. Integrated with the main branch

Avoid large uncontrolled changes across unrelated modules.

---

## 📜 License

Add the team's chosen license before public release.

---

## 🌾 Vision

Build a practical decision-support platform that helps farmers move from:

**"I don't know what is happening"**

to:

**"I know the risk, I know what action to take, and I know where to get help."**
