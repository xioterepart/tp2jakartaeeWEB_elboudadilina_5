package ma.emsi.elboudadi.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;

import java.time.Duration;

/**
 * Gère l'interaction avec l'API Gemini via LangChain4j.
 * C'est la couche métier qui est appelée par le Backing Bean (Bb).
 */
public class LlmClient {

    private final Assistant assistant;
    private final ChatMemory chatMemory;
    private final ChatModel chatModel;

    public LlmClient() {
        // 1. Récupérer la clé API
        String apiKey = System.getenv("GEMINI-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("GEMINI_API_KEY environment variable is not set.");
        }

        // 2. Créer le modèle de Chat
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash") // Modèle rapide et polyvalent
                .temperature(0.7) // Créativité
                .timeout(Duration.ofSeconds(15))
                .logRequestsAndResponses(true) // Utile pour le débogage
                .build();

        // 3. Configurer la mémoire de conversation
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 4. Créer l'Assistant (Service IA)
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système pour la conversation.
     * @param role Le rôle à appliquer.
     */
    public void setSystemRole(String role) {
        // Vider la mémoire car le rôle change le contexte entièrement
        chatMemory.clear();

        // Ajouter le nouveau rôle comme SystemMessage dans la mémoire
        chatMemory.add(SystemMessage.from(role));
    }

    /**
     * Envoie le message au LLM. LangChain4j gère l'ajout à la ChatMemory.
     * @param message Le message de l'utilisateur.
     * @return La réponse générée par le LLM.
     */
    public String sendMessage(String message) {
        return assistant.chat(message);
    }
}