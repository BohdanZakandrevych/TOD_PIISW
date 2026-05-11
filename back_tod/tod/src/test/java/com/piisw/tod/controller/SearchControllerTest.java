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
}