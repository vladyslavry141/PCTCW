import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Benchmark {
    final int matrixSize;
    final int attemptsNum;
    final int threadNum;
    final int foxBlockSize;
    final int blockStripedBlockSize;
    final int serialAttemptsNum;

    Benchmark(int matrixSize, int attemptsNum, int threadNum, int foxBlockSize, int blockStripedBlockSize, int serialAttemptsNum) {
        this.matrixSize = matrixSize;
        this.attemptsNum = attemptsNum;
        this.threadNum = threadNum;
        this.foxBlockSize = foxBlockSize;
        this.blockStripedBlockSize = blockStripedBlockSize;
        this.serialAttemptsNum = serialAttemptsNum;
    }

    Benchmark(int matrixSize, int attemptsNum, int threadNum, int blockSize, int serialAttemptsNum) {
        this.matrixSize = matrixSize;
        this.attemptsNum = attemptsNum;
        this.threadNum = threadNum;
        this.foxBlockSize = blockSize;
        this.blockStripedBlockSize = blockSize;
        this.serialAttemptsNum = serialAttemptsNum;
    }

    void run() throws ExecutionException, InterruptedException {
        System.out.printf("Matrix size: %-6d Thread number: %-2d Fox block number: %-5d Fox block size: %-5d Block-striped block number: %-5d Block-striped size: %-5d\n", matrixSize, threadNum, (matrixSize * matrixSize) / (foxBlockSize * foxBlockSize), foxBlockSize, matrixSize / blockStripedBlockSize, blockStripedBlockSize);
        Matrix matrix1 = Matrix.generateRandom(matrixSize, 100);
        Matrix matrix2 = Matrix.generateRandom(matrixSize, 100);

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        Matrix concurrentFoxMatrix = null;
        double foxTime;
        Matrix concurentBockStripedMatrix = null;
        double blockTime;
        Matrix syncMatrix = null;
        double serialTime = 0;

        if (serialAttemptsNum != 0) {
            long startSerial = System.currentTimeMillis();
            for (int i = 0; i < serialAttemptsNum; i++) {
                syncMatrix = Matrix.multiply(matrix1, matrix2);
            }
            long endSerial = System.currentTimeMillis();
            serialTime = (endSerial - startSerial) / (serialAttemptsNum);
        }

        long startConcurrentFox = System.currentTimeMillis();
        for (int i = 0; i < attemptsNum; i++) {
            concurrentFoxMatrix = Matrix.futureFoxMultiply(executor, matrix1, matrix2, foxBlockSize);
        }
        long endConcurrentFox = System.currentTimeMillis();
        foxTime = (endConcurrentFox - startConcurrentFox) / (attemptsNum);

        long startConcurrentBlock = System.currentTimeMillis();
        for (int i = 0; i < attemptsNum; i++) {
            concurentBockStripedMatrix = Matrix.futureBlockStripedMultiply(executor, matrix1, matrix2, foxBlockSize);
        }
        long endConcurrentBlock = System.currentTimeMillis();
        blockTime = (endConcurrentBlock - startConcurrentBlock) / (attemptsNum);

        if (serialAttemptsNum != 0) {
            System.out.printf("| %-20s | %-20s | %-30s | %-40s | %-30s |\n", "Algorithm", "Average time", "Is equal to fox result", "is equal to block-striped result", "Is equal to serial result");
            System.out.printf("| %-20s | %-20f | %-30s | %-40s | %-30s |\n", "Serial", serialTime, Arrays.deepEquals(concurrentFoxMatrix.getArray(), syncMatrix.getArray()), Arrays.deepEquals(concurentBockStripedMatrix.getArray(), syncMatrix.getArray()), Arrays.deepEquals(syncMatrix.getArray(), syncMatrix.getArray()));
            System.out.printf("| %-20s | %-20f | %-30s | %-40s | %-30s |\n", "Fox", foxTime, Arrays.deepEquals(concurrentFoxMatrix.getArray(), concurrentFoxMatrix.getArray()), Arrays.deepEquals(concurentBockStripedMatrix.getArray(), concurrentFoxMatrix.getArray()), Arrays.deepEquals(syncMatrix.getArray(), concurrentFoxMatrix.getArray()));
            System.out.printf("| %-20s | %-20f | %-30s | %-40s | %-30s |\n\n", "Block-striped", blockTime, Arrays.deepEquals(concurrentFoxMatrix.getArray(), concurentBockStripedMatrix.getArray()), Arrays.deepEquals(concurentBockStripedMatrix.getArray(), concurentBockStripedMatrix.getArray()), Arrays.deepEquals(syncMatrix.getArray(), concurentBockStripedMatrix.getArray()));
        } else {
            System.out.printf("| %-20s | %-20s | %-30s | %-40s |\n", "Algorithm", "Average time", "Is equal to fox result", "Is equal to block-striped result");
            System.out.printf("| %-20s | %-20f | %-30s | %-40s |\n", "Fox", foxTime, Arrays.deepEquals(concurrentFoxMatrix.getArray(), concurrentFoxMatrix.getArray()), Arrays.deepEquals(concurentBockStripedMatrix.getArray(), concurrentFoxMatrix.getArray()));
            System.out.printf("| %-20s | %-20f | %-30s | %-40s |\n\n", "Block-striped", blockTime, Arrays.deepEquals(concurrentFoxMatrix.getArray(), concurentBockStripedMatrix.getArray()), Arrays.deepEquals(concurentBockStripedMatrix.getArray(), concurentBockStripedMatrix.getArray()));
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

    }
}
