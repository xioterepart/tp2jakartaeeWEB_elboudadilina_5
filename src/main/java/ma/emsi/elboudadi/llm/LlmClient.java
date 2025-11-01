// Fichier : src/main/java/ma/emsi/elboudadi/llm/LlmClient.java
package ma.emsi.elboudadi.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel; // Updated import
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel; // Updated import
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Client pour interagir avec le LLM (Gemini) en utilisant LangChain4j.
 * La portée est définie sur ApplicationScoped si vous utilisez CDI pour l'injection
 * dans le Backing Bean. Si vous n'utilisez pas CDI, vous l'instancierez manuellement.
 */
// @ApplicationScoped // Décommentez si vous utilisez CDI
public class LlmClient {

    private String systemRole;
    private Assistant assistant;
    private ChatMemory chatMemory;

    public LlmClient() {
        // 1. Récupération de la clé API
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("La variable d'environnement GEMINI_API_KEY doit être définie.");
        }

        // 2. Création du Modèle de Chat
        // Utilise GoogleAiGeminiChatModel à la place de GeminiChatModel
        ChatModel model = GoogleAiGeminiChatModel.builder() // Changed class
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
     * @param newSystemRole Le nouveau rôle à assigner.
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
     * @param userMessage Le message de l'utilisateur.
     * @return La réponse du LLM.
     */
    public String sendMessage(String userMessage) {
        // La méthode .chat(prompt) de l'instance Assistant est gérée par LangChain4j.
        // LangChain4j ajoute automatiquement l'historique (via chatMemory) avant d'envoyer
        // la requête au LLM.
        return assistant.chat(userMessage);
    }
}