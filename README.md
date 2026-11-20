# Personal Expense & Income Tracker

A complete, enterprise-grade Java Web Application for tracking income and expenses with separate Bank and Cash balance pools, category-wise spending analytics powered by **Chart.js**, and automated database updates using **Hibernate ORM 6**, **Jakarta Servlets 6**, and **MySQL**. All currency amounts are denominated and formatted in **Indian Rupees (₹)**.

---

## 🌟 Key Features

1. **Dual-Pool Account Management**:
   - Tracks **Bank Account Balance** and **Cash Balance** independently in **Indian Rupees (₹)**.
   - Automatically computes **Total Net Worth** (`Bank Balance + Cash Balance`).
   - Every transaction dynamically affects the designated pool based on the payment mode selected.

2. **Negative Balance Prevention (Expense & Reversal Protection)**:
   - **Expense Transactions**: The system strictly validates available funds in the target account pool (`Bank Account` or `Cash`). If an expense exceeds the pool's balance, the transaction is rejected and rolled back with a clear error banner showing the current available balance.
   - **Income Deletions**: When deleting an income transaction, the system verifies that deducting the income amount will not push the corresponding account pool into a negative balance. If it would, the deletion is aborted with a detailed error alert.
   - **Registration**: Enforces non-negative initial balances (`min="0"` and server-side validation).

3. **Automated Registration & Default Seeding**:
   - Captures Full Name, Email, Password, **Initial Bank Balance (₹)**, and **Initial Cash Balance (₹)**.
   - Automatically seeds standard **Categories** (Food & Dining, Rent & Housing, Groceries, Shopping, Salary, etc.) and **Payment Modes** (Cash, Debit Card, UPI / Digital Wallet, Net Banking, Credit Card) for every new user.
   - Passwords securely hashed with salted SHA-256 (`PasswordUtil`).

4. **Atomic Balance Calculations & Accurate Deletion Reversals**:
   - **Income** adds to Bank or Cash based on Payment Mode (`accountType`).
   - **Expense** subtracts from Bank or Cash based on Payment Mode (`accountType`).
   - **Deleting a transaction** accurately reverses the calculation and restores the balance atomically within a Hibernate database transaction.

5. **Visual Spending Analytics (Chart.js with ₹ formatting)**:
   - Modern interactive **Doughnut/Pie Chart** of category-wise expense breakdown.
   - Tooltips formatted in `en-IN` currency format (`₹1,00,000.00`).
   - Prominent **Highest-Spend Category** and **Lowest-Spend Category** callouts.
   - Sorted breakdown list showing total amounts, percentage shares, and progress bars.

6. **Transaction History & Dynamic Filtering (`/transactions`)**:
   - Search and filter transactions by **Start Date**, **End Date**, **Category**, and **Type** (`ALL`, `EXPENSE`, `INCOME`).
   - Dynamic HQL filtering query (`findFilteredTransactions`).
   - Interactive metric strip displaying **Filtered Income**, **Filtered Expense**, and **Net Cashflow** for the active filter set.
   - Quick one-click filter reset.

7. **Integrated Category & Payment Mode Management**:
   - Dedicated side-by-side management interface for custom categories and payment modes.
   - Conflict-safe deletion prevents removing categories or modes referenced by existing transactions.

8. **Authentication & Route Protection**:
   - Session-based authentication via `AuthFilter`.
   - Protects all private routes (`/dashboard`, `/transactions`, `/add-transaction`, `/manage-categories`, etc.).
   - Includes cache-control headers preventing browser history leaks after logout.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|---|---|---|
| **Language** | Java (JDK) | 17+ (tested with Java 20/24) |
| **Build Tool** | Apache Maven | Packaging: `war` |
| **Servlet Specification** | Jakarta EE / Servlet API | 6.0.0 (Tomcat 10 / 11) |
| **ORM Framework** | Hibernate ORM | 6.4.4.Final |
| **Database** | MySQL Server | 8.0+ |
| **View Layer** | JSP & JSTL (Jakarta tags) | JSP 3.1 / JSTL 3.0 (`en_IN` locale) |
| **Styling & Icons** | Bootstrap 5 & Bootstrap Icons | 5.3.3 / 1.11.3 |
| **Data Visualization** | Chart.js | 4.4.3 |

---

## 📁 Project Architecture & Package Structure

