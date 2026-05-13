package com.piisw.tod.controller;

import com.piisw.tod.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SearchControllerTest extends BaseIntegrationTest {

    @Test
    void shouldSearchAds() throws Exception {
        mockMvc.perform(get("/api/search/ads?text=laptop&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldSuggestTags() throws Exception {
        mockMvc.perform(get("/api/search/tags/suggest?prefix=elec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("electronics"));
    }

    @Test
    void shouldReturnEmptySearchResults() throws Exception {
        mockMvc.perform(get("/api/search/ads?text=nonexistent&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldHandleLargePageSize() throws Exception {
        mockMvc.perform(get("/api/search/ads?page=0&size=1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldHandleNegativePage() throws Exception {
        mockMvc.perform(get("/api/search/ads?page=-1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldSuggestTagsWithNoMatches() throws Exception {
        mockMvc.perform(get("/api/search/tags/suggest?prefix=xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}