
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {

    private Map<String, Integer> items = new LinkedHashMap<>();

    // Add a product to the cart
    public void addProduct(String productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        items.put(productId,
            items.getOrDefault(productId, 0) + quantity);

        System.out.println("Product added to cart.");
    }

    // Update product quantity
    public void updateQuantity(String productId, int quantity) {
        if (!items.containsKey(productId)) {
            System.out.println("Product not found in cart.");
        } else if (quantity <= 0) {
            items.remove(productId);
        } else {
            items.put(productId, quantity);
        }
    }

    // Remove a product from the cart
    public void removeProduct(String productId) {
        items.remove(productId);
    }

    // Get the products and quantities
    public Map<String, Integer> getItems() {
        return new LinkedHashMap<>(items);
    }

    // Calculate total using product prices
    public double calculateTotal(Map<String, Double> prices) {
        double total = 0;

        for (String productId : items.keySet()) {
            int quantity = items.get(productId);
            double price = prices.getOrDefault(productId, 0.0);

            total += price * quantity;
        }

        return total;
    }
}
