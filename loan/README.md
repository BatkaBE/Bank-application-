# Loan Service

Энэ бол Golomt Bank-ийн зээлийн үйлчилгээний микросервис юм.

## Тайлбар

Loan Service нь банкны зээлийн үйл ажиллагааг удирдах микросервис бөгөөд дараах үндсэн функцуудтай:

- Зээл хүсэх
- Зээл зөвшөөрөх/татгалзах
- Зээлийн мэдээлэл харах
- Зээлийн төлбөр тооцоолох
- Зээлийн төлбөр төлөх

## Технологи

- **Java 17**
- **Spring Boot 2.7.18**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL**
- **Gradle**
- **Eureka Client**

## Суулгах

1. Java 17 суулгах
2. PostgreSQL суулгах
3. Gradle wrapper ашиглан төслийг build хийх:

```bash
./gradlew build
```

## Ажиллуулах

```bash
./gradlew bootRun
```

Эсвэл JAR файлаар:

```bash
java -jar build/libs/loan-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Зээл хүсэх
```
POST /api/loans
```

### Зээлийн мэдээлэл авах
```
GET /api/loans/{loanId}
```

### Хэрэглэгчийн бүх зээл
```
GET /api/loans/user/{userId}
```

### Дансны дугаараар зээл хайх
```
GET /api/loans/account/{accountNumber}
```

### Огнооны хязгаараар зээл хайх
```
GET /api/loans/date-range?startDate=...&endDate=...
```

### Хэмжээний хязгаараар зээл хайх
```
GET /api/loans/amount-range?minAmount=...&maxAmount=...
```

### Зээлийн статус шинэчлэх
```
PUT /api/loans/{loanId}/status?status=...&updatedBy=...
```

### Зээл зөвшөөрөх
```
PUT /api/loans/{loanId}/approve?approvedBy=...
```

### Зээл татгалзах
```
PUT /api/loans/{loanId}/reject?rejectedBy=...&rejectionReason=...
```

### Статусаар зээл хайх
```
GET /api/loans/status/{status}/user/{userId}
```

### Зээлийн төлбөр тооцоолох
```
GET /api/loans/calculate?loanAmount=...&interestRate=...&loanTerm=...
```

### Идэвхтэй зээлүүд
```
GET /api/loans/active
```

### Зээлийн төлбөр төлөх
```
POST /api/loans/{loanId}/payment?paymentAmount=...
```

## Тохиргоо

`application.properties` файл дээр дараах тохиргоонуудыг өөрчлөх боломжтой:

- `server.port` - Серверийн порт (default: 8083)
- `spring.datasource.url` - Database холболтын URL
- `spring.datasource.username` - Database хэрэглэгчийн нэр
- `spring.datasource.password` - Database нууц үг

## Хэрэглээний жишээ

### Зээл хүсэх
```json
{
  "data": {
    "userId": "user123",
    "accountNumber": "1234567890",
    "loanAmount": 1000000,
    "interestRate": 12.5,
    "loanTerm": 24,
    "loanType": "PERSONAL",
    "purpose": "Гэр ахуйн хэрэгцээнд",
    "currencyCode": "MNT",
    "collateral": "Байр",
    "guarantor": "Гарант хүн",
    "notes": "Нэмэлт тайлбар"
  }
}
```

## Хөгжүүлэлт

Төслийг хөгжүүлэхдээ дараах зөвлөмжүүдийг баримтлах:

1. Кодын стандартыг дагах
2. Unit test бичих
3. API documentation шинэчлэх
4. Logging ашиглах

## Холбоо

Асуулт эсвэл санал байвал хөгжүүлэгчийн багтай холбогдоно уу.
