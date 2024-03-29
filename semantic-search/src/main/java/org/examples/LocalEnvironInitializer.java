package org.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalEnvironInitializer {
    private static final Logger logger = LoggerFactory.getLogger(LocalEnvironInitializer.class);
    private final String pineconeApiKey;
    private final String openAiApiKey;

    // Default constructor that implicitly calls the 2nd constructor through the "this" keyword
    public LocalEnvironInitializer() {
        this(System.getenv("PINECONE_API_KEY"), System.getenv("OPENAI_API_KEY"));
    }

    // 2nd constructor
    public LocalEnvironInitializer(String pineconeApiKey, String openAiApiKey) {
        this.pineconeApiKey = validateEnvVar(pineconeApiKey, "PINECONE_API_KEY");
        this.openAiApiKey = validateEnvVar(openAiApiKey, "OPENAI_API_KEY");
    }

    private String validateEnvVar(String value, String envVarName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Mandatory environment variable " + envVarName + " is not set");
        }
        return value;
    }

    public String getPineconeApiKey() {
        return pineconeApiKey;
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
