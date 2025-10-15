package dev.voltic.helektra.plugin.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.voltic.helektra.api.model.arena.Region;
import dev.voltic.helektra.api.repository.IArenaTemplateRepository;
import dev.voltic.helektra.plugin.model.arena.world.WorldGateway;

@Singleton
public class FileArenaTemplateRepository implements IArenaTemplateRepository {
    private final Path templatesDir;
    private final WorldGateway worldGateway;

    @Inject
    public FileArenaTemplateRepository(JavaPlugin plugin, WorldGateway worldGateway) {
        this.templatesDir = plugin.getDataFolder().toPath().resolve("arena-templates");
        this.worldGateway = worldGateway;
        
        try {
            Files.createDirectories(templatesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create templates directory", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String arenaId, Region region) {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] data = worldGateway.captureRegion(region);
                Path templateFile = templatesDir.resolve(arenaId + ".template");
                Files.write(templateFile, data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save template: " + arenaId, e);
            }
        });
    }

    @Override
    public CompletableFuture<byte[]> loadTemplate(String arenaId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path templateFile = templatesDir.resolve(arenaId + ".template");
                return Files.readAllBytes(templateFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load template: " + arenaId, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String arenaId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Path templateFile = templatesDir.resolve(arenaId + ".template");
                Files.deleteIfExists(templateFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete template: " + arenaId, e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> exists(String arenaId) {
        return CompletableFuture.supplyAsync(() -> {
            Path templateFile = templatesDir.resolve(arenaId + ".template");
            return Files.exists(templateFile);
        });
    }

    @Override
    public CompletableFuture<Long> getSize(String arenaId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path templateFile = templatesDir.resolve(arenaId + ".template");
                return Files.size(templateFile);
            } catch (IOException e) {
                return 0L;
            }
        });
    }
}
