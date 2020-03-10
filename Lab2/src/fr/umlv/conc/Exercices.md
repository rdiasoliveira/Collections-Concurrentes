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