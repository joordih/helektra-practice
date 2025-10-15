package dev.voltic.helektra.plugin.repository;

import com.mongodb.client.MongoDatabase;

import dev.voltic.helektra.api.model.Model;
import dev.voltic.helektra.api.repository.ObjectRepository;
import dev.voltic.helektra.api.repository.RepositoryFactory;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MongoRepositoryFactory implements RepositoryFactory {

    private final MongoDatabase database;

    @Override
    public <T extends Model> ObjectRepository<T> create(Class<T> clazz) {
        return new MongoObjectRepository<>(database, clazz);
    }
    
}
