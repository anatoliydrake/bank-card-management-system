package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByNumber(String cardNumber);
    List<Card> findByHolderUsername(String username);
    Page<Card> findByHolder(User user, Pageable pageable);

    Page<Card> findByHolderAndStatus(User user, CardStatus status, Pageable pageable);
}
