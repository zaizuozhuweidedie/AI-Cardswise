# CardWise Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a full-stack AI-powered Flashcard learning app with Spring Boot 3 backend and Vue 3 frontend, featuring SM-2 spaced repetition and multi-provider AI card generation.

**Architecture:** Standard 3-tier backend (Controller → Service → Repository) with an AI provider abstraction layer using Strategy Pattern. Frontend is SPA with Pinia stores communicating via Axios to REST API. Authentication via JWT. AI providers configured through Spring properties.

**Tech Stack:** Java 17, Spring Boot 3.2.x, Spring Data JPA, PostgreSQL (Neon), Spring Security + jjwt 0.12.x, Maven — Vue 3 + Vite 5, Vue Router 4, Pinia, Tailwind CSS 3, Axios

---

## Phase 1: Backend Foundation

### Task 1.1 — Maven Project + Application Entry Point

**Files to create:**
- `cardwise-server/pom.xml`
- `cardwise-server/src/main/java/com/cardwise/CardwiseApplication.java`
- `cardwise-server/src/main/resources/application.yml`

**Steps:**

1. Create `cardwise-server/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.cardwise</groupId>
    <artifactId>cardwise-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>CardWise Server</name>
    <description>AI-powered Flashcard learning app backend</description>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

2. Create `cardwise-server/src/main/java/com/cardwise/CardwiseApplication.java`:

```java
package com.cardwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CardwiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardwiseApplication.class, args);
    }
}
```

3. Create `cardwise-server/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false

