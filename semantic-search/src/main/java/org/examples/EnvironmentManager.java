package org.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentManager {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManager.class);
    private String pineconeApiKey;
    private String openAiApiKey;

    public EnvironmentManager() {
        checkAndSetEnvVars("PINECONE_API_KEY", "OPENAI_API_KEY");
    }

    private void checkAndSetEnvVars(String... envVars) {
        boolean allVarsSet = true;

        for (String envVar : envVars) {
            String value = System.getenv(envVar);
            if (value == null || value.isEmpty()) {
                logger.error("Mandatory environment variable {} is not set", envVar);
                allVarsSet = false;
            } else {
                setEnvVarField(envVar, value);
            }
        }

        if (!allVarsSet) {
            logger.error("One or more mandatory environment variables are missing. Exiting program.");
            System.exit(1); // Exit with an error code
        } else {
            logger.info("All mandatory environment variables are set.");
        }
    }

    private void setEnvVarField(String envVar, String value) {
        switch (envVar) {
            case "PINECONE_API_KEY":
                this.pineconeApiKey = value;
                break;
            case "OPENAI_API_KEY":
                this.openAiApiKey = value;
                break;
            default:
                logger.warn("Unknown environment variable: {}", envVar);
        }
    }

    public String getPineconeApiKey() {
        return pineconeApiKey;
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
