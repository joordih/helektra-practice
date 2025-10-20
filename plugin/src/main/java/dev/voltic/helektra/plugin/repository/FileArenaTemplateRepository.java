package dev.voltic.helektra.plugin.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.ArenaTemplateBundle;
import dev.voltic.helektra.api.model.arena.ArenaTemplateMetadata;
import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class FileArenaTemplateRepository implements IArenaTemplateRepository {

  private static final String TEMPLATE_EXTENSION = ".template";
  private static final String META_EXTENSION = ".meta";
  private static final String VERSION_KEY = "version";
  private static final String CHECKSUM_KEY = "checksum";
  private static final String CAPTURED_KEY = "capturedAt";

  private final Path templatesDir;
  private final WorldGateway worldGateway;
  private final JavaPlugin plugin;

  @Inject
  public FileArenaTemplateRepository(
    JavaPlugin plugin,
    WorldGateway worldGateway
  ) {
    this.templatesDir = plugin
      .getDataFolder()
      .toPath()
      .resolve("arena-templates");
    this.worldGateway = worldGateway;
    this.plugin = plugin;
    try {
      Files.createDirectories(templatesDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public CompletableFuture<ArenaTemplateMetadata> saveTemplate(
    String arenaId,
    Region region
  ) {
    CompletableFuture<byte[]> captureFuture = new CompletableFuture<>();
    Bukkit.getScheduler()
      .runTask(plugin, () -> {
        try {
          captureFuture.complete(worldGateway.captureRegion(region));
        } catch (Throwable t) {
          captureFuture.completeExceptionally(t);
        }
      });
    return captureFuture.thenApplyAsync(data -> {
      writeBytes(dataPath(arenaId), data);
      ArenaTemplateMetadata metadata = buildMetadata(arenaId, data);
      writeMetadata(arenaId, metadata);
      return metadata;
    });
  }

  @Override
  public CompletableFuture<ArenaTemplateBundle> loadTemplate(String arenaId) {
    return CompletableFuture.supplyAsync(() -> {
      byte[] data = readBytes(dataPath(arenaId));
      ArenaTemplateMetadata metadata = readMetadata(arenaId).orElseGet(() -> {
        ArenaTemplateMetadata generated = buildMetadata(arenaId, data, 1L);
        writeMetadata(arenaId, generated);
        return generated;
      });
      validateChecksum(arenaId, data, metadata);
      return ArenaTemplateBundle.builder()
        .data(data)
        .metadata(metadata)
        .build();
    });
  }

  @Override
  public CompletableFuture<Void> deleteTemplate(String arenaId) {
    return CompletableFuture.runAsync(() -> {
      try {
        Files.deleteIfExists(dataPath(arenaId));
        Files.deleteIfExists(metaPath(arenaId));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  @Override
  public CompletableFuture<Boolean> exists(String arenaId) {
    return CompletableFuture.supplyAsync(() ->
      Files.exists(dataPath(arenaId)) && Files.exists(metaPath(arenaId))
    );
  }

  @Override
  public CompletableFuture<Long> getSize(String arenaId) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return Files.size(dataPath(arenaId));
      } catch (IOException e) {
        return 0L;
      }
    });
  }

  private Path dataPath(String arenaId) {
    return templatesDir.resolve(arenaId + TEMPLATE_EXTENSION);
  }

  private Path metaPath(String arenaId) {
    return templatesDir.resolve(arenaId + META_EXTENSION);
  }

  private void writeBytes(Path path, byte[] data) {
    try {
      Files.write(path, data);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private byte[] readBytes(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Optional<ArenaTemplateMetadata> readMetadata(String arenaId) {
    Path path = metaPath(arenaId);
    if (!Files.exists(path)) {
      return Optional.empty();
    }
    Properties properties = new Properties();
    try (
      var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
    ) {
      properties.load(reader);
      long version = Long.parseLong(
        properties.getProperty(VERSION_KEY, "1")
      );
      String checksum = properties.getProperty(CHECKSUM_KEY);
      long capturedAt = Long.parseLong(
        properties.getProperty(
          CAPTURED_KEY,
          String.valueOf(Instant.now().toEpochMilli())
        )
      );
      return Optional.of(
        ArenaTemplateMetadata.builder()
          .version(version)
          .checksum(checksum)
          .capturedAt(capturedAt)
          .build()
      );
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeMetadata(String arenaId, ArenaTemplateMetadata metadata) {
    Properties properties = new Properties();
    properties.setProperty(VERSION_KEY, String.valueOf(metadata.getVersion()));
    properties.setProperty(CHECKSUM_KEY, metadata.getChecksum());
    properties.setProperty(
      CAPTURED_KEY,
      String.valueOf(metadata.getCapturedAt())
    );
    try (
      var writer = Files.newBufferedWriter(metaPath(arenaId), StandardCharsets.UTF_8)
    ) {
      properties.store(writer, null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private ArenaTemplateMetadata buildMetadata(String arenaId, byte[] data) {
    long version = readMetadata(arenaId)
      .map(existing -> existing.getVersion() + 1)
      .orElse(1L);
    return buildMetadata(arenaId, data, version);
  }

  private ArenaTemplateMetadata buildMetadata(
    String arenaId,
    byte[] data,
    long version
  ) {
    return ArenaTemplateMetadata.builder()
      .version(version)
      .checksum(computeChecksum(data))
      .capturedAt(Instant.now().toEpochMilli())
      .build();
  }

  private void validateChecksum(
    String arenaId,
    byte[] data,
    ArenaTemplateMetadata metadata
  ) {
    String expected = metadata.getChecksum();
    if (expected == null || expected.isEmpty()) {
      ArenaTemplateMetadata regenerated = buildMetadata(arenaId, data, 1L);
      writeMetadata(arenaId, regenerated);
      return;
    }
    String actual = computeChecksum(data);
    if (!actual.equals(expected)) {
      throw new IllegalStateException(
        "Template checksum mismatch for " + arenaId
      );
    }
  }

  private String computeChecksum(byte[] data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