cardwise:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 604800000
  ai:
    provider: deepseek
    providers:
      deepseek:
        api-url: ${AI_API_URL:https://api.deepseek.com}
        api-key: ${AI_API_KEY}
        model: deepseek-chat
```

4. Verify: `cd cardwise-server && mvn compile` compiles without errors.

---

### Task 1.2 — JPA Entity Models

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/model/User.java`
- `cardwise-server/src/main/java/com/cardwise/model/Deck.java`
- `cardwise-server/src/main/java/com/cardwise/model/Card.java`
- `cardwise-server/src/main/java/com/cardwise/model/ReviewLog.java`

**Steps:**

1. Create `User.java`:

```java
package com.cardwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

2. Create `Deck.java`:

```java
package com.cardwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decks")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String color = "#6366f1";

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

3. Create `Card.java`:

```java
package com.cardwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_cards_user_next_review", columnList = "userId, nextReviewAt"),
    @Index(name = "idx_cards_deck_id", columnList = "deckId")
})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 0;

    @Column(nullable = false)
    private Integer repetitions = 0;

    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;

    @Column(name = "last_review_at")
    private LocalDateTime lastReviewAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nextReviewAt == null) {
            nextReviewAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(Double easeFactor) { this.easeFactor = easeFactor; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public Integer getRepetitions() { return repetitions; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }
    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(LocalDateTime nextReviewAt) { this.nextReviewAt = nextReviewAt; }
    public LocalDateTime getLastReviewAt() { return lastReviewAt; }
    public void setLastReviewAt(LocalDateTime lastReviewAt) { this.lastReviewAt = lastReviewAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

4. Create `ReviewLog.java`:

```java
package com.cardwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_logs", indexes = {
    @Index(name = "idx_review_logs_user", columnList = "userId, reviewedAt")
})
public class ReviewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer quality;

    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @Column(nullable = false)
    private Integer repetitions;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        reviewedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Integer getQuality() { return quality; }
    public void setQuality(Integer quality) { this.quality = quality; }
    public Double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(Double easeFactor) { this.easeFactor = easeFactor; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public Integer getRepetitions() { return repetitions; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
```

5. Verify: `cd cardwise-server && mvn compile` passes.

---

### Task 1.3 — JPA Repositories

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/repository/UserRepository.java`
- `cardwise-server/src/main/java/com/cardwise/repository/DeckRepository.java`
- `cardwise-server/src/main/java/com/cardwise/repository/CardRepository.java`
- `cardwise-server/src/main/java/com/cardwise/repository/ReviewLogRepository.java`

**Steps:**

1. Create `UserRepository.java`:

```java
package com.cardwise.repository;

import com.cardwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

2. Create `DeckRepository.java`:

```java
package com.cardwise.repository;

import com.cardwise.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {
    List<Deck> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
```

3. Create `CardRepository.java`:

```java
package com.cardwise.repository;

import com.cardwise.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByDeckIdOrderByCreatedAtAsc(UUID deckId);

    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.nextReviewAt <= :now ORDER BY c.nextReviewAt ASC")
    List<Card> findDueCardsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Card c WHERE c.deckId = :deckId AND c.userId = :userId AND c.nextReviewAt <= :now ORDER BY c.nextReviewAt ASC")
    List<Card> findDueCardsByDeckIdAndUserId(@Param("deckId") UUID deckId, @Param("userId") UUID userId, @Param("now") LocalDateTime now);

    long countByUserId(UUID userId);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.userId = :userId AND c.repetitions >= 5")
    long countMasteredByUserId(@Param("userId") UUID userId);

    long countByDeckIdAndUserId(UUID deckId, UUID userId);
}
```

4. Create `ReviewLogRepository.java`:

```java
package com.cardwise.repository;

import com.cardwise.model.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {
    long countByUserIdAndReviewedAtAfter(UUID userId, LocalDateTime after);

    @Query("SELECT CAST(r.reviewedAt AS LocalDate) as date, COUNT(DISTINCT r.cardId) as count " +
           "FROM ReviewLog r WHERE r.userId = :userId AND r.reviewedAt >= :since " +
           "GROUP BY CAST(r.reviewedAt AS LocalDate) ORDER BY date ASC")
    List<Object[]> countDailyActivity(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
```

---

### Task 1.4 — JWT Utility

**File to create:** `cardwise-server/src/main/java/com/cardwise/config/JwtUtil.java`

```java
package com.cardwise.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${cardwise.jwt.secret}") String secret,
            @Value("${cardwise.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

---

### Task 1.5 — DTOs

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/dto/LoginRequest.java`
- `cardwise-server/src/main/java/com/cardwise/dto/RegisterRequest.java`
- `cardwise-server/src/main/java/com/cardwise/dto/AuthResponse.java`
- `cardwise-server/src/main/java/com/cardwise/dto/DeckRequest.java`
- `cardwise-server/src/main/java/com/cardwise/dto/CardRequest.java`
- `cardwise-server/src/main/java/com/cardwise/dto/ReviewRequest.java`
- `cardwise-server/src/main/java/com/cardwise/dto/SM2ResultResponse.java`
- `cardwise-server/src/main/java/com/cardwise/dto/StatsResponse.java`
- `cardwise-server/src/main/java/com/cardwise/dto/ErrorResponse.java`

**Steps:**

1. Create `LoginRequest.java`:

```java
package com.cardwise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;

    public @NotBlank @Email String getEmail() { return email; }
    public void setEmail(@NotBlank @Email String email) { this.email = email; }
    public @NotBlank String getPassword() { return password; }
    public void setPassword(@NotBlank String password) { this.password = password; }
}
```

2. Create `RegisterRequest.java`:

```java
package com.cardwise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotBlank
    private String name;

    public @NotBlank @Email String getEmail() { return email; }
    public void setEmail(@NotBlank @Email String email) { this.email = email; }
    public @NotBlank @Size(min = 6) String getPassword() { return password; }
    public void setPassword(@NotBlank @Size(min = 6) String password) { this.password = password; }
    public @NotBlank String getName() { return name; }
    public void setName(@NotBlank String name) { this.name = name; }
}
```

3. Create `AuthResponse.java`:

```java
package com.cardwise.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;
    private String name;

    public AuthResponse(String token, UUID userId, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}
```

4. Create `DeckRequest.java`:

```java
package com.cardwise.dto;

import jakarta.validation.constraints.NotBlank;

public class DeckRequest {
    @NotBlank
    private String name;
    private String description;
    private String color;

    public @NotBlank String getName() { return name; }
    public void setName(@NotBlank String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
```

5. Create `CardRequest.java`:

```java
package com.cardwise.dto;

import jakarta.validation.constraints.NotBlank;

public class CardRequest {
    @NotBlank
    private String front;
    @NotBlank
    private String back;
    private String tags;

    public @NotBlank String getFront() { return front; }
    public void setFront(@NotBlank String front) { this.front = front; }
    public @NotBlank String getBack() { return back; }
    public void setBack(@NotBlank String back) { this.back = back; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
```

6. Create `ReviewRequest.java`:

```java
package com.cardwise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {
    @NotNull @Min(1) @Max(4)
    private Integer quality;

    public @NotNull @Min(1) @Max(4) Integer getQuality() { return quality; }
    public void setQuality(@NotNull @Min(1) @Max(4) Integer quality) { this.quality = quality; }
}
```

7. Create `SM2ResultResponse.java`:

```java
package com.cardwise.dto;

import java.time.LocalDateTime;

public class SM2ResultResponse {
    private double newEaseFactor;
    private int newIntervalDays;
    private int newRepetitions;
    private LocalDateTime nextReviewAt;

    public SM2ResultResponse(double newEaseFactor, int newIntervalDays, int newRepetitions, LocalDateTime nextReviewAt) {
        this.newEaseFactor = newEaseFactor;
        this.newIntervalDays = newIntervalDays;
        this.newRepetitions = newRepetitions;
        this.nextReviewAt = nextReviewAt;
    }

    public double getNewEaseFactor() { return newEaseFactor; }
    public int getNewIntervalDays() { return newIntervalDays; }
    public int getNewRepetitions() { return newRepetitions; }
    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
}
```

8. Create `StatsResponse.java`:

```java
package com.cardwise.dto;

import java.util.List;
import java.util.Map;

public class StatsResponse {
    private long totalCards;
    private long dueCards;
    private long studiedToday;
    private long masteredCards;
    private List<Map<String, Object>> dailyActivity;

    public long getTotalCards() { return totalCards; }
    public void setTotalCards(long totalCards) { this.totalCards = totalCards; }
    public long getDueCards() { return dueCards; }
    public void setDueCards(long dueCards) { this.dueCards = dueCards; }
    public long getStudiedToday() { return studiedToday; }
    public void setStudiedToday(long studiedToday) { this.studiedToday = studiedToday; }
    public long getMasteredCards() { return masteredCards; }
    public void setMasteredCards(long masteredCards) { this.masteredCards = masteredCards; }
    public List<Map<String, Object>> getDailyActivity() { return dailyActivity; }
    public void setDailyActivity(List<Map<String, Object>> dailyActivity) { this.dailyActivity = dailyActivity; }
}
```

9. Create `ErrorResponse.java`:

```java
package com.cardwise.dto;

public class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() { return error; }
}
```

---

### Task 1.6 — Exception Classes

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/exception/ResourceNotFoundException.java`
- `cardwise-server/src/main/java/com/cardwise/exception/AiGenerationException.java`
- `cardwise-server/src/main/java/com/cardwise/exception/GlobalExceptionHandler.java`

**Steps:**

1. Create `ResourceNotFoundException.java`:

```java
package com.cardwise.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

2. Create `AiGenerationException.java`:

```java
package com.cardwise.exception;

public class AiGenerationException extends RuntimeException {
    public AiGenerationException(String message) {
        super(message);
    }
    public AiGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

3. Create `GlobalExceptionHandler.java`:

```java
package com.cardwise.exception;

import com.cardwise.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message));
    }

    @ExceptionHandler(AiGenerationException.class)
    public ResponseEntity<ErrorResponse> handleAiGeneration(AiGenerationException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("AI generation failed: " + e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }
}
```

---

### Task 1.7 — Security Configuration

**File to create:** `cardwise-server/src/main/java/com/cardwise/config/SecurityConfig.java`

```java
package com.cardwise.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        UUID userId = jwtUtil.getUserIdFromToken(token);
                        JwtAuthenticationToken authentication =
                                new JwtAuthenticationToken(userId);
                        org.springframework.security.core.context.SecurityContextHolder
                                .getContext().setAuthentication(authentication);
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
```

Also create `JwtAuthenticationToken.java`:

```java
package com.cardwise.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.util.Collections;
import java.util.UUID;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final UUID userId;

    public JwtAuthenticationToken(UUID userId) {
        super(Collections.emptyList());
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return userId; }

    public UUID getUserId() { return userId; }
}
```

---

### Task 1.8 — SM-2 Algorithm + Service Layer

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/service/SM2Algorithm.java`
- `cardwise-server/src/main/java/com/cardwise/service/AuthService.java`
- `cardwise-server/src/main/java/com/cardwise/service/CardService.java`
- `cardwise-server/src/main/java/com/cardwise/service/StatsService.java`

**Steps:**

1. Create `SM2Algorithm.java`:

```java
package com.cardwise.service;

import com.cardwise.dto.SM2ResultResponse;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SM2Algorithm {

    public SM2ResultResponse calculate(int quality, double easeFactor, int intervalDays, int repetitions) {
        if (quality < 1 || quality > 4) {
            throw new IllegalArgumentException("Quality must be between 1 and 4");
        }

        int newRepetitions;
        int newInterval;

        if (quality < 3) {
            // Again(1) or Hard(2): reset
            newRepetitions = 0;
            newInterval = 1;
        } else {
            newRepetitions = repetitions + 1;
            if (newRepetitions == 1) {
                newInterval = 1;
            } else if (newRepetitions == 2) {
                newInterval = 6;
            } else {
                newInterval = (int) Math.round(intervalDays * easeFactor);
            }
        }

        // Update ease factor
        double newEase = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (newEase < 1.3) {
            newEase = 1.3;
        }

        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(newInterval);

        return new SM2ResultResponse(newEase, newInterval, newRepetitions, nextReviewAt);
    }
}
```

2. Create `AuthService.java`:

```java
package com.cardwise.service;

import com.cardwise.config.JwtUtil;
import com.cardwise.dto.AuthResponse;
import com.cardwise.dto.LoginRequest;
import com.cardwise.dto.RegisterRequest;
import com.cardwise.model.User;
import com.cardwise.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }
}
```

3. Create `CardService.java`:

```java
package com.cardwise.service;

import com.cardwise.dto.CardRequest;
import com.cardwise.dto.ReviewRequest;
import com.cardwise.dto.SM2ResultResponse;
import com.cardwise.exception.ResourceNotFoundException;
import com.cardwise.model.Card;
import com.cardwise.model.ReviewLog;
import com.cardwise.repository.CardRepository;
import com.cardwise.repository.DeckRepository;
import com.cardwise.repository.ReviewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final SM2Algorithm sm2Algorithm;

    public CardService(CardRepository cardRepository, DeckRepository deckRepository,
                       ReviewLogRepository reviewLogRepository, SM2Algorithm sm2Algorithm) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.sm2Algorithm = sm2Algorithm;
    }

    public List<Card> getCardsByDeck(UUID deckId, UUID userId) {
        verifyDeckOwnership(deckId, userId);
        return cardRepository.findByDeckIdOrderByCreatedAtAsc(deckId);
    }

    public Card createCard(UUID deckId, CardRequest request, UUID userId) {
        verifyDeckOwnership(deckId, userId);
        Card card = new Card();
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setTags(request.getTags());
        card.setDeckId(deckId);
        card.setUserId(userId);
        card.setNextReviewAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    public Card updateCard(UUID cardId, CardRequest request, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setTags(request.getTags());
        return cardRepository.save(card);
    }

    public void deleteCard(UUID cardId, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        cardRepository.delete(card);
    }

    public List<Card> getDueCards(UUID userId, UUID deckId) {
        if (deckId != null) {
            verifyDeckOwnership(deckId, userId);
            return cardRepository.findDueCardsByDeckIdAndUserId(deckId, userId, LocalDateTime.now());
        }
        return cardRepository.findDueCardsByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public SM2ResultResponse reviewCard(UUID cardId, ReviewRequest request, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        int quality = request.getQuality();

        SM2ResultResponse result = sm2Algorithm.calculate(
                quality, card.getEaseFactor(), card.getIntervalDays(), card.getRepetitions());

        card.setEaseFactor(result.getNewEaseFactor());
        card.setIntervalDays(result.getNewIntervalDays());
        card.setRepetitions(result.getNewRepetitions());
        card.setNextReviewAt(result.getNextReviewAt());
        card.setLastReviewAt(LocalDateTime.now());
        cardRepository.save(card);

        ReviewLog log = new ReviewLog();
        log.setCardId(cardId);
        log.setUserId(userId);
        log.setQuality(quality);
        log.setEaseFactor(result.getNewEaseFactor());
        log.setIntervalDays(result.getNewIntervalDays());
        log.setRepetitions(result.getNewRepetitions());
        reviewLogRepository.save(log);

        return result;
    }

    private Card findCardByIdAndUserId(UUID cardId, UUID userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        if (!card.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Card not found");
        }
        return card;
    }

    private void verifyDeckOwnership(UUID deckId, UUID userId) {
        if (!deckRepository.existsByIdAndUserId(deckId, userId)) {
            throw new ResourceNotFoundException("Deck not found");
        }
    }
}
```

4. Create `StatsService.java`:

```java
package com.cardwise.service;

import com.cardwise.dto.StatsResponse;
import com.cardwise.repository.CardRepository;
import com.cardwise.repository.ReviewLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatsService {

    private final CardRepository cardRepository;
    private final ReviewLogRepository reviewLogRepository;

    public StatsService(CardRepository cardRepository, ReviewLogRepository reviewLogRepository) {
        this.cardRepository = cardRepository;
        this.reviewLogRepository = reviewLogRepository;
    }

    public StatsResponse getStats(UUID userId) {
        StatsResponse stats = new StatsResponse();
        stats.setTotalCards(cardRepository.countByUserId(userId));
        stats.setDueCards(cardRepository.findDueCardsByUserId(userId, LocalDateTime.now()).size());
        stats.setStudiedToday(
                reviewLogRepository.countByUserIdAndReviewedAtAfter(userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));
        stats.setMasteredCards(cardRepository.countMasteredByUserId(userId));

        List<Map<String, Object>> activity = new ArrayList<>();
        List<Object[]> rawActivity = reviewLogRepository.countDailyActivity(userId, LocalDateTime.now().minusDays(30));
        for (Object[] row : rawActivity) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0].toString());
            entry.put("count", ((Number) row[1]).longValue());
            activity.add(entry);
        }
        stats.setDailyActivity(activity);
        return stats;
    }
}
```

---

### Task 1.9 — AI Provider Layer

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/ai/AiProvider.java`
- `cardwise-server/src/main/java/com/cardwise/ai/AiProperties.java`
- `cardwise-server/src/main/java/com/cardwise/ai/DeepSeekAiProvider.java`
- `cardwise-server/src/main/java/com/cardwise/ai/AiProviderFactory.java`

**Steps:**

1. Create `AiProvider.java`:

```java
package com.cardwise.ai;

import java.util.List;
import java.util.Map;

public interface AiProvider {
    String getProviderName();
    List<Map<String, String>> generateCards(String source, String sourceType);
}
```

2. Create `AiProperties.java`:

```java
package com.cardwise.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "cardwise.ai")
public class AiProperties {
    private String provider;
    private Map<String, ProviderConfig> providers = new HashMap<>();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Map<String, ProviderConfig> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderConfig> providers) { this.providers = providers; }

    public static class ProviderConfig {
        private String apiUrl;
        private String apiKey;
        private String model;

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
```

3. Create `DeepSeekAiProvider.java`:

```java
package com.cardwise.ai;

import com.cardwise.exception.AiGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@ConditionalOnProperty(name = "cardwise.ai.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekAiProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a professional Flashcard generator. Extract key concepts from the user's learning material and create question-answer flashcards.

            Each flashcard has:
            - front: A clear question or term
            - back: The corresponding answer or explanation, concise and accurate

            Requirements:
            1. Extract the most important concepts, definitions, formulas, and processes
            2. Each card focuses on one knowledge point
            3. Questions should be specific, answers should be accurate
            4. Use the same language as the source material (Chinese material → Chinese, English material → English)
            5. Return JSON array format: [{"front": "question", "back": "answer"}]
            6. Generate 5-15 cards depending on material length
            """;

    public DeepSeekAiProvider(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public List<Map<String, String>> generateCards(String source, String sourceType) {
        AiProperties.ProviderConfig config = aiProperties.getProviders().get("deepseek");
        if (config == null || config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new AiGenerationException("DeepSeek API key is not configured");
        }

        String url = config.getApiUrl() + "/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel() != null ? config.getModel() : "deepseek-chat");
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 4096);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.add(Map.of("role", "user", "content", source));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("choices")) {
                throw new AiGenerationException("Empty response from AI provider");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices.isEmpty()) {
                throw new AiGenerationException("No choices in AI response");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            // Extract JSON array from response (handle markdown code blocks)
            String jsonStr = content;
            if (content.contains("```json")) {
                jsonStr = content.substring(content.indexOf("```json") + 7, content.lastIndexOf("```"));
            } else if (content.contains("```")) {
                jsonStr = content.substring(content.indexOf("```") + 3, content.lastIndexOf("```"));
            }
            jsonStr = jsonStr.trim();

            return objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, String>>>() {});

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiGenerationException("Failed to generate cards: " + e.getMessage(), e);
        }
    }
}
```

4. Create `AiProviderFactory.java`:

```java
package com.cardwise.ai;

