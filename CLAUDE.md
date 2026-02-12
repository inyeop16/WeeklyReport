# Weekly Report

AI 기반 주간보고 자동 생성기. 일일 업무 기록을 수집하여 AI로 주간보고서를 생성하고, Teams/Email로 발송한다.

## Tech Stack

- **Language**: Kotlin 2.2.21, Java 21
- **Framework**: Spring Boot 3.4.2
- **Persistence**: Spring Data JPA + PostgreSQL (port 5433)
- **Build**: Gradle 9.3.0 (Kotlin DSL)
- **Serialization**: Jackson (`com.fasterxml.jackson`)
- **Validation**: `spring-boot-starter-validation` (Jakarta)
- **Auth**: Spring Security + JJWT 0.12.6 (JWT 기반 Stateless 인증)
- **Env**: `spring-dotenv:4.0.0` — `.env` 파일로 환경 변수 관리

## Build & Run

```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

Windows PowerShell:
```powershell
cd \path\to\your\project; .\gradlew.bat build -x test
```

## Project Structure

```
com.pluxity.weeklyreport/
├── domain/
│   ├── entity/          User, Template, DailyEntry, Report, Department, Role(enum)
│   └── repository/      JPA repositories
├── auth/                JWT 인증 인프라
│   ├── JwtProperties     @ConfigurationProperties(prefix="app.jwt")
│   ├── JwtTokenProvider  토큰 생성/검증/파싱
│   ├── JwtAuthenticationFilter  OncePerRequestFilter, Bearer 토큰 추출 → SecurityContext
│   └── SecurityConfig    SecurityFilterChain, BCryptPasswordEncoder, 공개/보호 URL 정의
├── service/             비즈니스 로직 (AuthService, UserService, DepartmentService, ...)
├── controller/          REST API (AuthController, UserController, DepartmentController, ...)
├── bot/                 Teams Bot Framework 연동 (BotController, BotService, AdaptiveCardBuilder)
│   └── dto/             Activity 등 Bot 프로토콜 DTO
├── ai/                  AI 어댑터 (Strategy 패턴)
│   ├── config/          AiAdapterConfig, AiProperties
│   └── dto/             AiRequest, AiResponse
├── notification/        알림 어댑터 (Strategy 패턴)
│   ├── config/          NotificationAdapterConfig, NotificationProperties
│   └── dto/             NotificationRequest
├── dto/
│   ├── request/         SignupRequest, LoginRequest, Generate*, Update*, Send*
│   └── response/        TokenResponse, UserResponse, DepartmentResponse, ...
└── exception/           GlobalExceptionHandler, BusinessException, ResourceNotFoundException
```

## Architecture

### Strategy Pattern

| 영역 | 인터페이스 | 구현체 | 설정 키 |
|------|-----------|--------|---------|
| AI | `AiAdapter` | Claude, Groq, LocalLlm | `app.ai.provider` (claude/groq/local) |
| 알림 | `NotificationAdapter` | Email, Teams | `app.notification.provider` (email/teams) |

### Bot Framework

- SDK 미사용, REST 프로토콜 직접 구현
- `POST /api/messages` → BotController → BotService → `serviceUrl` 콜백으로 응답
- Bot 명령어: `@주간보고` (보고서 생성), `@일일보고` (일일 기록), 기타 → 도움말 카드
- Emulator 테스트: `http://localhost:8080/api/messages` (App ID/Password 비워둠)
- 현재 blocking됨

### Authentication (JWT)

- **방식**: Stateless JWT (Bearer 토큰)
- **비밀번호**: BCrypt 해싱
- **토큰 구조**: subject=userId, claims: username, role
- **설정**: `app.jwt.secret` (Base64 32바이트), `app.jwt.expiration` (밀리초, 기본 7일)
- **로그아웃**: 클라이언트 사이드 (토큰 삭제), 서버 블랙리스트 없음
- **Role**: `USER` (기본), `ADMIN` — JWT claim에 포함, `ROLE_*` GrantedAuthority로 변환

#### 접근 규칙

| URL 패턴 | 접근 |
|----------|------|
| `/api/auth/**` | permitAll |
| `/api/messages` | permitAll (Bot) |
| `/api/departments` | permitAll |
| `/`, 정적 리소스 | permitAll |
| `/api/**` (나머지) | authenticated |

