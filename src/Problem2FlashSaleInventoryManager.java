import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Problem2FlashSaleInventoryManager {
    private static class ProductInventory {
        private final AtomicInteger stock;
        private final Queue<Long> waitingList = new ConcurrentLinkedQueue<>();

        private ProductInventory(int initialStock) {
            this.stock = new AtomicInteger(initialStock);
        }
    }

    private final Map<String, ProductInventory> inventory = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stockCount) {
        inventory.put(productId, new ProductInventory(stockCount));
    }

    public int checkStock(String productId) {
        ProductInventory productInventory = inventory.get(productId);
        return productInventory == null ? 0 : productInventory.stock.get();
    }

    public String purchaseItem(String productId, long userId) {
        ProductInventory productInventory = inventory.get(productId);
        if (productInventory == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = productInventory.stock.get();
            if (currentStock <= 0) {
                productInventory.waitingList.offer(userId);
                return "Added to waiting list, position #" + productInventory.waitingList.size();
            }
            if (productInventory.stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    public static void main(String[] args) {
        Problem2FlashSaleInventoryManager manager = new Problem2FlashSaleInventoryManager();
        manager.addProduct("IPHONE15_256GB", 2);
        System.out.println("checkStock -> " + manager.checkStock("IPHONE15_256GB") + " units available");
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}
