import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class Client implements Runnable {
    private static final int PORT = 3000;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int MIN_MATRIX_SIZE = 1000;

    @Override
    public void run() {
        // Generate and fill matrices with random values NxM and MxL, where N,M,L >= 1000
        Random random = new Random();
        int N = MIN_MATRIX_SIZE + random.nextInt(2);
        int M = MIN_MATRIX_SIZE + random.nextInt(2);
        int L = MIN_MATRIX_SIZE + random.nextInt(2);
        RequestPayload requestPayload1 = new RequestPayload(generateMatrix(N, M));
        RequestPayload requestPayload2 = new RequestPayload(generateMatrix(M, L));

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            // Send matrices
            sendRequest(outputStream, requestPayload1);
            sendRequest(outputStream, requestPayload2);

            // Receive computation result
            RequestPayload response = receiveResponse(inputStream);

            if (!response.getError().isEmpty()) {
                System.out.println("Client error: " + response.getError());
                return;
            }

            // Display computation results in the console
            for (int[] row : response.getMatrix()) System.out.println(Arrays.toString(row));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(ObjectOutputStream outputStream, RequestPayload requestPayload) throws IOException {
        outputStream.writeObject(requestPayload);
    }

    private RequestPayload receiveResponse(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        return (RequestPayload) inputStream.readObject();
    }

    private int[][] generateMatrix(int rows, int cols) {
        Random rand = new Random();
        int[][] matrix = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt(10);
            }
        }

        return matrix;
    }
}