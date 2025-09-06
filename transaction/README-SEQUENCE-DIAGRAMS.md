# Transaction Service - Гүйлгээний үйлчилгээ Documentation

## Overview

Энэ directory нь Transaction Service системийн comprehensive documentation-ыг агуулна:
- **Sequence Diagrams**: Үйлчилгээний харилцааны дэлгэрэнгүй flow diagram-ууд
- **Architecture Overview**: Системийн ерөнхий дизайн болон component-уудын харилцаа
- **PlantUML Source**: Diagram-уудыг үүсгэх source файлууд

## Files Description

### 1. `transaction-service-sequence-diagram.puml`
- **Format**: PlantUML source code
- **Content**: Бүх гүйлгээний үйлчилгээний үйл ажиллагааны дэлгэрэнгүй sequence diagram-ууд
- **Usage**: PlantUML tool-ууд эсвэл online viewer-үүдээр render хийх боломжтой

### 2. `transaction-service-sequence-diagram.md`
- **Format**: Markdown with Mermaid diagrams
- **Content**: Mermaid форматаар sequence diagram-ууд (GitHub/GitLab-д автоматаар харагдана)
- **Usage**: Mermaid дэмждэг Markdown viewer-үүдээр шууд харах

### 3. `transaction-service-architecture.md`
- **Format**: Markdown with architecture diagrams
- **Content**: Системийн архитектурын ерөнхий харагдах байдал, component-уудын дэлгэрэнгүй мэдээлэл, deployment diagram-ууд
- **Usage**: Системийн comprehensive documentation

## How to View the Diagrams

