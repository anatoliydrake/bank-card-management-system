package com.example.bankcards.service;

import java.util.Collection;

public interface CRUDService<T> {
    T getById(Long id);
    Collection<T> getAll();
    T create(Long id);
    T update(T item);
    void deleteById(Long id);
}
