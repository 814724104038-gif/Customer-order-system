🛒 Customer Order Management System
A complete, professional, and animated Customer Order Placement System built with Servlet + JDBC + MySQL + HTML/CSS/JS. Features a modern, responsive UI with stunning animations and full CRUD operations.

https://img.shields.io/badge/Java-Servlet-red?style=for-the-badge&logo=java
https://img.shields.io/badge/Tomcat-10.1-orange?style=for-the-badge&logo=apache-tomcat
https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge&logo=mysql
https://img.shields.io/badge/HTML5-Animated-green?style=for-the-badge&logo=html5

✨ Features
🎨 Stunning UI/UX
Gradient color schemes with animated backgrounds

Floating elements and confetti animations

Interactive form validation with real-time feedback

Progress tracking with animated steps

Responsive design for all devices

⚡ Core Functionality
✅ Customer Order Placement with complete details

✅ Address Management (Street, City, State, Pincode)

✅ Product Selection with price calculator

✅ Quantity Control with animated buttons

✅ Payment Method Selection

✅ Order Tracking with delivery timeline

✅ Order History & Reports with charts

🔧 Technical Features
Servlet-based backend with JDBC connectivity

MySQL Database with auto-table creation

Tomcat 10.1 compatible (jakarta packages)

Animated form submission with confetti

Real-time validation and error handling

Professional dashboard with statistics

📁 Project Structure
text
CustomerOrderSystem/
├── src/
│   └── com/order/
│       ├── OrderServlet.java          # Main order processing servlet
│       └── ViewOrdersServlet.java     # Order viewing servlet
├── WebContent/
│   ├── index.html                     # Animated dashboard homepage
│   ├── order_form.html                # Interactive order form (MAIN)
│   ├── track.html                     # Order tracking page
│   ├── reports.html                   # Analytics & reports
│   ├── css/
│   │   └── style.css                  # All styling (gradients, animations)
│   └── WEB-INF/
│       ├── lib/
│       │   └── mysql-connector.jar    # MySQL JDBC driver
│       └── web.xml                    # Servlet configuration
└── README.md                          # This file
🚀 Quick Start Guide
Prerequisites
Java JDK 11+

Apache Tomcat 10.1

MySQL 8.0+

Eclipse IDE (or any Java IDE)

Step 1: Database Setup
sql
-- Create database
CREATE DATABASE customer_orders;
USE customer_orders;

