import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class server {

    
    static class Product {
        String id, category, name, imagePath;
        double price;
        int stock;

        Product(String id, String category, String name, double price, int stock, String imagePath) {
            this.id = id;
            this.category = category;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.imagePath = imagePath;
        }
    }

    static class OrderItem {
        String productId, productName;
        int quantity;
        double unitPrice;

        OrderItem(String productId, String productName, int quantity, double unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    static class Order {
        String username, timestamp, status;
        List<OrderItem> items = new ArrayList<>();

        Order(String username) {
            this.username = username;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.status = "CONFIRMED";
        }

        double getTotal() {
            double total = 0;
            for (OrderItem item : items) {
                total += item.unitPrice * item.quantity;
            }
            return total;
        }
    }

    
    private static final Map<String, Product> products = new LinkedHashMap<>();
    private static final List<Order> orders = Collections.synchronizedList(new ArrayList<>());
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    static {
        // Pet Food
        products.put("F1", new Product("F1", "Pet Food", "Dry Chicken Dog Food", 45.0, 10, "images/dog_food.png"));
        products.put("F2", new Product("F2", "Pet Food", "Salmon Kitten Kibble", 38.0, 8, "images/cat_food.png"));
        products.put("F3", new Product("F3", "Pet Food", "Beef Dental Chews", 25.0, 12, "images/chews.png"));

        // Pet Toys
        products.put("T1", new Product("T1", "Pet Toys", "Squeaky Plush Bone", 18.0, 9, "images/bone_toy.png"));
        products.put("T2", new Product("T2", "Pet Toys", "Feather Wand Teaser", 15.0, 7, "images/feather_toy.png"));
        products.put("T3", new Product("T3", "Pet Toys", "Rubber Fetch Ball", 12.0, 5, "images/ball_toy.png"));

        // Pet Accessories
        products.put("A1", new Product("A1", "Pet Accessories", "Nylon Adjustable Collar", 24.0, 10, "images/collar.png"));
        products.put("A2", new Product("A2", "Pet Accessories", "Stainless Steel Bowl", 30.0, 8, "images/bowl.png"));
        products.put("A3", new Product("A3", "Pet Accessories", "Soft Pet Carrier Bag", 95.0, 4, "images/carrier.png"));
    }

    
    private static synchronized String buildStockMessage() {
        StringBuilder sb = new StringBuilder("STOCK|");
        for (Product p : products.values()) {
            sb.append(p.id).append(":")
              .append(p.category).append(":")
              .append(p.name).append(":")
              .append(p.price).append(":")
              .append(p.stock).append(":")
              .append(p.imagePath).append(";");
        }
        return sb.toString();
    }

    private static void broadcastStock() {
        String msg = buildStockMessage();
        synchronized (clientWriters) {
            for (PrintWriter out : clientWriters) {
                out.println(msg);
            }
        }
    }

    private static synchronized String handleOrder(String username, String itemsPayload) {
        if (itemsPayload == null || itemsPayload.trim().isEmpty()) {
            return "ORDER_FAIL|Cart is empty";
        }

        String[] entries = itemsPayload.split(",");
        List<OrderItem> parsedItems = new ArrayList<>();

   
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) return "ORDER_FAIL|Invalid item format";

            Product p = products.get(parts[0]);
            int qty = Integer.parseInt(parts[1]);

            if (p == null || qty <= 0 || p.stock < qty) {
                return "ORDER_FAIL|Insufficient stock for " + (p != null ? p.name : parts[0]);
            }
            parsedItems.add(new OrderItem(p.id, p.name, qty, p.price));
        }

        // 
        Order order = new Order(username);
        for (OrderItem item : parsedItems) {
            Product p = products.get(item.productId);
            p.stock -= item.quantity;
            order.items.add(item);
        }
        orders.add(order);

        broadcastStock(); // 
        return "ORDER_OK|" + order.timestamp + "|Total=" + String.format("%.2f", order.getTotal());
    }

    private static String handleGetOrders(String username) {
        StringBuilder sb = new StringBuilder("ORDERS|");
        boolean found = false;
        synchronized (orders) {
            for (Order o : orders) {
                if (o.username.equalsIgnoreCase(username)) {
                    found = true;
                    sb.append(o.timestamp).append(" (").append(o.status).append(") - Total: ")
                      .append(String.format("%.2f", o.getTotal())).append(" || ");
                }
            }
        }
        return found ? sb.toString() : "ORDERS|No orders found";
    }

    private static synchronized String handleCancelOrder(String username, String timestamp) {
        for (Order o : orders) {
            if (o.username.equalsIgnoreCase(username) && o.timestamp.equals(timestamp)) {
                if ("CANCELLED".equals(o.status)) {
                    return "CANCEL_FAIL|Order already cancelled";
                }
                for (OrderItem item : o.items) {
                    Product p = products.get(item.productId);
                    if (p != null) {
                        p.stock += item.quantity;
                    }
                }
                o.status = "CANCELLED";
                broadcastStock(); // 
                return "CANCEL_OK|" + timestamp;
            }
        }
        return "CANCEL_FAIL|Order not found";
    }
}