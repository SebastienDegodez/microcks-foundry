# Microcks Foundry — Product Requirements Document

| | |
|---|---|
| Statut | Brouillon de travail |
| Date | 2026-11-11 |
| Périmètre | API REST (OpenAPI 3.x) ; autres protocoles en extension |
| Dépôt | github.com/SebastienDegodez/microcks-foundry |

## 1. Résumé

Microcks Foundry fabrique, à côté de Microcks, les artefacts que Microcks consomme : contrats OpenAPI, fichiers APIExamples et workflows Arazzo. Il les construit à partir de trois sources — les contrats existants, les scénarios Gherkin et, facultativement, le trafic observé en pré-production — puis les propose à l'équipe sous forme de diff relu, livré en pull request ou en archive ZIP.

Foundry ne simule rien et ne teste rien : Microcks importe les artefacts, simule les API et teste les implémentations. Foundry propose, l'humain décide.

## 2. Problème et valeur

**Problème.** Les équipes QA et développement écrivent les exemples Microcks à la main. C'est long, rarement exhaustif, et les exemples vieillissent dès que le contrat ou l'implémentation évolue. Les approches fondées uniquement sur l'enregistrement de trafic reproduisent ce qui a été vu : elles ne conçoivent pas de cas de test et ne couvrent que le trafic observé.

**Valeur visée.**

- Des jeux d'exemples conçus avec des techniques de test nommées (partitions d'équivalence, valeurs limites, transitions d'états), chacun étiqueté avec la technique qui l'a produit.
- Des valeurs propres au contexte de chaque équipe (dictionnaires, règles, mémoire).
- Un fonctionnement complet sans trafic réel et sans IA ; le trafic et l'IA enrichissent, ils ne conditionnent rien.
- Une traçabilité de bout en bout : exigence Gherkin → exemple → décision humaine → PR.

**Mesures de succès** (cibles à fixer après les premières équipes pilotes) :

- délai entre un OpenAPI fourni et un premier jeu d'APIExamples importé dans Microcks ;
- part des opérations couvertes par au moins un exemple nominal et un exemple d'erreur ;
- part des exemples proposés acceptés sans retouche ;
- nombre d'écarts contrat / trafic résolus par une PR.

## 3. Utilisateurs

| Rôle | Ce qu'il fait avec Foundry | Persona des user stories |
|---|---|---|
| QA | Conçoit les cas, fournit le Gherkin, relit et valide les exemples | Claire, QA de l'équipe paiements |
| Développeur | Génère contrat et exemples pour son API, branche Foundry dans son pipeline | Karim, développeur de l'Order API |
| Propriétaire de contrat | Audite le contrat, arbitre les écarts avec le trafic | Inès, propriétaire du contrat Order API |
| Plateforme | Administre l'instance, les espaces, les fournisseurs IA, le déploiement | Nadia, ingénieure plateforme |
| Entreprise | Injecte dictionnaires, règles, agents et skills communs | — |

## 4. Principes

1. **Humain au centre.** Foundry propose, l'humain décide. Chaque proposition montre l'algorithme qui l'a produite et ses preuves ; chaque décision est tracée avec son auteur. Toute sortie passe par un diff relu puis une PR ou un ZIP.
2. **Déterministe d'abord.** Un catalogue d'algorithmes déterministes et versionnés produit les propositions. L'IA ne fait que combler un manque ; elle est désactivée par défaut et ses sorties sont toujours séparées, signalées et relues.
3. **Fonctionnel sans trafic réel.** Le trafic est une source parmi d'autres.
4. **Autonomie par équipe.** Chaque équipe a son espace, son contexte de données et ses règles, isolés des autres.
5. **À côté de Microcks.** Foundry génère ; Microcks importe, simule et teste. Microcks ne déclenche rien dans Foundry.
6. **Hors production.** Seuls les environnements amont sont branchés.
7. **Au service de Microcks, jamais à sa place.** Foundry ne sert aucun mock, n'exécute aucun test contre une implémentation, ne rend aucun verdict ni score de conformité, ne produit aucun format de mock pour un autre outil et n'altère jamais le trafic qu'il observe.

## 5. Glossaire