import com.cardwise.exception.AiGenerationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiProviderFactory {

    private final List<AiProvider> providers;
    private final AiProperties aiProperties;

    public AiProviderFactory(List<AiProvider> providers, AiProperties aiProperties) {
        this.providers = providers;
        this.aiProperties = aiProperties;
    }

    public AiProvider getProvider() {
        String activeProvider = aiProperties.getProvider();
        return providers.stream()
                .filter(p -> p.getProviderName().equals(activeProvider))
                .findFirst()
                .orElseThrow(() -> new AiGenerationException(
                        "No AI provider found for: " + activeProvider));
    }
}
```

---

### Task 1.10 — REST Controllers

**Files to create:**
- `cardwise-server/src/main/java/com/cardwise/controller/AuthController.java`
- `cardwise-server/src/main/java/com/cardwise/controller/DeckController.java`
- `cardwise-server/src/main/java/com/cardwise/controller/CardController.java`
- `cardwise-server/src/main/java/com/cardwise/controller/AiController.java`
- `cardwise-server/src/main/java/com/cardwise/controller/StatsController.java`

**Steps:**

1. Create `AuthController.java`:

```java
package com.cardwise.controller;

import com.cardwise.dto.AuthResponse;
import com.cardwise.dto.LoginRequest;
import com.cardwise.dto.RegisterRequest;
import com.cardwise.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

