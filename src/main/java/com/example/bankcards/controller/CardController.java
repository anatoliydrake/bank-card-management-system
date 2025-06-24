package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cards", description = "Card management operations")
public class CardController {
    private final CardService service;

    @Operation(summary = "Get card by ID", description = "Get card details by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CardDto> getById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all cards", description = "List of all cards",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json", examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = """
                                    [
                                      {
                                          "id": 1,
                                          "number": "**** **** **** 5013",
                                          "holderName": "username",
                                          "expirationDate": "2030-06-22",
                                          "status": "BLOCKED",
                                          "balance": 42000.00
                                      },
                                      {
                                          "id": 2,
                                          "number": "**** **** **** 3201",
                                          "holderName": "username",
                                          "expirationDate": "2030-06-23",
                                          "status": "ACTIVE",
                                          "balance": 37000.00
                                      }
                                    ]
                                    """
                    )
            }, array = @ArraySchema(schema = @Schema(implementation = CardDto.class)
            )))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Collection<CardDto>> getAll(Authentication authentication) {
        log.info("User '{}' requested all cards", authentication.getName());
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Create card", description = "Create card for user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User with ID 10 not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/user/{id}")
    public ResponseEntity<CardDto> create(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested creating card for user ID: {}", authentication.getName(), id);
        return new ResponseEntity<>(service.create(id), HttpStatus.CREATED);
    }

    @Operation(summary = "Block card", description = "Block card by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                          "id": 10,
                                          "number": "**** **** **** 5013",
                                          "holderName": "username",
                                          "expirationDate": "2030-06-22",
                                          "status": "BLOCKED",
                                          "balance": 42000.00
                                      }"""))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested blocking card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.BLOCKED));
    }

    @Operation(summary = "Activate card", description = "Activate card by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                          "id": 10,
                                          "number": "**** **** **** 5013",
                                          "holderName": "username",
                                          "expirationDate": "2030-06-22",
                                          "status": "ACTIVE",
                                          "balance": 42000.00
                                      }"""))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested activating card ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.changeCardStatus(id, CardStatus.ACTIVE));
    }

    @Operation(summary = "Delete card", description = "Delete card by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested deleting card ID: {}", authentication.getName(), id);
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get own cards", description = "Get current user's cards",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json", examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = """
                                    {
                                        "content": [
                                            {
                                                "id": 2,
                                                "number": "**** **** **** 5013",
                                                "holderName": "user",
                                                "expirationDate": "2030-06-22",
                                                "status": "BLOCKED",
                                                "balance": 42000.00
                                            },
                                            {
                                                "id": 4,
                                                "number": "**** **** **** 3201",
                                                "holderName": "user",
                                                "expirationDate": "2030-06-23",
                                                "status": "ACTIVE",
                                                "balance": 37000.00
                                            }
                                        ],
                                        "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 2,
                                            "sort": {
                                                "empty": true,
                                                "sorted": false,
                                                "unsorted": true
                                            },
                                            "offset": 0,
                                            "paged": true,
                                            "unpaged": false
                                        },
                                        "totalPages": 3,
                                        "totalElements": 6,
                                        "last": false,
                                        "size": 2,
                                        "number": 0,
                                        "sort": {
                                            "empty": true,
                                            "sorted": false,
                                            "unsorted": true
                                        },
                                        "numberOfElements": 2,
                                        "first": true,
                                        "empty": false
                                    }
                                    """
                    )
            }, array = @ArraySchema(schema = @Schema(implementation = CardDto.class)
            )))
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

    @Operation(summary = "Get total balance", description = "Returns total balance of user's cards",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"balance\": \"304000.00\"}")))
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        log.info("User '{}' requested balance", authentication.getName());
        return ResponseEntity.ok(Map.of("balance", service.getBalanceByUsername(authentication.getName())));
    }

    @Operation(summary = "Transfer funds", description = "Transfer funds between own cards",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Transfer completed successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            example = "{\"error\": \"You can transfer only between your own cards.\"}"))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Bad Request - card not active or insufficient funds\"}")))
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferDto transferDto, Authentication authentication) {
        log.info("User '{}' requested transfer {} from card {} to card {}", authentication.getName(),
                transferDto.getAmount(), transferDto.getFromCardId(), transferDto.getToCardId());
        service.transfer(transferDto, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Transfer completed successfully"));
    }

    @Operation(summary = "Request to block card", description = "Request to block for own card",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Block request submitted\"}"))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Card with ID: 10 not found\"}"))),
            @ApiResponse(responseCode = "403", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Only active cards can be blocked.\"}")))
    })
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/request-block")
    public ResponseEntity<?> requestBlock(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested to block card ID: {}", authentication.getName(), id);
        service.requestBlock(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Block request submitted"));
    }
}
