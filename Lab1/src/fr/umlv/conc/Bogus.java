package fr.umlv.conc;

/**
 * Sans exécuter le code, que fait ce programme ?
 * R: Le programme fait une boucle infinie, la valeur du boolean est toujours false lorsqu'on est dans la boucle
 * car la JVM n'accède pas à la mémoire.
 *
 * Comment doit-on corriger le problème ?
 * Modifier la classe Bogus en conséquence.
 * R: En mettant un bloc synchronized.
 *
 * On cherche maintenant a accélérer le code de Bogus en utilisant le mot clé volatile au lieu des blocs synchronized.
 * Créer une classe BogusVolatile qui n'utilise pas de bloc synchronized.
 * Comment appelle-t-on les implantations qui n'ont ni blocs synchronized, ni lock ?
 * R: Lock-free.
 */

public class Bogus {

    private volatile boolean stop;

    public void runCounter() {
        var localCounter = 0;
        for(;;) {
            if (stop) {
                break;
            }
            localCounter++;
        }
        System.out.println(localCounter);
    }

    public void stop() {
        stop = true;
    }

    public static void main(String[] args) throws InterruptedException {
        var bogus = new Bogus();
        var thread = new Thread(bogus::runCounter);
        thread.start();
        Thread.sleep(100);
        bogus.stop();
        thread.join();
    }

}