2. Create `DeckController.java`:

```java
package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.DeckRequest;
import com.cardwise.exception.ResourceNotFoundException;
import com.cardwise.model.Deck;
import com.cardwise.repository.DeckRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckRepository deckRepository;

    public DeckController(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @GetMapping
    public ResponseEntity<List<Deck>> getAllDecks(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(deckRepository.findByUserIdOrderByCreatedAtDesc(auth.getUserId()));
    }

    @PostMapping
    public ResponseEntity<Deck> createDeck(@Valid @RequestBody DeckRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = new Deck();
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setColor(request.getColor() != null ? request.getColor() : "#6366f1");
        deck.setUserId(auth.getUserId());
        return ResponseEntity.ok(deckRepository.save(deck));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deck> updateDeck(@PathVariable UUID id,
                                           @Valid @RequestBody DeckRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
        if (!deck.getUserId().equals(auth.getUserId())) {
            throw new ResourceNotFoundException("Deck not found");
        }
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setColor(request.getColor() != null ? request.getColor() : deck.getColor());
        return ResponseEntity.ok(deckRepository.save(deck));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable UUID id,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
        if (!deck.getUserId().equals(auth.getUserId())) {
            throw new ResourceNotFoundException("Deck not found");
        }
        deckRepository.delete(deck);
        return ResponseEntity.noContent().build();
    }
}
```

3. Create `CardController.java`:

```java
package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.CardRequest;
import com.cardwise.dto.ReviewRequest;
import com.cardwise.dto.SM2ResultResponse;
import com.cardwise.model.Card;
import com.cardwise.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<Card>> getCards(@PathVariable UUID deckId,
                                               @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.getCardsByDeck(deckId, auth.getUserId()));
    }

    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<Card> createCard(@PathVariable UUID deckId,
                                           @Valid @RequestBody CardRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.createCard(deckId, request, auth.getUserId()));
    }

    @PutMapping("/api/cards/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable UUID id,
                                           @Valid @RequestBody CardRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.updateCard(id, request, auth.getUserId()));
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        cardService.deleteCard(id, auth.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/cards/due")
    public ResponseEntity<List<Card>> getDueCards(@RequestParam(required = false) UUID deckId,
                                                  @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.getDueCards(auth.getUserId(), deckId));
    }

    @PostMapping("/api/cards/{id}/review")
    public ResponseEntity<SM2ResultResponse> reviewCard(@PathVariable UUID id,
                                                        @Valid @RequestBody ReviewRequest request,
                                                        @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.reviewCard(id, request, auth.getUserId()));
    }
}
```

4. Create `AiController.java`:

```java
package com.cardwise.controller;

import com.cardwise.ai.AiProviderFactory;
import com.cardwise.config.JwtAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiProviderFactory aiProviderFactory;

    public AiController(AiProviderFactory aiProviderFactory) {
        this.aiProviderFactory = aiProviderFactory;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, List<Map<String, String>>>> generate(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal JwtAuthenticationToken auth) {

        String source = request.get("source");
        String sourceType = request.getOrDefault("sourceType", "text");

        if (source == null || source.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("cards", List.of()));
        }

        List<Map<String, String>> cards = aiProviderFactory.getProvider().generateCards(source, sourceType);
        return ResponseEntity.ok(Map.of("cards", cards));
    }
}
```

5. Create `StatsController.java`:

```java
package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.StatsResponse;
import com.cardwise.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(statsService.getStats(auth.getUserId()));
    }
}
```

---

### Task 1.11 — Backend Smoke Test

**Steps:**

1. Run `cd cardwise-server && mvn compile` — verify no compile errors.
2. Start PostgreSQL (or confirm Neon URL env vars are set) then run `mvn spring-boot:run` — verify app starts on port 8080 without errors. (Set env vars: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`.)

---

## Phase 2: Frontend Foundation

### Task 2.1 — Vue 3 Project Scaffold

**Files to create:**
- `cardwise-vue/package.json`
- `cardwise-vue/vite.config.js`
- `cardwise-vue/tailwind.config.js`
- `cardwise-vue/postcss.config.js`
- `cardwise-vue/index.html`
- `cardwise-vue/src/main.js`
- `cardwise-vue/src/App.vue`
- `cardwise-vue/src/assets/main.css`

**Steps:**

1. Create `cardwise-vue/package.json`:

```json
{
  "name": "cardwise-vue",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0",
    "tailwindcss": "^3.4.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0"
  }
}
```

2. Create `cardwise-vue/vite.config.js`:

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```

3. Create `cardwise-vue/tailwind.config.js`:

