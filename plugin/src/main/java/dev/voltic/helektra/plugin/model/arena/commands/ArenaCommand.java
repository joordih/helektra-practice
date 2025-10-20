package dev.voltic.helektra.plugin.model.arena.commands;

import com.google.inject.Inject;
import dev.voltic.helektra.api.model.arena.*;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "arena", "arenas" })
public class ArenaCommand implements CommandClass {

  private final IArenaService arenaService;
  private final IArenaSelectionService selectionService;
  private final IArenaTemplateService templateService;
  private final IArenaPoolService poolService;
  private final IMetricsService metricsService;
  private final JavaPlugin plugin;

  @Inject
  public ArenaCommand(
    IArenaService arenaService,
    IArenaSelectionService selectionService,
    IArenaTemplateService templateService,
    IArenaPoolService poolService,
    IMetricsService metricsService,
    JavaPlugin plugin
  ) {
    this.arenaService = arenaService;
    this.selectionService = selectionService;
    this.templateService = templateService;
    this.poolService = poolService;
    this.metricsService = metricsService;
    this.plugin = plugin;
  }

  @Command(names = "")
  public void mainCommand(@Sender CommandSender sender) {
    sender.sendMessage(TranslationUtils.translate("arena.commands-header"));
    sender.sendMessage("/arena create <id> <nombre> <tipo>");
    sender.sendMessage("/arena delete <id>");
    sender.sendMessage("/arena list");
    sender.sendMessage("/arena info <id>");
    sender.sendMessage("/arena enable <id>");
    sender.sendMessage("/arena disable <id>");
    sender.sendMessage("/arena reload");
    sender.sendMessage("/arena wand");
    sender.sendMessage("/arena setspawn <a|b>");
    sender.sendMessage("/arena pool <id> <scale|clear> [valor]");
  }

  @Command(names = "create")
  public void create(
    @Sender Player player,
    String id,
    String name,
    String typeStr
  ) {
    if (arenaService.getArena(id).isPresent()) {
      player.sendMessage(
        TranslationUtils.translate("arena.already-exists", "id", id)
      );
      return;
    }
    ArenaType type;
    try {
      type = ArenaType.valueOf(typeStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      player.sendMessage(
        TranslationUtils.translate("arena.invalid-type", "type", typeStr)
      );
      return;
    }
    ValidationResult validation = selectionService.validateSelection(
      player.getUniqueId()
    );
    if (!validation.isValid()) {
      player.sendMessage(TranslationUtils.translate("arena.validation-failed"));
      validation
        .getErrors()
        .forEach(error ->
          player.sendMessage(TranslationUtils.translate(error))
        );
      return;
    }
    Optional<Region> regionOpt = selectionService.getSelectedRegion(
      player.getUniqueId()
    );
    Optional<Location> spawnAOpt = selectionService.getSpawnA(
      player.getUniqueId()
    );
    Optional<Location> spawnBOpt = selectionService.getSpawnB(
      player.getUniqueId()
    );
    if (regionOpt.isEmpty() || spawnAOpt.isEmpty() || spawnBOpt.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate("arena.incomplete-selection")
      );
      return;
    }
    Arena arena = Arena.builder()
      .id(id)
      .name(name)
      .displayName(name)
      .type(type)
      .region(regionOpt.get())
      .spawnA(spawnAOpt.get())
      .spawnB(spawnBOpt.get())
      .build();
    arenaService.saveArena(arena);
    templateService
      .createTemplate(arena)
      .thenRun(() -> {
        player.sendMessage(
          TranslationUtils.translate("arena.created", "id", id, "name", name)
        );
        selectionService.clearSelection(player.getUniqueId());
      });
  }

