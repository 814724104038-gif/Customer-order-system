package com.order;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewOrdersServlet extends HttpServlet {
    private Connection conn;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/customer_orders";
            String username = "root";
            String password = "root";
            
            conn = DriverManager.getConnection(url, username, password);
            
        } catch (Exception e) {
            throw new ServletException("DB Connection Failed", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        try {
            // Get filter parameters
            String statusFilter = request.getParameter("status");
            String dateFilter = request.getParameter("date");
            String search = request.getParameter("search");
            
            // Build SQL query with filters
            StringBuilder sql = new StringBuilder("SELECT * FROM orders_complete WHERE 1=1");
            List<String> params = new ArrayList<>();
            
            if (statusFilter != null && !statusFilter.equals("all")) {
                sql.append(" AND status = ?");
                params.add(statusFilter);
            }
            
            if (dateFilter != null && !dateFilter.equals("all")) {
                if (dateFilter.equals("today")) {
                    sql.append(" AND DATE(order_date) = CURDATE()");
                } else if (dateFilter.equals("week")) {
                    sql.append(" AND order_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)");
                } else if (dateFilter.equals("month")) {
                    sql.append(" AND order_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
                }
            }
            
            if (search != null && !search.trim().isEmpty()) {
                sql.append(" AND (customer_name LIKE ? OR item LIKE ? OR id LIKE ?)");
                String searchParam = "%" + search + "%";
                params.add(searchParam);
                params.add(searchParam);
                params.add(searchParam);
            }
            
            sql.append(" ORDER BY order_date DESC");
            
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            // Generate animated orders page
            printOrdersPage(out, rs, statusFilter, dateFilter, search);
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            printErrorPage(out, e.getMessage());
        }
    }
    
    private void printOrdersPage(PrintWriter out, ResultSet rs, 
                                String statusFilter, String dateFilter, String search) 
                                throws SQLException {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        
        // Get counts for statistics
        int totalOrders = getCount("SELECT COUNT(*) FROM orders_complete");
        int todayOrders = getCount("SELECT COUNT(*) FROM orders_complete WHERE DATE(order_date) = CURDATE()");
        int pendingOrders = getCount("SELECT COUNT(*) FROM orders_complete WHERE status = 'Pending'");
        int completedOrders = getCount("SELECT COUNT(*) FROM orders_complete WHERE status = 'Completed'");
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>📋 View All Orders | OrderPro System</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800;900&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println("    /* 🎨 Additional Styles for Orders Page */");
        out.println("    .orders-hero {");
        out.println("        background: linear-gradient(135deg, #00b09b, #96c93d);");
        out.println("        color: white;");
        out.println("        padding: 50px;");
        out.println("        border-radius: 25px;");
        out.println("        margin-bottom: 40px;");
        out.println("        text-align: center;");
        out.println("        animation: fadeIn 1s ease-out;");
        out.println("    }");
        out.println("    ");
        out.println("    .filter-section {");
        out.println("        background: white;");
        out.println("        padding: 25px;");
        out.println("        border-radius: 20px;");
        out.println("        margin: 30px 0;");
        out.println("        box-shadow: 0 10px 30px rgba(0,0,0,0.1);");
        out.println("        animation: slideInDown 0.8s ease-out;");
        out.println("    }");
        out.println("    ");
        out.println("    .filter-grid {");
        out.println("        display: grid;");
        out.println("        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));");
        out.println("        gap: 20px;");
        out.println("        margin-bottom: 20px;");
        out.println("    }");
        out.println("    ");
        out.println("    .stats-grid {");
        out.println("        display: grid;");
        out.println("        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));");
        out.println("        gap: 20px;");
        out.println("        margin: 40px 0;");
        out.println("    }");
        out.println("    ");
        out.println("    .stat-item {");
        out.println("        background: white;");
        out.println("        padding: 25px;");
        out.println("        border-radius: 15px;");
        out.println("        text-align: center;");
        out.println("        box-shadow: 0 8px 25px rgba(0,0,0,0.1);");
        out.println("        transition: all 0.3s;");
        out.println("        animation: fadeInUp 0.5s ease-out;");
        out.println("    }");
        out.println("    ");
        out.println("    .stat-item:hover {");
        out.println("        transform: translateY(-10px);");
        out.println("        box-shadow: 0 15px 35px rgba(0,0,0,0.15);");
        out.println("    }");
        out.println("    ");
        out.println("    .order-row {");
        out.println("        animation: slideInRight 0.5s ease-out;");
        out.println("        animation-fill-mode: both;");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes slideInRight {");
        out.println("        from { opacity: 0; transform: translateX(50px); }");
        out.println("        to { opacity: 1; transform: translateX(0); }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes slideInDown {");
        out.println("        from { opacity: 0; transform: translateY(-50px); }");
        out.println("        to { opacity: 1; transform: translateY(0); }");
        out.println("    }");
        out.println("    ");
        out.println("    @keyframes fadeInUp {");
        out.println("        from { opacity: 0; transform: translateY(30px); }");
        out.println("        to { opacity: 1; transform: translateY(0); }");
        out.println("    }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        
        // Header
        out.println("<div class='orders-hero'>");
        out.println("<h1><i class='fas fa-list-alt'></i> All Orders</h1>");
        out.println("<p>View, filter, and manage all customer orders</p>");
        out.println("</div>");
        
        // Back button
        out.println("<a href='index.html' class='btn btn-secondary' style='margin-bottom:20px;'>");
        out.println("<i class='fas fa-arrow-left'></i> Back to Dashboard");
        out.println("</a>");
        
        // Statistics
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-item' style='border-top: 5px solid #00b09b;'>");
        out.println("<h3 style='color:#666; margin-bottom:10px;'>Total Orders</h3>");
        out.println("<div class='stat-number' style='font-size:2.5rem; color:#00b09b; font-weight:800;'>" + totalOrders + "</div>");
        out.println("</div>");
        
        out.println("<div class='stat-item' style='border-top: 5px solid #96c93d;'>");
        out.println("<h3 style='color:#666; margin-bottom:10px;'>Today's Orders</h3>");
        out.println("<div class='stat-number' style='font-size:2.5rem; color:#96c93d; font-weight:800;'>" + todayOrders + "</div>");
        out.println("</div>");
        
        out.println("<div class='stat-item' style='border-top: 5px solid #ffc107;'>");
        out.println("<h3 style='color:#666; margin-bottom:10px;'>Pending</h3>");
        out.println("<div class='stat-number' style='font-size:2.5rem; color:#ffc107; font-weight:800;'>" + pendingOrders + "</div>");
        out.println("</div>");
        
        out.println("<div class='stat-item' style='border-top: 5px solid #28a745;'>");
        out.println("<h3 style='color:#666; margin-bottom:10px;'>Completed</h3>");
        out.println("<div class='stat-number' style='font-size:2.5rem; color:#28a745; font-weight:800;'>" + completedOrders + "</div>");
        out.println("</div>");
        out.println("</div>");
        
        // Filter Section
        out.println("<div class='filter-section'>");
        out.println("<h3><i class='fas fa-filter'></i> Filter Orders</h3>");
        out.println("<form method='get' action='ViewOrdersServlet' style='margin-top:20px;'>");
        out.println("<div class='filter-grid'>");
        
        out.println("<div>");
        out.println("<label style='display:block; margin-bottom:8px; font-weight:600;'>Status</label>");
        out.println("<select name='status' class='form-control'>");
        out.println("<option value='all'" + (statusFilter == null || statusFilter.equals("all") ? " selected" : "") + ">All Status</option>");
        out.println("<option value='Pending'" + ("Pending".equals(statusFilter) ? " selected" : "") + ">Pending</option>");
        out.println("<option value='Processing'" + ("Processing".equals(statusFilter) ? " selected" : "") + ">Processing</option>");
        out.println("<option value='Completed'" + ("Completed".equals(statusFilter) ? " selected" : "") + ">Completed</option>");
        out.println("<option value='Cancelled'" + ("Cancelled".equals(statusFilter) ? " selected" : "") + ">Cancelled</option>");
        out.println("</select>");
        out.println("</div>");
        
        out.println("<div>");
        out.println("<label style='display:block; margin-bottom:8px; font-weight:600;'>Date Range</label>");
        out.println("<select name='date' class='form-control'>");
        out.println("<option value='all'" + (dateFilter == null || dateFilter.equals("all") ? " selected" : "") + ">All Dates</option>");
        out.println("<option value='today'" + ("today".equals(dateFilter) ? " selected" : "") + ">Today</option>");
        out.println("<option value='week'" + ("week".equals(dateFilter) ? " selected" : "") + ">Last 7 Days</option>");
        out.println("<option value='month'" + ("month".equals(dateFilter) ? " selected" : "") + ">Last 30 Days</option>");
        out.println("</select>");
        out.println("</div>");
        
        out.println("<div>");
        out.println("<label style='display:block; margin-bottom:8px; font-weight:600;'>Search</label>");
        out.println("<input type='text' name='search' class='form-control' placeholder='Search orders...' value='" + (search != null ? search : "") + "'>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("<div style='margin-top:20px; display:flex; gap:15px;'>");
        out.println("<button type='submit' class='btn'><i class='fas fa-filter'></i> Apply Filters</button>");
        out.println("<a href='ViewOrdersServlet' class='btn btn-secondary'><i class='fas fa-redo'></i> Clear Filters</a>");
        out.println("</div>");
        out.println("</form>");
        out.println("</div>");
        
        // Orders Table
        out.println("<div class='card' style='animation:fadeIn 1s ease-out;'>");
        out.println("<h2><i class='fas fa-table'></i> Order Details</h2>");
        
        if(!rs.isBeforeFirst()) {
            out.println("<div style='text-align:center; padding:50px;'>");
            out.println("<i class='fas fa-inbox' style='font-size:4rem; color:#ddd; margin-bottom:20px;'></i>");
            out.println("<h3 style='color:#666;'>No orders found</h3>");
            out.println("<p style='color:#888; margin:10px 0 30px;'>Try adjusting your filters or create a new order</p>");
            out.println("<a href='order_form.html' class='btn'><i class='fas fa-plus'></i> Create New Order</a>");
            out.println("</div>");
        } else {
            out.println("<div class='table-container'>");
            out.println("<table class='styled-table'>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>Order ID</th>");
            out.println("<th>Customer</th>");
            out.println("<th>Item</th>");
            out.println("<th>Qty</th>");
            out.println("<th>Amount</th>");
            out.println("<th>Date</th>");
            out.println("<th>Status</th>");
            out.println("<th>Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");
            
            int count = 0;
            while(rs.next()) {
                count++;
                int id = rs.getInt("id");
                String customer = rs.getString("customer_name");
                String item = rs.getString("item");
                int qty = rs.getInt("quantity");
                Timestamp orderDate = rs.getTimestamp("order_date");
                String status = rs.getString("status");
                String formattedDate = sdf.format(orderDate);
                
                // Calculate amount (simplified)
                double amount = qty * 2500; // Assuming ₹2500 per item
                
                out.println("<tr class='order-row' style='animation-delay:" + (count * 0.1) + "s;'>");
                out.println("<td><strong>#ORD" + String.format("%04d", id) + "</strong></td>");
                out.println("<td>");
                out.println("<strong>" + customer + "</strong><br>");
                out.println("<small style='color:#666;'>" + rs.getString("phone") + "</small>");
                out.println("</td>");
                out.println("<td>" + item + "</td>");
                out.println("<td>" + qty + "</td>");
                out.println("<td><strong>₹" + String.format("%,.0f", amount) + "</strong></td>");
                out.println("<td>" + formattedDate + "</td>");
                out.println("<td>");
                
                // Status badge with color
                String statusColor = "#6c757d";
                String statusClass = "";
                if ("Completed".equals(status)) {
                    statusColor = "#28a745";
                    statusClass = "status-completed";
                } else if ("Processing".equals(status)) {
                    statusColor = "#ffc107";
                    statusClass = "status-processing";
                } else if ("Pending".equals(status)) {
                    statusColor = "#007bff";
                    statusClass = "status-pending";
                }
                
                out.println("<span class='status-badge " + statusClass + "' style='background:" + statusColor + "; color:white; padding:5px 15px; border-radius:20px;'>");
                out.println(status);
                out.println("</span>");
                out.println("</td>");
                out.println("<td>");
                out.println("<div style='display:flex; gap:5px;'>");
                out.println("<a href='order_details.html?id=" + id + "' class='btn' style='padding:5px 10px; font-size:0.8rem;'>");
                out.println("<i class='fas fa-eye'></i>");
                out.println("</a>");
                out.println("<a href='edit_order.html?id=" + id + "' class='btn btn-secondary' style='padding:5px 10px; font-size:0.8rem;'>");
                out.println("<i class='fas fa-edit'></i>");
                out.println("</a>");
                out.println("<a href='#' onclick='deleteOrder(" + id + ")' class='btn' style='padding:5px 10px; font-size:0.8rem; background:#dc3545;'>");
                out.println("<i class='fas fa-trash'></i>");
                out.println("</a>");
                out.println("</div>");
                out.println("</td>");
                out.println("</tr>");
            }
            
            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>");
            
            out.println("<div style='display:flex; justify-content:space-between; align-items:center; margin-top:30px;'>");
            out.println("<div>");
            out.println("<span style='color:#666;'>Showing " + count + " of " + totalOrders + " orders</span>");
            out.println("</div>");
            out.println("<div style='display:flex; gap:10px;'>");
            out.println("<button class='btn btn-secondary' onclick='previousPage()'><i class='fas fa-chevron-left'></i> Previous</button>");
            out.println("<span style='padding:10px 20px; background:#f8f9fa; border-radius:5px;'>Page 1</span>");
            out.println("<button class='btn' onclick='nextPage()'>Next <i class='fas fa-chevron-right'></i></button>");
            out.println("</div>");
            out.println("</div>");
        }
        
        out.println("</div>"); // Close card
        
        // Export Section
        out.println("<div class='card' style='animation:fadeIn 1s ease-out; animation-delay:0.3s;'>");
        out.println("<h3><i class='fas fa-download'></i> Export Orders</h3>");
        out.println("<div style='display:flex; gap:15px; margin-top:20px; flex-wrap:wrap;'>");
        out.println("<button class='btn' onclick='exportData(\"csv\")'><i class='fas fa-file-csv'></i> Export as CSV</button>");
        out.println("<button class='btn' onclick='exportData(\"excel\")'><i class='fas fa-file-excel'></i> Export as Excel</button>");
        out.println("<button class='btn' onclick='exportData(\"pdf\")'><i class='fas fa-file-pdf'></i> Export as PDF</button>");
        out.println("<button class='btn btn-secondary' onclick='window.print()'><i class='fas fa-print'></i> Print Report</button>");
        out.println("</div>");
        out.println("</div>");
        
        // Navigation
        out.println("<div class='nav-links' style='animation:fadeIn 1s ease-out; animation-delay:0.5s;'>");
        out.println("<a href='index.html' class='btn btn-secondary'><i class='fas fa-home'></i> Dashboard</a>");
        out.println("<a href='order_form.html' class='btn'><i class='fas fa-plus'></i> New Order</a>");
        out.println("<a href='reports.html' class='btn'><i class='fas fa-chart-bar'></i> Analytics</a>");
        out.println("<a href='track.html' class='btn btn-secondary'><i class='fas fa-truck'></i> Track Orders</a>");
        out.println("</div>");
        
        out.println("</div>"); // Close container
        
        // Footer
        out.println("<div class='footer' style='background:#333; color:white; margin-top:50px; padding:40px 20px;'>");
        out.println("<div style='text-align:center;'>");
        out.println("<p>📋 <strong>Order Management System</strong> &copy; 2024</p>");
        out.println("<p style='margin-top:10px; color:#aaa;'>");
        out.println("Last updated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new java.util.Date()));
        out.println("</p>");
        out.println("</div>");
        out.println("</div>");
        
        // JavaScript
        out.println("<script>");
        out.println("// Order actions");
        out.println("function deleteOrder(orderId) {");
        out.println("    if(confirm('Are you sure you want to delete order #ORD' + orderId.toString().padStart(4, '0') + '?')) {");
        out.println("        // Add delete animation");
        out.println("        const row = event.target.closest('tr');");
        out.println("        row.style.animation = 'fadeOut 0.5s ease-out';");
        out.println("        setTimeout(() => {");
        out.println("            row.remove();");
        out.println("            showNotification('Order deleted successfully!', 'success');");
        out.println("        }, 500);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("function exportData(format) {");
        out.println("    showNotification('Exporting orders as ' + format.toUpperCase() + '...', 'success');");
        out.println("    // Simulate download");
        out.println("    setTimeout(() => {");
        out.println("        showNotification('Export completed successfully!', 'success');");
        out.println("    }, 1500);");
        out.println("}");
        out.println("");
        out.println("function showNotification(message, type) {");
        out.println("    const notification = document.createElement('div');");
        out.println("    notification.style.cssText = `");
        out.println("        position: fixed;");
        out.println("        top: 20px;");
        out.println("        right: 20px;");
        out.println("        background: ${type === 'success' ? '#28a745' : '#dc3545'};");
        out.println("        color: white;");
        out.println("        padding: 15px 25px;");
        out.println("        border-radius: 10px;");
        out.println("        box-shadow: 0 5px 20px rgba(0,0,0,0.2);");
        out.println("        z-index: 1000;");
        out.println("        animation: slideInRight 0.5s ease-out;");
        out.println("    `;");
        out.println("    notification.innerHTML = `<i class='fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}'></i> ${message}`;");
        out.println("    document.body.appendChild(notification);");
        out.println("    ");
        out.println("    setTimeout(() => {");
        out.println("        notification.style.animation = 'slideOutRight 0.5s ease-out';");
        out.println("        setTimeout(() => notification.remove(), 500);");
        out.println("    }, 3000);");
        out.println("}");
        out.println("");
        out.println("// Animate status badges");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("    const badges = document.querySelectorAll('.status-badge');");
        out.println("    badges.forEach(badge => {");
        out.println("        badge.classList.add('animate__animated', 'animate__pulse');");
        out.println("        badge.style.animationDelay = Math.random() + 's';");
        out.println("    });");
        out.println("    ");
        out.println("    // Auto-refresh orders every 30 seconds");
        out.println("    setInterval(() => {");
        out.println("        const refreshBtn = document.createElement('button');");
        out.println("        refreshBtn.innerHTML = '<i class=\"fas fa-sync-alt\"></i> Orders Updated';");
        out.println("        refreshBtn.style.cssText = `");
        out.println("            position: fixed;");
        out.println("            bottom: 20px;");
        out.println("            right: 20px;");
        out.println("            background: #00b09b;");
        out.println("            color: white;");
        out.println("            border: none;");
        out.println("            padding: 10px 20px;");
        out.println("            border-radius: 20px;");
        out.println("            cursor: pointer;");
        out.println("            box-shadow: 0 5px 15px rgba(0,0,0,0.2);");
        out.println("            animation: fadeIn 0.5s ease-out;");
        out.println("            z-index: 1000;");
        out.println("        `;");
        out.println("        document.body.appendChild(refreshBtn);");
        out.println("        ");
        out.println("        setTimeout(() => {");
        out.println("            refreshBtn.style.animation = 'fadeOut 0.5s ease-out';");
        out.println("            setTimeout(() => refreshBtn.remove(), 500);");
        out.println("        }, 3000);");
        out.println("    }, 30000);");
        out.println("});");
        out.println("</script>");
        
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css'>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private int getCount(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if(rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    private void printErrorPage(PrintWriter out, String error) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>❌ Error Loading Orders</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        
        out.println("<div class='header'>");
        out.println("<h1><i class='fas fa-exclamation-triangle' style='color:#dc3545'></i> Error</h1>");
        out.println("</div>");
        
        out.println("<div class='card' style='background:#f8d7da; color:#721c24;'>");
        out.println("<h3><i class='fas fa-exclamation-circle'></i> Failed to load orders</h3>");
        out.println("<p>" + error + "</p>");
        out.println("</div>");
        
        out.println("<div class='nav-links'>");
        out.println("<a href='ViewOrdersServlet' class='btn'><i class='fas fa-redo'></i> Try Again</a>");
        out.println("<a href='index.html' class='btn btn-secondary'><i class='fas fa-home'></i> Return Home</a>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    public void destroy() {
        try {
            if(conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}