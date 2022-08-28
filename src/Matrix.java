import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class Matrix {
    final double[][] matrix;
    final private int matrixSizeX;
    final private int matrixSizeY;

    Matrix(double[][] matrix) {
        this.matrix = matrix;
        this.matrixSizeX = matrix.length;
        this.matrixSizeY = matrix[0].length;
    }

    double getElement(int i, int j) {
        return matrix[i][j];
    }

    void setElement(int i, int j, double element) {
        this.matrix[i][j] = element;
    }

    void increaseElement(int i, int j, double delta) {
        this.matrix[i][j] += delta;
    }

    int getSizeX() {
        return matrixSizeX;
    }

    int getSizeY() {
        return matrixSizeY;
    }

    double[][] getArray() {
        return matrix;
    }

    void add(Matrix rightMatrix) {
        if (rightMatrix.getSizeX() != matrixSizeX || rightMatrix.getSizeY() != matrixSizeY) {
            throw new RuntimeException("Matrices should be the same size");
        }

        for (int i = 0; i < matrixSizeX; i++) {
            for (int j = 0; j < matrixSizeY; j++) {
                increaseElement(i, j, rightMatrix.getElement(i, j));
            }
        }
    }

    Matrix[][] split(int blockSizeX, int blockSizeY) {
        if (matrixSizeX % blockSizeX != 0 || matrixSizeY % blockSizeY != 0) {
            throw new RuntimeException("Block sizes should divide matrix sizes without remainder");
        }
        int blockMatrixSizeX = matrixSizeX / blockSizeX;
        int blockMatrixSizeY = matrixSizeY / blockSizeY;
        Matrix[][] result = new Matrix[blockMatrixSizeX][blockMatrixSizeY];
        for (int i = 0; i < blockMatrixSizeX; i++) {
            for (int j = 0; j < blockMatrixSizeY; j++) {
                result[i][j] = new Matrix(getMatrixPart(i * blockSizeX, j * blockSizeY, blockSizeX, blockSizeY));
            }
        }
        return result;
    }

    Matrix[] splitRow(int rowBlockSize) {
        if (matrixSizeX % rowBlockSize != 0) {
            throw new RuntimeException("Row block size should divide matrix x size without remainder");
        }
        int rowNum = matrixSizeX / rowBlockSize;
        Matrix[] result = new Matrix[rowNum];

        for (int k = 0; k < rowNum; k++) {
            Matrix row = createEmpty(rowBlockSize, matrixSizeY);
            for (int i = 0; i < rowBlockSize; i++) {
                for (int j = 0; j < matrixSizeY; j++) {
                    row.setElement(i, j, getElement(k * rowBlockSize + i, j));
                }
            }
            result[k] = row;
        }

        return result;
    }

    Matrix[] splitCol(int colBlockSize) {
        if (matrixSizeY % colBlockSize != 0) {
            throw new RuntimeException("Col block size should divide matrix x size without remainder");
        }
        int colNum = matrixSizeY / colBlockSize;
        Matrix[] result = new Matrix[colNum];

        for (int k = 0; k < colNum; k++) {
            Matrix col = createEmpty(matrixSizeX, colBlockSize);
            for (int i = 0; i < matrixSizeX; i++) {
                for (int j = 0; j < colBlockSize; j++) {
                    col.setElement(i, j, getElement(i, colBlockSize * k + j));
                }
            }
            result[k] = col;
        }

        return result;
    }


    double[][] getMatrixPart(int offsetX, int offsetY, int blockSizeX, int blockSizeY) {
        double[][] matrixPart = new double[blockSizeX][blockSizeX];
        for (int i = 0; i < blockSizeX; i++) {
            for (int j = 0; j < blockSizeY; j++) {
                matrixPart[i][j] = getElement(offsetX + i, offsetY + j);
            }
        }
        return matrixPart;
    }

    void setMatrixPart(int offsetX, int offsetY, Matrix matrixPart) {
        for (int i = 0; i < matrixPart.getSizeX(); i++) {
            for (int j = 0; j < matrixPart.getSizeY(); j++) {
                setElement(i + offsetX, j + offsetY, matrixPart.getElement(i, j));
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(matrix);
    }

    static Matrix createEmpty(int sizeX, int sizeY) {
        return new Matrix(new double[sizeX][sizeY]);
    }

    static Matrix createEmpty(int size) {
        return createEmpty(size, size);
    }

    static Matrix createFilled(int sizeX, int sizeY, double fillValue) {
        double[][] matrix = new double[sizeX][sizeY];
        Arrays.stream(matrix).forEach(subArr -> Arrays.fill(subArr, fillValue));
        return new Matrix(matrix);
    }

    static Matrix createFilled(int size, double fillValue) {
        return createFilled(size, size, fillValue);
    }

    static Matrix generateRandom(int sizeX, int sizeY, int maxNum) {
        double[][] matrix = new double[sizeX][sizeY];
        Random random = new Random();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                matrix[i][j] = random.nextInt(maxNum);
            }
        }
        return new Matrix(matrix);
    }

    static Matrix generateRandom(int size, int maxNum) {
        return generateRandom(size, size, maxNum);
    }

    static double countMatrixElement(Matrix leftMatrix, Matrix rightMatrix, int i, int j) {
        double result = 0;
        for (int k = 0; k < leftMatrix.getSizeY(); k++) {
            result += leftMatrix.getElement(i, k) * rightMatrix.getElement(k, j);
        }
        return result;
    }

    static Matrix multiply(Matrix leftMatrix, Matrix rightMatrix) {
        if (leftMatrix.getSizeY() != rightMatrix.getSizeX()) {
            throw new RuntimeException("This matrices can not be multiplied");
        }

        int matrixSizeX = leftMatrix.getSizeX();
        int matrixSizeY = rightMatrix.getSizeY();
        Matrix matrix = Matrix.createEmpty(matrixSizeX, matrixSizeY);

        for (int i = 0; i < leftMatrix.getSizeX(); i++) {
            for (int j = 0; j < rightMatrix.getSizeY(); j++) {
                matrix.setElement(i, j, countMatrixElement(leftMatrix, rightMatrix, i, j));
            }
        }

        return matrix;
    }

    static Matrix futureFoxMultiply(ExecutorService executor, Matrix leftMatrix, Matrix rightMatrix, int blockSize)
            throws ExecutionException, InterruptedException {
        if (leftMatrix.getSizeY() != rightMatrix.getSizeX()) {
            throw new RuntimeException("This matrices can not be multiplied");
        }

        CompletableFuture<Matrix[][]> leftBlockMatrixFuture = CompletableFuture.supplyAsync(() -> leftMatrix.split(blockSize, blockSize), executor);
        CompletableFuture<Matrix[][]> rightBlockMatrixFuture = CompletableFuture.supplyAsync(() -> rightMatrix.split(blockSize, blockSize), executor);
        Matrix[][] rightBlocksMatrix = rightBlockMatrixFuture.get();
        Matrix[][] leftBlocksMatrix = leftBlockMatrixFuture.get();

        int multiplicationTasksSizeX = leftBlocksMatrix.length;
        int multiplicationTasksSizeY = rightBlocksMatrix[0].length;

        FoxMultiplicationTask[][] multiplicationTasks = new FoxMultiplicationTask[multiplicationTasksSizeX][multiplicationTasksSizeY];

        for (int i = 0; i < multiplicationTasksSizeX; i++) {
            for (int j = 0; j < multiplicationTasksSizeY; j++) {
                multiplicationTasks[i][j] = new FoxMultiplicationTask(leftBlocksMatrix[i][j], rightBlocksMatrix[i][j]);
            }
        }

        for (int k = 0; k < multiplicationTasksSizeX; k++) {
            for (int i = 0; i < multiplicationTasksSizeX; i++) {
                int stepJ = (i + k) % leftBlocksMatrix.length;
                Matrix leftMatrixBlock = multiplicationTasks[i][stepJ].getLeftMatrixBlock();
                for (int j = 0; j < multiplicationTasksSizeY; j++) {
                    multiplicationTasks[i][j].setStepLeftMatrixBlock(leftMatrixBlock);
                }
            }

            CompletableFuture.allOf(Arrays.stream(multiplicationTasks).flatMap(row -> Arrays.stream(row))
                    .map(task -> CompletableFuture.runAsync(task, executor)).toArray(CompletableFuture[]::new)).get();

            var tasks = multiplicationTasks;

            for (int j = 0; j < multiplicationTasksSizeY; j++) {
                Matrix firstRightMatrixBlock = multiplicationTasks[0][j].getStepRightMatrixBlock();
                for (int i = 1; i < multiplicationTasksSizeX; i++) {
                    multiplicationTasks[i - 1][j].setStepRightMatrixBlock(multiplicationTasks[i][j].getStepRightMatrixBlock());
                }
                multiplicationTasks[multiplicationTasksSizeX - 1][j].setStepRightMatrixBlock(firstRightMatrixBlock);
            }
            var tasks2 = multiplicationTasks;
        }

        Matrix resultMatrix = createEmpty(leftMatrix.getSizeX(), rightMatrix.getSizeY());

        for (int i = 0; i < multiplicationTasksSizeX; i++) {
            for (int j = 0; j < multiplicationTasksSizeY; j++) {
                resultMatrix.setMatrixPart(i * blockSize, j * blockSize,
                        multiplicationTasks[i][j].getResultBlockMatrix());
            }
        }

        return resultMatrix;
    }

    static Matrix futureBlockStripedMultiply(ExecutorService executor, Matrix leftMatrix, Matrix rightMatrix, int blockSize)
            throws ExecutionException, InterruptedException {
        if (leftMatrix.getSizeY() != rightMatrix.getSizeX() || leftMatrix.getSizeX() != rightMatrix.getSizeY()) {
            throw new RuntimeException("This matrices can not be multiplied");
        }

        CompletableFuture<Matrix[]> leftMatrixRowsFuture = CompletableFuture.supplyAsync(() -> leftMatrix.splitRow(blockSize), executor);
        CompletableFuture<Matrix[]> rightMatrixColsFuture = CompletableFuture.supplyAsync(() -> rightMatrix.splitCol(blockSize), executor);
        Matrix[] rightMatrixCols = rightMatrixColsFuture.get();
        Matrix[] leftMatrixRows = leftMatrixRowsFuture.get();
        Matrix[][] resultBlockMatrix = new Matrix[leftMatrixRows.length][rightMatrixCols.length];
        BlockStripedMultiplicationTask[] multiplicationTasks = IntStream.range(0, leftMatrixRows.length)
                .mapToObj((i) -> new BlockStripedMultiplicationTask(leftMatrixRows[i]))
                .toArray(BlockStripedMultiplicationTask[]::new);

        int stepNum = rightMatrixCols.length;
        for (int k = 0; k < stepNum; k++) {
            for (int i = 0; i < multiplicationTasks.length; i++) {
                int stepJ = (i + k) % rightMatrixCols.length;
                multiplicationTasks[i].setRightMatrixCol(rightMatrixCols[stepJ]);
            }

            int finalK = k;
            CompletableFuture.allOf(IntStream.range(0, multiplicationTasks.length)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                                Matrix m = multiplicationTasks[i].call();
                                int stepJ = (i + finalK) % rightMatrixCols.length;
                                resultBlockMatrix[i][stepJ] = m;
                            }, executor)
                    ).toArray(CompletableFuture[]::new)).get();

        }

        Matrix resultMatrix = createEmpty(leftMatrix.getSizeX(), rightMatrix.getSizeY());

        for (int i = 0; i < resultBlockMatrix.length; i++) {
            for (int j = 0; j < resultBlockMatrix[0].length; j++) {
                resultMatrix.setMatrixPart(i * blockSize, j * blockSize, resultBlockMatrix[i][j]);
            }
        }

        return resultMatrix;
    }

}
