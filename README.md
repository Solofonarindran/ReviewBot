# ReviewBot

Revue de code automatique de pull requests (GitHub, GitLab, Bitbucket) pour projets Maven :
analyses statiques + LLM local, commentaires sur la PR et dashboard web.

## Prérequis
- JDK 27 (IntelliJ : File > Project Structure > SDK)
- Maven 3.9+ (celui d'IntelliJ convient)
- Pas besoin d'installer Node : Maven le télécharge dans `target/`

## Commandes
| Pour… | Commande |
|---|---|
| Tout construire et tester | `mvn clean verify` |
| Lancer l'application | `mvn spring-boot:run` puis http://localhost:8080 |
| Documentation de l'API | http://localhost:8080/swagger-ui.html |
| Reconstruire sans le front | `mvn verify -Dskip.frontend=true` |
| Développer le front (rechargement à chaud) | `cd frontend && npm install && npm run dev` puis http://localhost:5173 (le back doit tourner sur :8080) |

## Test de faisabilité du LLM (semaine 1)
1. Télécharger le modèle `Qwen2.5-Coder-1.5B-Instruct` au format GGUF, quantification `Q4_K_M`
   (dépôt Hugging Face `Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF`) dans le dossier `models/`.
2. Lancer :
   ```
   mvn test -Dtest=LlmSpikeTest -Dskip.frontend=true -Dreviewbot.llm.model=models/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf
   ```
3. Relever : temps de chargement, temps de réponse, mémoire, qualité de la réponse.

## Architecture (hexagonale)
```
fr.reviewbot
├── domain                      modèle métier, sans dépendance technique
├── application
│   └── port
│       ├── in                  cas d'utilisation
│       └── out                 ports vers la base, Git, LLM, sandbox
└── infrastructure
    ├── in.web                  contrôleurs REST
    ├── out.persistence         JDBC / HSQLDB
    ├── out.llm                 llama.cpp
    └── config                  configuration Spring
```
