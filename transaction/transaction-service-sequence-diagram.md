# Transaction Service - Гүйлгээний үйлчилгээ Sequence Diagrams

Энэ файл нь Transaction Service-ийн үндсэн бизнес процессуудын sequence diagram-уудыг агуулна.

## 1. Гүйлгээ хийх (Process Transfer)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant AccountService
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: POST /api/transactions
    activate TransactionController
    
    TransactionController->>TransactionController: Validate request
    TransactionController->>TransactionController: Log transaction.init
    TransactionController->>TransactionService: processTransfer(transactionDTO, request)
    activate TransactionService
    
    TransactionService->>TransactionService: Validate transaction data
    TransactionService->>TransactionService: Check account balances
    TransactionService->>TransactionService: Validate transaction limits
    TransactionService->>TransactionService: Calculate fees
    
    TransactionService->>AccountService: debitAccount(fromAccount, amount + fee)
    activate AccountService
    AccountService->>AccountService: Check sufficient balance
    AccountService->>AccountService: Update account balance
    AccountService-->>TransactionService: Account updated
    deactivate AccountService
    
    TransactionService->>AccountService: creditAccount(toAccount, amount)
    activate AccountService
    AccountService->>AccountService: Update account balance
    AccountService-->>TransactionService: Account updated
    deactivate AccountService
    
    TransactionService->>TransactionService: Create transaction entity
    TransactionService->>TransactionRepository: save(transactionEntity)
    activate TransactionRepository
    TransactionRepository->>Database: INSERT INTO transaction (...)
    activate Database
    Database-->>TransactionRepository: transaction saved
    deactivate Database
    TransactionRepository-->>TransactionService: Saved transaction entity
    deactivate TransactionRepository
    
    TransactionService->>NotificationService: sendNotification(fromUserId, "TRANSFER_DEBITED")
    activate NotificationService
    NotificationService-->>TransactionService: Notification sent
    deactivate NotificationService
    
    TransactionService->>NotificationService: sendNotification(toUserId, "TRANSFER_CREDITED")
    activate NotificationService
    NotificationService-->>TransactionService: Notification sent
    deactivate NotificationService
    
    TransactionService->>AuditService: logAudit("TRANSFER_COMPLETED", transactionId)
    activate AuditService
    AuditService-->>TransactionService: Audit logged
    deactivate AuditService
    
    TransactionService-->>TransactionController: GMTResponseDTO
    deactivate TransactionService
    
    TransactionController->>TransactionController: Log transaction.end
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 2. Банк хоорондын гүйлгээ (Inter-Bank Transfer)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant AccountService
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: POST /api/transactions/inter-bank
    activate TransactionController
    
    TransactionController->>TransactionController: Validate request
    TransactionController->>TransactionController: Log interBankTransfer.init
    TransactionController->>TransactionService: processInterBankTransfer(interBankDTO, request)
    activate TransactionService
    
    TransactionService->>TransactionService: Validate inter-bank data
    TransactionService->>TransactionService: Check SWIFT/BIC codes
    TransactionService->>TransactionService: Validate currency conversion
    TransactionService->>TransactionService: Calculate international fees
    
    TransactionService->>AccountService: debitAccount(fromAccount, amount + internationalFee)
    activate AccountService
    AccountService->>AccountService: Check sufficient balance
    AccountService->>AccountService: Update account balance
    AccountService-->>TransactionService: Account updated
    deactivate AccountService
    
    TransactionService->>TransactionService: Create inter-bank transaction
    TransactionService->>TransactionRepository: save(interBankTransaction)
    activate TransactionRepository
    TransactionRepository->>Database: INSERT INTO transaction (...)
    activate Database
    Database-->>TransactionRepository: transaction saved
    deactivate Database
    TransactionRepository-->>TransactionService: Saved transaction entity
    deactivate TransactionRepository
    
    TransactionService->>TransactionService: Send to external bank system
    TransactionService->>TransactionService: Set status to PENDING
    
    TransactionService->>NotificationService: sendNotification(fromUserId, "INTERBANK_INITIATED")
    activate NotificationService
    NotificationService-->>TransactionService: Notification sent
    deactivate NotificationService
    
    TransactionService->>AuditService: logAudit("INTERBANK_INITIATED", transactionId)
    activate AuditService
    AuditService-->>TransactionService: Audit logged
    deactivate AuditService
    
    TransactionService-->>TransactionController: GMTResponseDTO
    deactivate TransactionService
    
    TransactionController->>TransactionController: Log interBankTransfer.end
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 3. Банк хоорондын гүйлгээний төлөв шинэчлэх (Update Inter-Bank Status)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant AccountService
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: PUT /api/transactions/inter-bank/{transactionId}/status
    activate TransactionController
    
    TransactionController->>TransactionController: Validate request
    TransactionController->>TransactionController: Log updateStatus.init
    TransactionController->>TransactionService: updateInterBankTransactionStatus(transactionId, status, externalRef, failureReason, request)
    activate TransactionService
    
    TransactionService->>TransactionRepository: findByTransactionId(transactionId)
    activate TransactionRepository
    TransactionRepository->>Database: SELECT * FROM transaction WHERE transaction_id = ?
    activate Database
    Database-->>TransactionRepository: transaction data
    deactivate Database
    TransactionRepository-->>TransactionService: Transaction entity
    deactivate TransactionRepository
    
    TransactionService->>TransactionService: Validate status transition
    TransactionService->>TransactionService: Update transaction status
    TransactionService->>TransactionService: Set external reference
    TransactionService->>TransactionService: Set failure reason if failed
    
    alt Status = COMPLETED
        TransactionService->>AccountService: creditAccount(toAccount, amount)
        activate AccountService
        AccountService->>AccountService: Update account balance
        AccountService-->>TransactionService: Account updated
        deactivate AccountService
        
        TransactionService->>NotificationService: sendNotification(toUserId, "INTERBANK_COMPLETED")
        activate NotificationService
        NotificationService-->>TransactionService: Notification sent
        deactivate NotificationService
        
    else Status = FAILED
        TransactionService->>AccountService: creditAccount(fromAccount, amount + internationalFee)
        activate AccountService
        AccountService->>AccountService: Reverse the debit
        AccountService-->>TransactionService: Account updated
        deactivate AccountService
        
        TransactionService->>NotificationService: sendNotification(fromUserId, "INTERBANK_FAILED")
        activate NotificationService
        NotificationService-->>TransactionService: Notification sent
        deactivate NotificationService
    end
    
    TransactionService->>TransactionRepository: save(updatedTransaction)
    activate TransactionRepository
    TransactionRepository->>Database: UPDATE transaction SET status=?, external_reference=?, failure_reason=? WHERE id=?
    activate Database
    Database-->>TransactionRepository: transaction updated
    deactivate Database
    TransactionRepository-->>TransactionService: Updated transaction entity
    deactivate TransactionRepository
    
    TransactionService->>AuditService: logAudit("INTERBANK_STATUS_UPDATED", transactionId)
    activate AuditService
    AuditService-->>TransactionService: Audit logged
    deactivate AuditService
    
    TransactionService-->>TransactionController: GMTResponseDTO
    deactivate TransactionService
    
    TransactionController->>TransactionController: Log updateStatus.end
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 4. Хадгаламж хийх (Process Deposit)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant AccountService
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: POST /api/transactions/deposit
    activate TransactionController
    
    TransactionController->>TransactionController: Validate request
    TransactionController->>TransactionController: Log deposit.init
    TransactionController->>TransactionService: processDeposit(depositDTO, request)
    activate TransactionService
    
    TransactionService->>TransactionService: Validate deposit data
    TransactionService->>TransactionService: Check account existence
    TransactionService->>TransactionService: Validate deposit amount
    TransactionService->>TransactionService: Calculate deposit fees
    
    TransactionService->>AccountService: creditAccount(toAccount, amount - depositFee)
    activate AccountService
    AccountService->>AccountService: Update account balance
    AccountService-->>TransactionService: Account updated
    deactivate AccountService
    
    TransactionService->>TransactionService: Create deposit transaction
    TransactionService->>TransactionRepository: save(depositTransaction)
    activate TransactionRepository
    TransactionRepository->>Database: INSERT INTO transaction (...)
    activate Database
    Database-->>TransactionRepository: transaction saved
    deactivate Database
    TransactionRepository-->>TransactionService: Saved transaction entity
    deactivate TransactionRepository
    
    TransactionService->>NotificationService: sendNotification(toUserId, "DEPOSIT_COMPLETED")
    activate NotificationService
    NotificationService-->>TransactionService: Notification sent
    deactivate NotificationService
    
    TransactionService->>AuditService: logAudit("DEPOSIT_COMPLETED", transactionId)
    activate AuditService
    AuditService-->>TransactionService: Audit logged
    deactivate AuditService
    
    TransactionService-->>TransactionController: GMTResponseDTO
    deactivate TransactionService
    
    TransactionController->>TransactionController: Log deposit.end
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 5. Гүйлгээний мэдээлэл авах (Get Transaction by ID)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant Database

    Client->>TransactionController: GET /api/transactions/{transactionId}
    activate TransactionController
    
    TransactionController->>TransactionService: getTransactionByTransactionId(transactionId, request)
    activate TransactionService
    
    TransactionService->>TransactionRepository: findByTransactionId(transactionId)
    activate TransactionRepository
    TransactionRepository->>Database: SELECT * FROM transaction WHERE transaction_id = ?
    activate Database
    Database-->>TransactionRepository: transaction data
    deactivate Database
    TransactionRepository-->>TransactionService: Transaction entity
    deactivate TransactionRepository
    
    alt Transaction found
        TransactionService->>TransactionService: Convert to DTO
        TransactionService-->>TransactionController: GMTResponseDTO with transaction data
    else Transaction not found
        TransactionService-->>TransactionController: GMTResponseDTO with error
    end
    
    deactivate TransactionService
    
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 6. Хэрэглэгчийн гүйлгээнүүд (Get Transactions by User)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant Database

    Client->>TransactionController: GET /api/transactions/user/{userId}
    activate TransactionController
    
    TransactionController->>TransactionService: getAllTransactionsByUser(userId, request)
    activate TransactionService
    
    TransactionService->>TransactionRepository: findByUserId(userId)
    activate TransactionRepository
    TransactionRepository->>Database: SELECT * FROM transaction WHERE from_user_id = ? OR to_user_id = ? ORDER BY created_at DESC
    activate Database
    Database-->>TransactionRepository: list of transactions
    deactivate Database
    TransactionRepository-->>TransactionService: List<Transaction> entities
    deactivate TransactionRepository
    
    TransactionService->>TransactionService: Convert to DTOs
    TransactionService-->>TransactionController: GMTResponseDTO with transaction list
    deactivate TransactionService
    
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 7. Гүйлгээний төлөв шинэчлэх (Update Transaction Status)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: PUT /api/transactions/{transactionId}/status
    activate TransactionController
    
    TransactionController->>TransactionService: updateTransactionStatus(transactionId, status, updatedBy, failureReason, request)
    activate TransactionService
    
    TransactionService->>TransactionRepository: findByTransactionId(transactionId)
    activate TransactionRepository
    TransactionRepository->>Database: SELECT * FROM transaction WHERE transaction_id = ?
    activate Database
    Database-->>TransactionRepository: transaction data
    deactivate Database
    TransactionRepository-->>TransactionService: Transaction entity
    deactivate TransactionRepository
    
    TransactionService->>TransactionService: Validate status transition
    TransactionService->>TransactionService: Update transaction status
    TransactionService->>TransactionService: Set updated timestamp
    TransactionService->>TransactionService: Set failure reason if failed
    
    TransactionService->>TransactionRepository: save(updatedTransaction)
    activate TransactionRepository
    TransactionRepository->>Database: UPDATE transaction SET status=?, updated_at=?, failure_reason=? WHERE id=?
    activate Database
    Database-->>TransactionRepository: transaction updated
    deactivate Database
    TransactionRepository-->>TransactionService: Updated transaction entity
    deactivate TransactionRepository
    
    TransactionService->>NotificationService: sendNotification(userId, "TRANSACTION_STATUS_UPDATED")
    activate NotificationService
    NotificationService-->>TransactionService: Notification sent
    deactivate NotificationService
    
    TransactionService->>AuditService: logAudit("TRANSACTION_STATUS_UPDATED", transactionId)
    activate AuditService
    AuditService-->>TransactionService: Audit logged
    deactivate AuditService
    
    TransactionService-->>TransactionController: GMTResponseDTO
    deactivate TransactionService
    
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 8. Гүйлгээ цуцлах (Cancel Transaction)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService
    participant TransactionRepository
    participant AccountService
    participant NotificationService
    participant AuditService
    participant Database

    Client->>TransactionController: PUT /api/transactions/{transactionId}/cancel
    activate TransactionController
    
    TransactionController->>TransactionService: cancelTransaction(transactionId, cancelledBy, reason, request)
    activate TransactionService
    
    TransactionService->>TransactionRepository: findByTransactionId(transactionId)
    activate TransactionRepository
    TransactionRepository->>Database: SELECT * FROM transaction WHERE transaction_id = ?
    activate Database
    Database-->>TransactionRepository: transaction data
    deactivate Database
    TransactionRepository-->>TransactionService: Transaction entity
    deactivate TransactionRepository
    
    TransactionService->>TransactionService: Validate cancellation rules
    TransactionService->>TransactionService: Check if transaction can be cancelled
    
    alt Transaction can be cancelled
        TransactionService->>AccountService: reverseTransaction(fromAccount, toAccount, amount, fee)
        activate AccountService
        AccountService->>AccountService: Reverse account changes
        AccountService-->>TransactionService: Accounts reversed
        deactivate AccountService
        
        TransactionService->>TransactionService: Update transaction status to CANCELLED
        TransactionService->>TransactionService: Set cancellation reason
        TransactionService->>TransactionRepository: save(cancelledTransaction)
        activate TransactionRepository
        TransactionRepository->>Database: UPDATE transaction SET status=?, failure_reason=? WHERE id=?
        activate Database
        Database-->>TransactionRepository: transaction updated
        deactivate Database
        TransactionRepository-->>TransactionService: Updated transaction entity
        deactivate TransactionRepository
        
        TransactionService->>NotificationService: sendNotification(fromUserId, "TRANSACTION_CANCELLED")
        activate NotificationService
        NotificationService-->>TransactionService: Notification sent
        deactivate NotificationService
        
        TransactionService->>NotificationService: sendNotification(toUserId, "TRANSACTION_CANCELLED")
        activate NotificationService
        NotificationService-->>TransactionService: Notification sent
        deactivate NotificationService
        
        TransactionService->>AuditService: logAudit("TRANSACTION_CANCELLED", transactionId)
        activate AuditService
        AuditService-->>TransactionService: Audit logged
        deactivate AuditService
        
        TransactionService-->>TransactionController: GMTResponseDTO
    else Transaction cannot be cancelled
        TransactionService-->>TransactionController: GMTResponseDTO with error
    end
    
    deactivate TransactionService
    
    TransactionController-->>Client: GMTResponseDTO
    deactivate TransactionController
