package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class CardController {
    private final CardService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Collection<CardDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/user/{id}")
    public ResponseEntity<CardDto> create(@PathVariable Long id) {
        return new ResponseEntity<>(service.create(id), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.BLOCKED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.ACTIVE));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<Collection<CardDto>> getMyCards(Authentication authentication) {
        return ResponseEntity.ok(service.getCardsByUsername(authentication.getName()));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        return ResponseEntity.ok(Map.of("balance", service.getBalanceByUsername(authentication.getName())));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferDto transferDto, Authentication authentication) {
        service.transfer(transferDto, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Transfer completed successfully"));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/request-block")
    public ResponseEntity<?> requestBlock(@PathVariable Long id, Authentication authentication) {
        service.requestBlock(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Block request submitted"));
    }
}