```
d:\expense-tracker\
├── pom.xml                               # Maven project descriptor
├── README.md                             # Documentation & setup guide
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/expensetracker/
│   │   │       ├── model/                # Hibernate Entity & Enum definitions
│   │   │       │   ├── User.java
│   │   │       │   ├── Category.java
│   │   │       │   ├── PaymentMode.java
│   │   │       │   ├── Transaction.java
│   │   │       │   ├── CategoryType.java (EXPENSE, INCOME)
│   │   │       │   ├── AccountType.java  (CASH, BANK)
│   │   │       │   └── TransactionType.java (EXPENSE, INCOME)
│   │   │       ├── dto/                  # Data Transfer Objects
│   │   │       │   └── CategorySpendSummary.java
│   │   │       ├── dao/                  # Hibernate Data Access Layer
│   │   │       │   ├── UserDao.java
│   │   │       │   ├── CategoryDao.java
│   │   │       │   ├── PaymentModeDao.java
│   │   │       │   └── TransactionDao.java
│   │   │       ├── servlet/              # Jakarta HTTP Servlets
│   │   │       │   ├── LoginServlet.java
│   │   │       │   ├── RegisterServlet.java
│   │   │       │   ├── LogoutServlet.java
│   │   │       │   ├── DashboardServlet.java
│   │   │       │   ├── TransactionsServlet.java
│   │   │       │   ├── AddTransactionServlet.java
│   │   │       │   ├── DeleteTransactionServlet.java
│   │   │       │   └── ManageCategoriesServlet.java
│   │   │       ├── filter/               # Security & Access Filter
│   │   │       │   └── AuthFilter.java
│   │   │       └── util/                 # Utilities
│   │   │           ├── HibernateUtil.java
│   │   │           ├── JsonUtil.java
│   │   │           └── PasswordUtil.java
│   │   ├── resources/
│   │   │   ├── hibernate.cfg.xml         # Database & SessionFactory configuration
│   │   │   └── schema.sql                # Standalone MySQL DDL schema script
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml               # Web application deployment descriptor
│   │       ├── css/
│   │       │   └── style.css             # Custom styles & financial color palettes
│   │       ├── js/
│   │       │   └── dashboard.js          # Chart.js initialization script (₹ formatting)
│   │       ├── includes/                 # Reusable JSP components
│   │       │   ├── header.jsp            # UTF-8 charset & en_IN locale configuration
│   │       │   ├── navbar.jsp            # Top navigation & Total Balance in ₹
│   │       │   └── footer.jsp            # Shared footer & scripts
│   │       ├── login.jsp                 # User authentication
│   │       ├── register.jsp              # Registration with starting balances (₹)
│   │       ├── dashboard.jsp             # Balances (₹), Chart.js pie chart & history
│   │       ├── transactions.jsp          # Date range, category & type filter history
│   │       ├── add-transaction.jsp       # Income & expense logging with balance guards
│   │       └── manage-categories.jsp     # Side-by-side categories & payment modes
```

---

## 🗄️ Database Configuration

### 1. Connection Details
Configuration file: [`src/main/resources/hibernate.cfg.xml`](file:///d:/expense-tracker/src/main/resources/hibernate.cfg.xml)

- **JDBC URL**:
  ```
  jdbc:mysql://localhost:3306/expense_tracker_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
  ```
- **Default Username**: `root`
- **Default Password**: `root`
- **Auto DDL**: `update` (Hibernate automatically creates/updates all tables on launch)

### 2. Updating MySQL Password
If your MySQL password is not `root`, update lines 18-19 in `hibernate.cfg.xml`:
```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">YOUR_PASSWORD_HERE</property>
```

---

## 🧮 Balance Calculation & Guard Rules

All calculations are executed atomically within database transactions inside [`TransactionDao.java`](file:///d:/expense-tracker/src/main/java/com/expensetracker/dao/TransactionDao.java):

| Action | Transaction Type | Payment Mode Pool | Negative Balance Guard | Effect on Balances |
|---|---|---|---|---|
| **Add Transaction** | `INCOME` | `BANK` | None (Always adds) | `user.bankBalance += amount` |
| **Add Transaction** | `INCOME` | `CASH` | None (Always adds) | `user.cashBalance += amount` |
| **Add Transaction** | `EXPENSE` | `BANK` | Rejects if `amount > bankBalance` | `user.bankBalance -= amount` |
| **Add Transaction** | `EXPENSE` | `CASH` | Rejects if `amount > cashBalance` | `user.cashBalance -= amount` |
| **Delete Transaction** | `INCOME` | `BANK` | Rejects if `amount > bankBalance` | `user.bankBalance -= amount` *(Reversal)* |
| **Delete Transaction** | `INCOME` | `CASH` | Rejects if `amount > cashBalance` | `user.cashBalance -= amount` *(Reversal)* |
| **Delete Transaction** | `EXPENSE` | `BANK` | None (Refunds to Bank) | `user.bankBalance += amount` *(Reversal)* |
| **Delete Transaction** | `EXPENSE` | `CASH` | None (Refunds to Cash) | `user.cashBalance += amount` *(Reversal)* |

---

## 🚀 Building & Running the Application

### 1. Build WAR Package with Maven
Run the standard Maven package command from the project root:
```bash
mvn clean package
```
This produces `target/expense-tracker.war`.

### 2. Deploy to Apache Tomcat (Version 10 or 11)
Because the project targets **Jakarta EE 10 / Servlet 6.0** (`jakarta.servlet.*`), it is designed for **Apache Tomcat 10.1.x or 11.x**:

1. Copy `target/expense-tracker.war` to the `webapps/` folder of your Tomcat installation:
   ```bash
   cp target/expense-tracker.war /path/to/tomcat/webapps/
   ```
2. Start Tomcat:
   - On Windows: `bin\startup.bat`
   - On Linux/macOS: `bin/startup.sh`
3. Open your browser and navigate to:
   ```
   http://localhost:8080/expense-tracker
   ```
