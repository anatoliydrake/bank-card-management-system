package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {
    private final CardService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CardDto> getById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Collection<CardDto>> getAll(Authentication authentication) {
        log.info("User '{}' requested all cards", authentication.getName());
        return ResponseEntity.ok(service.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/user/{id}")
    public ResponseEntity<CardDto> create(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested creating card for user ID: {}", authentication.getName(), id);
        return new ResponseEntity<>(service.create(id), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id, Authentication authentication){
        log.info("User '{}' requested blocking card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.BLOCKED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id, Authentication authentication){
        log.info("User '{}' requested activating card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.ACTIVE));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested deleting card ID: {}", authentication.getName(), id);
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<Page<CardDto>> getMyCards(
            Authentication authentication,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        log.info("User '{}' requested own cards", authentication.getName());
        Page<CardDto> cards = service.getCardsByUsername(authentication.getName(), status, page, size);
        return ResponseEntity.ok(cards);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        log.info("User '{}' requested balance", authentication.getName());
        return ResponseEntity.ok(Map.of("balance", service.getBalanceByUsername(authentication.getName())));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferDto transferDto, Authentication authentication) {
        log.info("User '{}' requested transfer {} from card {} to card {}", authentication.getName(),
                transferDto.getAmount(), transferDto.getFromCardId(), transferDto.getToCardId());
        service.transfer(transferDto, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Transfer completed successfully"));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/request-block")
    public ResponseEntity<?> requestBlock(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested to block card ID: {}", authentication.getName(), id);
        service.requestBlock(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Block request submitted"));
    }
}
