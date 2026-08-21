# 🍽️ Restaurant Table Reservation and Order Management

> A complete system for managing **restaurant table reservations, customer orders, tables, menu items, and billing** efficiently.

---

## 🌟 Overview

**Restaurant Table Reservation and Order Management** is a database management project designed to simplify restaurant operations.

The system allows restaurants to manage table reservations, customer information, menu items, orders, and payments in an organized way.

The project focuses on designing an efficient **database system** that reduces manual work and improves restaurant management.

---

## ✨ Features

- 🪑 **Table Management**
  - Add and manage restaurant tables
  - Track table availability
  - Manage table capacity

- 📅 **Table Reservation**
  - Create reservations
  - Store reservation date and time
  - Manage reservation status

- 👤 **Customer Management**
  - Store customer information
  - View customer reservation history

- 🍕 **Menu Management**
  - Add, update, and remove menu items
  - Manage item prices and categories

- 🛒 **Order Management**
  - Create customer orders
  - Add multiple menu items to an order
  - Track order status

- 💳 **Billing & Payment**
  - Calculate order totals
  - Manage payment details
  - Track payment status

---

## 🗂️ Database Entities

The main entities used in the system are:

```text
Customer
   │
   │ makes
   ▼
Reservation ─────── reserves ───────► Table
   │
   │ creates
   ▼
Order ───────── contains ───────────► OrderItem
                                      │
                                      │ refers to
                                      ▼
                                   MenuItem
```

### Main Entities

| Entity | Description |
|---|---|
| 👤 Customer | Stores customer details |
| 🪑 Table | Stores restaurant table information |
| 📅 Reservation | Manages table bookings |
| 🛒 Order | Stores customer orders |
| 🍕 MenuItem | Stores food/menu information |
| 📦 OrderItem | Connects orders with menu items |
| 💳 Payment | Stores payment information |

---

## 🛠️ Technologies Used

- 🗄️ **Database:** MySQL
- 💻 **Language:** SQL
- 📊 **Database Design:** ER Diagram
- 📝 **Documentation:** Markdown
- 🖼️ **ER Diagram Tool:** Dia

---

## 📐 ER Diagram

The project includes an **Entity Relationship Diagram (ER Diagram)** showing the relationships between customers, reservations, tables, orders, menu items, and payments.

> 📌 Add your ER diagram image here:

```markdown
![ER Diagram](./ER-Diagram/restaurant-er-diagram.png)
```

---

## 📁 Project Structure

```text
Restaurant-Table-Reservation-and-Order-Management/
│
├── 📁 ER-Diagram/
│   └── restaurant-er-diagram.png
│
├── 📁 SQL/
│   ├── create_database.sql
│   ├── create_tables.sql
│   ├── insert_data.sql
│   └── queries.sql
│
├── 📄 README.md
└── 📄 LICENSE
```

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/Restaurant-Table-Reservation-and-Order-Management.git
```

### 2️⃣ Open MySQL

Open **MySQL Workbench**, MySQL CLI, or another SQL client.

### 3️⃣ Create the Database

Run the database creation script:

```sql
CREATE DATABASE restaurant_management;
```

### 4️⃣ Select the Database

```sql
USE restaurant_management;
```

### 5️⃣ Run the SQL Files

Execute the table creation, data insertion, and query scripts in the appropriate order.

---

## 📊 Example Operations

The database supports operations such as:

```sql
-- View available tables
SELECT *
FROM RestaurantTable
WHERE status = 'Available';
```

```sql
-- View customer reservations
SELECT *
FROM Reservation
WHERE customer_id = 1;
```

```sql
-- View orders
SELECT *
FROM Orders;
```

---

## 🎯 Project Objectives

The main objectives of this project are:

- Understand **database design**
- Implement **relationships between entities**
- Practice **SQL queries**
- Learn **primary keys and foreign keys**
- Understand **normalization**
- Manage restaurant reservations and orders efficiently
- Design a real-world database management system

---

## 🔮 Future Improvements

Some possible future improvements include:

- 🌐 Web-based restaurant management interface
- 📱 Mobile application
- 🔐 User authentication
- 💳 Online payment integration
- 📧 Reservation confirmation emails
- 📈 Restaurant analytics dashboard
- 🔔 Real-time table availability
- 🤖 AI-based food recommendations

---

## 👨‍💻 Contributors

| Name | Role |
|---|---|
| **Soham Pachakatte** | Developer |

Want to contribute? Feel free to **fork this repository**, make your changes, and submit a **Pull Request**.

---

## ⭐ Support

If you find this project useful, consider giving the repository a **⭐ Star**!

---

## 📄 License

This project is created for **educational and academic purposes**.

---

### 🍽️ Restaurant Management Made Simple

**Reserve • Order • Manage • Serve**
