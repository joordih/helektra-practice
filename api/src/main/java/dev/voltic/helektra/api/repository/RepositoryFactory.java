package dev.voltic.helektra.api.repository;

import dev.voltic.helektra.api.model.Model;

public interface RepositoryFactory {
    <T extends Model> ObjectRepository<T> create(Class<T> clazz);
}