#### 인증 API

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/auth/signup` | 회원가입 → 201 UserResponse |
| `POST /api/auth/login` | 로그인 → 200 TokenResponse (accessToken, tokenType, expiresIn) |
| `POST /api/auth/logout` | 안내 메시지 → 200 |

### Department

- 별도 테이블 (`departments`), `User.department` → `@ManyToOne Department?`
- `GET /api/departments` — 부서 목록 (공개)

### Schema

- `ddl-auto: update` — Hibernate가 스키마 자동 관리
- `open-in-view: false`

## Frontend Conventions

### 관심사 분리 (HTML / CSS / JS)

- **HTML에는 HTML만** — 인라인 `<style>`, `<script>` 블록 금지
- CSS → `static/css/pages/{page}.css`, JS → `static/js/pages/{page}.js`로 분리
- HTML에서 외부 참조: `<link rel="stylesheet" th:href="@{/css/pages/{page}.css}">`, `<script th:src="@{/js/pages/{page}.js}"></script>`
- 공통 모듈(`common.js`, `custom.css`)은 각 페이지에서 직접 로드하거나 layout fragment에서 로드

### 디렉토리 구조

```
src/main/resources/
├── static/
│   ├── css/
│   │   ├── custom.css          공통 스타일 (CSS 변수, 다크모드 등)
│   │   └── pages/              페이지별 CSS
│   │       ├── login.css
│   │       ├── signup.css
│   │       └── reports.css
│   └── js/
│       ├── common.js           공통 유틸 (토큰 관리, escapeHtml 등)
│       └── pages/              페이지별 JS
│           ├── login.js
│           ├── signup.js
│           └── reports.js
└── templates/
    ├── layout/fragments.html   navbar 등 공통 fragment
    └── pages/                  Thymeleaf 페이지 (HTML만)
```

## Code Conventions

### Entity

- 일반 `class` (data class 아님), `var` 프로퍼티
- `val id: Long = 0` 을 **생성자 마지막 파라미터**로 배치
- Jakarta Persistence (`jakarta.persistence.*`)
- allOpen 플러그인: Entity, MappedSuperclass, Embeddable

### DTO

- `data class`, `val` only
- Response DTO: `companion object { fun from(entity) }` 팩토리
- Request DTO: `@field:NotBlank`, `@field:Email` 등 validation, 메시지는 한국어

### Service

- 생성자 주입 (primary constructor)
- 클래스에 `@Transactional(readOnly = true)`, 변경 메서드에 `@Transactional`
- 단일 표현식은 expression body 사용
- 예외: `BusinessException`, `ResourceNotFoundException`
- **보안**: 현재 사용자 식별은 `SecurityContextHolder.getContext().authentication.principal as Long`로 추출
  - Request DTO에 userId를 포함하지 않음 (권한 상승 공격 방지)
  - 리소스 소유자 검증: 조회/수정/삭제 시 `resource.user.id != userId` 확인 후 예외 발생

### Controller

- `@RestController` + `@RequestMapping("/api/...")`
- expression body + `ResponseEntity` 래핑
- POST → `ResponseEntity.status(HttpStatus.CREATED).body(...)`, GET → `ResponseEntity.ok(...)`
- `@Valid @RequestBody` 로 요청 검증

### General

- 메시지/에러 텍스트는 **한국어**
- 테이블명 snake_case, 프로퍼티 camelCase
- Kotlin 컴파일러: `-Xjsr305=strict`, `-Xannotation-default-target=param-property`

## Environment Variables

`.env` 파일에 정의 (spring-dotenv가 로드):

- `DB_PASSWORD` — PostgreSQL 비밀번호
- `GROQ_API_KEY` — Groq API 키
- `CLAUDE_API_KEY` — Claude API 키 (선택)
- `BOT_APP_ID`, `BOT_APP_PASSWORD` — Bot Framework 인증 (선택)
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` — 메일 설정 (선택)
- `TEAMS_WEBHOOK_URL` — Teams 웹훅 (선택)
- `JWT_SECRET` — JWT 서명 키 (Base64 인코딩 32바이트)

## Testing

- 현재 최소 커버리지 (contextLoads 수준)
- 테스트 추가 시: **Kotest BDD** + **MockK** 사용
- 기존 테스트: JUnit 5 + `@SpringBootTest`
