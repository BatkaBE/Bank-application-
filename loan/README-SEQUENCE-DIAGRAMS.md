# Loan Service - Зээлийн үйлчилгээ Documentation

Энэ README файл нь Loan Service-ийн бүх үүсгэгдсэн диаграм, архитектурын тайлбар болон системийн ерөнхий харагдах байдлыг агуулна.

## 📁 Файлуудын жагсаалт

### 1. Sequence Diagrams (PlantUML)
- **`loan-service-sequence-diagram.puml`** - PlantUML форматаар sequence diagram-ууд
- **`loan-service-sequence-diagram.md`** - Mermaid форматаар sequence diagram-ууд

### 2. Architecture Documentation
- **`loan-service-architecture.md`** - Системийн архитектурын ерөнхий харагдах байдал

### 3. README Files
- **`README-SEQUENCE-DIAGRAMS.md`** - Энэ файл (документацийн заавар)

## 🔍 Диаграммуудыг харах

### PlantUML файлуудыг харах
1. **PlantUML Extension** суулгах (VS Code-д)
2. **PlantUML Server** ашиглах
3. **Online PlantUML Editor** ашиглах: http://www.plantuml.com/plantuml/

### Mermaid диаграммуудыг харах
1. **Mermaid Extension** суулгах (VS Code-д)
2. **GitHub** дээр шууд харагдана
3. **Online Mermaid Editor** ашиглах: https://mermaid.live/

## 📊 Үндсэн Sequence Diagram-ууд

### 1. Зээл хүсэх (Loan Application)
- Хэрэглэгчийн зээлийн хүсэлтийг боловсруулах
- Эрхийн шалгалт, зээлийн хэмжээг тооцоолох
- Мэдэгдэх, аудит бүртгэх

### 2. Зээлийн мэдээлэл авах (Get Loan by ID)
- ID-аар зээлийн мэдээллийг хайх
- Өгөгдлийг DTO болгон хөрвүүлэх

### 3. Хэрэглэгчийн бүх зээлүүд (Get All Loans by User)
- Хэрэглэгчийн бүх зээлийг хуудаслаж авах
- Pagination дэмжлэг

### 4. Зээл батлах (Loan Approval)
- Админ эрхээр зээлийг батлах
- Статусыг шинэчлэх, мэдэгдэх

### 5. Зээлийн төлбөр төлөх (Loan Payment Processing)
- Төлбөрийг боловсруулах
- Үлдэгдэл тооцоолох
- Бүрэн төлөгдсөн эсэхийг шалгах

### 6. Зээл татгалзах (Loan Rejection)
- Зээлийг татгалзах
- Шалтгааныг бүртгэх
- Мэдэгдэх

### 7. Алдааны боловсруулалт (Error Handling)
- Validation алдаа
- Business logic алдаа
- System алдаа

## 🏗️ Системийн бүрэлдэхүүн хэсгүүд

### Controller Layer
- **GMTLoanController**: HTTP request-үүдийг хүлээн авах
- **Request Validation**: Хүсэлтийн баталгаажуулалт
- **Response Handling**: Хариу буцаах

### Service Layer
- **GMTLoanService**: Бизнес логик
- **Loan Calculations**: Зээлийн тооцоолол
- **External Integration**: Гадаад үйлчилгээтэй холбогдох

### Repository Layer
- **GMTLoanRepository**: Өгөгдлийн хандалт
- **Custom Queries**: Тусгай хайлтын функцүүд
- **Data Persistence**: Өгөгдлийг хадгалах

### Entity Layer
- **GMTLoanEntity**: Зээлийн өгөгдлийн загвар
- **JPA Annotations**: Database mapping
- **Business Logic**: Бизнес дүрмүүд

## 🔌 API Endpoints

### Зээлийн үйлдлүүд
- `POST /api/loans/apply` - Зээл хүсэх
- `GET /api/loans/{id}` - Зээлийн мэдээлэл авах
- `GET /api/loans/user/{userId}` - Хэрэглэгчийн зээлүүд
- `PUT /api/loans/{id}/approve` - Зээл батлах
- `PUT /api/loans/{id}/reject` - Зээл татгалзах
- `POST /api/loans/{id}/payment` - Төлбөр төлөх

