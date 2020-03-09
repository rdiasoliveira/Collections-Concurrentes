package fr.umlv.conc;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Expliquer pourquoi le code ci-dessus n'est pas thread-safe ?
 * Ecrire un main démontrant le problème.
 * R: Le code ci-dessus n'est pas thread-safe car si par exemple un thread t1 est deschedule avant d'incrementer la
 * valeur de counter, un autre thread t2 va incrementer la valeur de counter et est ensuite est deschedule. Si le thread
 * t1 est ensuite reschedule il va incrementer l'ancienne valeur de counter.
 *
 * Que se passe t'il si on déclare le champ counter volatile ?
 * R: Ca change rien car l'incrementation se fait en plusieurs étapes et pas dans une seule.
 *
 * A quoi correspond la valeur de retour de la méthode compareAndSet de la classe AtomicInteger ?
 * Utiliser la méthode compareAndSet pour créer une implantation thread-safe de la classe Counter.
 * R: La méthode compareAndSet compare la valeur en paramètre avec la valeur qui est en ram, si la valeur est pareille
 * on set la valeur avec qui est présente dans le deuxième paramètre.
 *
 * A quoi sert la méthode getAndIncrement de la classe AtomicInteger ?
 * Créer une nouvelle classe Counter2, marchant comme Counter, utilisant la méthode getAndIncrement pour obtenir une
 * implantation thread-safe.
 * R: Cela permet d'incrementer atomatiquement la valeur càd de prendre la valeur actuelle en ram et de l'incrementer.
 *
 * Que veut dire le terme lock-free pour un algorithme ?
 * Les implantations Counter et Counter2 sont-elles lock-free ?
 * R: Le terme lock-free veut dire que l'algorithme n'a aucun bloc synchronized ni aucun lock, donc le code n'est pas
 * bloquant.
 * Donc les implantations Counter et Counter2 sont lock-free.
 */

public class Counter {

    //private int counter;
    private final AtomicInteger counter = new AtomicInteger();

    public int nextInt() {
        //return counter++;

        /* for(;;) {
            var value = counter.get();
            if(counter.compareAndSet(value, value + 1)) return value;
        } */

        counter.getAndIncrement();

        return counter.get();
    }

    public static void main(String [] args) throws InterruptedException {
        var counter = new Counter();
        var threads = new ArrayList<Thread>();
        for(var i = 0; i < 4; i++) {
            var thread = new Thread(() -> {
                for(var j = 0; j < 100_000; j++){
                    counter.nextInt();
                }
            });
            thread.start();
            threads.add(thread);
        }
    }

}