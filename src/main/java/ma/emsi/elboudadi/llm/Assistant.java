package ma.emsi.elboudadi.llm;

// Cette interface définit l'interaction simple de chat pour LangChain4j.
public interface Assistant {

    /**
     * Envoie un prompt au LLM et retourne la réponse.
     * LangChain4j implémentera automatiquement cette méthode.
     * @param prompt La question de l'utilisateur.
     * @return La réponse du LLM.
     */
    String chat(String prompt);
}