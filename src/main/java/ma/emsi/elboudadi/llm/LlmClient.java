// Fichier : src/main/java/ma/emsi/elboudadi/llm/LlmClient.java
package ma.emsi.elboudadi.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Client pour interagir avec le LLM (Gemini) en utilisant LangChain4j.
 */
// @ApplicationScoped // Décommentez si vous utilisez CDI
public class LlmClient {

    private String systemRole;
    private Assistant assistant;
    private ChatMemory chatMemory;

    public LlmClient() {
        // 1. Récupération de la clé API
        // CORRIGÉ: Changé "GEMINI-API-KEY" à "GEMINI_API_KEY"
        String apiKey = System.getenv("GEMINI-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("La variable d'environnement GEMINI-API-KEY doit être définie.");
        }

        // 2. Création du Modèle de Chat
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .build();

        // 3. Initialisation de la Mémoire (max 10 messages)
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 4. Création de l'Assistant via AiServices
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système pour l'assistant et réinitialise la mémoire.
     */
    public void setSystemRole(String newSystemRole) {
        if (!newSystemRole.equals(this.systemRole)) {
            // Le rôle a changé, on vide la mémoire pour commencer une nouvelle conversation
            this.chatMemory.clear();
            this.systemRole = newSystemRole;

            // Ajoute le SystemMessage à la mémoire pour le prendre en compte dans la prochaine requête
            this.chatMemory.add(SystemMessage.from(newSystemRole));
        }
    }

    /**
     * Envoie la requête de l'utilisateur au LLM et retourne la réponse.
     */
    public String sendMessage(String userMessage) {
        return assistant.chat(userMessage);
    }
}