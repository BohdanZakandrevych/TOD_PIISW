package com.piisw.tod.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.piisw.tod.BaseIntegrationTest;
import com.piisw.tod.dto.AdCreateRequestDto;
import com.piisw.tod.dto.AdStatusUpdateRequestDto;
import com.piisw.tod.dto.AdUpdateRequestDto;
import com.piisw.tod.model.AdStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdControllerTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAd() throws Exception {
        String token = getAuthToken();
        AdCreateRequestDto request = new AdCreateRequestDto(
                "Test Ad Title",
                "Test description for the ad.",
                List.of("http://example.com/img1.jpg"),
                List.of("test-tag"),
                List.of()
        );

        mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Ad Title"))
                .andExpect(jsonPath("$.status").value(AdStatus.DRAFT.name()));
    }

    @Test
    void shouldGetMyAds() throws Exception {
        String token = getAuthToken();

        mockMvc.perform(get("/api/ads/mine")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldChangeAdStatus() throws Exception {
        String token = getAuthToken();

        // 1. Create Ad First
        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Status Test Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(createResponse);
        Long adId = rootNode.get("id").asLong();

        // 2. Change Status to PUBLISHED
        AdStatusUpdateRequestDto statusRequest = new AdStatusUpdateRequestDto(AdStatus.PUBLISHED);

        mockMvc.perform(patch("/api/ads/" + adId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void shouldUpdateAd() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Update Test Ad", "Desc", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        AdUpdateRequestDto updateRequest = new AdUpdateRequestDto(
                "Updated Title", "Updated Description", List.of(), List.of(), List.of()
        );

        mockMvc.perform(put("/api/ads/" + adId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void shouldDeleteAd() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Delete Test Ad", "Desc", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/ads/" + adId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify deletion throws 404
        mockMvc.perform(get("/api/ads/" + adId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreviewDraftByToken() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Preview Test Ad", "Desc", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        String secretToken = objectMapper.readTree(createResponse).get("secretPreviewToken").asText();

        mockMvc.perform(get("/api/ads/preview/" + secretToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Preview Test Ad"));
    }
}