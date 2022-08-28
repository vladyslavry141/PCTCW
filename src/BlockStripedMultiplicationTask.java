import java.util.concurrent.Callable;

public class BlockStripedMultiplicationTask implements Callable<Matrix> {
    final Matrix leftMatrixRow;
    Matrix rightMatrixCol;


    BlockStripedMultiplicationTask(Matrix leftMatrixRow) {
        this.leftMatrixRow = leftMatrixRow;
    }

    public void setRightMatrixCol(Matrix rightMatrixCol) {
        this.rightMatrixCol = rightMatrixCol;
    }

    @Override
    public Matrix call() {
        return Matrix.multiply(leftMatrixRow, rightMatrixCol);
    }

}
