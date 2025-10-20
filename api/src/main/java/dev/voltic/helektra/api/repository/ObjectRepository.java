package dev.voltic.helektra.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.voltic.helektra.api.model.Model;

public interface ObjectRepository<T extends Model> {
    CompletableFuture<Optional<T>> findById(String id);
    CompletableFuture<List<T>> findAll();
    CompletableFuture<Void> save(T object);
    CompletableFuture<Void> deleteById(String id);
}
