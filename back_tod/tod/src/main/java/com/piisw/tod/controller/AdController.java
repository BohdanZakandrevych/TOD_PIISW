package com.piisw.tod.controller;

import com.piisw.tod.dto.*;
import com.piisw.tod.model.AdStatus;
import com.piisw.tod.service.AdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdResponseDto create(@Valid @RequestBody AdCreateRequestDto request) {
        return adService.createAd(request);
    }

    @PutMapping("/{id}")
    public AdResponseDto update(@PathVariable Long id, @Valid @RequestBody AdUpdateRequestDto request) {
        return adService.updateAd(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adService.deleteAd(id);
    }

    @PatchMapping("/{id}/status")
    public AdResponseDto changeStatus(@PathVariable Long id, @Valid @RequestBody AdStatusUpdateRequestDto request) {
        return adService.changeStatus(id, request.status());
    }

    @GetMapping("/{id}")
    public AdResponseDto get(@PathVariable Long id) {
        return adService.getAd(id);
    }

    @GetMapping("/mine")
    public List<AdResponseDto> mine(@RequestParam(required = false) AdStatus status) {
        return adService.listMyAds(status);
    }

    @GetMapping("/preview/{token}")
    public AdResponseDto previewDraft(@PathVariable String token) {
        return adService.previewDraftByToken(token);
    }
}