| Terme | Définition |
|---|---|
| APIExamples | Artefact Microcks (schéma `v1alpha1`) qui ajoute des exemples à une API importée ; opérations indexées par « VERBE /chemin », propriétés supplémentaires interdites |
| APIMetadata | Artefact Microcks portant métadonnées et labels (`metadata.labels`) d'une API ; Foundry y lit la correspondance flux → contrat |
| Arazzo | Spécification OpenAPI Initiative (1.1) décrivant des workflows enchaînant plusieurs opérations |
| Espace d'équipe | Partition isolée de Foundry : sources, trafic, dictionnaires, mémoire, règles et repo cible d'une équipe |
| Échange | Paire requête / réponse HTTP normalisée (méthode, chemin, statut, en-têtes, corps, identifiant de trace), quelle que soit la source |
| Proposition | Ensemble de fichiers modifiés, avec diff, raison de chaque changement et algorithme d'origine, en attente de décision |
| Technique de conception | Technique de test nommée (ISTQB) : partitions d'équivalence, valeurs limites, transitions d'états |
| Algorithme | Traitement déterministe identifié par un nom et une version (ex. `boundary.values v1`) |
| Graine | Valeur stockée dans la configuration de l'équipe rendant la génération reproductible |
| Source primaire / secondaire | Quand un contrat existe dans Microcks et dans git, la configuration désigne celle que Foundry peut modifier (primaire) ; la secondaire est seulement comparée |
| Écart | Différence entre le contrat source et le trafic observé, classée cassante ou non cassante |

## 6. Vue d'ensemble

```mermaid
flowchart LR
  subgraph Entrées
    C[Contrats OpenAPI<br/>Microcks ou git]
    G[Scénarios Gherkin]
    D[Dictionnaires et règles<br/>de l'équipe]
    T[Trafic pré-prod<br/>OTLP ou HAR]
  end
  subgraph Foundry
    R[Réception]
    K[Cœur déterministe<br/>catalogue d'algorithmes]
    AI[Moteur IA<br/>optionnel]
    V[Validation]
    P[Proposition<br/>diff relu]
  end
  subgraph Sorties
    PR[Branche + PR<br/>GitHub / Azure DevOps]
    Z[Archive ZIP]
  end
  T --> R --> K
  C --> K
  G --> K
  D --> K
  AI -.-> K
  K --> V --> P
  P -->|décision humaine| PR
  P -->|décision humaine| Z
  PR --> M[(Microcks importe,<br/>simule et teste)]
  Z --> M
```

**Composants.** Le socle suit une clean architecture : les dépendances pointent vers le domaine, jamais l'inverse.

| Composant | Rôle |
|---|---|
| Récepteur OTLP | Reçoit les traces en gRPC (4317) et HTTP (4318, `/v1/traces`), protobuf ou JSON ; convertit les spans HTTP serveur en échanges |
| Import HAR | Convertit un fichier HAR en échanges, même modèle que l'OTLP |
| Domaine (`foundry-domain`) | Entités (échange, exemple, proposition, écart, décision), règles et catalogue d'algorithmes ; Java pur |
| Cas d'usage (`foundry-application`) | Générer, auditer, comparer, proposer, livrer ; définit les ports (sources, sorties, IA, stockage) ; Java pur |
| Adaptateurs (`foundry-infrastructure`) | Implémentent les ports : OTLP, HAR, Microcks, GitHub, Azure DevOps, moteurs IA, persistance |
| Application (`foundry-app`) | Point d'entrée Quarkus : API, IHM, file de traitement |
| CLI (`foundry-cli`) | Mêmes capacités que l'IHM, pour les pipelines |
| Connecteurs de sources | Microcks (lecture), GitHub puis Azure DevOps (lecture et écriture) |
| Moteurs IA | Interface unique : Copilot CLI, endpoint compatible OpenAI, modèle local |
| IHM | Web component `<microcks-foundry>` intégrable dans Microcks ou utilisé seul |

## 7. Exigences par tranche

Cinq tranches livrables dans l'ordre, chacune utilisable seule.

### T1 — Du contrat aux APIExamples, sans trafic

