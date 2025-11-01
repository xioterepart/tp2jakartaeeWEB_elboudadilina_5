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
import java.util.stream.Collectors;

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
            // Ajoute un message d'erreur initial pour l'utilisateur
            this.conversation = new ArrayList<>();
            this.conversation.add(new Message("System Error", "LLM Client not initialized. Check your GEMINI_API_KEY environment variable."));
            this.llmClient = null;
        }

        if (this.conversation == null) {
            this.conversation = new ArrayList<>();
        }

        // Configuration des rôles disponibles pour la liste déroulante JSF
        availableRoles = new ArrayList<>();
        availableRoles.add("You are a helpful and friendly assistant.");
        availableRoles.add("You are a witty poet, answer all questions in verse.");
        availableRoles.add("You are a strict technical reviewer, focused on Java code.");
        availableRoles.add("You are a translator from French to English.");
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur le bouton "Nouveau chat".
     * CORRIGÉ: Ajouté la méthode manquante.
     */
    public String nouveauChat() {
        this.conversation.clear();
        this.question = null;
        // Réinitialise le rôle système pour qu'il soit renvoyé lors de la prochaine requête.
        this.lastSystemRoleSent = null;
        return null; // Reste sur la même page
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur le bouton "Envoyer".
     */
    public void envoyer() {
        if (llmClient == null) {
            return;
        }
        if (question == null || question.trim().isEmpty()) {
            return;
        }

        String userQuestion = question.trim();

        // 1. Vérifier et définir le Rôle Système
        // CORRIGÉ: Utilise getRoleSysteme() au lieu de l'ancienne méthode getSelectedSystemRole()
        if (lastSystemRoleSent == null || !getRoleSysteme().equals(lastSystemRoleSent)) {
            llmClient.setSystemRole(getRoleSysteme());
            lastSystemRoleSent = getRoleSysteme();
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

    /**
     * Getter/Setter utilisé par l'expression JSF #{bb.roleSysteme}
     * CORRIGÉ: Ajouté pour mapper la propriété JSF `roleSysteme` au champ `selectedSystemRole`.
     */
    public String getRoleSysteme() {
        return selectedSystemRole;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.selectedSystemRole = roleSysteme;
    }

    /**
     * Renvoie le flag pour désactiver le menu après la première requête.
     * CORRIGÉ: Ajouté la méthode manquante isRoleSystemeChangeable().
     */
    public boolean isRoleSystemeChangeable() {
        // Le rôle est changeable tant qu'aucune requête n'a été envoyée
        return lastSystemRoleSent == null;
    }

    /**
     * Getter utilisé par l'expression JSF #{bb.rolesSysteme}
     * CORRIGÉ: Ajouté pour mapper la propriété JSF `rolesSysteme` au champ `availableRoles`.
     */
    public List<String> getRolesSysteme() {
        return availableRoles;
    }

    /**
     * Renvoie la dernière réponse de l'LLM, formatée pour l'affichage dans 'reponse'.
     * CORRIGÉ: Ajouté la méthode manquante getReponse().
     */
    public String getReponse() {
        if (conversation.isEmpty()) {
            return "";
        }
        // Cherche le dernier message du LLM
        for (int i = conversation.size() - 1; i >= 0; i--) {
            Message msg = conversation.get(i);
            if ("LLM".equals(msg.getSender())) {
                return msg.getContent();
            }
        }
        return "";
    }

    /**
     * Renvoie la conversation entière, formatée pour l'affichage dans 'conversation'.
     * CORRIGÉ: Ajouté la méthode manquante getConversation() qui retourne un String.
     */
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