```js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

4. Create `cardwise-vue/postcss.config.js`:

```js
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

5. Create `cardwise-vue/index.html`:

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>CardWise - AI Learning Assistant</title>
</head>
<body class="bg-white">
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

6. Create `cardwise-vue/src/main.js`:

```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

7. Create `cardwise-vue/src/App.vue`:

```vue
<template>
  <router-view />
</template>
```

8. Create `cardwise-vue/src/assets/main.css`:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

9. Verify: `cd cardwise-vue && npm install && npm run dev` starts dev server.

---

### Task 2.2 — Axios API Client + Router

**Files to create:**
- `cardwise-vue/src/api/index.js`
- `cardwise-vue/src/router/index.js`

**Steps:**

1. Create `cardwise-vue/src/api/index.js`:

```js
import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/auth/login'
    }
    return Promise.reject(error)
  }
)

export default api
```

2. Create `cardwise-vue/src/router/index.js`:

```js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomePage.vue')
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('../views/auth/LoginPage.vue')
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('../views/auth/RegisterPage.vue')
  },
  {
    path: '/dashboard',
    component: () => import('../views/dashboard/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/dashboard/IndexPage.vue')
      },
      {
        path: 'decks',
        name: 'Decks',
        component: () => import('../views/decks/DecksPage.vue')
      },
      {
        path: 'decks/new',
        name: 'NewDeck',
        component: () => import('../views/decks/NewDeckPage.vue')
      },
      {
        path: 'decks/:id',
        name: 'DeckDetail',
        component: () => import('../views/decks/DeckDetailPage.vue'),
        props: true
      },
      {
        path: 'decks/:id/study',
        name: 'DeckStudy',
        component: () => import('../views/study/StudyPage.vue'),
        props: true
      },
      {
        path: 'study',
        name: 'Study',
        component: () => import('../views/study/StudyPage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/auth/login')
  } else {
    next()
  }
})

