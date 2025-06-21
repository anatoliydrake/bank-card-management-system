package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        CardDto cardDto = service.getById(id);
        if (cardDto == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Card with ID " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(cardDto);
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
    public ResponseEntity<?> blockCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.BLOCKED));
    }

    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<?> activateCard(@PathVariable Long id){
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.ACTIVE));
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

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long id) {
        BigDecimal balance = service.getBalance(id);
        if (balance == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Card with ID " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferDto transferDto) {
        String result = service.transfer(transferDto);

        if (result.equals("Transfer successful")) {
            return ResponseEntity.ok().body(Map.of("message", result));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", result));
        }
    }
}
