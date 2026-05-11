package com.piisw.tod.service;

import com.piisw.tod.dto.AdResponseDto;
import com.piisw.tod.dto.ContactInfoDto;
import com.piisw.tod.dto.TagDto;
import com.piisw.tod.dto.TagSuggestionDto;
import com.piisw.tod.model.Ad;
import com.piisw.tod.repository.AdRepository;
import com.piisw.tod.repository.TagRepository;
import com.piisw.tod.repository.spec.AdSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchService {

    private final AdRepository adRepository;
    private final TagRepository tagRepository;

    public SearchService(AdRepository adRepository, TagRepository tagRepository) {
        this.adRepository = adRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdResponseDto> searchPublishedAds(String text, List<String> tagNames, Pageable pageable) {
        Specification<Ad> spec = Specification.where(AdSpecifications.isPublished())
                .and(AdSpecifications.textInTitleOrDescription(text))
                .and(AdSpecifications.hasAnyTagName(tagNames));

        return adRepository.findAll(spec, pageable)
                .map(ad -> new AdResponseDto(
                        ad.getId(),
                        ad.getTitle(),
                        ad.getDescription(),
                        ad.getStatus(),
                        ad.getCreatedAt(),
                        ad.getUpdatedAt(),
                        ad.getAuthor() != null ? ad.getAuthor().getId() : null,
                        ad.getAuthor() != null ? ad.getAuthor().getEmail() : null,
                        null,
                        ad.getImageUrls() != null ? List.copyOf(ad.getImageUrls()) : List.of(),
                        ad.getTags() != null ? ad.getTags().stream().map(t -> new TagDto(t.getId(), t.getName())).toList() : List.of(),
                        ad.getContactInfos() != null ? ad.getContactInfos().stream().map(ci -> new ContactInfoDto(ci.getId(), ci.getType(), ci.getValue())).toList() : List.of()
                ));
    }

    @Transactional(readOnly = true)
    public List<TagSuggestionDto> suggestTags(String prefix) {
        String p = prefix == null ? "" : prefix.trim();
        return tagRepository.findByNameStartingWithOrderByUsageCount(p).stream()
                .map(t -> new TagSuggestionDto(t.getId(), t.getName()))
                .toList();
    }
}
