import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        ExecutorService clientExecutor = Executors.newCachedThreadPool();

        startServerAsync(serverExecutor);
        waitForServerStart();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nEnter the number of client threads to spawn\nType 'exit' to stop\nPress 'Enter' to run one client");

        while (true) {
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                clientExecutor.submit(Main::startClient);
            } else if (input.equalsIgnoreCase("exit")) {
                break;
            } else {
                try {
                    int numClients = Integer.parseInt(input);
                    startMultipleClients(clientExecutor, numClients);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number, 'exit', or press Enter.");
                }
            }
        }

        shutdownExecutor(clientExecutor, "Client");
        shutdownExecutor(serverExecutor, "Server");

        scanner.close();
    }

    private static void startServerAsync(ExecutorService serverExecutor) {
        serverExecutor.submit(Main::startServer);
    }

    private static void waitForServerStart() {
        sleepSeconds(1);
    }

    private static void startMultipleClients(ExecutorService clientExecutor, int numClients) {
        for (int i = 0; i < numClients; i++) {
            clientExecutor.submit(Main::startClient);
        }
        sleepSeconds(1);
    }

    private static void shutdownExecutor(ExecutorService executor, String executorName) {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                System.out.println(executorName + " executor did not terminate within the expected time frame.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void startServer() {
        System.out.println("Server started");
        new Server().run();
    }

    private static void startClient() {
        System.out.println("Client started");
        new Client().run();
    }

    private static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}