public class MemoryCheck {
    public static void main(String[] args) {
        // Get current runtime
        Runtime runtime = Runtime.getRuntime();
        
        // Calculate memory in MB
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Convert to MB
        System.out.println("=== JVM Memory Information ===");
        System.out.println(String.format("Max Memory:   %d MB", maxMemory / (1024 * 1024)));
        System.out.println(String.format("Total Memory: %d MB", totalMemory / (1024 * 1024)));
        System.out.println(String.format("Used Memory:  %d MB", usedMemory / (1024 * 1024)));
        System.out.println(String.format("Free Memory:  %d MB", freeMemory / (1024 * 1024)));
        
        // Additional JVM info
        System.out.println("\n=== JVM Information ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JVM Name: " + System.getProperty("java.vm.name"));
        System.out.println("JVM Vendor: " + System.getProperty("java.vendor"));
        System.out.println("JVM Version: " + System.getProperty("java.vm.version"));
    }
}
