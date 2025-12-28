package com.order;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderServlet extends HttpServlet {
    private Connection conn;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL Driver loaded successfully!");
            
            // Database connection details - CHANGE THESE FOR YOUR DATABASE
            String url = "jdbc:mysql://localhost:3306/customer_orders";
            String username = "root"; // Change this
            String password = "root"; // Change this
            
            // Establish connection
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("🚀 Database connected successfully!");
            
            // Test connection by creating table if not exists
            createTableIfNotExists();
            
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Database Connection Failed: " + e.getMessage());
        }
    }
    
    // Create orders table if it doesn't exist
    private void createTableIfNotExists() {
        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS orders_complete (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(15) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "address TEXT NOT NULL, " +
                    "city VARCHAR(50) NOT NULL, " +
                    "state VARCHAR(50) NOT NULL, " +
                    "pincode VARCHAR(10) NOT NULL, " +
                    "item VARCHAR(100) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "total_amount DECIMAL(10,2) NOT NULL, " +
                    "notes TEXT, " +
                    "payment_method VARCHAR(20) NOT NULL, " +
                    "delivery_date DATE, " +
                    "status VARCHAR(20) DEFAULT 'Processing', " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
            System.out.println("✅ Table 'orders_complete' is ready!");
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating table: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Handle GET requests (for testing)
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Order Servlet</title></head>");
        out.println("<body>");
        out.println("<h1>Order Servlet is Running! ✅</h1>");
        out.println("<p>This servlet handles POST requests from order forms.</p>");
        out.println("<p>Database Status: " + (conn != null ? "Connected" : "Not Connected") + "</p>");
        out.println("<a href='order_form.html'>Go to Order Form</a>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("📥 Received order submission request!");
        
        // Set response content type
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Debug: Print all parameters
            System.out.println("🔍 Form parameters received:");
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String paramValue = request.getParameter(paramName);
                System.out.println("  " + paramName + ": " + paramValue);
            }
            
            // Get form parameters with null checks
            String name = getParameter(request, "name");
            String phone = getParameter(request, "phone");
            String email = getParameter(request, "email");
            String address = getParameter(request, "address");
            String city = getParameter(request, "city");
            String state = getParameter(request, "state");
            String pincode = getParameter(request, "pincode");
            String item = getParameter(request, "item");
            String quantityStr = getParameter(request, "quantity");
            String notes = getParameter(request, "notes");
            String payment = getParameter(request, "payment");
            String deliveryDate = getParameter(request, "delivery_date");
            
            // Validate required fields
            if (name == null || name.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                address == null || address.trim().isEmpty() ||
                city == null || city.trim().isEmpty() ||
                state == null || state.trim().isEmpty() ||
                pincode == null || pincode.trim().isEmpty() ||
                item == null || item.trim().isEmpty() ||
                quantityStr == null || quantityStr.trim().isEmpty() ||
                payment == null || payment.trim().isEmpty()) {
                
                System.err.println("❌ Missing required fields!");
                printErrorPage(out, "Missing required fields. Please fill all mandatory fields.");
                return;
            }
            
            // Parse quantity
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0 || quantity > 100) {
                    throw new NumberFormatException("Invalid quantity");
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid quantity: " + quantityStr);
                printErrorPage(out, "Invalid quantity. Please enter a number between 1 and 100.");
                return;
            }
            
            // Calculate total amount
            double unitPrice = getItemPrice(item);
            double totalAmount = unitPrice * quantity;
            
            // Combine address
            String fullAddress = address + ", " + city + ", " + state + " - " + pincode;
            
            System.out.println("💾 Attempting to save order to database...");
            System.out.println("  Customer: " + name);
            System.out.println("  Item: " + item);
            System.out.println("  Quantity: " + quantity);
            System.out.println("  Total: ₹" + totalAmount);
            
            // Insert into database
            int orderId = saveOrderToDatabase(name, phone, email, fullAddress, city, state, 
                                            pincode, item, quantity, totalAmount, notes, 
                                            payment, deliveryDate);
            
            if (orderId > 0) {
                System.out.println("✅ Order saved successfully with ID: " + orderId);
                printAnimatedSuccessPage(out, name, phone, email, fullAddress, item, quantity, 
                                       orderId, totalAmount, notes, payment, deliveryDate);
            } else {
                System.err.println("❌ Failed to save order!");
                printErrorPage(out, "Failed to save order to database. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing order: " + e.getMessage());
            e.printStackTrace();
            printErrorPage(out, "Error processing order: " + e.getMessage());
        }
    }
    
    // Helper method to get parameter with null check
    private String getParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
    
    // Get item price
    private double getItemPrice(String item) {
        if (item == null) return 0.0;
        
        switch(item.toLowerCase()) {
            case "laptop": return 45000.00;
            case "smartphone": return 25000.00;
            case "headphones": return 5000.00;
            case "keyboard": return 2500.00;
            case "mouse": return 1500.00;
            default: return 1000.00;
        }
    }
    
    // Save order to database
    private int saveOrderToDatabase(String name, String phone, String email, String address,
                                   String city, String state, String pincode, String item,
                                   int quantity, double totalAmount, String notes,
                                   String payment, String deliveryDate) {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int orderId = -1;
        
        try {
            // SQL query with all fields
            String sql = "INSERT INTO orders_complete (" +
                        "customer_name, phone, email, address, city, state, pincode, " +
                        "item, quantity, total_amount, notes, payment_method, delivery_date" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, address);
            pstmt.setString(5, city);
            pstmt.setString(6, state);
            pstmt.setString(7, pincode);
            pstmt.setString(8, item);
            pstmt.setInt(9, quantity);
            pstmt.setDouble(10, totalAmount);
            pstmt.setString(11, notes);
            pstmt.setString(12, payment);
            
            // Handle delivery date (can be null)
            if (deliveryDate != null && !deliveryDate.trim().isEmpty()) {
                pstmt.setDate(13, java.sql.Date.valueOf(deliveryDate));
            } else {
                pstmt.setNull(13, java.sql.Types.DATE);
            }
            
            // Execute insert
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get generated order ID
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        }
        
        return orderId;
    }
    
    // Print animated success page
    private void printAnimatedSuccessPage(PrintWriter out, String name, String phone, String email,
                                         String address, String item, int quantity, int orderId,
                                         double totalAmount, String notes, String payment, 
                                         String deliveryDate) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a");
        String currentTime = sdf.format(new Date());
        String orderNumber = String.format("ORD%04d", orderId);
        
        // Calculate delivery estimate
        String estimatedDelivery = "3-5 business days";
        if (deliveryDate != null && !deliveryDate.trim().isEmpty()) {
            try {
                estimatedDelivery = new SimpleDateFormat("dd MMM yyyy")
                    .format(java.sql.Date.valueOf(deliveryDate));
            } catch (Exception e) {
                estimatedDelivery = deliveryDate;
            }
        }
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>🎉 Order Confirmed! | Order #" + orderNumber + "</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800;900&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println("    /* 🎨 Success Page Animations */");
        out.println("    @keyframes celebration {");
        out.println("        0% { transform: scale(0) rotate(0deg); opacity: 0; }");
        out.println("        50% { transform: scale(1.2) rotate(180deg); opacity: 1; }");
        out.println("        100% { transform: scale(1) rotate(360deg); opacity: 1; }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes floatUp {");
        out.println("        0% { transform: translateY(100px); opacity: 0; }");
        out.println("        100% { transform: translateY(0); opacity: 1; }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes colorShift {");
        out.println("        0% { background-position: 0% 50%; }");
        out.println("        50% { background-position: 100% 50%; }");
        out.println("        100% { background-position: 0% 50%; }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes pulseSuccess {");
        out.println("        0%, 100% { box-shadow: 0 0 30px rgba(40, 167, 69, 0.5); }");
        out.println("        50% { box-shadow: 0 0 60px rgba(40, 167, 69, 0.8); }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes confettiFall {");
        out.println("        to { transform: translateY(100vh) rotate(360deg); opacity: 0; }");
        out.println("    }");
        out.println("    ");
        out.println("    .success-hero {");
        out.println("        background: linear-gradient(135deg, ");
        out.println("            #28a745 0%, ");
        out.println("            #20c997 25%, ");
        out.println("            #1dd1a1 50%, ");
        out.println("            #10ac84 75%, ");
        out.println("            #00b894 100%);");
        out.println("        background-size: 400% 400%;");
        out.println("        color: white;");
        out.println("        padding: 60px 40px;");
        out.println("        border-radius: 30px;");
        out.println("        margin-bottom: 40px;");
        out.println("        text-align: center;");
        out.println("        position: relative;");
        out.println("        overflow: hidden;");
        out.println("        animation: colorShift 8s ease infinite, pulseSuccess 3s infinite;");
        out.println("    }");
        out.println("    ");
        out.println("    .celebration-icon {");
        out.println("        font-size: 5rem;");
        out.println("        margin-bottom: 20px;");
        out.println("        animation: celebration 2s ease-out;");
        out.println("        display: inline-block;");
        out.println("    }");
        out.println("    ");
        out.println("    .order-card {");
        out.println("        background: white;");
        out.println("        padding: 40px;");
        out.println("        border-radius: 25px;");
        out.println("        margin: 30px 0;");
        out.println("        box-shadow: 0 20px 60px rgba(0,0,0,0.1);");
        out.println("        animation: floatUp 0.8s ease-out;");
        out.println("        border-left: 10px solid #28a745;");
        out.println("        position: relative;");
        out.println("        overflow: hidden;");
        out.println("    }");
        out.println("    ");
        out.println("    .order-card::before {");
        out.println("        content: '';");
        out.println("        position: absolute;");
        out.println("        top: 0;");
        out.println("        left: -100%;");
        out.println("        width: 100%;");
        out.println("        height: 5px;");
        out.println("        background: linear-gradient(90deg, transparent, #28a745, transparent);");
        out.println("        animation: shimmer 3s infinite;");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes shimmer {");
        out.println("        0% { left: -100%; }");
        out.println("        100% { left: 100%; }");
        out.println("    }");
        out.println("    ");
        out.println("    .detail-item {");
        out.println("        display: flex;");
        out.println("        justify-content: space-between;");
        out.println("        padding: 15px 0;");
        out.println("        border-bottom: 1px solid #eee;");
        out.println("        animation: slideInFromLeft 0.5s ease-out;");
        out.println("        animation-fill-mode: both;");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes slideInFromLeft {");
        out.println("        from { opacity: 0; transform: translateX(-50px); }");
        out.println("        to { opacity: 1; transform: translateX(0); }");
        out.println("    }");
        out.println("    ");
        out.println("    .total-amount {");
        out.println("        font-size: 3rem;");
        out.println("        font-weight: 800;");
        out.println("        background: linear-gradient(45deg, #28a745, #20c997, #1dd1a1);");
        out.println("        -webkit-background-clip: text;");
        out.println("        background-clip: text;");
        out.println("        color: transparent;");
        out.println("        text-align: center;");
        out.println("        margin: 30px 0;");
        out.println("        animation: pulse 2s infinite;");
        out.println("    }");
        out.println("    ");
        out.println("    .tracking-animation {");
        out.println("        height: 100px;");
        out.println("        background: linear-gradient(135deg, #e3f2fd, #bbdefb);");
        out.println("        border-radius: 20px;");
        out.println("        margin: 40px 0;");
        out.println("        position: relative;");
        out.println("        overflow: hidden;");
        out.println("    }");
        out.println("    ");
        out.println("    .delivery-truck {");
        out.println("        position: absolute;");
        out.println("        font-size: 3rem;");
        out.println("        color: #28a745;");
        out.println("        animation: deliveryMove 10s linear infinite;");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes deliveryMove {");
        out.println("        0% { left: -50px; transform: translateY(0) rotate(0); }");
        out.println("        25% { transform: translateY(-10px) rotate(-5deg); }");
        out.println("        50% { left: 50%; transform: translateY(0) rotate(0); }");
        out.println("        75% { transform: translateY(10px) rotate(5deg); }");
        out.println("        100% { left: calc(100% + 50px); transform: translateY(0) rotate(0); }");
        out.println("    }");
        out.println("    ");
        out.println("    .confetti {");
        out.println("        position: fixed;");
        out.println("        width: 15px;");
        out.println("        height: 15px;");
        out.println("        background: var(--color);");
        out.println("        top: -20px;");
        out.println("        animation: confettiFall var(--duration) linear forwards;");
        out.println("        pointer-events: none;");
        out.println("        z-index: 9999;");
        out.println("    }");
        out.println("    ");
        out.println("    .action-btn {");
        out.println("        animation: bounceIn 1s ease-out;");
        out.println("        animation-fill-mode: both;");
        out.println("    }");
        out.println("    ");
        out.println("    .action-btn:nth-child(1) { animation-delay: 0.2s; }");
        out.println("    .action-btn:nth-child(2) { animation-delay: 0.4s; }");
        out.println("    .action-btn:nth-child(3) { animation-delay: 0.6s; }");
        out.println("    ");
        out.println("    @keyframes bounceIn {");
        out.println("        0% { opacity: 0; transform: scale(0.3); }");
        out.println("        50% { opacity: 1; transform: scale(1.05); }");
        out.println("        70% { transform: scale(0.9); }");
        out.println("        100% { transform: scale(1); }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes pulse {");
        out.println("        0%, 100% { transform: scale(1); }");
        out.println("        50% { transform: scale(1.05); }");
        out.println("    }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        
        // Confetti container
        out.println("<div id='confettiContainer'></div>");
        
        out.println("<div class='container'>");
        
        // Success Hero Section
        out.println("<div class='success-hero'>");
        out.println("<div class='celebration-icon'>");
        out.println("<i class='fas fa-check-circle'></i>");
        out.println("</div>");
        out.println("<h1 style='font-size: 3rem; margin-bottom: 20px;'>Order Confirmed!</h1>");
        out.println("<p style='font-size: 1.3rem; opacity: 0.9;'>Thank you for your order, " + name + "!</p>");
        out.println("<div style='margin-top: 30px; font-size: 1.5rem; background: rgba(255,255,255,0.2); " +
                   "padding: 15px 30px; border-radius: 50px; display: inline-block;'>");
        out.println("<i class='fas fa-hashtag'></i> Order #" + orderNumber);
        out.println("</div>");
        out.println("</div>");
        
        // Order Summary Card
        out.println("<div class='order-card'>");
        out.println("<h2 style='color: #333; margin-bottom: 30px;'><i class='fas fa-receipt'></i> Order Summary</h2>");
        
        // Order Details with animations
        String[][] details = {
            {"📝 Order Number", "#" + orderNumber},
            {"👤 Customer Name", name},
            {"📱 Contact", phone + (email != null && !email.isEmpty() ? " / " + email : "")},
            {"📦 Product", item},
            {"🔢 Quantity", String.valueOf(quantity)},
            {"💰 Unit Price", "₹" + String.format("%,.2f", getItemPrice(item))},
            {"🏠 Delivery Address", address},
            {"💳 Payment Method", payment},
            {"📅 Order Date", currentTime},
            {"🚚 Estimated Delivery", estimatedDelivery}
        };
        
        for (int i = 0; i < details.length; i++) {
            out.println("<div class='detail-item' style='animation-delay: " + (i * 0.1) + "s;'>");
            out.println("<span style='font-weight: 600; color: #555;'>" + details[i][0] + "</span>");
            out.println("<span style='font-weight: 700; color: #333;'>" + details[i][1] + "</span>");
            out.println("</div>");
        }
        
        // Special Instructions if any
        if (notes != null && !notes.trim().isEmpty()) {
            out.println("<div class='detail-item' style='animation-delay: 0.9s; border-bottom: none;'>");
            out.println("<span style='font-weight: 600; color: #555;'>📝 Special Instructions</span>");
            out.println("<span style='font-weight: 700; color: #333;'>" + notes + "</span>");
            out.println("</div>");
        }
        
        out.println("</div>");
        
        // Total Amount Display
        out.println("<div class='order-card' style='text-align: center; background: linear-gradient(135deg, #f8f9fa, #e9ecef);'>");
        out.println("<h3 style='color: #666; margin-bottom: 20px;'><i class='fas fa-rupee-sign'></i> Total Amount</h3>");
        out.println("<div class='total-amount'>₹" + String.format("%,.2f", totalAmount) + "</div>");
        out.println("<p style='color: #888;'>Inclusive of all taxes and charges</p>");
        out.println("</div>");
        
        // Tracking Animation
        out.println("<div class='tracking-animation'>");
        out.println("<div class='delivery-truck'><i class='fas fa-truck'></i></div>");
        out.println("<div style='position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); " +
                   "text-align: center; color: #333; z-index: 1;'>");
        out.println("<h3><i class='fas fa-shipping-fast'></i> Your Order is Being Processed</h3>");
        out.println("<p>Real-time tracking will be available shortly</p>");
        out.println("</div>");
        out.println("</div>");
        
        // What Happens Next
        out.println("<div class='order-card'>");
        out.println("<h3 style='color: #333; margin-bottom: 25px;'><i class='fas fa-forward'></i> What Happens Next?</h3>");
        out.println("<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px;'>");
        
        String[][] nextSteps = {
            {"📧 Order Confirmation", "You'll receive an email confirmation within 15 minutes", "#667eea", "fas fa-envelope"},
            {"🔧 Order Processing", "Our team will start processing your order immediately", "#764ba2", "fas fa-cogs"},
            {"🚚 Shipping Preparation", "Order will be packed and shipped within 24 hours", "#f093fb", "fas fa-box"},
            {"📱 Delivery Updates", "Get real-time tracking updates on your phone", "#4facfe", "fas fa-mobile-alt"}
        };
        
        for (String[] step : nextSteps) {
            out.println("<div style='background: white; padding: 25px; border-radius: 15px; " +
                       "border-top: 5px solid " + step[2] + "; box-shadow: 0 10px 30px rgba(0,0,0,0.08); " +
                       "transition: all 0.3s; animation: floatUp 0.8s ease-out;'>");
            out.println("<div style='font-size: 2rem; color: " + step[2] + "; margin-bottom: 15px;'>");
            out.println("<i class='" + step[3] + "'></i>");
            out.println("</div>");
            out.println("<h4 style='color: #333; margin-bottom: 10px;'>" + step[0] + "</h4>");
            out.println("<p style='color: #666; font-size: 0.95rem;'>" + step[1] + "</p>");
            out.println("</div>");
        }
        
        out.println("</div>");
        out.println("</div>");
        
        // Action Buttons
        out.println("<div class='nav-links' style='margin-top: 40px;'>");
        out.println("<a href='order_form.html' class='btn action-btn' style='background: linear-gradient(135deg, #667eea, #764ba2);'>");
        out.println("<i class='fas fa-plus-circle'></i> Place Another Order");
        out.println("</a>");
        out.println("<a href='track.html?order=" + orderNumber + "' class='btn action-btn' style='background: linear-gradient(135deg, #28a745, #20c997);'>");
        out.println("<i class='fas fa-truck'></i> Track This Order");
        out.println("</a>");
        out.println("<a href='index.html' class='btn action-btn btn-secondary'>");
        out.println("<i class='fas fa-home'></i> Return to Dashboard");
        out.println("</a>");
        out.println("</div>");
        
        out.println("</div>"); // Close container
        
        // Footer
        out.println("<div class='footer' style='background: #333; color: white; margin-top: 50px; padding: 40px 20px;'>");
        out.println("<div style='text-align: center;'>");
        out.println("<p>🎉 <strong>Order #" + orderNumber + " Confirmed</strong> • " + currentTime + "</p>");
        out.println("<p style='margin-top: 10px; color: #aaa;'>");
        out.println("Need help? <a href='contact.html' style='color: #4facfe;'>Contact Customer Support</a>");
        out.println("</p>");
        out.println("</div>");
        out.println("</div>");
        
        // JavaScript for animations
        out.println("<script>");
        out.println("// Create confetti celebration");
        out.println("function createConfetti() {");
        out.println("    const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#28a745'];");
        out.println("    const container = document.getElementById('confettiContainer');");
        out.println("    ");
        out.println("    for (let i = 0; i < 200; i++) {");
        out.println("        const confetti = document.createElement('div');");
        out.println("        confetti.className = 'confetti';");
        out.println("        confetti.style.left = Math.random() * 100 + 'vw';");
        out.println("        confetti.style.setProperty('--color', colors[Math.floor(Math.random() * colors.length)]);");
        out.println("        confetti.style.setProperty('--duration', Math.random() * 3 + 2 + 's');");
        out.println("        confetti.style.setProperty('--rotation', Math.random() * 360 + 'deg');");
        out.println("        container.appendChild(confetti);");
        out.println("        ");
        out.println("        setTimeout(() => {");
        out.println("            if (confetti.parentNode === container) {");
        out.println("                container.removeChild(confetti);");
        out.println("            }");
        out.println("        }, 5000);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("// Play success sound");
        out.println("function playSuccessSound() {");
        out.println("    try {");
        out.println("        const audioContext = new (window.AudioContext || window.webkitAudioContext)();");
        out.println("        const oscillator = audioContext.createOscillator();");
        out.println("        const gainNode = audioContext.createGain();");
        out.println("        ");
        out.println("        oscillator.connect(gainNode);");
        out.println("        gainNode.connect(audioContext.destination);");
        out.println("        ");
        out.println("        oscillator.frequency.value = 523.25; // C5");
        out.println("        oscillator.type = 'sine';");
        out.println("        ");
        out.println("        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);");
        out.println("        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 1);");
        out.println("        ");
        out.println("        oscillator.start(audioContext.currentTime);");
        out.println("        oscillator.stop(audioContext.currentTime + 1);");
        out.println("    } catch (e) {");
        out.println("        console.log('Audio not supported or blocked');");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("// Start animations on page load");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("    console.log('✅ Success page loaded!');");
        out.println("    ");
        out.println("    // Create confetti");
        out.println("    createConfetti();");
        out.println("    ");
        out.println("    // Play success sound");
        out.println("    playSuccessSound();");
        out.println("    ");
        out.println("    // Auto-scroll to top with animation");
        out.println("    window.scrollTo({ top: 0, behavior: 'smooth' });");
        out.println("    ");
        out.println("    // Show celebration message");
        out.println("    setTimeout(() => {");
        out.println("        const celebration = document.createElement('div');");
        out.println("        celebration.innerHTML = '🎉 ORDER SUCCESSFUL! 🎉';");
        out.println("        celebration.style.cssText = `");
        out.println("            position: fixed;");
        out.println("            top: 20px;");
        out.println("            left: 50%;");
        out.println("            transform: translateX(-50%);");
        out.println("            background: linear-gradient(135deg, #28a745, #20c997);");
        out.println("            color: white;");
        out.println("            padding: 15px 30px;");
        out.println("            border-radius: 50px;");
        out.println("            font-weight: 700;");
        out.println("            font-size: 1.2rem;");
        out.println("            box-shadow: 0 10px 30px rgba(40, 167, 69, 0.5);");
        out.println("            z-index: 1000;");
        out.println("            animation: bounceIn 1s ease-out;");
        out.println("        `;");
        out.println("        document.body.appendChild(celebration);");
        out.println("        ");
        out.println("        setTimeout(() => {");
        out.println("            celebration.style.animation = 'fadeOut 0.5s ease-out';");
        out.println("            setTimeout(() => {");
        out.println("                if (celebration.parentNode === document.body) {");
        out.println("                    document.body.removeChild(celebration);");
        out.println("                }");
        out.println("            }, 500);");
        out.println("        }, 3000);");
        out.println("    }, 1000);");
        out.println("});");
        out.println("");
        out.println("// Add fadeOut animation");
        out.println("const style = document.createElement('style');");
        out.println("style.textContent = `");
        out.println("    @keyframes fadeOut {");
        out.println("        from { opacity: 1; transform: translateX(-50%) scale(1); }");
        out.println("        to { opacity: 0; transform: translateX(-50%) scale(0.8); }");
        out.println("    }");
        out.println("`;");
        out.println("document.head.appendChild(style);");
        out.println("</script>");
        
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css'>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // Print error page
    private void printErrorPage(PrintWriter out, String errorMessage) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>❌ Order Failed | Please Try Again</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'>");
        out.println("<style>");
        out.println("    @keyframes shakeError {");
        out.println("        0%, 100% { transform: translateX(0); }");
        out.println("        10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }");
        out.println("        20%, 40%, 60%, 80% { transform: translateX(10px); }");
        out.println("    }");
        out.println("    ");
        out.println("    .error-hero {");
        out.println("        background: linear-gradient(135deg, #dc3545, #fd7e14);");
        out.println("        color: white;");
        out.println("        padding: 50px;");
        out.println("        border-radius: 25px;");
        out.println("        margin-bottom: 40px;");
        out.println("        text-align: center;");
        out.println("        animation: shakeError 0.5s ease-out;");
        out.println("    }");
        out.println("    ");
        out.println("    .error-card {");
        out.println("        background: #f8d7da;");
        out.println("        color: #721c24;");
        out.println("        border-left: 10px solid #dc3545;");
        out.println("        padding: 30px;");
        out.println("        border-radius: 15px;");
        out.println("        margin: 20px 0;");
        out.println("        animation: floatUp 0.8s ease-out;");
        out.println("    }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        
        out.println("<div class='error-hero'>");
        out.println("<h1><i class='fas fa-exclamation-triangle'></i> Order Failed</h1>");
        out.println("<p>We encountered an issue processing your order</p>");
        out.println("</div>");
        
        out.println("<div class='error-card'>");
        out.println("<h3><i class='fas fa-exclamation-circle'></i> Error Details</h3>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("<p style='margin-top: 20px; font-size: 0.9rem; color: #856404;'>");
        out.println("<i class='fas fa-lightbulb'></i> Please check your internet connection and try again.");
        out.println("</p>");
        out.println("</div>");
        
        out.println("<div class='nav-links'>");
        out.println("<a href='order_form.html' class='btn' style='background: #dc3545;'>");
        out.println("<i class='fas fa-redo'></i> Try Again");
        out.println("</a>");
        out.println("<a href='index.html' class='btn btn-secondary'>");
        out.println("<i class='fas fa-home'></i> Return Home");
        out.println("</a>");
        out.println("<a href='contact.html' class='btn'>");
        out.println("<i class='fas fa-headset'></i> Contact Support");
        out.println("</a>");
        out.println("</div>");
        
        out.println("</div>");
        
        out.println("<script>");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("    console.log('❌ Error page displayed');");
        out.println("    ");
        out.println("    // Play error sound");
        out.println("    try {");
        out.println("        const audioContext = new (window.AudioContext || window.webkitAudioContext)();");
        out.println("        const oscillator = audioContext.createOscillator();");
        out.println("        const gainNode = audioContext.createGain();");
        out.println("        ");
        out.println("        oscillator.connect(gainNode);");
        out.println("        gainNode.connect(audioContext.destination);");
        out.println("        ");
        out.println("        oscillator.frequency.value = 220; // A3");
        out.println("        oscillator.type = 'sawtooth';");
        out.println("        ");
        out.println("        gainNode.gain.setValueAtTime(0.2, audioContext.currentTime);");
        out.println("        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);");
        out.println("        ");
        out.println("        oscillator.start(audioContext.currentTime);");
        out.println("        oscillator.stop(audioContext.currentTime + 0.5);");
        out.println("    } catch (e) {");
        out.println("        console.log('Audio not supported');");
        out.println("    }");
        out.println("});");
        out.println("</script>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    public void destroy() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("✅ Database connection closed gracefully.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
}