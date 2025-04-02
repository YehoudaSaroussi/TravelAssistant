package com.travelassistant.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.travelassistant.dto.ConversationRequest;
import com.travelassistant.dto.ConversationResponse;
import com.travelassistant.model.Conversation;
import com.travelassistant.util.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.util.List;

public class ConversationServiceTest {

        @InjectMocks
        private ConversationService conversationService;

        @Mock
        private ContextManager contextManager;

        @Mock
        private GeminiService geminiService;

        @Mock
        private WeatherService weatherService;

        @Mock
        private CountryInfoService countryInfoService;

        @Mock
        private PromptEngineeringService promptEngineeringService;

        private static final String USER_ID = "user123";

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        public void testProcessConversation() {
                ConversationRequest request = new ConversationRequest(
                                USER_ID,
                                "Where should I travel next?",
                                null);

                Conversation mockConversation = new Conversation();
                when(contextManager.getConversation(USER_ID)).thenReturn(mockConversation);

                when(contextManager.getCurrentContext()).thenReturn("Previous context");
                when(promptEngineeringService.generateDestinationRecommendation(anyString()))
                                .thenReturn("Prompt for destinations");
                when(geminiService.generateResponse(anyString()))
                                .thenReturn("I recommend visiting Paris, Rome, or Tokyo based on your interests.");

                ConversationResponse response = conversationService.processConversation(request);

                assertNotNull(response);
                assertFalse(response.isError());
                assertTrue(response.getResponseMessage().contains("Paris") ||
                                response.getResponseMessage().contains("Rome") ||
                                response.getResponseMessage().contains("Tokyo"));

                verify(contextManager).getConversation(eq(USER_ID));
                verify(contextManager, times(2)).updateConversation(eq(USER_ID), any(Conversation.class));

                ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
                verify(contextManager, times(2)).updateConversation(eq(USER_ID), conversationCaptor.capture());
                List<Conversation> capturedConversations = conversationCaptor.getAllValues();
                Conversation finalConversation = capturedConversations.get(1);

                assertEquals(2, finalConversation.getMessages().size());
                assertEquals("user", finalConversation.getMessages().get(0).getSender());
                assertEquals("assistant", finalConversation.getMessages().get(1).getSender());
        }

        @Test
        public void testMaintainContext() {
                ConversationRequest request1 = new ConversationRequest(
                                USER_ID,
                                "What about packing suggestions?",
                                null);

                ConversationRequest request2 = new ConversationRequest(
                                USER_ID,
                                "And what about local attractions?",
                                null);

                Conversation conversation = new Conversation();
                when(contextManager.getConversation(USER_ID)).thenReturn(conversation);

                when(geminiService.generateResponse(anyString()))
                                .thenReturn("Pack light clothes and comfortable shoes.")
                                .thenReturn("You should visit the Eiffel Tower and Louvre Museum.");
                when(promptEngineeringService.generatePackingSuggestions(anyString())).thenReturn("Packing prompt");
                when(promptEngineeringService.generateLocalAttractions(anyString())).thenReturn("Attractions prompt");

                conversationService.processConversation(request1);
                ConversationResponse response2 = conversationService.processConversation(request2);

                assertNotNull(response2);
                assertTrue(response2.getResponseMessage().contains("Eiffel Tower") ||
                                response2.getResponseMessage().contains("attractions"));

                verify(contextManager, times(2)).getConversation(eq(USER_ID));
                verify(contextManager, times(4)).updateConversation(eq(USER_ID), any(Conversation.class));
        }

        @Test
        public void testContextPreservation() {
                Conversation conversation = new Conversation();
                conversation.addMessage("user", "I want to visit Paris");
                conversation.addMessage("assistant", "Paris is beautiful in the spring!");

                when(contextManager.getConversation(USER_ID)).thenReturn(conversation);
                when(geminiService.generateResponse(anyString()))
                                .thenReturn("The Eiffel Tower is a must-see attraction.");

                ConversationRequest request = new ConversationRequest(
                                USER_ID,
                                "What should I see there?",
                                null);

                ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

                conversationService.processConversation(request);

                verify(geminiService).generateResponse(promptCaptor.capture());
                String capturedPrompt = promptCaptor.getValue();

                assertTrue(capturedPrompt.contains("Paris is beautiful"));
                assertTrue(capturedPrompt.contains("What should I see there?"));
        }

        @Test
        public void testErrorHandling() {
                ConversationRequest request = new ConversationRequest(
                                USER_ID,
                                "Invalid query",
                                null);

                Conversation conversation = new Conversation();
                when(contextManager.getConversation(USER_ID)).thenReturn(conversation);
                when(geminiService.generateResponse(anyString())).thenThrow(new RuntimeException("API Error"));

                ConversationResponse response = conversationService.processConversation(request);

                assertNotNull(response);
                assertTrue(response.isError());
                assertEquals("I'm sorry, I couldn't process your request at the moment.",
                                response.getResponseMessage());
        }
}