- **E1.1** Entrées : OpenAPI existant, features Gherkin, dictionnaires de valeurs. Les contrats sont lus dans Microcks (API déjà importées) et dans les repos git (GitHub, puis Azure DevOps).
- **E1.2** Génère des APIExamples par opération : nominal, erreurs déclarées, valeurs limites, partitions d'équivalence. Chaque exemple porte la technique qui l'a produit. Aucune réponse d'erreur n'est inventée si le contrat n'en déclare pas : l'absence est signalée.
- **E1.3** Chaque scénario Gherkin est relié aux exemples qu'il produit (traçabilité exigence → exemple). Les tableaux `Examples` fournissent des valeurs.
- **E1.4** Simulation de LLM : Foundry génère des exemples pour les API compatibles OpenAI ; Microcks les sert à la place du modèle réel pour réduire le coût des tests.
- **E1.5** Sorties : branche et PR GitHub, branche et PR Azure DevOps, ou archive ZIP. Rien ne sort sans validation humaine ; pour le ZIP, la validation se fait avant téléchargement.
- **E1.6** CLI utilisable en pipeline : code de retour et rapport de couverture par opération. Le code de retour reflète la validité des artefacts produits par Foundry, jamais la conformité de l'implémentation.
- **E1.7** Graine stockée dans la configuration de l'équipe : même contrat, même dictionnaire et même graine donnent des fichiers identiques. Les sorties de l'IA sont écrites une fois et régénérées seulement sur demande.
- **E1.8** Ordre de résolution de chaque valeur (voir §9). Toute la chaîne fonctionne sans IA. Chaque valeur garde la trace de son origine.
- **E1.9** Audit des contrats par règles déterministes : validité, réponses d'erreur manquantes, exemples absents, nommage incohérent. Chaque constat est expliqué ; une version corrigée est proposée. Les règles sont versionnées par équipe.
- **E1.10** Avant toute PR, une prévisualisation montre les fichiers touchés, le diff, la raison de chaque changement, l'algorithme d'origine et la description de la PR, modifiable. Contrôles exécutés aussi en CLI : OpenAPI valide, APIExamples valides contre `v1alpha1`, clés d'opération conformes au contrat, absence de secret, fichier IA signalé.
- **E1.11** REST uniquement dans ce document. GraphQL, AsyncAPI, gRPC et A2A viennent après REST ; MCP est mis de côté. Règle : une capacité n'entre pour un protocole que si elle y atteint le niveau du REST. Pour AsyncAPI et gRPC, seule la génération depuis le contrat est retenue (les messages ne sont pas dans les spans).
- **E1.12** Catalogue d'algorithmes versionnés (voir §8) : `equivalence.partitions`, `boundary.values`, `generators.format`, `diff.classifier`, `traffic.dedup`, `path.naming`, `schema.inference`, `state.transitions`. Chaque valeur et chaque changement citent l'algorithme et sa version.
- **E1.13** Journal des décisions : qui a accepté, corrigé ou rejeté quoi, et quand. Foundry ne décide jamais seul, sauf règle explicite de l'équipe.
- **E1.14** Compatibilité de chaque contrat proposé par Foundry, ancien contre nouveau : un changement cassant est toujours signalé et une version majeure suggérée.
- **E1.15** Patch minimal du contrat : seules les lignes concernées changent ; `$ref`, commentaires YAML et extensions `x-*` sont conservés.
- **E1.16** Choix entre exemples « champs obligatoires seuls » et exemples complets.
- **E1.17** Un exemple négatif par contrainte (type, format, pattern, required), associé à la réponse d'erreur déclarée.
- **E1.18** Règles d'audit réglables par équipe (erreur, avertissement, désactivée), dont un jeu orienté sécurité inspiré de l'OWASP API Top 10.
- **E1.19** Rapport SARIF des constats d'audit, lisible par les outils de revue de code. Pas de rapport de test : les tests relèvent de Microcks.
- **E1.20** Description de PR : changements groupés par gravité, lien de revue pour chaque changement cassant.
- **E1.21** Français et anglais : IHM, CLI, explications, descriptions de PR.

Critère de sortie : un OpenAPI seul donne un jeu d'exemples importé dans Microcks.

### T2 — Trafic et écarts contrat / trafic

- **E2.1** Réception OTLP directe, sans Collector modifié : gRPC, HTTP/protobuf, HTTP/JSON. Le Collector de l'équipe s'y branche par configuration d'export. En cas de saturation, Foundry répond 429 ou 503 et l'émetteur rejoue.
- **E2.2** Corps et masquage sont configurés par l'émetteur (instrumentation et Collector), selon OpenTelemetry. Foundry lit `http.request.body.content` et `http.response.body.content`, plus des alias configurables (ex. `request_body`, `response_body`). L'émetteur porte la responsabilité de ce qu'il envoie ; Foundry ne filtre pas.
- **E2.3** Le trafic devient des APIExamples candidats, dédoublonnés et rattachés aux opérations.
- **E2.4** Sans contrat, Foundry consolide un OpenAPI. Avec contrat, il compare le contrat source au trafic et signale les écarts. Si un contrat existe dans Microcks et dans git, la configuration désigne la source primaire ; la secondaire n'est jamais modifiée. Chaque flux est rattaché à son contrat par `service.name`, sinon par une correspondance déclarée dans les labels d'un APIMetadata (`service.name`, hôte, version).
- **E2.5** La collecte reste hors du chemin de la requête et se désactive sans effet sur le composant observé.
- **E2.6** Traitement gradué des écarts par équipe et par règle : ignorer, signaler, exiger une décision. Foundry ne fait jamais échouer le pipeline de l'application.
- **E2.7** Les spans HTTP serveur sont convertis en échanges ; les autres spans sont ignorés. Le jeton d'équipe, passé en en-tête d'export, désigne l'espace.
- **E2.8** Paramètres de chemin nommés d'après le contrat ou le dictionnaire (`{orderId}`), jamais génériques (`{param1}`).
- **E2.9** Écarts détaillés au niveau du champ, classés cassants ou non cassants, avec la proportion d'appels concernés et des traces de preuve.
- **E2.10** Suite donnée à un écart, configurable par équipe ou par API : alerte et PR automatique ; PR seule ; alerte puis PR déclenchée à la main (défaut). L'humain distingue « contrat en retard » (Foundry propose la correction) et « implémentation suspecte » (Foundry propose de lancer le test Microcks de l'opération, sans modifier le contrat). Une opération non observée n'est jamais signalée comme absente.
- **E2.11** Catalogue de règles d'écarts à trois niveaux (erreur, avertissement, info).
- **E2.12** Confirmation humaine des motifs de chemin proposés ; une correction s'applique aux appels suivants.
- **E2.13** Mise à jour d'un contrat existant : opérations documentées seulement, ou ajout des nouvelles.
- **E2.14** Champs instables (horodatages, UUID, nonces) rendus en expressions dynamiques Microcks.
- **E2.15** Valeurs masquées par l'émetteur remplacées par des valeurs synthétiques de même format.
- **E2.16** Dédoublonnage du trafic par classe d'équivalence.
- **E2.17** Import de fichiers HAR comme source de trafic.
- **E2.18** Couverture : opérations documentées non observées, opérations et paramètres observés non documentés.

