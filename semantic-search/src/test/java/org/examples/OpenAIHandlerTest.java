package org.examples;

import com.theokanning.openai.model.Model;
import com.theokanning.openai.service.OpenAiService;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OpenAIHandlerTest {

    @Test
    public void testListModels() {
        OpenAiService mockService = mock(OpenAiService.class);

        Model testModelOne = new Model();
        testModelOne.setId("text-embedding-3-small");
        Model testModelTwo = new Model();
        testModelTwo.setId("text-embedding-3-large");

        List<Model> mockModels = Arrays.asList(testModelOne, testModelTwo);

        when(mockService.listModels()).thenReturn(mockModels);

        OpenAIHandler handler = new OpenAIHandler(mockService);
        List<String> modelIds = handler.listModels();

        verify(mockService, times(2)).listModels(); // Called in OpenAIHandler constructor too

        assertEquals(modelIds.size(), 2, "Expected two models in list");
        assertTrue(modelIds.contains("text-embedding-3-small"), "List should contain text-embedding-3-small model");
        assertTrue(modelIds.contains("text-embedding-3-large"), "List should contain text-embedding-3-large model");
    }

    @Test
    public void testListModelsWithIllegalModel() {
        OpenAiService mockService = mock(OpenAiService.class);

        Model testModelOne = new Model();
        testModelOne.setId("blahblah-some-madeup-model");
        Model testModelTwo = new Model();
        testModelTwo.setId("text-embedding-3-large");

        List<Model> mockModels = Arrays.asList(testModelOne, testModelTwo);

        when(mockService.listModels()).thenReturn(mockModels);

        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIHandler handler = new OpenAIHandler(mockService);
        }, "OpenAI model provided is invalid.");
    }







} // ending brace
