package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getCardById(@PathVariable Long id) {
        CardDto cardDto = service.getById(id);
        if (cardDto == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Card with ID " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(cardDto);
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getAllCards() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping(path = "/user/{id}")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCardsByHolderId(id));
    }

    @PostMapping(path = "/user/{id}")
    public ResponseEntity<CardDto> createCard(@PathVariable Long id) {
        return new ResponseEntity<>(service.create(id), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @RequestBody CardDto cardDto) {
        cardDto.setId(id);
        service.update(cardDto);
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        if (service.getById(id) == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Card with ID " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
