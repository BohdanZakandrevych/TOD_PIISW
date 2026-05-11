package com.piisw.tod.controller;

import com.piisw.tod.dto.AdResponseDto;
import com.piisw.tod.dto.TagSuggestionDto;
import com.piisw.tod.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/ads")
    public Page<AdResponseDto> searchAds(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return searchService.searchPublishedAds(text, tags, pageable);
    }

    @GetMapping("/tags/suggest")
    public List<TagSuggestionDto> suggestTags(@RequestParam String prefix) {
        return searchService.suggestTags(prefix);
    }
}
