# StarLoco - Game
The most advanced public 1.39 dofus emulator written in Java 21.
 
## Système de cartes de monstres

Le serveur permet désormais aux monstres de posséder une carte pouvant être obtenue en combat.

### Fonctionnement

* Chaque monstre peut avoir une carte associée.
* La carte peut tomber après un combat avec une **chance de base de 10 %**.
* Les cartes obtenues peuvent être équipées dans barre raccourci **3 slots** : 1, 2 et 3.
* Au début d'un combat, les cartes équipées permettent d'**invoquer automatiquement les monstres correspondants**.
* Le récapitulatif de fin de combat affiche les **cartes potentiellement obtenables** parmi les monstres combattus.

### Puissance des invocations

La puissance dépend du nombre d'exemplaires de la carte possédés :

```text
1 carte  = 10 %
2 cartes = 20 %
3 cartes = 30 %
...
10 cartes = 100 %
```

Le bonus est plafonné à **10 cartes**.

La vitalité de base de l'invocation suit également cette progression :

```text
1 carte  = 5
2 cartes = 10
...
10 cartes = 50
``` 