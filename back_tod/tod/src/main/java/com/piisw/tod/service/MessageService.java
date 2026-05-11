package com.piisw.tod.service;

import com.piisw.tod.dto.MessageDto;
import com.piisw.tod.dto.MessageReplyRequestDto;
import com.piisw.tod.dto.MessageSendRequestDto;
import com.piisw.tod.model.Ad;
import com.piisw.tod.model.Message;
import com.piisw.tod.model.User;
import com.piisw.tod.repository.AdRepository;
import com.piisw.tod.repository.MessageRepository;
import com.piisw.tod.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final CurrentUserService currentUserService;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            AdRepository adRepository,
            CurrentUserService currentUserService
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public MessageDto sendMessage(MessageSendRequestDto request) {
        User sender = currentUserService.requireCurrentUser();

        Ad relatedAd = null;
        if (request.relatedAdId() != null) {
            relatedAd = adRepository.findById(request.relatedAdId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        }

        User receiver;
        if (request.receiverId() != null) {
            receiver = userRepository.findById(request.receiverId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));
        } else if (relatedAd != null) {
            receiver = relatedAd.getAuthor();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receiverId or relatedAdId is required");
        }

        if (receiver == null || receiver.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver is not resolvable");
        }

        if (Objects.equals(sender.getId(), receiver.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send message to yourself");
        }

        Message message = new Message();
        message.setContent(request.content());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setRelatedAd(relatedAd);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        return toDto(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> inbox(Pageable pageable) {
        User current = currentUserService.requireCurrentUser();
        return messageRepository.findByReceiverIdOrderBySentAtDesc(current.getId(), pageable)
                .map(MessageService::toDto);
    }

    @Transactional
    public MessageDto reply(Long parentMessageId, MessageReplyRequestDto request) {
        User current = currentUserService.requireCurrentUser();
        Message parent = messageRepository.findById(parentMessageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent message not found"));

        boolean currentIsSender = parent.getSender() != null && Objects.equals(parent.getSender().getId(), current.getId());
        boolean currentIsReceiver = parent.getReceiver() != null && Objects.equals(parent.getReceiver().getId(), current.getId());

        if (!currentIsSender && !currentIsReceiver) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this conversation");
        }

        User receiver = currentIsSender ? parent.getReceiver() : parent.getSender();
        if (receiver == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot resolve reply receiver");
        }

        Message reply = new Message();
        reply.setContent(request.content());
        reply.setSender(current);
        reply.setReceiver(receiver);
        reply.setRelatedAd(parent.getRelatedAd());
        reply.setParentMessage(parent);
        reply.setSentAt(LocalDateTime.now());
        reply.setIsRead(false);

        return toDto(messageRepository.save(reply));
    }

    private static MessageDto toDto(Message m) {
        return new MessageDto(
                m.getId(),
                m.getContent(),
                m.getSentAt(),
                m.getIsRead(),
                m.getSender() != null ? m.getSender().getId() : null,
                m.getSender() != null ? m.getSender().getEmail() : null,
                m.getReceiver() != null ? m.getReceiver().getId() : null,
                m.getReceiver() != null ? m.getReceiver().getEmail() : null,
                m.getRelatedAd() != null ? m.getRelatedAd().getId() : null,
                m.getParentMessage() != null ? m.getParentMessage().getId() : null
        );
    }
}