### Option 1: Mermaid Diagrams (Recommended)
`.md` файлууд нь Mermaid diagram-уудыг агуулдаг бөгөөд дараах газарт автоматаар render хийгддэг:
- **GitHub**: Mermaid diagram-уудыг автоматаар харуулна
- **GitLab**: Mermaid diagram-уудыг автоматаар харуулна
- **VS Code**: "Markdown Preview Mermaid Support" extension суулгах
- **Online**: [Mermaid Live Editor](https://mermaid.live/) ашиглах

### Option 2: PlantUML Diagrams
`.puml` файлын хувьд:
- **VS Code**: "PlantUML" extension суулгах
- **Online**: [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/) ашиглах
- **Local**: PlantUML болон Java суулгах

### Option 3: Convert to Images
Diagram-уудыг image болгон хөрвүүлэх:
```bash
# PlantUML to PNG
java -jar plantuml.jar transaction-service-sequence-diagram.puml

# Mermaid to PNG (using mermaid-cli)
mmdc -i transaction-service-sequence-diagram.md -o output.png
```

## Key Diagrams Included

### 1. Гүйлгээний Flow
Гүйлгээ хийх бүх үйл явцыг харуулна:
- Client хүсэлт
- Service layer боловсруулалт
- Data validation
- Database үйлдлүүд
- Response mapping

### 2. Гүйлгээний мэдээлэл авах Flow-ууд
Гүйлгээний мэдээллийг хэрхэн авах:
- ID-аар гүйлгээ хайх
- Хэрэглэгчийн гүйлгээнүүд
- Хайлт, filter үйлдлүүд

### 3. Гүйлгээний боловсруулалт Flow-ууд
Business үйл ажиллагаа:
- Гүйлгээний батлалт
- Гүйлгээний татгалзалт
- Төлбөр боловсруулалт
- Төлөв шинэчлэлт

### 4. Алдааны боловсруулалт Flow
Давхарга бүрт хэрхэн exception-уудыг боловсруулах:
- Validation errors
- Business logic errors
- System errors
- Error response formatting

### 5. Системийн Architecture
High-level харагдах байдал:
- Component харилцаа
- Data flow pattern-ууд
- External service integration
- Deployment architecture

## System Components

### Controller Layer
- **GMTTransactionController**: REST API endpoint-ууд authentication болон validation-тай

### Service Layer
- **GMTTransactionService**: Service contract-ыг тодорхойлсон interface
- **GMTTransactionServiceImple**: Business logic implementation

### Data Access Layer
- **GMTTransactionRepository**: Database үйлдлүүд болон custom query-үүд
- **GMTTransactionEntity**: Database entity mapping

### Utility Layer
- **GMTLOGUtilities**: Centralized logging
- **GMTMapper**: Data transformation utilities
- **GMTHelper**: Common utility functions
- **GMTException**: Custom exception handling

## API Endpoints Documented

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Гүйлгээ хийх |
| POST | `/api/transactions/inter-bank` | Банк хоорондын гүйлгээ |
| PUT | `/api/transactions/inter-bank/{transactionId}/status` | Банк хоорондын төлөв шинэчлэх |
| POST | `/api/transactions/deposit` | Хадгаламж хийх |
| GET | `/api/transactions/{transactionId}` | ID-аар гүйлгээ хайх |
| GET | `/api/transactions/user/{userId}` | Хэрэглэгчийн гүйлгээнүүд |
| PUT | `/api/transactions/{transactionId}/status` | Гүйлгээний төлөв шинэчлэх |
| PUT | `/api/transactions/{transactionId}/cancel` | Гүйлгээ цуцлах |

## Business Flows Documented

1. **Гүйлгээний Process**
   - Хэрэглэгч гүйлгээ хүсэлт илгээнэ
   - Систем input data-г validate хийнэ
   - Гүйлгээ entity үүсгэнэ
   - Гүйлгээний дэлгэрэнгүй тооцоолно
   - Database-д хадгална
   - Баталгаа буцаана

2. **Гүйлгээний батлалт Process**
   - Admin гүйлгээний хүсэлтийг хянана
   - Систем гүйлгээний төлөвийг шинэчилнэ
   - Баталгаажуулалтын дэлгэрэнгүй бүртгэнэ
   - Notification илгээнэ
   - Audit trail шинэчилнэ

3. **Төлбөр боловсруулалт**
   - Хэрэглэгч төлбөр илгээнэ
   - Систем дүнг validate хийнэ
   - Үлдэгдэл balance шинэчилнэ
   - Гүйлгээ дууссан эсэхийг шалгана
   - Гүйлгээний төлөвийг шинэчилнэ

4. **Гүйлгээний удирдлага**
   - Төлөв tracking
   - Төлбөрийн түүх
   - Balance тооцоолол
   - Risk assessment
   - Reporting

## Security Features

- **Authentication**: JWT token validation
- **Authorization**: Role-based access control (ROLE_USER, ROLE_CORPORATE, ROLE_ADMIN)
- **Input Validation**: Request DTO validation
- **Audit Logging**: Complete operation tracking
- **Data Encryption**: Sensitive data protection

## Performance Considerations

- **Caching**: Redis for frequently accessed data
- **Connection Pooling**: Database optimization
- **Async Processing**: Non-blocking operations
- **Pagination**: Large result set handling
- **Indexing**: Query optimization

## Monitoring & Observability

- **Structured Logging**: JSON format with correlation IDs
- **Metrics**: Performance and business metrics
- **Health Checks**: Service health monitoring
- **Tracing**: Request flow tracking
- **Alerts**: Error and performance notifications

## Getting Started

1. **View Diagrams**: `.md` файлуудыг Markdown viewer-ээр нээх
2. **Understand Flow**: Sequence diagram-уудыг судлах
3. **Review Architecture**: Системийн архитектурын ерөнхий харагдах байдлыг шалгах
4. **Explore Code**: Diagram-уудыг ашиглан codebase-ийн бүтцийг ойлгох

## Contributing

Системийг шинэчлэх үед:
1. Холбогдох sequence diagram-уудыг шинэчлэх
2. Architecture documentation-ыг засах
3. Code болон diagram-уудын хооронд consistency хадгалах
4. Шаардлагатай бол README-г шинэчлэх

## Support

Diagram-ууд эсвэл architecture-тай холбоотой асуултуудын хувьд:
- Code comment-уудыг шалгах
- Sequence diagram-уудыг flow дэлгэрэнгүй мэдээллийн хувьд шалгах
- Architecture overview-г component харилцааны хувьд шалгах
- API documentation-г endpoint дэлгэрэнгүй мэдээллийн хувьд шалгах

## Key Features

✅ **Layered Architecture** - Controller, Service, Repository давхарга
✅ **Comprehensive Logging** - Бүх үйл ажиллагаа log хийгддэг
✅ **Exception Handling** - Алдааны мэдээллийн зөв боловсруулалт
✅ **Security** - JWT authentication, role-based authorization
✅ **Performance** - Caching, connection pooling, async processing
✅ **Real-time Processing** - Шуурхай гүйлгээний боловсруулалт
✅ **Transaction Atomicity** - Гүйлгээний атом байдал
✅ **Rollback Support** - Буцаах боломж
✅ **Audit Trail** - Аудит мөр
✅ **Multi-currency** - Олон валютын дэмжлэг