```

## 9. Алдааны боловсруулалт (Error Handling)

```mermaid
sequenceDiagram
    participant Client
    participant TransactionController
    participant TransactionService

    Client->>TransactionController: Any API call
    activate TransactionController
    
    TransactionController->>TransactionService: Service method call
    activate TransactionService
    
    TransactionService->>TransactionService: Business logic processing
    
    alt Validation Error
        TransactionService-->>TransactionController: GMTValidationException
    else Business Error
        TransactionService-->>TransactionController: GMTBusinessException
    else RMI Error
        TransactionService-->>TransactionController: GMTRMIException
    else Runtime Error
        TransactionService-->>TransactionController: GMTRuntimeException
    else Custom Error
        TransactionService-->>TransactionController: GMTCustomException
    end
    
    deactivate TransactionService
    
    TransactionController->>TransactionController: Create appropriate error response
    TransactionController-->>Client: GMTResponseDTO with error details
    deactivate TransactionController
```

## Үндсэн онцлогууд

- **Гүйлгээ хийх**: Дотоод данс хоорондын шилжүүлэг
- **Банк хоорондын гүйлгээ**: SWIFT/BIC кодын дагуу гадаад банк руу
- **Хадгаламж**: Данс руу мөнгө хадгалах
- **Төлөв шинэчлэх**: Гүйлгээний төлөвийг шинэчлэх
- **Гүйлгээ цуцлах**: Гүйлгээг цуцлах, дансны мөнгийг буцаах
- **Мэдээлэл**: Хэрэглэгчид мэдэгдэх
- **Аудит**: Бүх үйл ажиллагааг бүртгэх
- **Алдааны боловсруулалт**: Янз бүрийн алдааг зохицуулах

## API Endpoint-ууд

- `POST /api/transactions` - Гүйлгээ хийх
- `POST /api/transactions/inter-bank` - Банк хоорондын гүйлгээ
- `PUT /api/transactions/inter-bank/{transactionId}/status` - Банк хоорондын төлөв шинэчлэх
- `POST /api/transactions/deposit` - Хадгаламж хийх
- `GET /api/transactions/{transactionId}` - Гүйлгээний мэдээлэл авах
- `GET /api/transactions/user/{userId}` - Хэрэглэгчийн гүйлгээнүүд
- `PUT /api/transactions/{transactionId}/status` - Гүйлгээний төлөв шинэчлэх
- `PUT /api/transactions/{transactionId}/cancel` - Гүйлгээ цуцлах
