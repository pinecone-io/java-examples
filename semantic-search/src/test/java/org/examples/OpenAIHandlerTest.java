package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OpenAIHandlerTest {

    private OpenAiService mockService;
    private OpenAIHandler handler;
    private Model testModelTwo;

    @BeforeEach
    public void setUp() {
        mockService = mock(OpenAiService.class);

        Model testModelOne = new Model();
        testModelOne.setId("text-embedding-3-small");
        testModelTwo = new Model();
        testModelTwo.setId("text-embedding-3-large");
        List<Model> mockModels = Arrays.asList(testModelOne, testModelTwo);
        when(mockService.listModels()).thenReturn(mockModels);

        handler = new OpenAIHandler(mockService);
    }

    @Test
    public void testListModelsContainsExpectedNumberAndContents() {
        List<String> modelIds = this.handler.listModels();
        verify(this.mockService, times(2)).listModels(); // Called in OpenAIHandler constructor too
        assertEquals(modelIds.size(), 2, "Expected two models in list");
        assertTrue(modelIds.contains("text-embedding-3-small"), "List should contain text-embedding-3-small model");
        assertTrue(modelIds.contains("text-embedding-3-large"), "List should contain text-embedding-3-large model");
    }

    @Test
    public void testListModelsWithIllegalModel() {
        Model testModelOne = new Model();
        testModelOne.setId("blahblah-some-madeup-model");
        List<Model> mockModels = Arrays.asList(testModelOne, this.testModelTwo);
        when(this.mockService.listModels()).thenReturn(mockModels);

        assertThrows(IllegalArgumentException.class, () -> {
            this.handler = new OpenAIHandler(this.mockService);
        }, "OpenAI model provided is invalid.");
    }

    @Test
    public void testEmbedManyAndEmbedOne() {
        EmbeddingResult mockEmbeddingResult = new EmbeddingResult();
        Embedding mockEmbeddingContents = new Embedding();
        mockEmbeddingContents.setEmbedding(Arrays.asList(1.0, 2.0, 3.0));
        mockEmbeddingResult.setData(Arrays.asList(mockEmbeddingContents));
        when(this.mockService.createEmbeddings(any())).thenReturn(mockEmbeddingResult);

        // Call embedMany with a list of a single string
        List<List<Float>> resultEmbedMany = this.handler.embedMany(Arrays.asList("a single test string"));
        List<Float> resultEmbedOne = this.handler.embedOne("a single test string");

        // Verify that createEmbeddings was called twice: once for embedMany, once for embedOnce
        verify(this.mockService, times(2)).createEmbeddings(any());

        // Assert that resultEmbedMany is as expected
        assertEquals(1, resultEmbedMany.size(), "Expected one embedding in the result");
        assertEquals(3, resultEmbedMany.get(0).size(), "Expected three values in the embedding itself");
        assertEquals(Arrays.asList(1.0f, 2.0f, 3.0f), resultEmbedMany.get(0), "Provided embedding values do not match expected" +
                " embedding values");

        // Assert that resultEmbedOne is as expected
        assertEquals(3, resultEmbedOne.size(), "Expected three values in the embedding itself");
        assertEquals(Arrays.asList(1.0f, 2.0f, 3.0f), resultEmbedOne, "Provided embedding values do not match expected" +
                " embedding values");

        // Call embedMany with an empty list
        assertThrows(IllegalArgumentException.class, () -> {
            handler.embedMany(Arrays.asList(""));
        }, "Input list cannot contain empty strings.");

        // Call embedMany with a list containing an empty string
        assertThrows(IllegalArgumentException.class, () -> {
            handler.embedMany(Arrays.asList("a", ""));
        }, "Input list cannot contain empty strings.");

        // Call embedOne with an empty string
        assertThrows(IllegalArgumentException.class, () -> {
            handler.embedOne("");
        }, "User query cannot be empty.");

    }

}
