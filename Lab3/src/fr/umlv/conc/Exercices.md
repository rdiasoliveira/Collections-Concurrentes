#### Exercice 1.

> Rappeler ce que "réentrant" veux dire.

Réentrant veut dire qu'un seul thread peut exécuter un bloc de code à la fois. Un thread acquérit un verrou et tant qu'il
relâche pas le verrou, un autre Thread ne peut pas éxecuter ce bloc.
L'autre Thread peut essayer de prendre en main le verrou pour exécuter ce bloc de code lorsque le thread qui exécutait 
ce bloc de code relâche le verrou.

> Expliquer, pour le code ci-dessous, quel est le comportement que l'on attend si la classe est thread-safe.
  Puis expliquer pourquoi le code du Runnable n'est pas thread-safe (même si on déclare counter volatile).

```
package fr.umlv.conc;

public class SpinLock {
  public void lock() {
    // TODO
  }
  
  public void unlock() {
    // TODO
  }
  
  public static void main(String[] args) throws InterruptedException {
    var runnable = new Runnable() {
      private int counter;
      private final SpinLock spinLock = new SpinLock();
      
      @Override
      public void run() {
        for(int i = 0; i < 1_000_000; i++) {
          spinLock.lock();
          try {
            counter++;
          } finally {
            spinLock.unlock();
          }
        }
      }
    };
    var t1 = new Thread(runnable);
    var t2 = new Thread(runnable);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
    System.out.println("counter " + runnable.counter);
  }
}
```

Si la classe est thread-safe, on attends que le programme affiche `2 000 000` car les deux threads vont incrementer
sur la même variable grâce à une boucle. Le code du Runnable n'est pas thread-safe car même si le counter est volatile,
l'increméntation elle n'est pas atomique.

> Pour coder le lock, l'idée est d'utiliser un champ boolean pour représenter le jeton que doit prendre un thread pour rentrer dans le lock. Acquérir le lock revient à passer le champs booléen de faux à vrai. Redonner le lock revient à assigner le champ à faux.
  Que doit-on faire si on arrive pas à acquérir le lock ? Quel est le problème ? Pourquoi utiliser Thread.onSpinWait permet de résoudre le problème ? Sachant que les CPUs modernes ont un pipeline, à votre avis, à quoi sert Thread.onSpinWait ?
  
Si on arrive pas à acquérir le lock, nous devons attendre que le lock soit libre pour le reprendre. On fait donc de l'attente
active. Thread.onSpinWait permet d'indiquer au CPU qu'on attends une certaine action.
Entre-temps le CPU peut optimiser les ressources de chaque thread.
   
#### Exercice 3.

> Pourquoi le code suivant ne marche pas ?
```
public class Utils {
  private static Path HOME;

  public static Path getHome() {
    if (HOME == null) {
      return HOME = Path.of(System.getenv("HOME"));
    }
    return home;
  }
}
```
Le code suivant ne marche pas car plusieurs threads peuvent exécuter `Path.of()`, l'écriture d'un thread peut donc être
remplacé par une autre.

> Peux-t'on dire que le code ci-dessus n'est pas thread-safe ?

Comme dans l'exercice précedent, le code suivant n'est pas thread-safe car un thread T1 peut être deschédulé à l'intérieur de la condition
 `if(HOME == null)` alors qu'un thread T2 peut être schédulé et peut initiliasé `HOME`.
Quand le thread T1 sera reschedulé, il va remplacer l'écriture faite par le thread T2 sur `HOME`.
 
 > Pourquoi le code suivant ne marche pas non plus ?
   Indice: pensez au problème de publication !
   Comment peut-on corriger le problème ?

```
public class Utils {
     private volatile static Path HOME;
   
     public static Path getHome() {
       var home = HOME;
       if (home == null) {
         synchronized(Utils.class) {
           home = HOME;
           if (home == null) {
             return HOME = Path.of(System.getenv("HOME"));
           }
         }
       }
       return home;
     }
   }
```

Ce code ne marche pas non plus car nous avons une erreur de publication. Une thread T1 peut
être deschédulé au moment de l'initialisation `HOME = Path.of(System.getenv("HOME"));` L'objet `HOME` peut ne
pas être totatelement initialisé. Si un thread T2 est schédulé, il rentrera pas dans la condition `if (home == null)`
et retournera directement l'objet `HOME` qui n'est pas totalement initialisé.
Pour corriger ce problème nous pouvons déclarer l'objet `HOME` tant que volatile pour garantir l'initialisation de 
l'objet.

> Il existe une autre façon de corriger le problème en utilisant les méthodes getAcquire et setRelease de la classe VarHandle qui fournisse un accès relaxé à la mémoire.
  Quelles sont les garanties fournient par getAcquire et setRelease et en quoi ces garanties sont moins forte qu'un accès volatile ?
  En quoi ces garanties sont suffisantes dans notre cas ?

On peut donc utiliser un VarHandle sur le champs qu’on souhaite initialiser :
- On utilise la méthode getAcquire() (lecture) pour retourner la valeur de l’objet et assurer que les prochaines instructions ne seront pas exécutés avant cette lecture.
- On utilise la méthode setRelease() (écriture) pour initialiser la variable et assurer que les prochains chargements et sauvegardes ne seront pas exécutés avant cette écriture.


> En fait, il y a une façon beaucoup plus simple de résoudre le problème sans utiliser le pattern Double-checked locking et en utilisant le pattern initialization on demand holder.
 Modifier votre code (en commentant l'ancien) pour utiliser ce pattern.
 Pourquoi ce pattern est plus efficace ?

Ce design pattern n’utilise pas les volatiles ni des verrous (plus efficace).
On utilise les garanties de Java sur l’initialisation des classes pour être sûr que notre classe
sera thread-safe. L’initialisation paresseuse vient du fait que la classe interne statique n’est
chargé de façon synchronisé que quand celle-ci sera appelé. Si les variables de cette classe interne
sont final on a la garantie que celle-ci ne pourra pas être changé.