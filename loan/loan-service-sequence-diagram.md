# Loan Service - Зээлийн үйлчилгээ Sequence Diagrams

Энэ файл нь Loan Service-ийн үндсэн бизнес процессуудын sequence diagram-уудыг агуулна.

## 1. Зээл хүсэх (Loan Application)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository
    participant UserService
    participant NotificationService
    participant AuditService

    Client->>LoanController: POST /api/loans/apply
    activate LoanController
    
    LoanController->>LoanController: Validate request
    LoanController->>LoanService: createLoan(loanRequestDTO, request)
    activate LoanService
    
    LoanService->>LoanService: Validate loan data
    LoanService->>UserService: getUserById(userId)
    activate UserService
    UserService-->>LoanService: User information
    deactivate UserService
    
    LoanService->>LoanService: Check eligibility
    LoanService->>LoanService: Calculate loan amount
    LoanService->>LoanRepository: save(loanEntity)
    activate LoanRepository
    LoanRepository-->>LoanService: Saved loan entity
    deactivate LoanRepository
    
    LoanService->>NotificationService: sendNotification(userId, "LOAN_APPLIED")
    activate NotificationService
    NotificationService-->>LoanService: Notification sent
    deactivate NotificationService
    
    LoanService->>AuditService: logAudit("LOAN_APPLIED", userId)
    activate AuditService
    AuditService-->>LoanService: Audit logged
    deactivate AuditService
    
    LoanService-->>LoanController: GMTResponseDTO
    deactivate LoanService
    
    LoanController-->>Client: HTTP 201 Created
    deactivate LoanController
```

## 2. Зээлийн мэдээлэл авах (Get Loan by ID)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository

    Client->>LoanController: GET /api/loans/{id}
    activate LoanController
    
    LoanController->>LoanService: getLoanById(id)
    activate LoanService
    
    LoanService->>LoanRepository: findById(id)
    activate LoanRepository
    LoanRepository-->>LoanService: Loan entity
    deactivate LoanRepository
    
    alt Loan found
        LoanService->>LoanService: Convert to DTO
        LoanService-->>LoanController: GMTResponseDTO with loan data
    else Loan not found
        LoanService-->>LoanController: GMTResponseDTO with error
    end
    
    deactivate LoanService
    
    LoanController-->>Client: HTTP 200 OK / 404 Not Found
    deactivate LoanController
```

## 3. Хэрэглэгчийн бүх зээлүүд (Get All Loans by User)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository

    Client->>LoanController: GET /api/loans/user/{userId}
    activate LoanController
    
    LoanController->>LoanService: getAllLoansByUser(userId, page, size)
    activate LoanService
    
    LoanService->>LoanRepository: findByUserId(userId, pageable)
    activate LoanRepository
    LoanRepository-->>LoanService: Page<Loan> entities
    deactivate LoanRepository
    
    LoanService->>LoanService: Convert to DTOs
    LoanService-->>LoanController: GMTResponseDTO with loan list
    deactivate LoanService
    
    LoanController-->>Client: HTTP 200 OK
    deactivate LoanController
```

## 4. Зээл батлах (Loan Approval)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository
    participant NotificationService
    participant AuditService

    Client->>LoanController: PUT /api/loans/{id}/approve
    activate LoanController
    
    LoanController->>LoanController: Validate request
    LoanController->>LoanService: approveLoan(id, approvalData)
    activate LoanService
    
    LoanService->>LoanRepository: findById(id)
    activate LoanRepository
    LoanRepository-->>LoanService: Loan entity
    deactivate LoanRepository
    
    LoanService->>LoanService: Validate loan status
    LoanService->>LoanService: Update loan status to APPROVED
    LoanService->>LoanService: Set approval date
    LoanService->>LoanRepository: save(loanEntity)
    activate LoanRepository
    LoanRepository-->>LoanService: Updated loan entity
    deactivate LoanRepository
    
    LoanService->>NotificationService: sendNotification(userId, "LOAN_APPROVED")
    activate NotificationService
    NotificationService-->>LoanService: Notification sent
    deactivate NotificationService
    
    LoanService->>AuditService: logAudit("LOAN_APPROVED", userId)
    activate AuditService
    AuditService-->>LoanService: Audit logged
    deactivate AuditService
    
    LoanService-->>LoanController: GMTResponseDTO
    deactivate LoanService
    
    LoanController-->>Client: HTTP 200 OK
    deactivate LoanController
```