### Хайлтын функцүүд
- `GET /api/loans/search` - Зээл хайх
- `GET /api/loans/status/{status}` - Статусаар хайх
- `GET /api/loans/date-range` - Огноогоор хайх

## 💼 Бизнес процессууд

### Зээлийн хүсэлтийн процесс
1. Хэрэглэгч зээл хүснэ
2. Систем эрхийг шалгана
3. Зээлийн хэмжээг тооцоолно
4. Хүсэлтийг хадгална
5. Мэдэгдэл илгээнэ
6. Аудит бүртгэнэ

### Зээлийн батлалтын процесс
1. Админ хүсэлтийг харна
2. Бизнес дүрмийг шалгана
3. Зээлийг батлана
4. Статусыг шинэчилнэ
5. Мэдэгдэл илгээнэ
6. Аудит бүртгэнэ

### Төлбөрийн процесс
1. Хэрэглэгч төлбөр төлнө
2. Систем төлбөрийг шалгана
3. Үлдэгдлийг тооцоолно
4. Өгөгдлийг шинэчилнэ
5. Мэдэгдэл илгээнэ
6. Аудит бүртгэнэ

## 🛡️ Аюулгүй байдлын онцлогууд

### Authentication & Authorization
- **JWT Token**: Хэрэглэгчийн нэвтрэх
- **Role-Based Access**: Эрхэд суурилсан хандалт
- **PreAuthorize**: Spring Security annotation

### Security Measures
- **Rate Limiting**: Хурдны хязгаарлалт
- **Input Validation**: Оролтын баталгаажуулалт
- **SQL Injection Protection**: SQL injection халдлагаас хамгаалах
- **XSS Protection**: Cross-site scripting халдлагаас хамгаалах

## 📈 Гүйцэтгэлийн онцлогууд

### Caching Strategy
- **Redis Cache**: Өгөгдлийн cache
- **Query Result Caching**: Хайлтын үр дүнг cache хийх
- **User Session Caching**: Хэрэглэгчийн session cache

### Database Optimization
- **Indexing**: Database index
- **Connection Pooling**: Холболтын pool
- **Query Optimization**: Хайлтын оновчлол

### Async Processing
- **Message Queue**: Асинхрон боловсруулалт
- **Background Jobs**: Арын ажлууд
- **Event-Driven**: Үйл явдалд суурилсан

## 🔧 Технологийн стек

### Backend Framework
- **Spring Boot**: Үндсэн framework
- **Spring Data JPA**: Database access
- **Spring Security**: Аюулгүй байдал
- **Spring Cloud**: Microservices

### Database & Cache
- **PostgreSQL/MySQL**: Үндсэн database
- **Redis**: Cache, session storage
- **Hibernate**: ORM framework

### Build & Deployment
- **Gradle**: Build tool
- **Docker**: Containerization
- **Kubernetes**: Orchestration
- **Helm**: Package management

### Monitoring & Logging
- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **ELK Stack**: Logging
- **Jaeger**: Distributed tracing

## 🚀 Getting Started

### 1. Системийн шаардлага
- Java 11+
- PostgreSQL/MySQL
- Redis
- Docker (optional)

### 2. Суулгах
```bash
# Repository clone хийх
git clone <repository-url>
cd loan-service

# Dependencies суулгах
./gradlew build

# Application эхлүүлэх
./gradlew bootRun
```

### 3. Configuration
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/loan_db
    username: loan_user
    password: loan_pass
  
  redis:
    host: localhost
    port: 6379
```

### 4. API Testing
```bash
# Зээл хүсэх
curl -X POST http://localhost:8080/api/loans/apply \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "amount": 1000000, "term": 12}'

# Зээлийн мэдээлэл авах
curl http://localhost:8080/api/loans/1
```

## 📚 Нэмэлт мэдээлэл

### Documentation Links
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)

### Related Services
- **User Service**: Хэрэглэгчийн мэдээлэл
- **Notification Service**: Мэдэгдэл илгээх
- **Audit Service**: Аудит бүртгэл
- **Payment Service**: Төлбөр боловсруулах

### Support & Contact
- **Development Team**: GMT Development Team
- **Email**: dev@golomt.mn
- **Documentation**: Internal Wiki

---

*Энэ документацийг GMT Development Team бэлтгэсэн. Сүүлийн шинэчлэлт: 2024*
