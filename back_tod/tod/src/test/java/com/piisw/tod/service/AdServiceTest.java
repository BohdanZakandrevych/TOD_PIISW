package com.piisw.tod.service;

import com.piisw.tod.dto.AdCreateRequestDto;
import com.piisw.tod.dto.AdResponseDto;
import com.piisw.tod.dto.AdUpdateRequestDto;
import com.piisw.tod.model.Ad;
import com.piisw.tod.model.AdStatus;
import com.piisw.tod.model.User;
import com.piisw.tod.repository.AdRepository;
import com.piisw.tod.repository.ContactInfoRepository;
import com.piisw.tod.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ContactInfoRepository contactInfoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AdService adService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("john.smith@example.com");
    }

    @Test
    void shouldCreateAdSuccessfully() {
        // Arrange
        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);

        Ad savedAd = new Ad();
        savedAd.setId(100L);
        savedAd.setTitle("Mocked Ad Title");
        savedAd.setAuthor(mockUser);
        savedAd.setStatus(AdStatus.DRAFT);

        when(adRepository.save(any(Ad.class))).thenReturn(savedAd);

        AdCreateRequestDto request = new AdCreateRequestDto(
                "Mocked Ad Title", "Desc", null, null, null
        );

        // Act
        AdResponseDto response = adService.createAd(request);

        // Assert
        assertNotNull(response);
        assertEquals("Mocked Ad Title", response.title());
        assertEquals(AdStatus.DRAFT, response.status());

        ArgumentCaptor<Ad> adCaptor = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(adCaptor.capture());

        Ad capturedAd = adCaptor.getValue();
        assertEquals("Mocked Ad Title", capturedAd.getTitle());
        assertEquals("Desc", capturedAd.getDescription());
        assertEquals(mockUser, capturedAd.getAuthor());
    }

    @Test
    void shouldChangeAdStatusSuccessfullyWhenOwner() {
        // Arrange
        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);

        Ad existingAd = new Ad();
        existingAd.setId(200L);
        existingAd.setAuthor(mockUser);
        existingAd.setStatus(AdStatus.DRAFT);

        when(adRepository.findById(200L)).thenReturn(Optional.of(existingAd));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AdResponseDto response = adService.changeStatus(200L, AdStatus.PUBLISHED);

        // Assert
        assertEquals(AdStatus.PUBLISHED, response.status());
        verify(adRepository).save(existingAd);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenChangingStatusOfNotOwnedAd() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);

        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);

        Ad existingAd = new Ad();
        existingAd.setId(200L);
        existingAd.setAuthor(otherUser);

        when(adRepository.findById(200L)).thenReturn(Optional.of(existingAd));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.changeStatus(200L, AdStatus.PUBLISHED);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentAd() {
        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        AdUpdateRequestDto request = new AdUpdateRequestDto("New Title", null, null, null, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.updateAd(999L, request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenWhenUpdatingNotOwnedAd() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);

        Ad existingAd = new Ad();
        existingAd.setId(200L);
        existingAd.setAuthor(otherUser);

        when(adRepository.findById(200L)).thenReturn(Optional.of(existingAd));

        AdUpdateRequestDto request = new AdUpdateRequestDto("New Title", null, null, null, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.updateAd(200L, request);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenGettingNonExistentAd() {
        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.getAd(999L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenWhenGettingDraftNotOwned() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(currentUserService.requireCurrentUser()).thenReturn(mockUser);

        Ad draftAd = new Ad();
        draftAd.setId(200L);
        draftAd.setAuthor(otherUser);
        draftAd.setStatus(AdStatus.DRAFT);

        when(adRepository.findById(200L)).thenReturn(Optional.of(draftAd));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.getAd(200L);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenPreviewingInvalidToken() {
        when(adRepository.findBySecretPreviewToken("invalid")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.previewDraftByToken("invalid");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenPreviewingPublishedAdToken() {
        Ad publishedAd = new Ad();
        publishedAd.setId(200L);
        publishedAd.setStatus(AdStatus.PUBLISHED);

        when(adRepository.findBySecretPreviewToken("token")).thenReturn(Optional.of(publishedAd));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adService.previewDraftByToken("token");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}