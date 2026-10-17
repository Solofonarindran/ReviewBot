package fr.reviewbot.infrastructure.out.llm;

import static org.junit.jupiter.api.Assertions.assertFalse;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.ModelParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Test de faisabilité du LLM local (semaine 1) : charge un modèle GGUF, lui envoie un petit diff
 * et mesure le temps et la mémoire. Ne tourne que si -Dreviewbot.llm.model=chemin/vers/modele.gguf est fourni.
 * Ce test est un outil de mesure : il pourra être supprimé une fois le LLM intégré (semaine 7).
 */
@EnabledIfSystemProperty(named = "reviewbot.llm.model", matches = ".+")
class LlmSpikeTest {

    private static final String DIFF = """
            --- a/PaymentService.java
            +++ b/PaymentService.java
            @@ -45,6 +45,7 @@ public class PaymentService {
                 public void pay(Customer customer) {
            -        String sql = "SELECT * FROM accounts WHERE id = ?";
            +        String sql = "SELECT * FROM accounts WHERE id = " + customer.getId();
                     execute(sql);
                 }
            """;

    @Test
    void modelAnswersOnSmallDiff() {
        String modelPath = System.getProperty("reviewbot.llm.model");
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

        ModelParameters modelParams = new ModelParameters()
                .setModel(modelPath)
                .setCtxSize(4096)
                .setThreads(threads)
                .setGpuLayers(0);

        long start = System.nanoTime();
        try (LlamaModel model = new LlamaModel(modelParams)) {
            long loadMs = (System.nanoTime() - start) / 1_000_000;

            String prompt = "<|im_start|>system\n"
                    + "Tu es un relecteur de code Java. Réponds uniquement en JSON : "
                    + "{\"remarques\":[{\"ligne\":0,\"categorie\":\"\",\"severite\":\"\",\"explication\":\"\"}]}\n"
                    + "<|im_end|>\n<|im_start|>user\nRelis ce diff :\n" + DIFF + "<|im_end|>\n<|im_start|>assistant\n";

            InferenceParameters inference = new InferenceParameters(prompt)
                    .setTemperature(0.2f)
                    .setNPredict(300)
                    .setStopStrings("<|im_end|>");

            long inferStart = System.nanoTime();
            String answer = model.complete(inference);
            long inferMs = (System.nanoTime() - inferStart) / 1_000_000;

            Runtime rt = Runtime.getRuntime();
            System.out.println("=== SPIKE LLM ===");
            System.out.println("Threads CPU      : " + threads);
            System.out.println("Chargement       : " + loadMs + " ms");
            System.out.println("Réponse          : " + inferMs + " ms");
            System.out.println("Mémoire JVM (Mo) : " + (rt.totalMemory() - rt.freeMemory()) / 1_048_576);
            System.out.println("--- Réponse du modèle ---\n" + answer);

            assertFalse(answer.isBlank(), "Le modèle n'a rien répondu");
        }
    }
}
