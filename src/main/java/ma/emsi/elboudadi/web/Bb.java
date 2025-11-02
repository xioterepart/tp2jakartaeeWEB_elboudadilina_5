package ma.emsi.elboudadi.web;

import ma.emsi.elboudadi.llm.LlmClient;
import ma.emsi.elboudadi.util.Message;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backing Bean (Contrôleur) pour l'interface de chat JSF.
 */
@Named("bb")
@ViewScoped
public class Bb implements Serializable {

    private String question;
    private List<Message> conversation;
    // Remarque : LlmClient DOIT être géré comme un bean CDI ou injecté,
    // mais ici on l'instancie manuellement pour simplifier la démo.
    private LlmClient llmClient;

    private String selectedSystemRole;
    private String lastSystemRoleSent = null;

    private List<String> availableRoles;

    @PostConstruct
    public void init() {
        // Définition des rôles disponibles (TRADUCTION APPLIQUÉE)
        availableRoles = new ArrayList<>();
        availableRoles.add("Tu es un assistant serviable et amical.");
        availableRoles.add("Tu es un poète spirituel, réponds à toutes les questions en vers.");
        availableRoles.add("Tu es un relecteur technique strict, concentré sur le code Java.");
        availableRoles.add("Tu es un traducteur du français vers l'anglais.");

        // Valeur par défaut
        this.selectedSystemRole = availableRoles.get(0);

        try {
            this.llmClient = new LlmClient(); // Instancie LlmClient
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            this.conversation = new ArrayList<>();
            this.conversation.add(new Message("System Error", "LLM Client not initialized. Check your GEMINI_API_KEY environment variable."));
            this.llmClient = null;
        }

        if (this.conversation == null) {
            this.conversation = new ArrayList<>();
        }
    }

    /**
     * Réinitialise la conversation et l'état de la page.
     */
    public String nouveauChat() {
        this.conversation.clear();
        this.question = null;
        // Permet de choisir un nouveau rôle
        this.lastSystemRoleSent = null;
        return null;
    }

    /**
     * Envoie la question au LLM, gère le rôle système et met à jour l'historique.
     */
    public void envoyer() {
        if (llmClient == null || question == null || question.trim().isEmpty()) {
            return;
        }

        String userQuestion = question.trim();

        // 1. Initialiser ou changer le Rôle Système
        if (lastSystemRoleSent == null || !getRoleSysteme().equals(lastSystemRoleSent)) {
            llmClient.setSystemRole(getRoleSysteme());
            lastSystemRoleSent = getRoleSysteme();
        }

        try {
            // 2. Ajouter la question à l'historique
            conversation.add(new Message("User", userQuestion));

            // 3. Envoyer la question au LLM (LangChain4j gère l'historique via ChatMemory)
            String llmResponse = llmClient.sendMessage(userQuestion);

            // 4. Ajouter la réponse à l'historique
            conversation.add(new Message("LLM", llmResponse));

        } catch (Exception e) {
            conversation.add(new Message("System Error", "Error communicating with LLM: " + e.getMessage()));
            e.printStackTrace();
        } finally {
            // 5. Réinitialiser le champ de saisie
            question = null;
        }
    }

    // --- Getters et Setters ---

    public String getRoleSysteme() {
        return selectedSystemRole;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.selectedSystemRole = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return lastSystemRoleSent == null;
    }

    public List<String> getRolesSysteme() {
        return availableRoles;
    }

    // Renvoie la dernière réponse pour la zone "reponse"
    public String getReponse() {
        if (conversation.isEmpty()) {
            return "";
        }
        for (int i = conversation.size() - 1; i >= 0; i--) {
            Message msg = conversation.get(i);
            if ("LLM".equals(msg.getSender())) {
                return msg.getContent();
            }
        }
        return "";
    }

    // Renvoie la conversation complète pour la zone "conversation"
    public String getConversation() {
        return conversation.stream()
                .map(msg -> msg.getSender() + ": " + msg.getContent())
                .collect(Collectors.joining("\n---\n"));
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}