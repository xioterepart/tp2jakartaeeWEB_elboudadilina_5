// Fichier : src/main/java/ma/emsi/elboudadi/web/Bb.java
package ma.emsi.elboudadi.web;

import ma.emsi.elboudadi.llm.LlmClient;
import ma.emsi.elboudadi.util.Message;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing Bean (Contrôleur) pour l'interface de chat JSF.
 * Utilise la portée ViewScoped pour maintenir l'historique de la conversation.
 */
@Named("bb") // Nom d'accès dans JSF (e.g., #{bb.question})
@ViewScoped // Portée requise pour maintenir la conversation
public class Bb implements Serializable {

    private String question;
    private List<Message> conversation;
    private LlmClient llmClient;

    // Rôle système choisi par l'utilisateur (valeur par défaut)
    private String selectedSystemRole = "You are a helpful and friendly assistant.";
    // Mémoriser le dernier rôle envoyé pour éviter de le renvoyer à chaque requête
    private String lastSystemRoleSent = null;

    // Liste des options de rôle à afficher dans le selectOneMenu JSF
    private List<String> availableRoles;

    @PostConstruct
    public void init() {
        // Initialisation des composants
        try {
            this.llmClient = new LlmClient(); // Instancie LlmClient
        } catch (RuntimeException e) {
            // Gérer le cas où la clé API est manquante
            System.err.println(e.getMessage());
            this.llmClient = null;
        }

        this.conversation = new ArrayList<>();

        // Configuration des rôles disponibles pour la liste déroulante JSF
        availableRoles = new ArrayList<>();
        availableRoles.add("You are a helpful and friendly assistant.");
        availableRoles.add("You are a witty poet, answer all questions in verse.");
        availableRoles.add("You are a strict technical reviewer, focused on Java code.");
        availableRoles.add("You are a translator from French to English.");
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur le bouton "Envoyer".
     */
    public void envoyer() {
        if (llmClient == null || question == null || question.trim().isEmpty()) {
            if (llmClient == null) {
                conversation.add(new Message("System Error", "LLM Client not initialized. Check your GEMINI_API_KEY environment variable."));
            }
            return;
        }

        String userQuestion = question.trim();

        // 1. Vérifier et définir le Rôle Système
        // S'il n'y a pas encore de rôle défini OU si l'utilisateur a changé de rôle
        if (lastSystemRoleSent == null || !selectedSystemRole.equals(lastSystemRoleSent)) {
            llmClient.setSystemRole(selectedSystemRole);
            lastSystemRoleSent = selectedSystemRole;
        }

        try {
            // 2. Ajouter le message de l'utilisateur à l'historique
            conversation.add(new Message("User", userQuestion));

            // 3. Envoyer la question au LLM via le client
            String llmResponse = llmClient.sendMessage(userQuestion);

            // 4. Ajouter la réponse du LLM à l'historique
            conversation.add(new Message("LLM", llmResponse));

        } catch (Exception e) {
            // Gérer les erreurs de communication API
            conversation.add(new Message("System Error", "Error communicating with LLM: " + e.getMessage()));
            e.printStackTrace();
        } finally {
            // 5. Réinitialiser le champ de saisie
            question = null;
        }
    }

    // --- Getters et Setters pour la Vue JSF ---

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Message> getConversation() {
        return conversation;
    }

    public String getSelectedSystemRole() {
        return selectedSystemRole;
    }

    public void setSelectedSystemRole(String selectedSystemRole) {
        this.selectedSystemRole = selectedSystemRole;
    }

    public List<String> getAvailableRoles() {
        return availableRoles;
    }
}