package com.piisw.tod.controller;

import com.piisw.tod.dto.MessageDto;
import com.piisw.tod.dto.MessageReplyRequestDto;
import com.piisw.tod.dto.MessageSendRequestDto;
import com.piisw.tod.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@Valid @RequestBody MessageSendRequestDto request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/inbox")
    public Page<MessageDto> inbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int effectivePage = Math.max(0, page);
        int effectiveSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize);
        return messageService.inbox(pageable);
    }

    @PostMapping("/{parentId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto reply(@PathVariable Long parentId, @Valid @RequestBody MessageReplyRequestDto request) {
        return messageService.reply(parentId, request);
    }
}
