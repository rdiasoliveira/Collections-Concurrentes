package fr.umlv.conc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Expliquer pourquoi le code ci-dessus n'est pas thread-safe ?
 * Ecrire un main démontrant le problème.
 * R: Le code ci-dessus n'est pas thread-safe car par exemple :
 * - un thread t1 veut rajouter un element dans l'en-tete de la liste chaine alors qu'il est deschedule avant attribuer
 * la nouvelle valeur, un thread t2 rajoute un ou plusieurs elements dans la liste chaine avant d'etre deschedule.
 * Quand le thread t1 est reschedule, il ne va pas lire la nouvelle valeur de head en memoire et va ecraser tous
 * les elements rajoutes par le thread t2.
 * - un thread t1 fait par exemple appel à size et deschedule dans la boucle, un thread t2 fait appel à addFirst et est
 * ensuite deschedule. Lorsque le thread t2 est reschedule, celui-ci va pas compter le nouveau ajout dans l'en-tete
 * de la liste chaine et retourne une mauvaise valeur.
 *
 * Modifier votre implantation pour rendre la classe Linked thread-safe (et lock-free) utilisant la classe
 * AtomicReference et sa méthode compareAndSet.
 *
 * Expliquer pourquoi utiliser la classe AtomicReference n'est pas super efficace ?
 * R: AtomicReference n'est pas super efficace car il y a une indirection en plus pour acceder à E.
 *
 */

public class Linked<E> {

    private static class Entry<E> {
        private final E element;
        private final Entry<E> next;

        private Entry(E element, Entry<E> next) {
            this.element = element;
            this.next = next;
        }
    }

    //private Entry<E> head;
    private final AtomicReference<Entry<E>> head = new AtomicReference<>();

    public void addFirst(E element) {
        Objects.requireNonNull(element);
        //head = new Entry<>(element, head);
        for (; ; ) {
            var current = head.get();
            var newEntry = new Entry<E>(element, current);
            if (head.compareAndSet(current, newEntry)) return;
        }
    }

    public int size() {
        var size = 0;
        for (var link = head.get(); link != null; link = link.next) {
            size++;
        }
        return size;
    }

    public static void main(String [] args) throws InterruptedException {
        Thread threads [] = new Thread[2];
        var list = new Linked<Integer>();
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for(int j = 0; j < 100; j++){
                    list.addFirst(j);
                }
            });
        }

        for(int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for(int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.println(list.size());
    }

}