Critère de sortie : une session de trafic donne une PR d'exemples et une liste d'écarts lisible.

### T3 — Workflows Arazzo

- **E3.1** Workflows construits depuis les scénarios Gherkin ou depuis des séquences d'appels corrélées.
- **E3.2** Chaque étape référence une opération du contrat et des exemples existants.
- **E3.3** Sortie en fichiers Arazzo 1.1 dans le repo ; Foundry valide leur structure tant que Microcks ne les importe pas.
- **E3.4** Cohérence entre opérations, à faire en premier : l'identifiant produit par une opération est celui que les suivantes consomment. Liens déduits du contrat (`links`), de l'en-tête `Location` observé, ou déclarés dans le dictionnaire.
- **E3.5** Cycle de vie : un exemple par état, un workflow par chemin de transitions ; couverture en transitions couvertes sur transitions possibles.
- **E3.6** (plus tard) Audit des Arazzo existants contre les séquences observées.
- **E3.7** Ajout de `links` proposé au contrat quand un lien est déduit du trafic.
- **E3.8** Exemples cohérents pour chaque étape d'un Arazzo existant.

Critère de sortie : un parcours métier de bout en bout est décrit en Arazzo valide et chaque étape s'appuie sur des exemples servis par Microcks.

### T4 — Contexte d'équipe, mémoire et IA

- **E4.1** Agents et skills injectés par l'entreprise ou l'équipe, versionnés dans le repo.
- **E4.2** Mémoire : valeurs apprises (formats, identifiants valides, règles métier), promues dans le repo par PR (voir H1).
- **E4.3** Score de confiance par artefact ; au-dessus du seuil de l'équipe, fusion automatique si l'équipe l'autorise, sinon revue. Chaque décision automatique est journalisée et annulable.
- **E4.4** Les exemples produits par l'IA sont dans un fichier séparé et portent la mention « Generated by AI, review needed ».
- **E4.5** Moteur IA remplaçable derrière une interface unique. Fournisseurs configurés uniquement en administration ; chaque équipe choisit parmi ceux activés, ou aucun. Le mode sans IA reste complet.
- **E4.6** Score de confiance calculé à partir de l'origine des valeurs (§9).
- **E4.7** Valeurs observées dans le trafic proposées au dictionnaire et promues par PR.

Critère de sortie : deux équipes obtiennent des jeux différents depuis le même contrat, chacune selon son contexte.

### T5 — IHM de revue et intégration

