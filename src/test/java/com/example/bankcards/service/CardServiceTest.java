package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardServiceTest {
    private final CardRepository cardRepository = Mockito.mock(CardRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final CardMapper cardMapper = Mockito.mock(CardMapper.class);
    private final EncryptionUtil encryptionUtil = Mockito.mock(EncryptionUtil.class);

    private final CardService cardService = new CardService(cardRepository, userRepository, cardMapper, encryptionUtil);

    @Test
    @DisplayName("Get card by ID when card exists")
    public void testGetByIdIfCardFound() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);

        CardDto cardDto = new CardDto();
        cardDto.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.mapToDto(card)).thenReturn(cardDto);

        CardDto actualCardDto = cardService.getById(cardId);

        assertEquals(cardId, actualCardDto.getId());
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardMapper, times(1)).mapToDto(card);
    }

    @Test
    @DisplayName("Get card by ID when card is not found")
    public void testGetByIdIfCardNotFound() {
        Long cardId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.getById(cardId));
        verify(cardRepository, times(1)).findById(cardId);
    }

    @Test
    @DisplayName("Get all cards")
    public void testGetAll() {
        Long firstCardId = 1L;
        Long secondCardId = 2L;
        String cardNumber1 = "1234";
        String cardNumber2 = "1235";

        Card card1 = new Card();
        card1.setId(firstCardId);
        card1.setNumber(cardNumber1);

        Card card2 = new Card();
        card2.setId(secondCardId);
        card2.setNumber(cardNumber2);

        CardDto dto1 = new CardDto();
        dto1.setId(firstCardId);
        dto1.setNumber(cardNumber1);

        CardDto dto2 = new CardDto();
        dto2.setId(secondCardId);
        dto2.setNumber(cardNumber2);

        List<Card> cardList = List.of(card1, card2);
        List<CardDto> expectedCardDtoList = List.of(dto1, dto2);

        when(cardRepository.findAll()).thenReturn(cardList);
        when(cardMapper.mapToDto(card1)).thenReturn(dto1);
        when(cardMapper.mapToDto(card2)).thenReturn(dto2);

        Collection<CardDto> actualCardDtoList = cardService.getAll();

        assertEquals(expectedCardDtoList.size(), actualCardDtoList.size());
        assertTrue(actualCardDtoList.containsAll(expectedCardDtoList));
        verify(cardRepository, times(1)).findAll();
        verify(cardMapper, times(1)).mapToDto(card1);
        verify(cardMapper, times(1)).mapToDto(card2);
    }

    @Test
    @DisplayName("Get all cards by username when no status provided")
    public void testGetCardsByUsernameIfStatusNotProvided() {
        String username = "user";
        int page = 0;
        int size = 2;

        User user = new User();
        user.setUsername(username);

        Long firstCardId = 1L;
        Long secondCardId = 2L;

        Card card1 = new Card();
        card1.setId(firstCardId);
        card1.setNumber("1234");
        Card card2 = new Card();
        card2.setId(secondCardId);
        card2.setNumber("1235");

        CardDto dto1 = new CardDto();
        dto1.setId(firstCardId);
        dto1.setNumber("1234");
        CardDto dto2 = new CardDto();
        dto2.setId(secondCardId);
        dto2.setNumber("1235");

        List<Card> cardList = List.of(card1, card2);
        List<CardDto> expectedCardDtoList = List.of(dto1, dto2);
        Page<Card> cardPage = new PageImpl<>(cardList);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findByHolder(eq(user), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.mapToDto(card1)).thenReturn(dto1);
        when(cardMapper.mapToDto(card2)).thenReturn(dto2);

        Page<CardDto> actualPage = cardService.getCardsByUsername(username, null, page, size);

        assertEquals(size, actualPage.getTotalElements());
        assertTrue(actualPage.getContent().containsAll(expectedCardDtoList));
        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1)).findByHolder(eq(user), any(Pageable.class));
        verify(cardMapper, times(1)).mapToDto(card1);
        verify(cardMapper, times(1)).mapToDto(card2);
    }

    @Test
    @DisplayName("Get all cards by username when status provided")
    public void testGetCardsByUsernameIfStatusProvided() {
        String username = "user";
        int page = 0;
        int size = 2;
        CardStatus status = CardStatus.ACTIVE;

        User user = new User();
        user.setUsername(username);

        Long firstCardId = 1L;
        Long secondCardId = 2L;

        Card card1 = new Card();
        card1.setId(firstCardId);
        card1.setNumber("1234");
        Card card2 = new Card();
        card2.setId(secondCardId);
        card2.setNumber("1235");

        CardDto dto1 = new CardDto();
        dto1.setId(firstCardId);
        dto1.setNumber("1234");
        CardDto dto2 = new CardDto();
        dto2.setId(secondCardId);
        dto2.setNumber("1235");

        List<Card> cardList = List.of(card1, card2);
        List<CardDto> expectedCardDtoList = List.of(dto1, dto2);
        Page<Card> cardPage = new PageImpl<>(cardList);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findByHolderAndStatus(eq(user), eq(status), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.mapToDto(card1)).thenReturn(dto1);
        when(cardMapper.mapToDto(card2)).thenReturn(dto2);

        Page<CardDto> actualPage = cardService.getCardsByUsername(username, status, page, size);

        assertEquals(size, actualPage.getTotalElements());
        assertTrue(actualPage.getContent().containsAll(expectedCardDtoList));
        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1))
                .findByHolderAndStatus(eq(user), eq(status), any(Pageable.class));
        verify(cardMapper, times(1)).mapToDto(card1);
        verify(cardMapper, times(1)).mapToDto(card2);
    }

    @Test
    @DisplayName("Get all cards by username when user is not found")
    public void testGetCardsByUsernameIfUserNotFound() {
        String username = "user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () ->
                cardService.getCardsByUsername(username, null, 0, 2));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Create card when user exists")
    void testCreateIfUserFound() {
        Long userId = 1L;
        String username = "user";
        String encryptedCardNumber = "<encrypted>";

        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        Card savedCard = new Card();
        savedCard.setId(2L);

        CardDto expectedDto = new CardDto();
        expectedDto.setId(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt(anyString())).thenReturn(encryptedCardNumber);
        when(cardRepository.existsByNumber(encryptedCardNumber)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(cardMapper.mapToDto(savedCard)).thenReturn(expectedDto);

        CardDto actual = cardService.create(userId);

        assertEquals(expectedDto.getId(), actual.getId());
        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, times(1)).existsByNumber(encryptedCardNumber);
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(cardMapper, times(1)).mapToDto(savedCard);
    }

    @Test
    @DisplayName("Create card when user is not found")
    void testCreateIfUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> cardService.create(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Change card status when card exists with different status")
    void testChangeCardStatusIfCardFoundAndStatusDifferent() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        Card updatedCard = new Card();
        updatedCard.setId(cardId);
        updatedCard.setStatus(CardStatus.BLOCKED);

        CardDto expectedDto = new CardDto();
        expectedDto.setId(cardId);
        expectedDto.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(updatedCard);
        when(cardMapper.mapToDto(updatedCard)).thenReturn(expectedDto);

        CardDto actual = cardService.changeCardStatus(cardId, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, actual.getStatus());
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
        verify(cardMapper, times(1)).mapToDto(updatedCard);
    }

    @Test
    @DisplayName("Delete card by ID when card exists")
    void testDeleteByIdIfCardFound() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        cardService.deleteById(cardId);
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    @DisplayName("Delete card by ID when card is not found")
    void testDeleteByIdIfCardNotFound() {
        Long cardId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.deleteById(cardId));
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).deleteById(cardId);
    }

    @Test
    @DisplayName("Get user's balance when user has cards")
    void testGetBalanceByUsernameIfHasCards() {
        String username = "user";
        Card card1 = new Card();
        card1.setBalance(BigDecimal.valueOf(50_000));
        Card card2 = new Card();
        card2.setBalance(BigDecimal.valueOf(100_000));
        List<Card> cards = List.of(card1, card2);

        when(cardRepository.findByHolderUsername(username)).thenReturn(cards);
        BigDecimal actualBalance = cardService.getBalanceByUsername(username);
        assertEquals(BigDecimal.valueOf(150_000), actualBalance);
        verify(cardRepository, times(1)).findByHolderUsername(username);
    }

    @Test
    @DisplayName("Get user's balance when user has no cards")
    void testGetBalanceByUsernameIfNoCards() {
        String username = "user";
        when(cardRepository.findByHolderUsername(username)).thenReturn(Collections.emptyList());
        BigDecimal actualBalance = cardService.getBalanceByUsername(username);
        assertEquals(BigDecimal.ZERO, actualBalance);
        verify(cardRepository, times(1)).findByHolderUsername(username);
    }

    @Test
    @DisplayName("Correct transfer between user's cards")
    void testTransferIfCorrect() {
        String username = "user";
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(10_000);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCardId);
        transferDto.setToCardId(toCardId);
        transferDto.setAmount(amount);

        User user = new User();
        user.setUsername(username);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setNumber("1234");
        fromCard.setHolder(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(20_000));

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setNumber("1235");
        toCard.setHolder(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(90_000));

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        cardService.transfer(transferDto, username);

        assertEquals(BigDecimal.valueOf(10_000), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(100_000), toCard.getBalance());
        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
    }

    @Test
    @DisplayName("Transfer fails when fromCard is not found")
    void testTransferIfFromCardNotFound() {
        String username = "user";
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(10_000);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCardId);
        transferDto.setToCardId(toCardId);
        transferDto.setAmount(amount);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.transfer(transferDto, username));
    }

    @Test
    @DisplayName("Transfer fails when at least one of cards is not owned by user")
    void testTransferIfNotOwnCards() {
        String firstUsername = "user1";
        String secondUsername = "user2";

        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(10_000);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCardId);
        transferDto.setToCardId(toCardId);
        transferDto.setAmount(amount);

        User user1 = new User();
        user1.setUsername(firstUsername);
        User user2 = new User();
        user1.setUsername(secondUsername);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setHolder(user1);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(20_000));

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setHolder(user2);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(90_000));

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(UnauthorizedActionException.class, () -> cardService.transfer(transferDto, firstUsername));
    }

    @Test
    @DisplayName("Transfer fails when fromCard is not active")
    void testTransferUsingNotActiveCard() {
        String username = "user";
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(10_000);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCardId);
        transferDto.setToCardId(toCardId);
        transferDto.setAmount(amount);

        User user = new User();
        user.setUsername(username);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setNumber("1234");
        fromCard.setHolder(user);
        fromCard.setStatus(CardStatus.BLOCKED);
        fromCard.setBalance(BigDecimal.valueOf(20_000));

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setNumber("1235");
        toCard.setHolder(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(90_000));

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(CardNotActiveException.class, () -> cardService.transfer(transferDto, username));
    }

    @Test
    @DisplayName("Transfer fails when user has insufficient funds on card")
    void testTransferIfInsufficientFunds() {
        String username = "user";
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(10_000);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCardId);
        transferDto.setToCardId(toCardId);
        transferDto.setAmount(amount);

        User user = new User();
        user.setUsername(username);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setNumber("1234");
        fromCard.setHolder(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(5_000));

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setNumber("1235");
        toCard.setHolder(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(90_000));

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(InsufficientFundsException.class, () -> cardService.transfer(transferDto, username));
    }

    @Test
    @DisplayName("Correct request block")
    void testRequestBlockIfOk() {
        Long cardId = 1L;
        String username = "user";

        User user = new User();
        user.setUsername(username);

        Card card = new Card();
        card.setId(cardId);
        card.setHolder(user);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.requestBlock(cardId, username);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    @DisplayName("Request block fails when card is not found")
    void testRequestBlockIfCardNotFound() {
        Long cardId = 1L;
        String username = "user";
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.requestBlock(cardId, username));
    }

    @Test
    @DisplayName("Request block fails when card not owned by user")
    void testRequestBlockIfUnauthorizedUser() {
        Long cardId = 1L;
        String username = "user";

        User anotherUser = new User();
        anotherUser.setUsername("other");

        Card card = new Card();
        card.setId(cardId);
        card.setHolder(anotherUser);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(UnauthorizedActionException.class, () -> cardService.requestBlock(cardId, username));
    }

    @Test
    @DisplayName("Request block fails when card is not active")
    void testRequestBlockIfCardNotActive() {
        Long cardId = 1L;
        String username = "user";

        User user = new User();
        user.setUsername(username);

        Card card = new Card();
        card.setId(cardId);
        card.setHolder(user);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(UnauthorizedActionException.class, () -> cardService.requestBlock(cardId, username));
    }

}