  @Command(names = "delete")
  public void delete(@Sender CommandSender sender, String id) {
    if (arenaService.getArena(id).isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("arena.not-found", "id", id)
      );
      return;
    }
    arenaService.deleteArena(id);
    templateService.deleteTemplate(id);
    sender.sendMessage(TranslationUtils.translate("arena.deleted", "id", id));
  }

  @Command(names = "list")
  public void list(@Sender CommandSender sender) {
    List<Arena> arenas = arenaService.getAllArenas();
    if (arenas.isEmpty()) {
      sender.sendMessage(TranslationUtils.translate("arena.no-arenas"));
      return;
    }
    sender.sendMessage(
      TranslationUtils.translate("arena.list-header", "count", arenas.size())
    );
    arenas.forEach(arena -> {
      int available = poolService.getAvailableCount(arena.getId());
      int inUse = poolService.getInUseCount(arena.getId());
      sender.sendMessage(
        TranslationUtils.translate(
          "arena.list-format",
          "id",
          arena.getId(),
          "name",
          arena.getDisplayName(),
          "type",
          arena.getType().name(),
          "enabled",
          arena.isEnabled(),
          "available",
          available,
          "inUse",
          inUse
        )
      );
    });
  }

  @Command(names = "info")
  public void info(@Sender CommandSender sender, String id) {
    Optional<Arena> arenaOpt = arenaService.getArena(id);
    if (arenaOpt.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("arena.not-found", "id", id)
      );
      return;
    }
    Arena arena = arenaOpt.get();
    ArenaMetrics metrics = metricsService.getMetrics(id);
    sender.sendMessage(
      TranslationUtils.translate(
        "arena.info-header",
        "name",
        arena.getDisplayName()
      )
    );
    sender.sendMessage(
      TranslationUtils.translate("arena.info-id", "id", arena.getId())
    );
    sender.sendMessage(
      TranslationUtils.translate(
        "arena.info-type",
        "type",
        arena.getType().name()
      )
    );
    sender.sendMessage(
      TranslationUtils.translate(
        "arena.info-enabled",
        "enabled",
        arena.isEnabled()
      )
    );
    sender.sendMessage(
      TranslationUtils.translate(
        "arena.info-pool",
        "available",
        poolService.getAvailableCount(id),
        "inUse",
        poolService.getInUseCount(id),
        "resetting",
        poolService.getResettingCount(id)
      )
    );
    sender.sendMessage(
      TranslationUtils.translate(
        "arena.info-metrics",
        "matches",
        metrics.getTotalMatches(),
        "resets",
        metrics.getTotalResets(),
        "avgReset",
        metrics.getAverageResetDurationMs()
      )
    );
  }

  @Command(names = "enable")
  public void enable(@Sender CommandSender sender, String id) {
    Optional<Arena> arenaOpt = arenaService.getArena(id);
    if (arenaOpt.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("arena.not-found", "id", id)
      );
      return;
    }
    Arena arena = arenaOpt.get();
    arena.setEnabled(true);
    arenaService.saveArena(arena);
    sender.sendMessage(TranslationUtils.translate("arena.enabled", "id", id));
  }

  @Command(names = "disable")
  public void disable(@Sender CommandSender sender, String id) {
    Optional<Arena> arenaOpt = arenaService.getArena(id);
    if (arenaOpt.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("arena.not-found", "id", id)
      );
      return;
    }
    Arena arena = arenaOpt.get();
    arena.setEnabled(false);
    arenaService.saveArena(arena);
    sender.sendMessage(TranslationUtils.translate("arena.disabled", "id", id));
  }

  @Command(names = "reload")
  public void reload(@Sender CommandSender sender) {
    arenaService.reloadArenas();
    sender.sendMessage(TranslationUtils.translate("arena.reloaded"));
  }

  @Command(names = "wand")
  public void wand(@Sender Player player) {
    ItemStack wand = new ItemStack(Material.STICK);
    ItemMeta meta = wand.getItemMeta();
    meta.setDisplayName(TranslationUtils.translate("arena.wand-name"));
    meta.setLore(
      List.of(
        TranslationUtils.translate("arena.wand-lore-1"),
        TranslationUtils.translate("arena.wand-lore-2"),
        TranslationUtils.translate("arena.wand-lore-3")
      )
    );
    wand.setItemMeta(meta);
    player.getInventory().addItem(wand);
    player.sendMessage(TranslationUtils.translate("arena.wand-received"));
  }

  @Command(names = "setspawn")
  public void setSpawn(@Sender Player player, String spawn) {
    Location location = Location.builder()
      .world(player.getWorld().getName())
      .x(player.getLocation().getX())
      .y(player.getLocation().getY())
      .z(player.getLocation().getZ())
      .yaw(player.getLocation().getYaw())
      .pitch(player.getLocation().getPitch())
      .build();
    if (spawn.equalsIgnoreCase("a")) {
      selectionService.setSpawnA(player.getUniqueId(), location);
      player.sendMessage(
        TranslationUtils.translate("arena.spawn-set", "spawn", "A")
      );
    } else if (spawn.equalsIgnoreCase("b")) {
      selectionService.setSpawnB(player.getUniqueId(), location);
      player.sendMessage(
        TranslationUtils.translate("arena.spawn-set", "spawn", "B")
      );
    } else {
      player.sendMessage(TranslationUtils.translate("arena.invalid-spawn"));
    }
  }

  @Command(names = "pool")
  public void pool(
    @Sender CommandSender sender,
    String id,
    String action,
    Integer value
  ) {
    if (arenaService.getArena(id).isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("arena.not-found", "id", id)
      );
      return;
    }
    switch (action.toLowerCase()) {
      case "scale" -> {
        if (value == null) {
          sender.sendMessage(
            TranslationUtils.translate("arena.pool-scale-missing-value")
          );
          return;
        }
        sender.sendMessage(
          TranslationUtils.translate(
            "arena.pool-scaling-start",
            "id",
            id,
            "size",
            value
          )
        );
        AtomicInteger lastPercent = new AtomicInteger(-1);
        poolService
          .scalePool(id, value, (arenaId, completed, total) -> {
            int computed = total <= 0
              ? 100
              : (int) Math.round((completed * 100.0) / total);
            int progress = Math.min(100, computed);
            if (progress <= lastPercent.get()) {
              return;
            }
            lastPercent.set(progress);
            plugin
              .getServer()
              .getScheduler()
              .runTask(plugin, () ->
                sender.sendMessage(
                  TranslationUtils.translate(
                    "arena.pool-scaling-progress",
                    "id",
                    arenaId,
                    "progress",
                    progress
                  )
                )
              );
          })
          .thenRun(() ->
            plugin
              .getServer()
              .getScheduler()
              .runTask(plugin, () ->
                sender.sendMessage(
                  TranslationUtils.translate(
                    "arena.pool-scaled",
                    "id",
                    id,
                    "size",
                    value
                  )
                )
              )
          )
          .exceptionally(ex -> {
            plugin
              .getServer()
              .getScheduler()
              .runTask(plugin, () ->
                sender.sendMessage(
                  TranslationUtils.translate("arena.pool-scale-error", "id", id)
                )
              );
            String errorMessage = ex.getMessage() != null
              ? ex.getMessage()
              : ex.toString();
            plugin
              .getLogger()
              .severe("Failed to scale arena pool " + id + ": " + errorMessage);
            return null;
          });
      }
      case "clear" -> {
        poolService.clearPool(id);
        sender.sendMessage(
          TranslationUtils.translate("arena.pool-cleared", "id", id)
        );
      }
      default -> sender.sendMessage(
        TranslationUtils.translate("arena.pool-invalid-action")
      );
    }
  }
}
