package org.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalEnvironmentInitializerTest {

    @Test
    public void testCheckAndSetEnvVars() {
        String fakePineconeApiKey = "fakePineconeApiKey";
        String fakeOpenAIApiKey = "fakeOpenAiApiKey";

        LocalEnvironInitializer initializer = new LocalEnvironInitializer(fakePineconeApiKey, fakeOpenAIApiKey);
        assertEquals(initializer.getPineconeApiKey(), fakePineconeApiKey);
        assertEquals(initializer.getOpenAiApiKey(), fakeOpenAIApiKey);

        IllegalStateException pineconeEmptyStringException = assertThrows(IllegalStateException.class, () -> {
            new LocalEnvironInitializer("", fakeOpenAIApiKey);
        });
        assertEquals("Mandatory environment variable PINECONE_API_KEY is not set", pineconeEmptyStringException.getMessage());

        IllegalStateException pineconeNullException = assertThrows(IllegalStateException.class, () -> {
            new LocalEnvironInitializer(null, fakeOpenAIApiKey);
        });
        assertEquals("Mandatory environment variable PINECONE_API_KEY is not set", pineconeNullException.getMessage());

        IllegalStateException openAIEmptyStringException = assertThrows(IllegalStateException.class, () -> {
            new LocalEnvironInitializer(fakePineconeApiKey, "");
        });
        assertEquals("Mandatory environment variable OPENAI_API_KEY is not set", openAIEmptyStringException.getMessage());

        IllegalStateException openAINullException = assertThrows(IllegalStateException.class, () -> {
            new LocalEnvironInitializer(fakePineconeApiKey, null);
        });
        assertEquals("Mandatory environment variable OPENAI_API_KEY is not set", openAINullException.getMessage());
    }

}
