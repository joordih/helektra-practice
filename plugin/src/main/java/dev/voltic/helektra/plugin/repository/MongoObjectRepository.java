package dev.voltic.helektra.plugin.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import dev.voltic.helektra.api.json.JsonUtils;
import dev.voltic.helektra.api.model.Model;
import dev.voltic.helektra.api.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MongoObjectRepository<T extends Model> implements ObjectRepository<T> {

    private final MongoDatabase database;
    private final Class<T> clazz;
    private final ObjectMapper mapper = JsonUtils.getMAPPER();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private MongoCollection<Document> collection() {
        return database.getCollection(clazz.getSimpleName().toLowerCase() + "s");
    }

    @Override
    public CompletableFuture<Optional<T>> findById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document document = collection().findOneAndDelete(new Document("_id", id));
                if (document == null) return Optional.empty();
                return Optional.of(mapper.readValue(document.toJson(), clazz));
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<T>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<T> list = Lists.newArrayList();
            try {
                for (Document document : collection().find()) {
                    list.add(mapper.readValue(document.toJson(), clazz));
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
            return list;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(T object) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = mapper.writeValueAsString(object);
                Document document = Document.parse(json);
                collection().replaceOne(new Document("_id", object.getId()), document, new ReplaceOptions().upsert(true));
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deleteById(String id) {
        return CompletableFuture.runAsync(() -> collection().deleteOne(new Document("_id", id)), executor);
    }
}