## 5. Зээлийн төлбөр төлөх (Loan Payment Processing)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository
    participant NotificationService
    participant AuditService

    Client->>LoanController: POST /api/loans/{id}/payment
    activate LoanController
    
    LoanController->>LoanController: Validate payment request
    LoanController->>LoanService: processPayment(id, paymentData)
    activate LoanService
    
    LoanService->>LoanRepository: findById(id)
    activate LoanRepository
    LoanRepository-->>LoanService: Loan entity
    deactivate LoanRepository
    
    LoanService->>LoanService: Validate payment amount
    LoanService->>LoanService: Calculate remaining balance
    LoanService->>LoanService: Update loan balance
    LoanService->>LoanRepository: save(loanEntity)
    activate LoanRepository
    LoanRepository-->>LoanService: Updated loan entity
    deactivate LoanRepository
    
    alt Loan fully paid
        LoanService->>LoanService: Update status to PAID
        LoanService->>LoanRepository: save(loanEntity)
        activate LoanRepository
        LoanRepository-->>LoanService: Final loan entity
        deactivate LoanRepository
    end
    
    LoanService->>NotificationService: sendNotification(userId, "PAYMENT_PROCESSED")
    activate NotificationService
    NotificationService-->>LoanService: Notification sent
    deactivate NotificationService
    
    LoanService->>AuditService: logAudit("PAYMENT_PROCESSED", userId)
    activate AuditService
    AuditService-->>LoanService: Audit logged
    deactivate AuditService
    
    LoanService-->>LoanController: GMTResponseDTO
    deactivate LoanService
    
    LoanController-->>Client: HTTP 200 OK
    deactivate LoanController
```

## 6. Зээл татгалзах (Loan Rejection)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant LoanRepository
    participant NotificationService
    participant AuditService

    Client->>LoanController: PUT /api/loans/{id}/reject
    activate LoanController
    
    LoanController->>LoanController: Validate request
    LoanController->>LoanService: rejectLoan(id, rejectionData)
    activate LoanService
    
    LoanService->>LoanRepository: findById(id)
    activate LoanRepository
    LoanRepository-->>LoanService: Loan entity
    deactivate LoanRepository
    
    LoanService->>LoanService: Validate loan status
    LoanService->>LoanService: Update loan status to REJECTED
    LoanService->>LoanService: Set rejection reason
    LoanService->>LoanRepository: save(loanEntity)
    activate LoanRepository
    LoanRepository-->>LoanService: Updated loan entity
    deactivate LoanRepository
    
    LoanService->>NotificationService: sendNotification(userId, "LOAN_REJECTED")
    activate NotificationService
    NotificationService-->>LoanService: Notification sent
    deactivate NotificationService
    
    LoanService->>AuditService: logAudit("LOAN_REJECTED", userId)
    activate AuditService
    AuditService-->>LoanService: Audit logged
    deactivate AuditService
    
    LoanService-->>LoanController: GMTResponseDTO
    deactivate LoanService
    
    LoanController-->>Client: HTTP 200 OK
    deactivate LoanController
```

## 7. Алдааны боловсруулалт (Error Handling)

```mermaid
sequenceDiagram
    participant Client
    participant LoanController
    participant LoanService
    participant UserService
    participant LoanRepository

    Client->>LoanController: POST /api/loans/apply
    activate LoanController
    
    LoanController->>LoanController: Validate request
    alt Invalid request data
        LoanController-->>Client: HTTP 400 Bad Request
        deactivate LoanController
    else Valid request
        LoanController->>LoanService: createLoan(loanRequestDTO, request)
        activate LoanService
        
        LoanService->>LoanService: Validate loan data
        alt Validation error
            LoanService-->>LoanController: GMTValidationException
            deactivate LoanService
            LoanController-->>Client: HTTP 400 Bad Request
            deactivate LoanController
        else Business logic error
            LoanService->>UserService: getUserById(userId)
            activate UserService
            UserService-->>LoanService: User not found
            deactivate UserService
            
            LoanService-->>LoanController: GMTBusinessException
            deactivate LoanService
            LoanController-->>Client: HTTP 404 Not Found
            deactivate LoanController
        else System error
            LoanService->>LoanRepository: save(loanEntity)
            activate LoanRepository
            LoanRepository-->>LoanService: Database error
            deactivate LoanRepository
            
            LoanService-->>LoanController: GMTCustomException
            deactivate LoanService
            LoanController-->>Client: HTTP 500 Internal Server Error
            deactivate LoanController
        end
    end
```

## Үндсэн онцлогууд

- **Зээлийн хүсэлт**: Хэрэглэгчийн эрх, зээлийн хэмжээг тооцоолох
- **Зээлийн батлалт**: Админ эрхээр зээлийг батлах
- **Төлбөр төлөлт**: Зээлийн төлбөрийг боловсруулах
- **Мэдээлэл**: Хэрэглэгчид мэдэгдэх
- **Аудит**: Бүх үйл ажиллагааг бүртгэх
- **Алдааны боловсруулалт**: Янз бүрийн алдааг зохицуулах

