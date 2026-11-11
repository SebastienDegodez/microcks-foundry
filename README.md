# Microcks Foundry

Microcks Foundry génère des artefacts Microcks à partir de contrats d'API, de scénarios et, facultativement, du trafic observé.

## Architecture

Le réacteur Maven garde le domaine et les cas d'usage indépendants de tout framework. Les dépendances pointent vers le domaine ; `foundry-app` et `foundry-cli` sont les points de composition.

| Module | Rôle |
| --- | --- |
| `foundry-domain` | Modèle et règles du domaine ; aucune dépendance d'exécution |
| `foundry-application` | Cas d'usage et ports ; dépend uniquement du domaine |
| `foundry-infrastructure` | Adaptateurs ; dépend des ports applicatifs et du domaine |
| `foundry-app` | Application Quarkus et point de composition |
| `foundry-cli` | Point de composition de la CLI |

Les tests ArchUnit de `foundry-app` protègent les frontières et nomment la classe ainsi que la dépendance interdite en cas de violation.

## Compilation et tests

JDK 21 et Maven 3.9 ou ultérieur sont requis.

```shell
mvn verify
```

GitHub Actions exécute ce build à chaque pull request. Après un push sur `main`, il construit et publie également l'exécutable natif Quarkus comme artefact CI.

Pour construire l'exécutable natif localement avec Docker :

```shell
mvn -pl foundry-app -am package -Dnative -Dquarkus.native.container-build=true
```
