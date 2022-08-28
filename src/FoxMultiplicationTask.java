public class FoxMultiplicationTask implements Runnable {
    final Matrix resultBlockMatrix;
    final Matrix leftMatrixBlock;
    Matrix stepLeftMatrixBlock;
    Matrix stepRightMatrixBlock;


    FoxMultiplicationTask(Matrix leftMatrixBlock, Matrix stepRightMatrixBlock) {
        this.leftMatrixBlock = leftMatrixBlock;
        this.stepLeftMatrixBlock = null;
        this.stepRightMatrixBlock = stepRightMatrixBlock;
        this.resultBlockMatrix = Matrix.createFilled(leftMatrixBlock.getSizeX(), stepRightMatrixBlock.getSizeY(), 0);
    }

    public void setStepLeftMatrixBlock(Matrix leftMatrixBlock) {
        this.stepLeftMatrixBlock = leftMatrixBlock;
    }

    public void setStepRightMatrixBlock(Matrix stepRightMatrixBlock) {
        this.stepRightMatrixBlock = stepRightMatrixBlock;
    }

    public Matrix getResultBlockMatrix() {
        return resultBlockMatrix;
    }

    public Matrix getStepRightMatrixBlock() {
        return stepRightMatrixBlock;
    }

    public Matrix getLeftMatrixBlock() {
        return leftMatrixBlock;
    }

    @Override
    public void run() {
        resultBlockMatrix.add(Matrix.multiply(stepLeftMatrixBlock, stepRightMatrixBlock));
    }

}
