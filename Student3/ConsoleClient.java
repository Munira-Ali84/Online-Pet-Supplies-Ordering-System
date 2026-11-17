
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ClientConnection client = new ClientConnection();
        Cart cart = new Cart();

        Map<String, Double> prices = new HashMap<>();
        prices.put("CatFood", 50.0);
        prices.put("DogToy", 25.0);
        prices.put("PetCollar", 30.0);

        System.out.println("=== Pet Supplies Store ===");

        System.out.print("Enter server IP: ");
        String host = scanner.nextLine();

        System.out.print("Enter server port: ");
        int port = Integer.parseInt(scanner.nextLine());

        if (!client.connect(host, port)) {
            System.out.println("Unable to connect.");
            scanner.close();
            return;
        }

        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. Update Quantity");
            System.out.println("4. Remove Product");
            System.out.println("5. View Cart");
            System.out.println("6. Place Order");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    System.out.println("Available Products:");
                    for (String product : prices.keySet()) {
                        System.out.println(product + " - "
                                + prices.get(product) + " SAR");
                    }
                    break;

                case "2":
                    System.out.print("Enter product name: ");
                    String product = scanner.nextLine();

                    if (!prices.containsKey(product)) {
                        System.out.println("Product not found.");
                        break;
                    }

                    System.out.print("Enter quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());

                    cart.addProduct(product, quantity);
                    break;

                case "3":
                    System.out.print("Enter product name: ");
                    String updateProduct = scanner.nextLine();

                    System.out.print("Enter new quantity: ");
                    int newQuantity = Integer.parseInt(scanner.nextLine());

                    cart.updateQuantity(updateProduct, newQuantity);
                    break;

                case "4":
                    System.out.print("Enter product name: ");
                    String removeProduct = scanner.nextLine();

                    cart.removeProduct(removeProduct);
                    break;

                case "5":
                    System.out.println("Cart: " + cart.getItems());
                    System.out.println("Total: "
                            + cart.calculateTotal(prices) + " SAR");
                    break;

                case "6":
                    if (cart.getItems().isEmpty()) {
                        System.out.println("Cart is empty.");
                        break;
                    }

                    String order = "ORDER:" + cart.getItems();

                    client.sendMessage(order);

String response = client.receiveMessage();

if (response != null && response.startsWith("ACCEPTED")) {
    System.out.println("Order accepted successfully!");
    cart = new Cart();

} else if (response != null && response.startsWith("REJECTED")) {
    System.out.println("Order rejected: " + response);
    System.out.println("Items remain in your cart.");

} else {
    System.out.println("Unexpected server response: " + response);
}

break;


                case "7":
                    client.disconnect();
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
