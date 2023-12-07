import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private static final int PORT = 3000;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    handleError("Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            handleError("Error creating server socket", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            RequestPayload requestPayload1 = (RequestPayload) inputStream.readObject();
            RequestPayload requestPayload2 = (RequestPayload) inputStream.readObject();

            if (!requestPayload1.getError().isEmpty() || !requestPayload2.getError().isEmpty()) {
                handleError(requestPayload1.getError(), outputStream);
                handleError(requestPayload2.getError(), outputStream);
                return;
            }

            if (requestPayload1.getMatrix().length != requestPayload2.getMatrix()[0].length) {
                handleError("Cannot multiply matrices", outputStream);
                return;
            }

            int[][] resultMatrix = multiplyMatrices(requestPayload1.getMatrix(), requestPayload2.getMatrix());
            outputStream.writeObject(new RequestPayload(resultMatrix));

        } catch (IOException | ClassNotFoundException e) {
            handleError("Error handling client", e);
        } finally {
            closeSocket(clientSocket);
        }
    }

    private void handleError(String errorMessage, ObjectOutputStream outputStream) throws IOException {
        System.out.println("Server error: " + errorMessage);
        outputStream.writeObject(new RequestPayload(new int[0][0], errorMessage));
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            handleError("Error closing client socket", e);
        }
    }

    private void handleError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }

    private int[][] multiplyMatrices(int[][] matrix1, int[][] matrix2) {
        int rows = matrix1.length;
        int cols = matrix2[0].length;
        int[][] result = new int[rows][cols];

        Arrays.parallelSetAll(result, i -> multiplyRow(matrix1, matrix2, i));

        return result;
    }

    private int[] multiplyRow(int[][] matrix1, int[][] matrix2, int row) {
        int cols = matrix2[0].length;
        int[] resultRow = new int[cols];

        for (int j = 0; j < cols; j++) {
            for (int k = 0; k < matrix1[0].length; k++) {
                resultRow[j] += matrix1[row][k] * matrix2[k][j];
            }
        }

        return resultRow;
    }
}