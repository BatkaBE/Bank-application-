# Голомт Банк - UML Диаграм Баримт Бичгийн Тайлбар

## 📋 Ерөнхий мэдээлэл

Энэхүү баримт бичиг нь Голомт Банкны интернет банкинг системийн Use Case диаграм болон PlantUML кодын дэлгэрэнгүй тайлбарыг агуулна.

## 📁 Файлын бүтэц

```
project-root/
├── system-requirements.md          # Үндсэн шаардлагын баримт
├── use-case-diagram.puml          # Ерөнхий use case диаграм
├── use-case-dependencies.puml     # Use case хамаарлууд
├── user-registration-sequence.puml # Хэрэглэгч бүртгүүлэх
├── customer-login-activity.puml   # Нэвтрэх үйл явц
├── account-states.puml           # Дансны төлөв
├── customer-journey-map.puml     # Хэрэглэгчийн үйл явц
├── loan-approval-sequence.puml   # Зээлийн баталгаажуулалт
└── README-UML-DIAGRAMS.md        # Энэ файл
```

## 🔧 PlantUML-г ашиглах заавар

### 1. IDE-д PlantUML plugin суулгах

#### Visual Studio Code
```bash
# PlantUML extension суулгах
# VS Code marketplace-аас "PlantUML" гэж хайж суулгана
```

#### IntelliJ IDEA
```bash
# PlantUML plugin суулгах
# File -> Settings -> Plugins -> PlantUML
```

### 2. Онлайн PlantUML сервер ашиглах

1. [PlantUML Online Server](https://www.plantuml.com/plantuml) руу орно
2. .puml файлын агуулгыг copy/paste хийнэ
3. Preview харах боломжтой
4. PNG/PDF экспортлох боломжтой

### 3. Desktop Application ашиглах

```bash
# PlantUML jar файл татаж авах
wget https://github.com/plantuml/plantuml/releases/download/v1.2023.10/plantuml-1.2023.10.jar

# Диаграм үүсгэх
java -jar plantuml.jar use-case-diagram.puml
```

## 📊 Диаграм төрлүүд

### 1. Use Case Диаграм
**Файл:** `use-case-diagram.puml`
**Зорилго:** Системийн функцууд болон actor-уудын харилцааг харуулна

**Агуулсан зүйлс:**
- 4 actor: Customer, Bank Officer, System Admin, External System
- 23 use case бүхий 5 бүлэг функц
- Include/Extend хамаарлууд

### 2. Use Case Dependencies
**Файл:** `use-case-dependencies.puml`
**Зорилго:** Use case-үүдийн хоорондын хамаарлыг харуулна

**Хамаарлын төрлүүд:**
- `<<include>>`: Заавал гүйцэтгэгдэх
- `<<extend>>`: Нэмэлт функц

### 3. Sequence Диаграм
**Файлууд:**
- `user-registration-sequence.puml`
- `loan-approval-sequence.puml`

**Зорилго:** Цаг хугацааны дарааллаар үйл явцыг харуулна

### 4. Activity Диаграм
**Файл:** `customer-login-activity.puml`
**Зорилго:** Нөхцөлт үйл явц болон шийдвэрийн цэгүүдийг харуулна

### 5. State Диаграм
**Файл:** `account-states.puml`
**Зорилго:** Объектын төлөв өөрчлөлтийг харуулна

### 6. Customer Journey Map
**Файл:** `customer-journey-map.puml`
**Зорилго:** Хэрэглэгчийн туршлагыг шат дараалан харуулна

## 🎨 Диаграмын өнгөний legend

| Өнгө | Утга | Жишээ |
|------|------|--------|
| 🔵 Цэнхэр | Хэрэглэгч (Customer) | Гол хэрэглэгч |
| 🟢 Ногоон | Банкны ажилтан (Bank Officer) | Системийн хэрэглэгч |
| 🟡 Шар | Authentication функц | Нэвтрэх, бүртгүүлэх |
| 🟠 Улбар шар | Account Management | Данс удирдах |
| 🔴 Улаан | Transaction Management | Гүйлгээ хийх |
| 🟣 Нил ягаан | Loan Management | Зээл удирдах |
| 🟤 Бор | Reporting & Analytics | Тайлан, шинжилгээ |

## 📋 Use Case-үүдийн жагсаалт

### Authentication & Authorization (🟡)
1. **UC-001**: User Registration - Хэрэглэгч бүртгүүлэх
2. **UC-002**: User Login - Нэвтрэх
3. **UC-003**: Password Reset - Нууц үг шинэчлэх
4. **UC-004**: Profile Management - Профайл удирдах
5. **UC-005**: Role Management - Эрх удирдах

### Account Management (🟠)
6. **UC-006**: Open Account - Данс нээх
7. **UC-007**: Close Account - Данс хаах
8. **UC-008**: View Account Details - Дансны мэдээлэл харах
9. **UC-009**: Account Balance Inquiry - Үлдэгдэл шалгах
10. **UC-010**: Set Account Limits - Дансны хязгаар тохируулах

### Transaction Management (🔴)
11. **UC-011**: Internal Transfer - Дотоод шилжүүлэг
12. **UC-012**: Inter-bank Transfer - Банк хоорондын шилжүүлэг
13. **UC-013**: Transaction History - Гүйлгээний түүх
14. **UC-014**: Cancel Transaction - Гүйлгээ цуцлах
15. **UC-015**: Recurring Payments - Давтамжтай төлөлт

### Loan Management (🟣)
16. **UC-016**: Apply for Loan - Зээл хүсэх
17. **UC-017**: Loan Approval - Зээл батлах
18. **UC-018**: Loan Repayment - Зээл төлөх
19. **UC-019**: Loan Calculator - Зээлийн тооцоолох
20. **UC-020**: Loan Status Inquiry - Зээлийн төлөв шалгах

### Reporting & Analytics (🟤)
21. **UC-021**: Generate Reports - Тайлан гаргах
22. **UC-022**: Export Data - Өгөгдөл экспортлох
23. **UC-023**: Audit Logs - Аудит лог харах

## 🔄 Диаграм шинэчлэх заавар

### Шинэ use case нэмэх
1. `use-case-diagram.puml` файлд use case нэмнэ
2. `use-case-dependencies.puml`-д хамаарлуудыг тодорхойлно
3. `system-requirements.md`-д тайлбар нэмнэ

### Диаграм өөрчлөх
1. PlantUML кодыг засна
2. Preview харах
3. Баримт бичгийг шинэчлэх

## 📞 Тусламж

### Алдаа гарвал
1. PlantUML syntax шалгах
2. Theme болон skinparam зөв эсэхийг шалгах
3. Online server ашиглан тест хийх

### Холбоос
- [PlantUML Official Site](https://plantuml.com/)
- [PlantUML Language Reference](https://plantuml.com/en/guide)
- [Online PlantUML Editor](https://www.plantuml.com/plantuml)

---

**Хөгжүүлэгч:** Системийн шинжээч
**Огноо:** 2024 оны 12 сар
**Хувилбар:** 1.0
