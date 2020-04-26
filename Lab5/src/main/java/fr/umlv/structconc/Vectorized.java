package fr.umlv.structconc;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class Vectorized {

    private static VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

    public static int SumLoop(int[] array) {
        var sum = 0;
        for (var value : array)
            sum += value;
        return sum;
    }

    public static int SumReduceLanes(int[] array) {
        var sum = 0;
        var pos = 0;
        var limit = array.length - (array.length % SPECIES.length());

        for (; pos < limit; pos += SPECIES.length()) {
            var vector = IntVector.fromArray(SPECIES, array, pos);
            sum += vector.reduceLanes(VectorOperators.ADD);
        }

        for (; pos < array.length; pos++)
            sum += array[pos];

        return sum;
    }

    public static int SumLanewise(int[] array) {
        var result = IntVector.zero(SPECIES);
        var pos = 0;
        var limit = array.length - (array.length % SPECIES.length());

        for (; pos < limit; pos += SPECIES.length())
            result = result.lanewise(VectorOperators.ADD, IntVector.fromArray(SPECIES, array, pos));

        var sum = result.reduceLanes(VectorOperators.ADD);

        for (; pos < array.length; pos++)
            sum += array[pos];

        return sum;
    }

    public static int differenceLanewise(int [] array) {
        if(array.length == 0) return 0;

        var result = IntVector.zero(SPECIES);
        var limit = array.length - (array.length % SPECIES.length());
        var pos = 0;

        for (; pos < limit; pos += SPECIES.length())
            result = result.lanewise(VectorOperators.SUB, IntVector.fromArray(SPECIES, array, pos));

        var totalSub = 0;
        var resultArray = result.toArray();
        for (var nb : resultArray) totalSub += nb;
        for(; pos < array.length; pos++) totalSub -= array[pos];

        return totalSub;
    }

    public static int[] minmax(int [] array) {
        var maxResult = IntVector.broadcast(SPECIES, Integer.MIN_VALUE);
        var minResult = IntVector.broadcast(SPECIES, Integer.MAX_VALUE);
        var limit = array.length - (array.length % SPECIES.length());
        var pos = 0;

        for (; pos < limit; pos += SPECIES.length()) {
            maxResult = maxResult.lanewise(VectorOperators.MAX, IntVector.fromArray(SPECIES, array, pos));
            minResult = minResult.lanewise(VectorOperators.MIN, IntVector.fromArray(SPECIES, array, pos));
        }

        var max = maxResult.reduceLanes(VectorOperators.MAX);
        var min = minResult.reduceLanes(VectorOperators.MIN);

        for(; pos < array.length; pos++) {
            if(array[pos] > max) max = array[pos];
            if(array[pos] < min) min = array[pos];
        }

        return new int[] { min, max };
    }
}