-- Create table (or use auto-create in servlet)
CREATE TABLE orders_complete (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    item VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    notes TEXT,
    payment_method VARCHAR(20) NOT NULL,
    delivery_date DATE,
    status VARCHAR(20) DEFAULT 'Processing',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
Step 2: Configure Database Connection
Edit OrderServlet.java:

java
String url = "jdbc:mysql://localhost:3306/customer_orders";
String username = "root";  // Change to your MySQL username
String password = "yourpassword";  // Change to your MySQL password
Step 3: Deploy to Tomcat
Import project into Eclipse as Dynamic Web Project

Add Tomcat 10.1 as server runtime

Add MySQL Connector JAR to WEB-INF/lib/

Start Tomcat Server

Access: http://localhost:8080/CustomerOrderSystem/

🎯 Key Pages
1. Dashboard (index.html)
https://via.placeholder.com/800x400/667eea/ffffff?text=Colorful+Dashboard+with+Statistics

Animated statistics cards

Quick action buttons

Recent orders overview

Feature highlights

2. Order Form (order_form.html)
https://via.placeholder.com/800x400/764ba2/ffffff?text=Interactive+Order+Form+with+Animations

4-step progress tracker

Real-time form validation

Animated quantity selector

Price calculator

Confetti animation on submit

3. Order Success Page
https://via.placeholder.com/800x400/28a745/ffffff?text=Order+Confirmation+with+Animations

Celebration animations

Order summary

Delivery tracking animation

Next steps guidance

4. Order Tracking (track.html)
https://via.placeholder.com/800x400/ff7e5f/ffffff?text=Live+Order+Tracking+Timeline

Delivery timeline with animations

Real-time status updates

Map animation with moving truck

Estimated delivery time

5. Reports (reports.html)
https://via.placeholder.com/800x400/6a11cb/ffffff?text=Analytics+%2526+Reports+with+Charts

Interactive charts (Chart.js)

Sales analytics

Top products table

Export options (PDF, Excel, CSV)

🔧 Troubleshooting
Common Issues & Solutions
Issue	Solution
Database connection failed	Check MySQL credentials in OrderServlet.java
Tomcat 10.1 errors	Ensure using jakarta.* packages, not javax.*
Buttons not working	Check browser console (F12) for JavaScript errors
404 Not Found	Verify web.xml configuration and URL patterns
CSS/JS not loading	Check paths in HTML files and file locations
Debug Mode
Enable console logging in browsers:

Press F12 → Console tab

Look for error messages

All pages include debug logs starting with ✅ or ❌

🎨 Customization
Change Color Scheme
Edit css/style.css:

css
:root {
    --primary: #667eea;    /* Change primary color */
    --secondary: #764ba2;  /* Change secondary color */
    --success: #28a745;    /* Change success color */
    --danger: #dc3545;     /* Change danger color */
}
Add New Products
Edit order_form.html:

html
<option value="New Product" data-price="9999">
    🎮 New Product - ₹9,999
</option>
Modify Database Schema
Edit table creation in OrderServlet.java or SQL script.

📊 Database Schema Details
Table: orders_complete
Column	Type	Description
id	INT	Auto-increment primary key
customer_name	VARCHAR(100)	Customer's full name
phone	VARCHAR(15)	Contact number
email	VARCHAR(100)	Email address (optional)
address	TEXT	Complete delivery address
city	VARCHAR(50)	City name
state	VARCHAR(50)	State name
pincode	VARCHAR(10)	6-digit pincode
item	VARCHAR(100)	Product ordered
quantity	INT	Quantity (1-100)
total_amount	DECIMAL(10,2)	Calculated total price
notes	TEXT	Special instructions
payment_method	VARCHAR(20)	Payment type
delivery_date	DATE	Preferred delivery date
status	VARCHAR(20)	Order status (Processing/Completed/Pending)
order_date	TIMESTAMP	Auto-generated timestamp
🔗 API Endpoints
Servlet Mappings
POST /OrderServlet - Process new order

GET /OrderServlet - Test servlet (debug)

GET /ViewOrdersServlet - View all orders (with filters)

Form Parameters
javascript
{
    name: "Customer Name",
    phone: "9876543210",
    email: "email@example.com",
    address: "Full address",
    city: "City",
    state: "State",
    pincode: "600001",
    item: "Product name",
    quantity: "2",
    notes: "Special instructions",
    payment: "Payment method",
    delivery_date: "2024-12-31"
}
🤝 Contributing
Fork the repository

Create a feature branch (git checkout -b feature/AmazingFeature)

Commit changes (git commit -m 'Add AmazingFeature')

Push to branch (git push origin feature/AmazingFeature)

Open a Pull Request

Development Guidelines
Use meaningful commit messages

Test all animations work correctly

Ensure mobile responsiveness

Add console logs for debugging

Update this README for new features

📝 License
This project is for educational purposes. Feel free to use, modify, and distribute.

🙏 Acknowledgments
Icons: Font Awesome

Charts: Chart.js

Animations: Animate.css

Fonts: Google Fonts (Poppins)

Colors: Coolors.co gradients

Inspiration: Modern web design trends

📞 Support
For issues, questions, or enhancements:

Check the troubleshooting section

Examine browser console logs

Verify database connection

Ensure Tomcat 10.1 is running

🚀 Live Demo Features
Feature	Status	Details
Order Placement	✅ Working	Full form with validation
Database Operations	✅ Working	Auto-table creation
Animations	✅ Working	Confetti, progress bars, floating elements
Responsive Design	✅ Working	Mobile-friendly
Error Handling	✅ Working	Graceful error pages
Form Validation	✅ Working	Real-time feedback
Order Tracking	✅ Working	Animated timeline
Reports & Analytics	✅ Working	Interactive charts
⭐ Star this repository if you find it helpful!

🔄 Share with friends who are learning Java web development!

💬 Leave feedback or suggestions in issues!

Built with ❤️ for Java Web Development students and professionals.

