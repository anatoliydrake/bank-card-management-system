package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.*;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(TestSecurityConfig.class)
public class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtConfig jwtConfig;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CardService cardService;

    @Test
    @DisplayName("GET /api/cards/{id} - returns 200 OK with card")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCardById_ReturnsCard_WhenExists() throws Exception {
        Long cardId = 1L;

        CardDto card = new CardDto();
        card.setId(cardId);
        card.setNumber("1234567890123456");
        card.setBalance(BigDecimal.valueOf(10_000));

        Mockito.when(cardService.getById(cardId)).thenReturn(card);
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.number").value(card.getNumber()))
                .andExpect(jsonPath("$.balance").value(card.getBalance()));
    }

    @Test
    @DisplayName("GET /api/cards/{id} - returns 404 Not Found when card is missing")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCardById_ReturnsNotFound_WhenMissing() throws Exception {
        Long cardId = 999L;

        Mockito.when(cardService.getById(cardId)).thenThrow(new CardNotFoundException(cardId));
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + cardId + " not found"));
    }

    @Test
    @DisplayName("GET /api/cards/{id} - returns 403 Forbidden for USER role")
    @WithMockUser(username = "user", roles = "USER")
    void getCardById_ReturnsForbidden_WhenRoleNotAdmin() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/cards - returns 200 OK with all cards for ADMIN")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllCards_ReturnsCards_WhenRoleAdmin() throws Exception {
        CardDto card1 = new CardDto();
        card1.setId(1L);
        card1.setNumber("**** **** **** 5013");

        CardDto card2 = new CardDto();
        card2.setId(2L);
        card2.setNumber("**** **** **** 3201");

        Mockito.when(cardService.getAll()).thenReturn(List.of(card1, card2));
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value(card1.getNumber()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].number").value(card2.getNumber()));
    }

    @Test
    @DisplayName("GET /api/cards - returns 403 Forbidden for USER role")
    @WithMockUser(username = "user", roles = "USER")
    void getAllCards_ReturnsForbidden_WhenRoleNotAdmin() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/cards/user/{id} - returns 201 Created with new card")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCard_ReturnsCreatedCard_WhenRoleAdminAndUserExists() throws Exception {
        Long userId = 1L;

        CardDto createdCard = new CardDto();
        createdCard.setId(5L);
        createdCard.setNumber("**** **** **** 5013");

        Mockito.when(cardService.create(userId)).thenReturn(createdCard);
        mockMvc.perform(post("/api/cards/user/{id}", userId))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value(createdCard.getNumber()));
    }

    @Test
    @DisplayName("POST /api/cards/user/{id} - returns 404 Not Found when user doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCard_ReturnsNotFound_WhenUserMissing() throws Exception {
        Long userId = 999L;

        Mockito.when(cardService.create(userId)).thenThrow(new UserNotFoundException(userId));
        mockMvc.perform(post("/api/cards/user/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("User with ID: " + userId + " not found"));
    }

    @Test
    @DisplayName("POST /api/cards/user/{id} - returns 403 Forbidden for user without ADMIN role")
    @WithMockUser(username = "user", roles = "USER")
    void createCard_ReturnsForbidden_WhenRoleUser() throws Exception {
        mockMvc.perform(post("/api/cards/user/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/block - returns 200 OK with blocked card")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void blockCard_ReturnsBlockedCard_WhenExists() throws Exception {
        Long cardId = 1L;

        CardDto blockedCard = new CardDto();
        blockedCard.setId(cardId);
        blockedCard.setStatus(CardStatus.BLOCKED);

        Mockito.when(cardService.changeCardStatus(cardId, CardStatus.BLOCKED)).thenReturn(blockedCard);
        mockMvc.perform(patch("/api/cards/{id}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(blockedCard.getId()))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/block - returns 404 Not Found when card doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void blockCard_ReturnsNotFound_WhenCardMissing() throws Exception {
        Long cardId = 999L;

        Mockito.when(cardService.changeCardStatus(cardId, CardStatus.BLOCKED))
                .thenThrow(new CardNotFoundException(cardId));
        mockMvc.perform(patch("/api/cards/{id}/block", cardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + cardId + " not found"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/block - returns 403 Forbidden for user without ADMIN role")
    @WithMockUser(username = "user", roles = "USER")
    void blockCard_ReturnsForbidden_WhenRoleUser() throws Exception {
        mockMvc.perform(patch("/api/cards/{id}/block", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/activate - returns 200 OK with activated card")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activateCard_ReturnsActivatedCard_WhenExists() throws Exception {
        Long cardId = 1L;

        CardDto blockedCard = new CardDto();
        blockedCard.setId(cardId);
        blockedCard.setStatus(CardStatus.ACTIVE);

        Mockito.when(cardService.changeCardStatus(cardId, CardStatus.ACTIVE)).thenReturn(blockedCard);
        mockMvc.perform(patch("/api/cards/{id}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(blockedCard.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/activate - returns 404 Not Found when card doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activateCard_ReturnsNotFound_WhenCardMissing() throws Exception {
        Long cardId = 999L;

        Mockito.when(cardService.changeCardStatus(cardId, CardStatus.ACTIVE))
                .thenThrow(new CardNotFoundException(cardId));
        mockMvc.perform(patch("/api/cards/{id}/activate", cardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + cardId + " not found"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/activate - returns 403 Forbidden for user without ADMIN role")
    @WithMockUser(username = "user", roles = "USER")
    void activateCard_ReturnsForbidden_WhenRoleUser() throws Exception {
        mockMvc.perform(patch("/api/cards/{id}/activate", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} - returns 204 No Content when card exists")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteById_ReturnsNoContent_WhenExists() throws Exception {
        Long cardId = 1L;

        Mockito.doNothing().when(cardService).deleteById(cardId);
        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNoContent());
        verify(cardService).deleteById(cardId);
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} - returns 404 Not Found when card doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteById_ReturnsNotFound_WhenCardMissing() throws Exception {
        Long cardId = 999L;

        doThrow(new CardNotFoundException(cardId)).when(cardService).deleteById(cardId);
        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + cardId + " not found"));
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} - returns 403 Forbidden for user without ADMIN role")
    @WithMockUser(username = "user", roles = "USER")
    void deleteById_ReturnsForbidden_WhenRoleUser() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/cards/my - returns 200 OK with user's cards")
    @WithMockUser(username = "user", roles = "USER")
    void getMyCards_ReturnsCards_WhenUserAuthenticated() throws Exception {
        CardDto card1 = new CardDto();
        card1.setId(2L);
        card1.setStatus(CardStatus.BLOCKED);

        CardDto card2 = new CardDto();
        card2.setId(4L);
        card2.setStatus(CardStatus.ACTIVE);

        List<CardDto> content = List.of(card1, card2);
        Page<CardDto> page = new PageImpl<>(content, PageRequest.of(0, 3), 6);

        Mockito.when(cardService.getCardsByUsername("user", null, 0, 3)).thenReturn(page);
        mockMvc.perform(get("/api/cards/my"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(card1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(card2.getId()))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(3));
    }

    @Test
    @DisplayName("GET /api/cards/my?status=ACTIVE - returns only active cards")
    @WithMockUser(username = "user", roles = "USER")
    void getMyCards_ReturnsFilteredCards_WhenStatusProvided() throws Exception {
        CardDto card = new CardDto();
        card.setId(5L);
        card.setStatus(CardStatus.ACTIVE);

        Page<CardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 3), 1);

        Mockito.when(cardService.getCardsByUsername("user", CardStatus.ACTIVE, 0, 3))
                .thenReturn(page);
        mockMvc.perform(get("/api/cards/my")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/cards/my - returns 403 Forbidden for user without USER role")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMyCards_ReturnsForbidden_WhenRoleNotUser() throws Exception {
        mockMvc.perform(get("/api/cards/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/cards/balance - returns 200 OK with user's total balance")
    @WithMockUser(username = "user", roles = "USER")
    void getBalance_ReturnsTotalBalance_WhenRoleUser() throws Exception {
        BigDecimal balance = new BigDecimal("100000.12");

        Mockito.when(cardService.getBalanceByUsername("user")).thenReturn(balance);
        mockMvc.perform(get("/api/cards/balance"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    @DisplayName("GET /api/cards/balance - returns 403 Forbidden for user without USER role")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getBalance_ReturnsForbidden_WhenRoleNotUser() throws Exception {
        mockMvc.perform(get("/api/cards/balance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 200 OK when transfer succeeds")
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ReturnsOk_WhenTransferSuccessful() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(5000));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));
        verify(cardService, times(1)).transfer(eq(dto), eq("user"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 404 Not Found when card doesn't exist")
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ReturnsNotFound_WhenCardMissing() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(5000));

        doThrow(new CardNotFoundException(dto.getFromCardId()))
                .when(cardService).transfer(eq(dto), eq("user"));
        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + dto.getFromCardId() + " not found"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 403 Forbidden when transfer between not own cards")
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ReturnsForbidden_WhenNotOwnCards() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(5000));

        doThrow(new UnauthorizedActionException("You can transfer only between your own cards."))
                .when(cardService).transfer(eq(dto), eq("user"));
        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("You can transfer only between your own cards."));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 400 Bad Request when card not active")
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ReturnsBadRequest_WhenCardInactive() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(100000));

        doThrow(new CardNotActiveException(dto.getFromCardId()))
                .when(cardService).transfer(eq(dto), eq("user"));
        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + dto.getFromCardId() + " is not active."));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 400 Bad Request when insufficient funds on card")
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ReturnsBadRequest_WhenInsufficientFunds() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(100000));

        doThrow(new InsufficientFundsException(dto.getFromCardId()))
                .when(cardService).transfer(eq(dto), eq("user"));
        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card ID: " + dto.getFromCardId() +
                                " has insufficient funds for the operation"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - returns 403 Forbidden for user without USER role")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void transfer_ReturnsForbidden_WhenRoleNotUser() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(BigDecimal.valueOf(5000));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/request-block - returns 200 OK when card is active")
    @WithMockUser(username = "user", roles = "USER")
    void requestBlock_ReturnsOk_WhenCardIsActive() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Block request submitted"));
        Mockito.verify(cardService, times(1)).requestBlock(cardId, "user");
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/request-block - returns 404 Not Found when card doesn't exist")
    @WithMockUser(username = "user", roles = "USER")
    void requestBlock_ReturnsNotFound_WhenCardMissing() throws Exception {
        Long cardId = 999L;

        Mockito.doThrow(new CardNotFoundException(cardId))
                .when(cardService).requestBlock(cardId, "user");
        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Card with ID: " + cardId + " not found"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/request-block - returns 400 Not Found when user does not own the card")
    @WithMockUser(username = "user", roles = "USER")
    void requestBlock_ReturnsBadRequest_WhenNotOwnCard() throws Exception {
        Long cardId = 1L;

        Mockito.doThrow(new UnauthorizedActionException("You can request block only for your own cards."))
                .when(cardService).requestBlock(cardId, "user");
        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("You can request block only for your own cards."));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/request-block - returns 400 Not Found when card is now active")
    @WithMockUser(username = "user", roles = "USER")
    void requestBlock_ReturnsBadRequest_WhenCardInactive() throws Exception {
        Long cardId = 1L;

        Mockito.doThrow(new UnauthorizedActionException("Only active cards can be blocked."))
                .when(cardService).requestBlock(cardId, "user");
        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Only active cards can be blocked."));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/request-block - returns 403 Forbidden for user without USER role")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void requestBlock_ReturnsForbidden_WhenRoleIsNotUser() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isForbidden());
    }
}
