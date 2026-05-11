package com.piisw.tod.service;

import com.piisw.tod.dto.*;
import com.piisw.tod.model.*;
import com.piisw.tod.repository.AdRepository;
import com.piisw.tod.repository.ContactInfoRepository;
import com.piisw.tod.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdService {

    private final AdRepository adRepository;
    private final TagRepository tagRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final CurrentUserService currentUserService;

    public AdService(
            AdRepository adRepository,
            TagRepository tagRepository,
            ContactInfoRepository contactInfoRepository,
            CurrentUserService currentUserService
    ) {
        this.adRepository = adRepository;
        this.tagRepository = tagRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AdResponseDto createAd(AdCreateRequestDto request) {
        User current = currentUserService.requireCurrentUser();

        Ad ad = new Ad();
        ad.setTitle(request.title());
        ad.setDescription(request.description());
        ad.setStatus(AdStatus.DRAFT);
        ad.setAuthor(current);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());
        ad.setSecretPreviewToken(UUID.randomUUID().toString());

        if (request.imageUrls() != null) {
            ad.setImageUrls(new ArrayList<>(request.imageUrls()));
        }

        ad.setTags(resolveTags(request.tagNames()));
        ad.setContactInfos(resolveContactInfos(current, request.contactInfoIds()));

        Ad saved = adRepository.save(ad);
        return toDto(saved, true);
    }

    @Transactional
    public AdResponseDto updateAd(Long adId, AdUpdateRequestDto request) {
        User current = currentUserService.requireCurrentUser();
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));

        requireOwner(current, ad);

        if (request.title() != null) {
            ad.setTitle(request.title());
        }
        if (request.description() != null) {
            ad.setDescription(request.description());
        }
        if (request.imageUrls() != null) {
            ad.setImageUrls(new ArrayList<>(request.imageUrls()));
        }
        if (request.tagNames() != null) {
            ad.setTags(resolveTags(request.tagNames()));
        }
        if (request.contactInfoIds() != null) {
            ad.setContactInfos(resolveContactInfos(current, request.contactInfoIds()));
        }

        ad.setUpdatedAt(LocalDateTime.now());

        return toDto(adRepository.save(ad), true);
    }

    @Transactional
    public AdResponseDto changeStatus(Long adId, AdStatus newStatus) {
        User current = currentUserService.requireCurrentUser();
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));

        requireOwner(current, ad);

        ad.setStatus(newStatus);
        ad.setUpdatedAt(LocalDateTime.now());

        if (newStatus == AdStatus.DRAFT && (ad.getSecretPreviewToken() == null || ad.getSecretPreviewToken().isBlank())) {
            ad.setSecretPreviewToken(UUID.randomUUID().toString());
        }

        return toDto(adRepository.save(ad), true);
    }

    @Transactional
    public void deleteAd(Long adId) {
        User current = currentUserService.requireCurrentUser();
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        requireOwner(current, ad);
        adRepository.delete(ad);
    }

    @Transactional(readOnly = true)
    public AdResponseDto getAd(Long adId) {
        User maybeCurrent = null;
        try {
            maybeCurrent = currentUserService.requireCurrentUser();
        } catch (ResponseStatusException ex) {
            // unauthenticated - allowed for published ads
        }

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));

        boolean isOwner = maybeCurrent != null && Objects.equals(ad.getAuthor().getId(), maybeCurrent.getId());

        if (ad.getStatus() == AdStatus.PUBLISHED) {
            return toDto(ad, false);
        }

        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can view non-published ads");
        }

        return toDto(ad, true);
    }

    @Transactional(readOnly = true)
    public List<AdResponseDto> listMyAds(AdStatus status) {
        User current = currentUserService.requireCurrentUser();
        List<Ad> ads;
        if (status == null) {
            ads = adRepository.findByAuthorId(current.getId());
        } else {
            ads = adRepository.findByAuthorIdAndStatus(current.getId(), status);
        }

        return ads.stream().map(a -> toDto(a, true)).toList();
    }

    @Transactional(readOnly = true)
    public AdResponseDto previewDraftByToken(String token) {
        Ad ad = adRepository.findBySecretPreviewToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preview token not found"));

        if (ad.getStatus() != AdStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Preview token not valid for non-draft ads");
        }

        return toDto(ad, true);
    }

    private void requireOwner(User current, Ad ad) {
        if (ad.getAuthor() == null || ad.getAuthor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ad has no author");
        }
        if (!Objects.equals(ad.getAuthor().getId(), current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can modify ad");
        }
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null) {
            return new HashSet<>();
        }

        Set<Tag> result = new HashSet<>();
        for (String raw : tagNames) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String name = raw.trim();
            Tag tag = tagRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            result.add(tag);
        }
        return result;
    }

    private Set<ContactInfo> resolveContactInfos(User current, List<Long> contactInfoIds) {
        if (contactInfoIds == null) {
            return new HashSet<>();
        }

        Set<ContactInfo> result = new HashSet<>();
        for (Long id : contactInfoIds) {
            if (id == null) {
                continue;
            }
            ContactInfo ci = contactInfoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ContactInfo not found: " + id));

            if (ci.getUser() == null || !Objects.equals(ci.getUser().getId(), current.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ContactInfo does not belong to current user");
            }
            result.add(ci);
        }
        return result;
    }

    private static AdResponseDto toDto(Ad ad, boolean includeSecretToken) {
        return new AdResponseDto(
                ad.getId(),
                ad.getTitle(),
                ad.getDescription(),
                ad.getStatus(),
                ad.getCreatedAt(),
                ad.getUpdatedAt(),
                ad.getAuthor() != null ? ad.getAuthor().getId() : null,
                ad.getAuthor() != null ? ad.getAuthor().getEmail() : null,
                includeSecretToken ? ad.getSecretPreviewToken() : null,
                ad.getImageUrls() != null ? List.copyOf(ad.getImageUrls()) : List.of(),
                ad.getTags() != null ? ad.getTags().stream().map(t -> new TagDto(t.getId(), t.getName())).toList() : List.of(),
                ad.getContactInfos() != null ? ad.getContactInfos().stream().map(ci -> new ContactInfoDto(ci.getId(), ci.getType(), ci.getValue())).toList() : List.of()
        );
    }
}