export default router
```

---

### Task 2.3 — Pinia Stores

**Files to create:**
- `cardwise-vue/src/stores/auth.js`
- `cardwise-vue/src/stores/decks.js`
- `cardwise-vue/src/stores/cards.js`
- `cardwise-vue/src/stores/stats.js`

**Steps:**

1. Create `cardwise-vue/src/stores/auth.js`:

```js
import { defineStore } from 'pinia'
import api from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    userName: (state) => state.user?.name || '',
    userEmail: (state) => state.user?.email || ''
  },
  actions: {
    async login(email, password) {
      const { data } = await api.post('/auth/login', { email, password })
      this.token = data.token
      this.user = { userId: data.userId, email: data.email, name: data.name }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async register(email, password, name) {
      const { data } = await api.post('/auth/register', { email, password, name })
      this.token = data.token
      this.user = { userId: data.userId, email: data.email, name: data.name }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
```

2. Create `cardwise-vue/src/stores/decks.js`:

```js
import { defineStore } from 'pinia'
import api from '../api'

export const useDecksStore = defineStore('decks', {
  state: () => ({
    decks: [],
    currentDeck: null
  }),
  actions: {
    async fetchDecks() {
      const { data } = await api.get('/decks')
      this.decks = data
    },
    async createDeck(deckData) {
      const { data } = await api.post('/decks', deckData)
      this.decks.unshift(data)
      return data
    },
    async updateDeck(id, deckData) {
      const { data } = await api.put(`/decks/${id}`, deckData)
      const index = this.decks.findIndex(d => d.id === id)
      if (index !== -1) this.decks[index] = data
      if (this.currentDeck?.id === id) this.currentDeck = data
      return data
    },
    async deleteDeck(id) {
      await api.delete(`/decks/${id}`)
      this.decks = this.decks.filter(d => d.id !== id)
    },
    async fetchDeck(id) {
      if (this.decks.length === 0) await this.fetchDecks()
      this.currentDeck = this.decks.find(d => d.id === id) || null
      return this.currentDeck
    }
  }
})
```

3. Create `cardwise-vue/src/stores/cards.js`:

```js
import { defineStore } from 'pinia'
import api from '../api'

export const useCardsStore = defineStore('cards', {
  state: () => ({
    cards: [],
    dueCards: [],
    loading: false
  }),
  actions: {
    async fetchCards(deckId) {
      const { data } = await api.get(`/decks/${deckId}/cards`)
      this.cards = data
    },
    async fetchDueCards(deckId = null) {
      const params = deckId ? { deckId } : {}
      const { data } = await api.get('/cards/due', { params })
      this.dueCards = data
    },
    async createCard(deckId, cardData) {
      const { data } = await api.post(`/decks/${deckId}/cards`, cardData)
      this.cards.push(data)
      return data
    },
    async updateCard(id, cardData) {
      const { data } = await api.put(`/cards/${id}`, cardData)
      const index = this.cards.findIndex(c => c.id === id)
      if (index !== -1) this.cards[index] = data
      return data
    },
    async deleteCard(id) {
      await api.delete(`/cards/${id}`)
      this.cards = this.cards.filter(c => c.id !== id)
    },
    async reviewCard(id, quality) {
      const { data } = await api.post(`/cards/${id}/review`, { quality })
      return data
    },
    async generateCards(source, sourceType = 'text') {
      this.loading = true
      try {
        const { data } = await api.post('/ai/generate', { source, sourceType })
        return data.cards
      } finally {
        this.loading = false
      }
    }
  }
})
```

4. Create `cardwise-vue/src/stores/stats.js`:

```js
import { defineStore } from 'pinia'
import api from '../api'

export const useStatsStore = defineStore('stats', {
  state: () => ({
    totalCards: 0,
    dueCards: 0,
    studiedToday: 0,
    masteredCards: 0,
    dailyActivity: []
  }),
  actions: {
    async fetchStats() {
      const { data } = await api.get('/stats')
      this.totalCards = data.totalCards
      this.dueCards = data.dueCards
      this.studiedToday = data.studiedToday
      this.masteredCards = data.masteredCards
      this.dailyActivity = data.dailyActivity
    }
  }
})
```

---

### Task 2.4 — HomePage + Auth Pages

**Files to create:**
- `cardwise-vue/src/views/HomePage.vue`
- `cardwise-vue/src/views/auth/LoginPage.vue`
- `cardwise-vue/src/views/auth/RegisterPage.vue`

**Steps:**

1. Create `cardwise-vue/src/views/HomePage.vue`:

```vue
<template>
  <div class="min-h-screen bg-white">
    <div class="max-w-3xl mx-auto px-4 py-20 text-center">
      <!-- Logo -->
      <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-900 text-white text-2xl font-bold rounded-xl mb-8">
        C
      </div>
      <h1 class="text-4xl font-bold text-gray-900 mb-4">CardWise</h1>
      <p class="text-lg text-gray-500 mb-2">AI-Powered Flashcard Learning</p>
      <p class="text-gray-400 mb-10">Paste your study material, let AI create flashcards, and master them with smart spaced repetition.</p>

      <div class="flex justify-center gap-4">
        <router-link to="/auth/register"
          class="px-6 py-3 bg-gray-900 text-white rounded-lg hover:bg-gray-800 font-medium">
          Get Started
        </router-link>
        <router-link to="/auth/login"
          class="px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium">
          Sign In
        </router-link>
      </div>
    </div>
  </div>
</template>
```

2. Create `cardwise-vue/src/views/auth/LoginPage.vue`:

```vue
<template>
  <div class="min-h-screen bg-white flex items-center justify-center px-4">
    <div class="w-full max-w-sm border border-gray-200 rounded-xl p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">Sign In</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="email" type="email" required
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input v-model="password" type="password" required minlength="6"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>
        <button type="submit"
          class="w-full py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 font-medium">
          Sign In
        </button>
      </form>
      <p class="text-center text-sm text-gray-500 mt-6">
        Don't have an account?
        <router-link to="/auth/register" class="text-gray-900 font-medium hover:underline">Sign up</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')

async function handleLogin() {
  try {
    error.value = ''
    await auth.login(email.value, password.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.error || 'Login failed'
  }
}
</script>
```

3. Create `cardwise-vue/src/views/auth/RegisterPage.vue`:

```vue
<template>
  <div class="min-h-screen bg-white flex items-center justify-center px-4">
    <div class="w-full max-w-sm border border-gray-200 rounded-xl p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">Create Account</h2>
      <form @submit.prevent="handleRegister">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
          <input v-model="name" type="text" required
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="email" type="email" required
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input v-model="password" type="password" required minlength="6"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>
        <button type="submit"
          class="w-full py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 font-medium">
          Create Account
        </button>
      </form>
      <p class="text-center text-sm text-gray-500 mt-6">
        Already have an account?
        <router-link to="/auth/login" class="text-gray-900 font-medium hover:underline">Sign in</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')

async function handleRegister() {
  try {
    error.value = ''
    await auth.register(email.value, password.value, name.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.error || 'Registration failed'
  }
}
</script>
```

---

### Task 2.5 — Dashboard Layout

**File to create:** `cardwise-vue/src/views/dashboard/DashboardLayout.vue`

```vue
<template>
  <div class="min-h-screen bg-white flex">
    <!-- Sidebar -->
    <aside class="w-64 border-r border-gray-200 flex flex-col">
      <div class="p-6">
        <router-link to="/dashboard" class="flex items-center gap-2">
          <div class="w-8 h-8 bg-gray-900 text-white text-sm font-bold rounded-lg flex items-center justify-center">C</div>
          <span class="text-lg font-bold text-gray-900">CardWise</span>
        </router-link>
      </div>
      <nav class="flex-1 px-4 space-y-1">
        <router-link to="/dashboard"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path === '/dashboard' }">
          <span>Dashboard</span>
        </router-link>
        <router-link to="/dashboard/decks"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path.startsWith('/dashboard/decks') }">
          <span>My Decks</span>
        </router-link>
        <router-link to="/dashboard/study"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path === '/dashboard/study' }">
          <span>Study</span>
        </router-link>
      </nav>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col">
      <!-- Top bar -->
      <header class="h-16 border-b border-gray-200 flex items-center justify-end px-6 gap-4">
        <span class="text-sm text-gray-500">{{ auth.userEmail }}</span>
        <button @click="handleLogout" class="text-sm text-gray-500 hover:text-gray-700">Sign out</button>
      </header>

      <!-- Page content -->
      <main class="flex-1 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>
```

---

### Task 2.6 — Dashboard Index Page (Stats)

**Files to create:**
- `cardwise-vue/src/views/dashboard/IndexPage.vue`
- `cardwise-vue/src/views/dashboard/components/StatCard.vue`

**Steps:**

1. Create `cardwise-vue/src/views/dashboard/components/StatCard.vue`:

```vue
<template>
  <div class="border border-gray-200 rounded-xl p-5">
    <p class="text-sm text-gray-500 mb-1">{{ label }}</p>
    <p class="text-3xl font-bold text-gray-900">{{ value }}</p>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [Number, String]
})
</script>
```

2. Create `cardwise-vue/src/views/dashboard/IndexPage.vue`:

```vue
<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

    <!-- Stat cards -->
    <div class="grid grid-cols-4 gap-4 mb-8">
      <StatCard label="Total Cards" :value="stats.totalCards" />
      <StatCard label="Due for Review" :value="stats.dueCards" />
      <StatCard label="Studied Today" :value="stats.studiedToday" />
      <StatCard label="Mastered" :value="stats.masteredCards" />
    </div>

    <!-- Due cards list -->
    <div class="border border-gray-200 rounded-xl p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Cards Due for Review</h2>
      <div v-if="dueCards.length === 0" class="text-gray-400 text-sm py-8 text-center">
        No cards due for review. Great job!
      </div>
      <div v-else class="space-y-2">
        <div v-for="card in dueCards" :key="card.id"
          class="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
          <p class="text-sm text-gray-700 truncate flex-1">{{ card.front }}</p>
          <router-link :to="`/dashboard/decks/${card.deckId}/study`"
            class="text-sm text-gray-900 font-medium hover:underline ml-4">
            Study
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStatsStore } from '../../stores/stats'
import { useCardsStore } from '../../stores/cards'
import StatCard from './components/StatCard.vue'

const stats = useStatsStore()
const cardsStore = useCardsStore()

onMounted(async () => {
  await Promise.all([
    stats.fetchStats(),
    cardsStore.fetchDueCards()
  ])
})

const dueCards = cardsStore.dueCards
</script>
```

---

### Task 2.7 — Decks Pages

**Files to create:**
- `cardwise-vue/src/views/decks/DecksPage.vue`
- `cardwise-vue/src/views/decks/NewDeckPage.vue`

**Steps:**

1. Create `cardwise-vue/src/views/decks/DecksPage.vue`:

```vue
<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">My Decks</h1>
      <router-link to="/dashboard/decks/new"
        class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
        New Deck
      </router-link>
    </div>

    <div v-if="decksStore.decks.length === 0" class="text-center py-20 text-gray-400">
      <p class="mb-4">No decks yet. Create your first one!</p>
      <router-link to="/dashboard/decks/new"
        class="text-gray-900 font-medium hover:underline">
        Create a deck
      </router-link>
    </div>

    <div v-else class="grid grid-cols-3 gap-4">
      <div v-for="deck in decksStore.decks" :key="deck.id"
        class="border border-gray-200 rounded-xl p-5 hover:border-gray-300 cursor-pointer"
        @click="$router.push(`/dashboard/decks/${deck.id}`)">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm mb-3"
          :style="{ backgroundColor: deck.color || '#6366f1' }">
          {{ deck.name.charAt(0).toUpperCase() }}
        </div>
        <h3 class="font-semibold text-gray-900 mb-1">{{ deck.name }}</h3>
        <p v-if="deck.description" class="text-sm text-gray-500 truncate">{{ deck.description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDecksStore } from '../../stores/decks'

const decksStore = useDecksStore()
onMounted(() => decksStore.fetchDecks())
</script>
```

2. Create `cardwise-vue/src/views/decks/NewDeckPage.vue`:

```vue
<template>
  <div class="max-w-lg">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">New Deck</h1>
    <form @submit.prevent="handleCreate" class="border border-gray-200 rounded-xl p-6 space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input v-model="name" required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Description (optional)</label>
        <textarea v-model="description" rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900"></textarea>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Color</label>
        <input v-model="color" type="color"
          class="w-10 h-10 p-0 border border-gray-300 rounded cursor-pointer" />
        <span class="ml-2 text-sm text-gray-500">{{ color }}</span>
      </div>
      <div class="flex gap-3 pt-2">
        <button type="submit"
          class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
          Create
        </button>
        <router-link to="/dashboard/decks"
          class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDecksStore } from '../../stores/decks'

