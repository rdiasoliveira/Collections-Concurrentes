package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class COWSet<E> {
    private final E[][] hashArray;

    private static final VarHandle HASH_ARRAY_HANDLE;
    static {
        HASH_ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[][].class);
    }

    private static final Object[] EMPTY = new Object[0];

    @SuppressWarnings("unchecked")
    public COWSet(int capacity) {
        var array = new Object[capacity][];
        Arrays.setAll(array, __ -> EMPTY);
        this.hashArray = (E[][])array;
    }

    public boolean add(E element) {
        Objects.requireNonNull(element);
        var index = element.hashCode() % hashArray.length;

        for(;;) {
            @SuppressWarnings("unchecked")
            var array = (E[]) HASH_ARRAY_HANDLE.getVolatile(hashArray, index);

            for (var e : array) {
                if (element.equals(e)) {
                    return false;
                }
            }

            var newArray = Arrays.copyOf(array, array.length + 1);
            newArray[array.length] = element;
            if(HASH_ARRAY_HANDLE.compareAndSet(hashArray, index, array, newArray)) {
                hashArray[index] = newArray;
                return true;
            }
        }
    }

    public void forEach(Consumer<? super E> consumer) {

        for(var index = 0; index < hashArray.length; index++) {

            @SuppressWarnings("unchecked")
            var array = (E[])HASH_ARRAY_HANDLE.getVolatile(hashArray, index);
            for(var element: array) {
                consumer.accept(element);
            }

        }
    }
}