- **E5.1** Revue des artefacts candidats : accepter, corriger, rejeter.
- **E5.2** Couverture par opération et par technique.
- **E5.3** Édition des dictionnaires et du seuil de confiance.
- **E5.4** L'IHM ne fait rien que la CLI ne sache faire.
- **E5.5** Édition d'un exemple avec validation du schéma en direct.
- **E5.6** Web component `<microcks-foundry>` : intégré dans Microcks (thème et session de l'hôte) ou autonome avec un parcours de premier lancement.

Critère de sortie : un QA valide une PR d'exemples sans ouvrir git.

## 8. Catalogue d'algorithmes

Chaque proposition cite l'algorithme qui l'a produite, par son identifiant et sa version (ex. `boundary.values v1`). Voici ce que fait chacun, en clair.

| Algorithme | En clair | Tranche |
|---|---|---|
| `equivalence.partitions` | Partitions d'équivalence : un exemple par groupe de valeurs qui se comportent pareil | T1 |
| `boundary.values` | Valeurs limites : chaque borne et la valeur juste à l'extérieur | T1 |
| `generators.format` | Générateurs sans IA : email, IBAN, date, UUID… d'après le format ou le nom du champ | T1 |
| `diff.classifier` | Classement des changements : cassant ou compatible | T1–T2 |
| `traffic.dedup` | Dédoublonnage du trafic : un exemple par cas observé | T2 |
| `path.naming` | Nommage des chemins : `/orders/C-1042` devient `/orders/{orderId}` | T2 |
| `schema.inference` | Déduction du schéma à partir des corps observés | T2 |
| `state.transitions` | Transitions d'états : un exemple par état, un workflow par chemin | T3 |

Le détail de chacun, avec un exemple, suit.

### `equivalence.partitions` — Partitions d'équivalence (T1)

Découpe les valeurs possibles d'un champ en groupes qui doivent se comporter de la même façon. Un seul représentant par groupe suffit : tester M ou L sur une taille autorisée apporte la même information.

*Exemple :* Champ `size` limité à S, M, L → groupe valide {S, M, L} : un exemple avec `M` ; groupe invalide (toute autre valeur) : un exemple avec `XL` associé à la réponse 400 déclarée.

### `boundary.values` — Valeurs limites (T1)

Les erreurs se cachent aux bornes (`<` au lieu de `<=`). Pour chaque minimum ou maximum, prend la borne elle-même et la valeur juste à l'extérieur.

*Exemple :* `price` entre 0.01 et 999.99 → valides : `0.01` et `999.99` ; invalides : `0.00` et `1000.00`. `name` de 2 à 30 caractères → 2 et 30 valides, 1 et 31 invalides.

### `generators.format` — Générateurs de valeurs réalistes, sans IA (T1)

Produit une valeur valide à partir du format déclaré ou du nom du champ. Même graine, même valeur. Les valeurs sont fictives : domaines et plages réservés aux exemples, aucune donnée réelle.

*Exemple :* `customer.email` (format email) → `lea.durand@example.com` ; champ `iban` → `FR76 3000 6000 0112 3456 7890 189` (clé de contrôle valide) ; `createdAt` (date-time) → `2026-03-14T09:30:00Z`.

### `state.transitions` — Transitions d'états (T3)

Repère les états d'une ressource (souvent une enum `status`) et les opérations qui la font changer. Produit un exemple par état et un workflow par enchaînement possible.

*Exemple :* `status` PENDING → PAID → SHIPPED : 3 exemples (un par état) et le parcours « créer la commande, payer, expédier » ; couverture affichée : 2 transitions décrites sur 3.

### `diff.classifier` — Classement des changements (T1–T2)

Compare deux versions d'un contrat, ou un contrat et le trafic, et dit si chaque différence casse les consommateurs actuels.

*Exemple :* Retirer `PAID` de l'enum `status` → cassant. Rendre `customer.email` facultatif dans une requête → compatible. Ajouter `SHIPPED` dans une réponse → risqué pour les clients, avertissement.

### `traffic.dedup` — Dédoublonnage du trafic (T2)

Regroupe les appels observés qui relèvent du même cas (même partition d'équivalence) et n'en garde qu'un comme exemple candidat.

*Exemple :* 120 appels `GET /pastry/{name}` → 3 exemples : pâtisserie trouvée, taille S, pâtisserie inconnue (404).

### `path.naming` — Nommage des chemins (T2)

Reconnaît la partie variable d'un chemin observé et lui donne un nom parlant, repris du contrat ou du dictionnaire, jamais `{param1}`.

*Exemple :* `/orders/C-1042` et `/orders/C-1099` → `/orders/{orderId}`. `/orders/history` reste un chemin fixe si l'humain le corrige.

### `schema.inference` — Déduction du schéma (T2)

Construit le schéma JSON d'une opération à partir des corps observés : un champ présent partout devient obligatoire, un type contradictoire est signalé au lieu d'être tranché.

*Exemple :* 40 corps de commande → `customer.id` obligatoire (40/40), `customer.email` facultatif (37/40) ; `quantity` vu en nombre et en texte → conflit signalé.

Le catalogue est extensible : une entreprise peut ajouter ses algorithmes (ex. un générateur de numéros de contrat) sans recompiler Foundry. Deux algorithmes avec le même identifiant et la même version empêchent le démarrage.

## 9. Résolution des valeurs

Pour chaque champ, la première source disponible l'emporte :

1. contrat (`example`, `enum`, `format`) ;
2. tableaux `Examples` du Gherkin ;
3. dictionnaire de l'équipe ;
4. mémoire promue ;
5. générateurs déterministes ;
6. IA, si l'espace l'autorise.

L'origine de chaque valeur est conservée ; elle alimente l'affichage, le score de confiance et le journal.

**Générateurs déterministes v1** (`generators.format`) — toujours sans IA, valeurs fictives, identiques pour une même graine :

| Reconnu par | Exemple produit | Garantie |
|---|---|---|
| format `email`, champ `*email*` | `lea.durand@example.com` | Domaine `example.com` réservé aux exemples |
| format `uuid` | `3f6c2a9e-1b7d-4c2e-9a41-0d5b8e7f1c23` | UUID v4 dérivé de la graine |
| format `date` / `date-time` | `2026-03-14` / `2026-03-14T09:30:00Z` | Fenêtre de dates fixée par la graine |
| champ `iban` | `FR76 3000 6000 0112 3456 7890 189` | Clé de contrôle mod 97 valide |
| champ `siren` / `siret` | `123 456 782` / `123 456 782 00002` | Clé de Luhn valide |
| champ `phone`, `telephone` | `+33 1 99 00 12 34` | Format E.164, plage fictive |
| champ `firstName`, `lastName` | `Léa`, `Durand` | Listes intégrées par langue de l'espace |
| champ `zip`, `postalCode` | `75011` | Format du pays de l'espace |
| champ `country`, `currency` | `FR`, `EUR` | Codes ISO 3166 et ISO 4217 |
| format `uri`, `ipv4` | `https://example.com/orders/C-1042`, `192.0.2.10` | Domaine et plage réservés à la documentation |
| `pattern` déclaré | `ORD-104233` pour `^ORD-[0-9]{6}$` | Valeur générée depuis l'expression régulière |
| nombre avec `minimum`, `maximum`, `multipleOf` | `42.50` pour 0.01–999.99, pas de 0.01 | Respecte toutes les contraintes déclarées |

## 10. Configuration d'une équipe (proposition)

```
.foundry/
  config.yaml          # graine, mode de traitement des écarts, sources primaire / secondaire, langue des PR
  rules/               # règles d'audit et niveaux
  dictionaries/        # valeurs métier (CSV, YAML)
  memory/              # valeurs promues par PR
  mappings.yaml        # correspondance flux → contrat sans Microcks
  skills/              # agents et skills de l'équipe
```

Sorties générées : `examples/<api>-<version>.examples.yaml`, `examples/<api>-<version>.ai-examples.yaml`, `arazzo/<workflow>.arazzo.yaml`.

## 11. Exigences transverses

| Thème | Exigence |
|---|---|
| Isolation | Instance partagée, un espace par équipe ; trafic, mémoire, dictionnaires et skills invisibles des autres |
| Données | Hors production uniquement ; trafic brut jamais commité ; rétention bornée |
| Innocuité | Une panne de Foundry n'a aucun effet sur le composant observé ni sur Microcks ; ressources bornées |
| Scalabilité | Réception et génération découplées par une file ; traitement sans état, multipliable |
| Déploiement | Kubernetes en priorité : image (native possible) et chart Helm ; secrets fournis par des Secrets existants |
| Audit | Toute décision, humaine ou automatique, est tracée ; pour l'IA : entrée, skill, score, version du modèle |
| Reproductibilité | Mêmes entrées, même mémoire et même graine donnent les mêmes sorties déterministes |
| Langues | Français et anglais dès T1 ; langue par utilisateur, et par équipe pour les PR ; artefacts jamais traduits ; une langue = un fichier de messages |
| Tests | Boîte blanche ; les dépendances externes (GitHub, Azure DevOps, LLM) sont simulées par Microcks ; jeux de référence versionnés et tests d'acceptance exécutables |

## 12. Hors périmètre

- **Production** : aucun branchement, aucune exception.
- **Simulation et test** : servir des mocks, tester une implémentation, rendre un verdict de conformité — rôle de Microcks.
- **Rejeu de trafic et tests de régression** : Foundry produit des artefacts, il ne rejoue pas.
- **Fuzzing** et **tests de charge**.
- **Export vers d'autres outils de mock**.
- **Injection de pannes** : Foundry n'altère jamais le trafic ; les pannes simulées relèvent de Microcks.
- **Record déclenché par Microcks** : la réception du trafic vit dans Foundry.

## 13. Étape suivante

| ID | Fonctionnalité | Pourquoi |
|---|---|---|
| N01 | Usage réel par les consommateurs : champs jamais lus, opérations jamais appelées, candidats à la dépréciation | Le trafic dit ce dont les consommateurs dépendent |
| N02 | Politique de dépréciation et de retrait | Encadre N01 |
| N03 | Import de collections comme entrée : Postman et OpenCollection (spécification ouverte en YAML, versionnable dans git) | Beaucoup d'équipes n'ont que des collections ; OpenCollection est un format ouvert, lisible et diffable |
| N04 | Import de jeux de règles de lint OpenAPI existants | Réutiliser la gouvernance déjà écrite |
| N05 | Exemples de réponses d'erreur 5xx et délais observés écrits comme artefacts (délai d'opération dans APIMetadata) | Microcks les sert ; Foundry ne fait que les écrire |
| N06 | Langues au-delà du français et de l'anglais | Équipes internationales |
| N07 | Fenêtre de capture par service et découverte automatique | Limite le volume reçu |
| N08 | Contexte de trafic fourni aux agents de code | Aide les agents à respecter le comportement réel |
| N09 | Sidecar de capture : proposé aux équipes, ajouté par elles à leur déploiement, Kubernetes en priorité ; observe sans modifier, n'émet qu'en OTLP, ne masque rien lui-même | Chaque équipe doit pouvoir fournir du trafic, instrumentée ou non, avec ou sans mesh ; tranche T2 ou T3 à trancher |

## 14. Décisions

| Sujet | Décision |
|---|---|
| Nom | Microcks Foundry, provisoire, à valider avec les mainteneurs Microcks |
| Isolation | Instance partagée, un espace isolé par équipe |
| Apport ISTQB | Techniques de conception nommées, traçabilité Gherkin → exemple, couverture par opération |
| LLM simulés | Foundry génère les exemples, Microcks sert les réponses |
| Langage | Quarkus, aligné sur Microcks v2 |
| Architecture | Clean architecture : domaine et cas d'usage en Java pur (ServiceLoader, compatible natif, candidats plugin Microcks v2) ; adaptateurs et points d'entrée à l'extérieur ; règle de dépendance vérifiée en CI |
| Entrée trafic | OTLP direct depuis un Collector standard ; HAR en complément ; Kafka possible plus tard |
| Données | Corps et masquage configurés par l'émetteur, qui en porte la responsabilité |
| Sorties | ZIP et GitHub d'abord, puis Azure DevOps |
| IA de démonstration | GitHub Copilot CLI (sous-processus) et OpenAI, derrière une interface interne |
| Code tiers | Aucun code tiers repris ; tous les algorithmes sont implémentés dans ce dépôt sous tests d'acceptance |
| Source de contrat | Microcks ou git ; si les deux, primaire et secondaire par configuration |
| Correspondance flux → contrat | `service.name`, sinon labels d'un APIMetadata, sinon `.foundry/mappings.yaml` |
| Écarts | Trois modes ; défaut : alerte puis PR déclenchée à la main |
| Mis de côté | Génération de scripts de charge ; MCP |
| Sidecar | Proposé, jamais imposé ; Kubernetes en priorité |

## 15. Questions ouvertes

- **H1 — Mémoire.** Recommandation : hybride, apprentissage en base et promotion en PR des valeurs confirmées. Alternatives : fichier seul, base seule, aucune mémoire, index sémantique réservé à l'IA.
- **H2 — Score de confiance.** Poids par origine de valeur à fixer.
- **H3 — Rétention** du trafic reçu.
- **H4 — Repos** : un par équipe ou un par API ?
- **H5 — Garde-fous de réception** : refuser un flux étiqueté production ?
- **H6 — Ordre des protocoles OTLP** : gRPC et HTTP/protobuf d'abord, JSON ensuite ?
- **H7 — Copilot CLI en automatique** : conditions d'usage et coût par appel.
- **H8 — Arazzo dans Microcks** : import absent des releases ; Foundry valide seul en attendant.
- **H9 — Corrélation des parcours** : `trace_id` seul ou identifiant métier en en-tête ?
- **H10 — Sidecar** : tranche T2 ou T3 ; cohabitation avec un mesh existant ; TLS terminé par l'application.
- **H11 — Langue intégrée** : suivre la langue de Microcks quand Foundry y est intégré ?
- **H12 — Authentification** en mode autonome.

## 16. Risques

| Risque | Parade |
|---|---|
| Périmètre large pour une petite équipe | Tranches livrables, chacune utile seule ; T1 d'abord |
| Corps absents des traces | T1 fonctionne sans trafic ; HAR en alternative ; activation documentée côté instrumentation |
| Données personnelles en pré-production | Masquage par l'émetteur, trafic brut jamais commité, rétention bornée |
| Sorties IA non reproductibles | Fichier séparé, PR, seuil de confiance |
| Dépendance à un fournisseur IA | Interface unique, moteurs remplaçables, mode sans IA complet |
| Sidecar intrusif | Proposé et non imposé ; hors du chemin critique ; désactivable sans effet |

## 17. Milestones

| Milestone | Tranche |
|---|---|
| M1 | Socle technique et T1 |
| M2 | T2 |
| M3 | T3 |
| M4 | T4 |
| M5 | T5 |

## 18. Références techniques

- Microcks : [schéma APIExamples v1alpha1](https://github.com/microcks/microcks/blob/master/api/APIExamples-v1alpha1-schema.json), [délais](https://microcks.io/documentation/guides/usage/delays), [GitHub Action d'import](https://microcks.io/documentation/guides/automation/github-actions).
- OpenTelemetry : [conventions HTTP](https://opentelemetry.io/docs/specs/semconv/http/http-spans/), [OTLP](https://opentelemetry.io/docs/specs/otlp/), processeurs [attributes](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/attributesprocessor/README.md), [redaction](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/redactionprocessor/README.md), [transform](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/transformprocessor/README.md).
- [Spécification Arazzo](https://spec.openapis.org/arazzo/latest).
- [Spécification OpenCollection](https://www.opencollection.com/).
- GitHub : [GitHub Apps](https://docs.github.com/en/apps/creating-github-apps/about-creating-github-apps/deciding-when-to-build-a-github-app), [Git Data API](https://docs.github.com/en/rest/git/trees).
- Azure DevOps : [Pushes](https://learn.microsoft.com/en-us/rest/api/azure/devops/git/pushes/create), [Pull Requests](https://learn.microsoft.com/en-us/rest/api/azure/devops/git/pull-requests/create).
- [Copilot CLI, mode programmatique](https://docs.github.com/en/copilot/reference/copilot-cli-reference/cli-programmatic-reference).
- Bibliothèques Java : `io.cucumber:gherkin`, `swagger-parser`, `openai-java`.

## 19. Écrans

Maquettes indicatives (libellés en anglais). Chaque zone numérotée est référencée par les issues qui la concernent.

### Vue d'ensemble (intégrée à Microcks)

![Vue d'ensemble (intégrée à Microcks)](ui/FoundryModule.png)

1. En attente de décision
2. Récepteur de trafic
3. Contrats
4. Écarts ouverts
5. Construction des propositions
6. Contrat vs trafic
7. Couverture par technique
8. Livraisons
9. Journal des décisions

### Générer

![Générer](ui/Generate.png)

1. Ce qu'on peut construire
2. Ce que chaque génération demande
3. Protocoles pris en charge
4. Étape 1 · Contrat
5. Étape 2 · Scénarios
6. Étape 3 · Techniques et valeurs
7. Étape 4 · Sortie

### Revue des exemples

![Revue des exemples](ui/Review.png)

1. En-tête : API, graine, acceptation automatique
2. Filtres par technique
3. Liste des exemples : technique, origines, traçabilité, score, décision
4. Aperçu de l'exemple
5. Pourquoi cet exemple
6. Barre de livraison : ZIP, PR

### Prévisualisation avant PR

![Prévisualisation avant PR](ui/PRPreview.png)

1. En-tête
2. Fichiers touchés
3. Diff du fichier et raisons
4. Pull request : titre et description
5. Contrôles avant ouverture

### Audit des contrats

![Audit des contrats](ui/Audit.png)

1. En-tête
2. Contrats audités
3. Constats du contrat
4. Corriger

### Écarts contrat / trafic

![Écarts contrat / trafic](ui/Divergences.png)

1. En-tête
2. Filtres
3. Liste des écarts
4. Sources secondaires
5. Trafic non rattaché

### Détail d'un écart

![Détail d'un écart](ui/Divergence.png)

1. En-tête de l'opération
2. Différences champ par champ
3. Contrat (Microcks)
4. Trafic observé (OTLP)
5. Qui a raison ?

### Workflows Arazzo

![Workflows Arazzo](ui/Workflows.png)

1. En-tête
2. Documents Arazzo
3. Workflow et ses étapes
4. Corrélation

### Sources

![Sources](ui/Sources.png)

1. En-tête
2. Contrats et correspondance du trafic
3. Récepteur de trafic
4. Configuration du Collector
5. Dépôts

### Contexte

![Contexte](ui/Context.png)

1. En-tête
2. Algorithmes
3. Dictionnaires
4. Mémoire
5. Skills et agents
6. IA, optionnelle

### Réglages de l'espace

![Réglages de l'espace](ui/Settings.png)

1. En-tête : espace
2. Traitement des écarts
3. Sorties
4. Langue
5. Rétention
6. Microcks

### Administration : fournisseurs IA

![Administration : fournisseurs IA](ui/Admin.png)

1. En-tête
2. Fournisseurs
3. Règles de données
4. Usage

### Premier lancement

![Premier lancement](ui/Onboarding.png)

1. En-tête
2. Espace
3. Contrats
4. Livraison
5. Microcks
6. Trafic
7. IA
8. Avec et sans Microcks

### Foundry autonome

![Foundry autonome](ui/Standalone.png)

1. Barre supérieure : espace, recherche, langue, thème
2. Navigation
3. Bandeau « sans Microcks »