const router = useRouter()
const decksStore = useDecksStore()

const name = ref('')
const description = ref('')
const color = ref('#6366f1')

async function handleCreate() {
  await decksStore.createDeck({ name: name.value, description: description.value, color: color.value })
  router.push('/dashboard/decks')
}
</script>
```

---

### Task 2.8 — Deck Detail Page (Card Management + AI Generation)

**Files to create:**
- `cardwise-vue/src/views/decks/DeckDetailPage.vue`
- `cardwise-vue/src/views/decks/components/AiGeneratePanel.vue`

**Steps:**

1. Create `cardwise-vue/src/views/decks/components/AiGeneratePanel.vue`:

```vue
<template>
  <div class="border border-gray-200 rounded-xl p-5">
    <h3 class="font-semibold text-gray-900 mb-3">AI Generate Cards</h3>
    <textarea v-model="source" placeholder="Paste your study material here..."
      rows="5"
      class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 mb-3"></textarea>
    <button @click="handleGenerate" :disabled="loading || !source.trim()"
      class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 disabled:opacity-50 text-sm font-medium">
      {{ loading ? 'Generating...' : 'Generate Cards' }}
    </button>

    <!-- Preview generated cards -->
    <div v-if="generatedCards.length > 0" class="mt-4 space-y-3">
      <h4 class="text-sm font-medium text-gray-700">Preview ({{ generatedCards.length }} cards)</h4>
      <div v-for="(card, index) in generatedCards" :key="index"
        class="border border-gray-200 rounded-lg p-3">
        <div class="mb-2">
          <label class="text-xs text-gray-400">Front</label>
          <input v-model="card.front"
            class="w-full text-sm text-gray-900 border border-gray-200 rounded px-2 py-1 mt-1" />
        </div>
        <div>
          <label class="text-xs text-gray-400">Back</label>
          <input v-model="card.back"
            class="w-full text-sm text-gray-900 border border-gray-200 rounded px-2 py-1 mt-1" />
        </div>
      </div>
      <button @click="handleSaveAll"
        class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium">
        Save All to Deck
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mt-3">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCardsStore } from '../../stores/cards'

const props = defineProps({
  deckId: { type: String, required: true }
})
const emit = defineEmits(['saved'])

const cardsStore = useCardsStore()
const source = ref('')
const generatedCards = ref([])
const loading = ref(false)
const error = ref('')

async function handleGenerate() {
  error.value = ''
  try {
    generatedCards.value = await cardsStore.generateCards(source.value)
  } catch (e) {
    error.value = e.response?.data?.error || 'Generation failed'
  }
}

async function handleSaveAll() {
  for (const card of generatedCards.value) {
    await cardsStore.createCard(props.deckId, {
      front: card.front,
      back: card.back,
      tags: ''
    })
  }
  generatedCards.value = []
  source.value = ''
  emit('saved')
}
</script>
```

2. Create `cardwise-vue/src/views/decks/DeckDetailPage.vue`:

```vue
<template>
  <div v-if="deck">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm"
          :style="{ backgroundColor: deck.color }">
          {{ deck.name.charAt(0).toUpperCase() }}
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{{ deck.name }}</h1>
          <p v-if="deck.description" class="text-sm text-gray-500">{{ deck.description }}</p>
        </div>
      </div>
      <div class="flex gap-2">
        <router-link :to="`/dashboard/decks/${deck.id}/study`"
          class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
          Study
        </router-link>
      </div>
    </div>

    <!-- AI Generate -->
    <div class="mb-8">
      <AiGeneratePanel :deck-id="deck.id" @saved="refreshCards" />
    </div>

    <!-- Manual add card -->
    <div class="border border-gray-200 rounded-xl p-5 mb-8">
      <h3 class="font-semibold text-gray-900 mb-3">Add Card Manually</h3>
      <div class="grid grid-cols-2 gap-4 mb-3">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Front</label>
          <input v-model="newFront"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Back</label>
          <input v-model="newBack"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
      </div>
      <button @click="handleAddCard" :disabled="!newFront || !newBack"
        class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50 text-sm font-medium">
        Add Card
      </button>
    </div>

    <!-- Cards list -->
    <div class="border border-gray-200 rounded-xl p-5">
      <h3 class="font-semibold text-gray-900 mb-3">Cards ({{ cards.length }})</h3>
      <div v-if="cards.length === 0" class="text-gray-400 text-sm py-8 text-center">
        No cards yet. Generate with AI or add manually.
      </div>
      <div v-else class="space-y-2">
        <div v-for="card in cards" :key="card.id"
          class="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ card.front }}</p>
            <p class="text-sm text-gray-500 truncate">{{ card.back }}</p>
          </div>
          <button @click="handleDeleteCard(card.id)"
            class="text-sm text-red-500 hover:text-red-700 ml-4">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useDecksStore } from '../../stores/decks'
import { useCardsStore } from '../../stores/cards'
import AiGeneratePanel from './components/AiGeneratePanel.vue'

const route = useRoute()
const decksStore = useDecksStore()
const cardsStore = useCardsStore()

const deck = ref(null)
const cards = ref([])
const newFront = ref('')
const newBack = ref('')

onMounted(async () => {
  deck.value = await decksStore.fetchDeck(route.params.id)
  await refreshCards()
})

async function refreshCards() {
  cards.value = await cardsStore.fetchCards(route.params.id)
}

async function handleAddCard() {
  await cardsStore.createCard(route.params.id, {
    front: newFront.value,
    back: newBack.value,
    tags: ''
  })
  newFront.value = ''
  newBack.value = ''
  await refreshCards()
}

