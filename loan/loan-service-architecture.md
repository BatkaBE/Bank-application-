# Loan Service - Зээлийн үйлчилгээ Architecture

Энэ файл нь Loan Service-ийн системийн архитектурын ерөнхий харагдах байдлыг тайлбарлана.

## 1. Системийн архитектурын диаграм

```mermaid
graph TB
    subgraph "Client Layer"
        Web[Web Application]
        Mobile[Mobile App]
        API[API Client]
    end
    
    subgraph "API Gateway"
        Gateway[API Gateway]
        LoadBalancer[Load Balancer]
    end
    
    subgraph "Loan Service"
        Controller[Loan Controller]
        Service[Loan Service]
        Repository[Loan Repository]
        Entity[Loan Entity]
    end
    
    subgraph "External Services"
        UserService[User Service]
        NotificationService[Notification Service]
        AuditService[Audit Service]
        PaymentService[Payment Service]
        RateLimitService[Rate Limiting Service]
    end
    
    subgraph "Data Layer"
        Database[(PostgreSQL/MySQL)]
        Cache[(Redis Cache)]
        MessageQueue[Message Queue]
    end
    
    subgraph "Infrastructure"
        Monitoring[Monitoring & Logging]
        Security[Security Service]
        Config[Configuration Service]
    end
    
    Web --> Gateway
    Mobile --> Gateway
    API --> Gateway
    
    Gateway --> LoadBalancer
    LoadBalancer --> Controller
    
    Controller --> Service
    Service --> Repository
    Repository --> Entity
    Entity --> Database
    
    Service --> UserService
    Service --> NotificationService
    Service --> AuditService
    Service --> PaymentService
    Service --> RateLimitService
    
    Service --> Cache
    Service --> MessageQueue
    
    Service --> Monitoring
    Service --> Security
    Service --> Config
```

## 2. Үйлчилгээний давхарга (Service Layers)

```mermaid
graph LR
    subgraph "Presentation Layer"
        Controller[Loan Controller]
        DTO[DTO Objects]
        Validation[Request Validation]
    end
    
    subgraph "Business Logic Layer"
        Service[Loan Service]
        BusinessRules[Business Rules]
        Calculation[Loan Calculations]
    end
    
    subgraph "Data Access Layer"
        Repository[Loan Repository]
        Entity[Loan Entity]
        Query[Custom Queries]
    end
    
    subgraph "External Integration"
        UserService[User Service]
        Notification[Notification Service]
        Audit[Audit Service]
    end
    
    Controller --> DTO
    DTO --> Validation
    Validation --> Service
    
    Service --> BusinessRules
    Service --> Calculation
    Service --> Repository
    
    Repository --> Entity
    Repository --> Query
    
    Service --> UserService
    Service --> Notification
    Service --> Audit
```

## 3. Бүрэлдэхүүн хэсгүүдийн хариуцлага

### Controller Layer
- **Хүсэлтийг хүлээн авах**: HTTP request-үүдийг хүлээн авах
- **Хүсэлтийг баталгаажуулах**: Request validation
- **Хариу буцаах**: Response formatting
- **Алдааны боловсруулалт**: Error handling

### Service Layer
- **Бизнес логик**: Зээлийн дүрэм, тооцоолол
- **Хүсэлтийг боловсруулах**: Request processing
- **Гадаад үйлчилгээтэй холбогдох**: External service integration
- **Аудит**: Audit logging

### Repository Layer
- **Өгөгдлийн хандалт**: Data access
- **CRUD үйлдлүүд**: Create, Read, Update, Delete
- **Хайлтын функцүүд**: Search functions
- **Өгөгдлийн баталгаажуулалт**: Data validation

## 4. Өгөгдлийн урсгал (Data Flow)

```mermaid
flowchart TD
    A[Client Request] --> B[API Gateway]
    B --> C[Load Balancer]
    C --> D[Loan Controller]
    
    D --> E{Request Valid?}
    E -->|No| F[Return Error]
    E -->|Yes| G[Loan Service]
    
    G --> H{Business Logic Valid?}
    H -->|No| I[Return Business Error]
    H -->|Yes| J[Process Request]
    
    J --> K[Update Database]
    K --> L[Send Notifications]
    L --> M[Log Audit]
    
    M --> N[Return Success Response]
    
    F --> O[Client]
    I --> O
    N --> O
```

