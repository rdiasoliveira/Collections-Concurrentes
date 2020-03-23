# Exercice 1.

> Expliquer ce qu'est le problème de publication ?

Le fait de voir un objet initialisée mais pas ses attributs.

> Le code suivant a un problème de publication, expliquer lequel.
```
public class Foo {

    private String value;

    public Foo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
```

Il y a un problème de publication. Un object de la classe Foo peut être initialisé sans que ses attributs le soient.

> Le code suivant a-t'il un problème de publication ? Si oui comment le corriger ?

```
public class Person {

    private String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

}
```

Oui il y a un problème de publication, dans un object Person la string name peut ne pas être initialisée.
On peut le corriger en mettant la string name en final, cela nous garantira que la string name sera initiliasée 
dans le constructeur.

> Le code suivant a-t'il un problème de publication ? Si oui comment le corriger ?
```
public class Person2 {

    private String name;
    private volatile int age;

    public Person2(String name, int age) {
        this.name = name;
        this.age = age;
    }

}
```
Le code suivant n'as pas de problème de publication car le champs volatile nous garantit que tous les attributs 
initialisées avant lui sont bien initialisées.

> Le code suivant a-t'il un problème de publication ? Si oui comment le corriger ?
```
public class Person3 {

    private final String name;
    private final int age;

    public Person3(String name, int age) {
        this.name = name;
        this.age = age;
        new Thread(() -> {
            System.out.println(this.name + " " + this.age);
        }).start();
    }

}
```
Le code suivant a un problème de publication car l'objet Person n'est toujours pas initialisée. Retirer le mot clé this
permettra de lire les paramètres et pas les attributs de la classe.

> Le code suivant a-t'il un problème de publication ? Si oui comment le corriger ?
```
public class Person4 {
    private final String name;
    private final int age;

    public Person4(String name, int age) {
        this.name = name;
        this.age = age;
        new Thread(() -> {
            System.out.println(name + " " + age);
        }).start();
    }

}
```
Le code suivant n'a pas de problème de publication car nous sommes en train d'utiliser les valeurs en paramètre
dans le constructeur.

# Exercice 2

> Recopier le code de la classe StringList dans une classe LockFreeStringList qui va implanter une liste thread-safe sans verrou.
  Dans un premier temps, implanter la méthode addLast. Pour cela vous utiliserez la classe VarHandle.
  Comment doit-on modifier le code de size pour que la méthode soit thread-safe ?

Pour que le code de size soit thread-safe nous devons mettre le champs next de Entry tant que volatile. Cela permettra
de lire en mémoire la vrai valeur de next et pas la valeur en cache.

# Exercice 3

> Expliquer pourquoi le code ci-dessus n'est pas thread-safe ?

```
public class COWSet<E> {

     private final E[][] hashArray;    
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
       for (var e : hashArray[index]) {
         if (element.equals(e)) {
           return false;
         }
       }
       var oldArray = hashArray[index];
       var newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
       newArray[oldArray.length] = element;
       hashArray[index] = newArray;
       return true;
     }
    
     public void forEach(Consumer<? super E> consumer) {
       for(var index = 0; index < hashArray.length; index++) {
         var oldArray = hashArray[index];
         for(var element: oldArray) {
           consumer.accept(element);
         }
       }
     }
}
```

On peut voir le code ci-dessous n'est pas thread-safe. Par exemple lorsqu'un thread T1 fait appel au forEach,
un autre thread T2 peut ajouter un nouveau élément. Le thread T1 qui a fait appel à forEach ne verra pas le nouveau
élément ajouté. <br/>
Il y a aussi un problème lors de l'ajout d'un nouveau élément. Imaginons que deux threads souhaitent ajouter un nouveau
élément. Le thread T1 fait la copie dans un nouveau tableau et est deschédulé après l'ajout de l'élement dans ce nouveau 
tableau. Le thread T2 est schédulé et ajoute un nouveau élément sans problème avant d'être deschédulé. Ensuite le thread T1
peut être reschédulé et va finir par remplacer l'ajout effectué par le thread T2 par le sien.