async function handleDeleteCard(id) {
  await cardsStore.deleteCard(id)
  await refreshCards()
}
</script>
```

---

### Task 2.9 — Study Page + FlashCard Component

**Files to create:**
- `cardwise-vue/src/views/study/components/FlashCard.vue`
- `cardwise-vue/src/views/study/StudyPage.vue`

**Steps:**

1. Create `cardwise-vue/src/views/study/components/FlashCard.vue`:

```vue
<template>
  <div class="perspective-container" @click="flip">
    <div class="flipper" :class="{ flipped: isFlipped }">
      <!-- Front -->
      <div class="front rounded-xl flex items-center justify-center p-8 cursor-pointer"
        :style="{ backgroundColor: color || '#6366f1' }">
        <p class="text-white text-xl text-center leading-relaxed">{{ front }}</p>
      </div>
      <!-- Back -->
      <div class="back rounded-xl flex flex-col items-center justify-center p-8 cursor-pointer bg-white border border-gray-200">
        <p class="text-gray-900 text-xl text-center leading-relaxed mb-8">{{ back }}</p>
        <div v-if="isFlipped" class="flex gap-3" @click.stop>
          <button @click="$emit('rate', 1)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-red-500 hover:bg-red-600">
            Again
          </button>
          <button @click="$emit('rate', 2)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-orange-500 hover:bg-orange-600">
            Hard
          </button>
          <button @click="$emit('rate', 3)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-green-500 hover:bg-green-600">
            Good
          </button>
          <button @click="$emit('rate', 4)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-blue-500 hover:bg-blue-600">
            Easy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  front: String,
  back: String,
  color: String
})

defineEmits(['rate'])

const isFlipped = ref(false)

function flip() {
  isFlipped.value = !isFlipped.value
}
</script>

<style scoped>
.perspective-container {
  perspective: 1000px;
  width: 100%;
  max-width: 480px;
  height: 320px;
  margin: 0 auto;
}
.flipper {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
  transform-style: preserve-3d;
}
.flipper.flipped {
  transform: rotateY(180deg);
}
.front, .back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
}
.back {
  transform: rotateY(180deg);
}
</style>
```

2. Create `cardwise-vue/src/views/study/StudyPage.vue`:

```vue
<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">
      {{ deckId ? 'Study: ' + (deck?.name || '') : 'Study' }}
    </h1>

    <!-- No cards -->
    <div v-if="dueCards.length === 0" class="text-center py-20 text-gray-400">
      <p class="text-lg mb-2">All caught up!</p>
      <p class="text-sm">No cards due for review.</p>
    </div>

    <!-- Study session -->
    <div v-else-if="currentIndex < dueCards.length" class="flex flex-col items-center">
      <p class="text-sm text-gray-400 mb-4">{{ currentIndex + 1 }} / {{ dueCards.length }}</p>
      <FlashCard
        :front="currentCard.front"
        :back="currentCard.back"
        :color="currentDeckColor"
        @rate="handleRate" />
    </div>

    <!-- Summary -->
    <div v-else class="text-center py-10">
      <h2 class="text-xl font-bold text-gray-900 mb-4">Session Complete!</h2>
      <p class="text-gray-500 mb-2">Reviewed {{ dueCards.length }} cards</p>
      <div class="flex justify-center gap-6 mb-8 text-sm">
        <span class="text-red-500">Again: {{ ratingCounts[1] || 0 }}</span>
        <span class="text-orange-500">Hard: {{ ratingCounts[2] || 0 }}</span>
        <span class="text-green-500">Good: {{ ratingCounts[3] || 0 }}</span>
        <span class="text-blue-500">Easy: {{ ratingCounts[4] || 0 }}</span>
      </div>
      <router-link to="/dashboard"
        class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
        Back to Dashboard
      </router-link>
    </div>

    <p v-if="error" class="text-red-500 text-sm mt-4 text-center">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCardsStore } from '../../stores/cards'
import { useDecksStore } from '../../stores/decks'
import FlashCard from './components/FlashCard.vue'

const route = useRoute()
const cardsStore = useCardsStore()
const decksStore = useDecksStore()

const deckId = computed(() => route.params.id || null)
const deck = ref(null)
const dueCards = ref([])
const currentIndex = ref(0)
const ratingCounts = ref({})
const error = ref('')

const currentCard = computed(() => dueCards.value[currentIndex.value] || {})

const currentDeckColor = computed(() => {
  if (!currentCard.value.deckId) return '#6366f1'
  if (deck.value?.id === currentCard.value.deckId) return deck.value.color
  const d = decksStore.decks.find(d => d.id === currentCard.value.deckId)
  return d?.color || '#6366f1'
})

onMounted(async () => {
  try {
    await cardsStore.fetchDueCards(deckId.value)
    dueCards.value = [...cardsStore.dueCards]
    if (deckId.value) {
      deck.value = await decksStore.fetchDeck(deckId.value)
    }
  } catch (e) {
    error.value = 'Failed to load cards'
  }
})

async function handleRate(quality) {
  const card = currentCard.value
  if (!card?.id) return
  try {
    await cardsStore.reviewCard(card.id, quality)
    ratingCounts.value[quality] = (ratingCounts.value[quality] || 0) + 1
    currentIndex.value++
  } catch (e) {
    error.value = 'Failed to save review'
  }
}
</script>
```

---

### Task 2.10 — Frontend Build Verification

**Steps:**

1. Run `cd cardwise-vue && npm run build` — verify production build succeeds without errors.
2. Run `cd cardwise-vue && npm run dev` — verify dev server starts on port 5173.

---

## Self-Review Checklist

1. **Spec coverage:**
   - [x] User registration/login → AuthController + AuthService
   - [x] Deck CRUD → DeckController + DeckRepository
   - [x] Card CRUD → CardController + CardService
   - [x] SM-2 algorithm → SM2Algorithm.java with quality 1-4
   - [x] Review logging → ReviewLog saved in CardService.reviewCard()
   - [x] Stats endpoint → StatsController + StatsService
   - [x] AI multi-provider strategy → AiProvider interface + DeepSeekAiProvider + AiProviderFactory
   - [x] Config-driven provider switching → AiProperties + @ConditionalOnProperty
   - [x] All frontend pages listed in spec → HomePage, Login, Register, Dashboard, Decks, NewDeck, DeckDetail, Study
   - [x] Color scheme → gray-900 buttons, white backgrounds, border-gray-200, deck.color for cards
   - [x] Study flip interaction → FlashCard.vue with CSS 3D flip
   - [x] Auth guard → router.beforeEach + Axios 401 interceptor
   - [x] Unified error response → GlobalExceptionHandler returns { error: "..." }
   - [x] Vite proxy → /api → localhost:8080

2. **Placeholder scan:** No TBD/TODO found. Every step has complete code.

3. **Type consistency:**
   - SM2ResultResponse fields match what CardService returns
   - AiProvider.generateCards() returns List<Map<String,String>> matching AiController response
   - DTOs use same field names as API spec
   - JwtAuthenticationToken.getUserId() returns UUID matching service signatures

4. **Scope check:** Two phases (backend + frontend) each produce independently testable software.