## 5. Аюулгүй байдлын онцлогууд

```mermaid
graph TB
    subgraph "Security Features"
        Auth[Authentication]
        Authz[Authorization]
        RateLimit[Rate Limiting]
        Encryption[Data Encryption]
        Audit[Audit Logging]
    end
    
    subgraph "Security Layers"
        Gateway[API Gateway Security]
        Service[Service Level Security]
        Data[Data Level Security]
    end
    
    Gateway --> Auth
    Gateway --> RateLimit
    
    Service --> Authz
    Service --> Audit
    
    Data --> Encryption
    Data --> Audit
```

## 6. Гүйцэтгэлийн хэмжээ (Performance Considerations)

```mermaid
graph LR
    subgraph "Performance Optimizations"
        Cache[Redis Caching]
        Async[Async Processing]
        ConnectionPool[Connection Pooling]
        Indexing[Database Indexing]
    end
    
    subgraph "Monitoring"
        Metrics[Performance Metrics]
        Alerts[Alert System]
        Logs[Structured Logging]
    end
    
    Cache --> Metrics
    Async --> Metrics
    ConnectionPool --> Metrics
    Indexing --> Metrics
    
    Metrics --> Alerts
    Metrics --> Logs
```

## 7. Хяналт, мэдээлэл (Monitoring & Observability)

```mermaid
graph TB
    subgraph "Monitoring Stack"
        Prometheus[Prometheus]
        Grafana[Grafana]
        ELK[ELK Stack]
        Jaeger[Jaeger Tracing]
    end
    
    subgraph "Metrics"
        Business[Business Metrics]
        Technical[Technical Metrics]
        Infrastructure[Infrastructure Metrics]
    end
    
    subgraph "Alerts"
        Critical[Critical Alerts]
        Warning[Warning Alerts]
        Info[Info Alerts]
    end
    
    Prometheus --> Business
    Prometheus --> Technical
    Prometheus --> Infrastructure
    
    Business --> Critical
    Technical --> Warning
    Infrastructure --> Info
    
    Grafana --> Prometheus
    ELK --> Prometheus
    Jaeger --> Prometheus
```

## 8. Суулгац, ашиглалт (Deployment Architecture)

```mermaid
graph TB
    subgraph "Deployment"
        Docker[Docker Containers]
        Kubernetes[Kubernetes]
        Helm[Helm Charts]
    end
    
    subgraph "Infrastructure"
        Cloud[Cloud Provider]
        VPC[Virtual Private Cloud]
        Subnets[Subnets]
    end
    
    subgraph "Scaling"
        HPA[Horizontal Pod Autoscaler]
        VPA[Vertical Pod Autoscaler]
        Cluster[Cluster Autoscaler]
    end
    
    Docker --> Kubernetes
    Kubernetes --> Helm
    Helm --> Cloud
    
    Cloud --> VPC
    VPC --> Subnets
    
    Kubernetes --> HPA
    Kubernetes --> VPA
    Kubernetes --> Cluster
```

## Үндсэн онцлогууд

### Технологийн стек
- **Backend**: Spring Boot, Java
- **Database**: PostgreSQL/MySQL
- **Cache**: Redis
- **Message Queue**: Apache Kafka/RabbitMQ
- **Container**: Docker
- **Orchestration**: Kubernetes
- **Monitoring**: Prometheus, Grafana, ELK Stack

### Архитектурын зарчим
- **Microservices**: Тусдаа үйлчилгээ
- **Layered Architecture**: Давхаргатай архитектур
- **Separation of Concerns**: Хариуцлагын тусгаарлалт
- **SOLID Principles**: SOLID зарчим
- **Event-Driven**: Үйл явдалд суурилсан

### Аюулгүй байдал
- **JWT Authentication**: JWT токен
- **Role-Based Access**: Эрхэд суурилсан хандалт
- **Rate Limiting**: Хурдны хязгаарлалт
- **Audit Logging**: Аудит бүртгэл
- **Data Encryption**: Өгөгдлийн шифрлэлт

