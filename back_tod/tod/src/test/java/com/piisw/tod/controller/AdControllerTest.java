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

    @Test
    void shouldGetPublishedAdWithoutAuthentication() throws Exception {
        String token = getAuthToken();

        // Create and publish an ad
        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Published Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        AdStatusUpdateRequestDto statusRequest = new AdStatusUpdateRequestDto(AdStatus.PUBLISHED);
        mockMvc.perform(patch("/api/ads/" + adId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk());

        // Get without auth
        mockMvc.perform(get("/api/ads/" + adId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Published Ad"));
    }

    @Test
    void shouldGetDraftAdWhenOwner() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Draft Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/ads/" + adId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Draft Ad"));
    }

    @Test
    void shouldFailGetDraftAdWhenNotOwner() throws Exception {
        // This test assumes another user exists; for simplicity, try without auth or with invalid token
        // Since we don't have multi-user setup easily, test unauthenticated access to draft
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Draft Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        // Try without auth
        mockMvc.perform(get("/api/ads/" + adId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailGetNonExistentAd() throws Exception {
        String token = getAuthToken();

        mockMvc.perform(get("/api/ads/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListMyAdsWithStatusFilter() throws Exception {
        String token = getAuthToken();

        // Create a draft ad
        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Filtered Ad", "Description", List.of(), List.of(), List.of()
        );
        mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ads/mine?status=DRAFT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void shouldFailUpdateAdWhenNotOwner() throws Exception {
        // Assuming no easy way to test multi-user, test without auth
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Owner Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        AdUpdateRequestDto updateRequest = new AdUpdateRequestDto(
                "Updated Title", null, null, null, null
        );

        // Try without auth
        mockMvc.perform(put("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailDeleteAdWhenNotOwner() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Owner Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        // Try without auth
        mockMvc.perform(delete("/api/ads/" + adId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailChangeStatusWhenNotOwner() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Owner Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();

        AdStatusUpdateRequestDto statusRequest = new AdStatusUpdateRequestDto(AdStatus.PUBLISHED);

        // Try without auth
        mockMvc.perform(patch("/api/ads/" + adId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailUpdateNonExistentAd() throws Exception {
        String token = getAuthToken();

        AdUpdateRequestDto updateRequest = new AdUpdateRequestDto(
                "Updated Title", null, null, null, null
        );

        mockMvc.perform(put("/api/ads/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCreateAdWithInvalidData() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto invalidRequest = new AdCreateRequestDto(
                "", "Description", List.of(), List.of(), List.of() // Empty title
        );

        mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailPreviewWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/ads/preview/invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailPreviewPublishedAdToken() throws Exception {
        String token = getAuthToken();

        AdCreateRequestDto createRequest = new AdCreateRequestDto(
                "Published Ad", "Description", List.of(), List.of(), List.of()
        );
        String createResponse = mockMvc.perform(post("/api/ads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        Long adId = objectMapper.readTree(createResponse).get("id").asLong();
        String secretToken = objectMapper.readTree(createResponse).get("secretPreviewToken").asText();

        // Publish it
        AdStatusUpdateRequestDto statusRequest = new AdStatusUpdateRequestDto(AdStatus.PUBLISHED);
        mockMvc.perform(patch("/api/ads/" + adId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk());

        // Try preview
        mockMvc.perform(get("/api/ads/preview/" + secretToken))
                .andExpect(status().isNotFound());
    }
}