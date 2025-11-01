// Fichier : src/main/java/ma/emsi/elboudadi/util/Message.java
package ma.emsi.elboudadi.util;

// Importation nécessaire pour l'annotation @Named (si vous l'utilisez)
import jakarta.inject.Named;

public class Message {

    private String sender; // "User" ou "LLM"
    private String content;

    // Constructeur
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    // Getters and Setters (nécessaires pour l'affichage en JSF)
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}