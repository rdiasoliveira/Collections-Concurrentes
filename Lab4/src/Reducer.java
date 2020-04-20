import java.util.Arrays;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

public class Reducer {

    private static class Task extends RecursiveTask<Integer> {
        private final int start;
        private final int end;
        private final int[] array;
        private final int initial;
        private final IntBinaryOperator op;

        public Task(int start, int end, int[] array, int initial, IntBinaryOperator op) {
            this.start = start;
            this.end = end;
            this.array = array;
            this.initial = initial;
            this.op = op;
        }

        @Override
        protected Integer compute() {
            if(end - start < 1024) return Arrays.stream(array, start, end).reduce(initial, op);
            var middle = (start + end) / 2;
            var part1 = new Task(start, middle, array, initial, op);
            var part2 = new Task(middle, end, array, initial, op);
            part1.fork();
            var result2 = part2.compute();
            var result1 = part1.join();
            return op.applyAsInt(result1, result2);
        }
    }

    public static int reduce(int [] array, int initial, IntBinaryOperator op) {
        var acc = initial;
        for(var value : array) acc = op.applyAsInt(acc, value);
        return acc;
    }

    public static int reduceWithStream(int [] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).reduce(initial, op);
    }

    public static int parallelReduceWithStream(int [] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).parallel().reduce(initial, op);
    }

    public static int parallelReduceWithForkJoin(int [] array, int initial, IntBinaryOperator op) {
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(new Task(0, array.length, array, initial, op));
    }

    public static <T> T sequentialReduce(Spliterator<T>  spliterator, T initial, BinaryOperator<T> op) {
        var box = new Object() {
            private T acc = initial;
        };
        while(spliterator.tryAdvance(e-> {
            box.acc = op.apply(box.acc, e);
        }));
        return box.acc;
    }

    public static int sum(int[] array) {
        /* var sum = 0;
        for(var value: array) {
            sum += value;
        }
        return sum; */
        return reduce(array, 0, Integer::sum);
    }

    public static int max(int[] array) {
        /* var max = Integer.MIN_VALUE;
        for(var value: array) {
            max = Math.max(max, value);
        } */
        return reduce(array, Integer.MIN_VALUE, Math::max);
    }

    public static void main(String [] args) {
        var random = new Random(0);
        var array = random.ints(1_000_000, 0, 1000).toArray();
        System.out.println(sum(array));
        System.out.println(max(array));
        System.out.println(reduceWithStream(array, 0, Math::max));
        System.out.println(parallelReduceWithStream(array, 0, Math::max));
        System.out.println(parallelReduceWithForkJoin(array, 0, Math::max));
    }

}
    