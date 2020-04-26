package fr.umlv.structconc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("static-method")
public class VectorizedTest {

    private static Stream<Arguments> provideIntArrays() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum)));
    }

    private static Stream<Arguments> provideIntArraySub() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, - Arrays.stream(array).reduce(0, Integer::sum)));
    }

    private static Stream<Arguments> provideIntArrayMinMax() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, new int[] { IntStream.of(array).min().orElse(Integer.MAX_VALUE),
                        IntStream.of(array).max().orElse(Integer.MIN_VALUE) }));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sum(int[] array, int expected) {
        assertEquals(expected, Vectorized.SumLoop(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sum_reduce_lanes(int[] array, int expected) {
        assertEquals(expected, Vectorized.SumReduceLanes(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sum_lanewise(int[] array, int expected) {
        assertEquals(expected, Vectorized.SumLanewise(array));
    }


    @ParameterizedTest
    @MethodSource("provideIntArraySub")
    public void differenceLanewise(int[] array, int expected) {
        assertEquals(expected, Vectorized.differenceLanewise(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrayMinMax")
    public void minmax(int[] array, int[] expected) {
        var v = Vectorized.minmax(array);
        assertEquals(expected[0], v[0]);
        assertEquals(expected[1], v[1]);
    }

}