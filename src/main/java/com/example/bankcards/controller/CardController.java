package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @GetMapping(path = "/{id}")
    public ResponseEntity<CardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<CardDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping(path = "/user/{id}")
    public ResponseEntity<Collection<CardDto>> getUserCards(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCardsByHolderId(id));
    }

    @PostMapping(path = "/user/{id}")
    public ResponseEntity<CardDto> create(@PathVariable Long id) {
        return new ResponseEntity<>(service.create(id), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.BLOCKED));
    }

    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.ACTIVE));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("balance", service.getBalance(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferDto transferDto) {
        service.transfer(transferDto);
        return ResponseEntity.ok(Map.of("message", "Transfer completed successfully"));
    }
}
