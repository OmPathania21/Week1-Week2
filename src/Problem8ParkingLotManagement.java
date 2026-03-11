import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Problem8ParkingLotManagement {
    private enum SpotStatus {
        EMPTY,
        OCCUPIED,
        DELETED
    }

    private static class ParkingSpot {
        private SpotStatus status = SpotStatus.EMPTY;
        private String licensePlate;
        private LocalDateTime entryTime;
    }

    private final ParkingSpot[] table;
    private final Map<Integer, Integer> probeHistogram = new HashMap<>();
    private int occupiedCount;

    public Problem8ParkingLotManagement(int capacity) {
        this.table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    public String parkVehicle(String licensePlate) {
        int preferredSpot = hash(licensePlate);
        for (int probe = 0; probe < table.length; probe++) {
            int index = (preferredSpot + probe) % table.length;
            if (table[index].status != SpotStatus.OCCUPIED) {
                table[index].status = SpotStatus.OCCUPIED;
                table[index].licensePlate = licensePlate;
                table[index].entryTime = LocalDateTime.now();
                occupiedCount++;
                probeHistogram.merge(probe, 1, Integer::sum);
                return "Assigned spot #" + index + " (" + probe + " probes)";
            }
        }
        return "Parking lot full";
    }

    public String exitVehicle(String licensePlate) {
        int preferredSpot = hash(licensePlate);
        for (int probe = 0; probe < table.length; probe++) {
            int index = (preferredSpot + probe) % table.length;
            ParkingSpot spot = table[index];
            if (spot.status == SpotStatus.EMPTY) {
                break;
            }
            if (spot.status == SpotStatus.OCCUPIED && licensePlate.equals(spot.licensePlate)) {
                Duration duration = Duration.between(spot.entryTime, LocalDateTime.now());
                double hours = Math.max(1.0, duration.toMinutes() / 60.0);
                double fee = hours * 5.5;
                spot.status = SpotStatus.DELETED;
                spot.licensePlate = null;
                spot.entryTime = null;
                occupiedCount--;
                return "Spot #" + index + " freed, Duration: " + duration.toMinutes() + "m, Fee: $" +
                        String.format("%.2f", fee);
            }
        }
        return "Vehicle not found";
    }

    public String getStatistics() {
        double occupancy = (occupiedCount * 100.0) / table.length;
        int totalProbes = probeHistogram.entrySet().stream()
                .mapToInt(entry -> entry.getKey() * entry.getValue())
                .sum();
        int totalParks = probeHistogram.values().stream().mapToInt(Integer::intValue).sum();
        double avgProbes = totalParks == 0 ? 0.0 : totalProbes / (double) totalParks;
        return String.format("Occupancy: %.1f%%, Avg Probes: %.2f", occupancy, avgProbes);
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % table.length;
    }

    public static void main(String[] args) {
        Problem8ParkingLotManagement parkingLot = new Problem8ParkingLotManagement(10);
        System.out.println(parkingLot.parkVehicle("ABC-1234"));
        System.out.println(parkingLot.parkVehicle("ABC-1235"));
        System.out.println(parkingLot.parkVehicle("XYZ-9999"));
        System.out.println(parkingLot.exitVehicle("ABC-1234"));
        System.out.println(parkingLot.getStatistics());
    }
}
