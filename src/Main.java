import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Benchmark[] benchmarks = new Benchmark[]{
                new Benchmark(1500, 3, 2, 10, 0),
                new Benchmark(1500, 3, 2, 20, 0),
                new Benchmark(1500, 3, 2, 30, 0),
                new Benchmark(1500, 3, 2, 50, 0),
                new Benchmark(1500, 3, 2, 100, 0),
                new Benchmark(1500, 3, 2, 300, 0),
                new Benchmark(1500, 3, 2, 500, 0),
                new Benchmark(1500, 3, 2, 750, 0),
                new Benchmark(1500, 3, 4, 10, 0),
                new Benchmark(1500, 3, 4, 20, 0),
                new Benchmark(1500, 3, 4, 30, 0),
                new Benchmark(1500, 3, 4, 50, 0),
                new Benchmark(1500, 3, 4, 100, 0),
                new Benchmark(1500, 3, 4, 300, 0),
                new Benchmark(1500, 3, 4, 500, 0),
                new Benchmark(1500, 3, 4, 750, 0),
                // 500
                new Benchmark(500, 50, 2, 50, 10, 10),
                new Benchmark(500, 50, 4, 50, 10, 0),
                // 1000
                new Benchmark(1000, 30, 2, 50, 10, 5),
                new Benchmark(1000, 30, 4, 50, 10, 0),
                // 1500
                new Benchmark(1500, 3, 2, 50, 10, 3),
                new Benchmark(1500, 3, 4, 50, 10, 0),
                // 2000
                new Benchmark(2000, 3, 2, 50, 10, 2),
                new Benchmark(2000, 3, 4, 50, 10, 0),
                // 2500
                new Benchmark(2500, 3, 2, 50, 10, 2),
                new Benchmark(2500, 3, 4, 50, 10, 0),
                // 3000
                new Benchmark(3000, 3, 2, 50, 10, 2),
                new Benchmark(3000, 3, 4, 50, 10, 0),
        };

        for (var benchmark : benchmarks) {
            benchmark.run();
        }

    }
}