package com.piisw.tod.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.piisw.tod.BaseIntegrationTest;
import com.piisw.tod.dto.MessageReplyRequestDto;
import com.piisw.tod.dto.MessageSendRequestDto;
import com.piisw.tod.model.User;
import com.piisw.tod.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MessageControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSendMessage() throws Exception {
        String token = getAuthToken();
        // John Smith sends message to Sarah Jones (pre-filled by DataFiller)
        User receiver = userRepository.findByEmail("sarah.jones@example.com").orElseThrow();

        MessageSendRequestDto request = new MessageSendRequestDto("Hello Sarah!", receiver.getId(), null);

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello Sarah!"));
    }

    @Test
    void shouldGetInbox() throws Exception {
        String token = getAuthToken();

        mockMvc.perform(get("/api/messages/inbox")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray()); // Content refers to Pageable payload content
    }

    @Test
    void shouldReplyToMessage() throws Exception {
        String token = getAuthToken();

        // Fetch Inbox populated by DataFiller for John Smith
        String inboxResponse = mockMvc.perform(get("/api/messages/inbox")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(inboxResponse);
        JsonNode firstMessage = root.get("content").get(0);

        assertNotNull(firstMessage, "Inbox should not be empty for John Smith");
        Long parentMessageId = firstMessage.get("id").asLong();

        MessageReplyRequestDto replyRequest = new MessageReplyRequestDto("This is a reply to the message");

        mockMvc.perform(post("/api/messages/" + parentMessageId + "/reply")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("This is a reply to the message"))
                .andExpect(jsonPath("$.parentMessageId").value(parentMessageId));
    }
}