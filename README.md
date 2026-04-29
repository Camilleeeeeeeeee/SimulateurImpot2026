# Simulateur d'Impôt 2024

Projet réalisé dans le cadre d'un TP à l'IUT par **Camil GOLLIOT** et **Lylian MONTALS**.

> **Note sur les commits** : le projet a été réalisé en une seule session de travail en présentiel à l'IUT. Les commits ne reflètent donc pas nécessairement la contribution des deux étudiants — le travail a été fait en binôme sur une même machine.

---

## Présentation

Ce projet implémente un simulateur de calcul d'impôt sur le revenu 2024 en Java. Il s'articule autour de deux objectifs principaux :

1. **Analyser et tester un code hérité** (legacy) pour en identifier les bugs
2. **Réusiner ce code** pour produire une implémentation correcte et maintenable

---

## Architecture

```
src/
├── main/java/com/kerware/
│   ├── simulateur/
│   │   ├── ICalculateurImpot.java        # Interface commune aux deux implémentations
│   │   ├── SituationFamiliale.java       # Enum des situations familiales
│   │   ├── Simulateur.java               # Code hérité original (non modifié)
│   │   └── AdaptateurCodeHerite.java     # Adaptateur du code hérité vers l'interface
│   └── simulateurreusine/
│       ├── Simulateur.java               # Code réusiné (corrigé et restructuré)
│       └── AdaptateurCodeReusine.java    # Adaptateur du code réusiné
└── test/java/com/kerware/
    ├── AbstractSimulateurImpotTest.java  # Batterie de tests commune (abstraite)
    ├── SimulateurImpotReusineTest.java   # Tests sur le code réusiné (tous passent)
    └── SimulateurImpotHeritageTest.java  # Tests sur le code hérité (bugs détectés)
```

### Pattern Adaptateur

L'interface `ICalculateurImpot` est implémentée par deux adaptateurs, ce qui permet d'exécuter exactement la même suite de tests sur les deux implémentations :

- `AdaptateurCodeHerite` → encapsule le code hérité (`simulateur.Simulateur`)
- `AdaptateurCodeReusine` → encapsule le code réusiné (`simulateurreusine.Simulateur`)

---

## Fonctionnalités couvertes

Le simulateur prend en compte :

| Exigence | Description |
|---|---|
| EXG_IMPOT_01 | Situations familiales : célibataire, marié, pacsé, divorcé, veuf |
| EXG_IMPOT_02 | Abattement de 10% (min 495 €, max 14 171 €) par déclarant |
| EXG_IMPOT_03 | Calcul du nombre de parts du foyer fiscal (enfants, handicap, parent isolé) |
| EXG_IMPOT_04 | Application du barème progressif par tranches |
| EXG_IMPOT_05 | Plafonnement du quotient familial |
| EXG_IMPOT_06 | Décote pour les foyers à faible imposition |
| EXG_IMPOT_07 | Contribution Exceptionnelle sur les Hauts Revenus (CEHR) |

---

## Bugs identifiés dans le code hérité

La suite `SimulateurImpotHeritageTest` détecte les anomalies suivantes du code original :

- **VEUF avec enfants** : le nombre de parts déclarants est écrasé à 1 au lieu de 2
- **PACSE non géré** : le `switch` ne couvre pas ce cas → nombre de parts = 0 → impôt incorrect
- **Abattement global** : calculé sur le revenu total du foyer au lieu d'être calculé par déclarant
- **CEHR absente** : la contribution exceptionnelle sur les hauts revenus n'est pas implémentée

---

## Tests

La classe abstraite `AbstractSimulateurImpotTest` fournit une batterie complète de tests unitaires partagée par les deux suites concrètes :

- Tests paramétrés depuis un fichier CSV (`cas_impot.csv`)
- Tests de valeurs limites (abattement min/max, tranches)
- Tests des résultats intermédiaires (abattement, parts, décote)
- Tests négatifs (préconditions : revenus négatifs, nombre d'enfants invalide, situation familiale nulle)

### Lancer les tests et générer le rapport de couverture

```bash
mvn clean test
```

Le rapport JaCoCo est généré dans `target/site/jacoco/index.html`.

---

## Technologies

- **Java 17**
- **JUnit Jupiter 5.11** (tests unitaires et paramétrés)
- **JaCoCo 0.8.11** (couverture de code)
- **Maven 3** (build et gestion des dépendances)
