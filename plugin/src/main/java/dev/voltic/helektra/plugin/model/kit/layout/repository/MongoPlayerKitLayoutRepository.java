package dev.voltic.helektra.plugin.model.kit.layout.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.voltic.helektra.plugin.model.kit.layout.PlayerKitLayout;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bson.Document;

@Singleton
public class MongoPlayerKitLayoutRepository implements PlayerKitLayoutRepository {

  private final MongoCollection<Document> collection;
  private final ObjectMapper mapper;

  @Inject
  public MongoPlayerKitLayoutRepository(MongoDatabase database, ObjectMapper mapper) {
    this.collection = database.getCollection("player_kit_layouts");
    this.mapper = mapper;
  }

  @Override
  public CompletableFuture<Optional<PlayerKitLayout>> findByPlayerAndKit(UUID playerId, String kitName) {
    return CompletableFuture.supplyAsync(() -> {
      String id = generateId(playerId, kitName);
      Document doc = collection.find(Filters.eq("_id", id)).first();
      if (doc == null) {
        return Optional.empty();
      }
      try {
        PlayerKitLayout layout = mapper.convertValue(doc, PlayerKitLayout.class);
        return Optional.of(layout);
      } catch (Exception e) {
        e.printStackTrace();
        return Optional.empty();
      }
    });
  }

  @Override
  public CompletableFuture<Void> save(PlayerKitLayout layout) {
    return CompletableFuture.runAsync(() -> {
      try {
        Document doc = mapper.convertValue(layout, Document.class);
        collection.replaceOne(
          Filters.eq("_id", layout.getId()),
          doc,
          new ReplaceOptions().upsert(true)
        );
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public CompletableFuture<Void> delete(UUID playerId, String kitName) {
    return CompletableFuture.runAsync(() -> {
      String id = generateId(playerId, kitName);
      collection.deleteOne(Filters.eq("_id", id));
    });
  }

  private String generateId(UUID playerId, String kitName) {
    return playerId.toString() + "_" + kitName.toLowerCase();
  }
}
