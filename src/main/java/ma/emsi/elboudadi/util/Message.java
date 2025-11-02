package ma.emsi.elboudadi.util;

import java.io.Serializable;

/**
 * Représente un message unique dans la conversation.
 */
public class Message implements Serializable {
    private final String sender; // "User", "LLM", ou "System Error"
    private final String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    // Vous pouvez aussi ajouter une méthode toString() pour le débogage si nécessaire
}