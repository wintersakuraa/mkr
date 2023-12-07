import java.io.Serial;
import java.io.Serializable;

public class RequestPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int[][] matrix;
    public String error;

    public RequestPayload(int[][] matrix) {
        this(matrix, "");
    }

    public RequestPayload(int[][] matrix, String error) {
        this.matrix = matrix;
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}