package dev.voltic.helektra.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.client.MongoDatabase;
import dev.voltic.helektra.api.IHelektraAPI;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.profile.IFriendService;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.scoreboard.IScoreboardService;
import dev.voltic.helektra.api.repository.RepositoryFactory;
import dev.voltic.helektra.plugin.api.HelektraAPI;
import dev.voltic.helektra.plugin.di.ArenaModule;
import dev.voltic.helektra.plugin.di.FriendModule;
import dev.voltic.helektra.plugin.di.KitModule;
import dev.voltic.helektra.plugin.di.MatchModule;
import dev.voltic.helektra.plugin.di.MongoModule;
import dev.voltic.helektra.plugin.di.ProfileModule;
import dev.voltic.helektra.plugin.di.ProfileStateModule;
import dev.voltic.helektra.plugin.di.ScoreboardModule;
import dev.voltic.helektra.plugin.di.UtilsModule;
import dev.voltic.helektra.plugin.listeners.PlayerListeners;
import dev.voltic.helektra.plugin.model.arena.commands.ArenaCommand;
import dev.voltic.helektra.plugin.model.arena.listeners.ArenaBlockTrackingListener;
import dev.voltic.helektra.plugin.model.arena.listeners.ArenaProtectionListener;
import dev.voltic.helektra.plugin.model.arena.listeners.ArenaSelectionListener;
import dev.voltic.helektra.plugin.model.kit.commands.KitCommand;
import dev.voltic.helektra.plugin.model.match.MatchServiceImpl;
import dev.voltic.helektra.plugin.model.match.commands.DuelCommand;
import dev.voltic.helektra.plugin.model.match.commands.QueueCommand;
import dev.voltic.helektra.plugin.model.match.listeners.MatchListener;
import dev.voltic.helektra.plugin.model.profile.commands.FriendCommand;
import dev.voltic.helektra.plugin.model.profile.commands.SettingsCommand;
import dev.voltic.helektra.plugin.model.profile.friend.listeners.FriendAddPromptListener;
import dev.voltic.helektra.plugin.model.profile.hotbar.HotbarInteractListener;
import dev.voltic.helektra.plugin.model.scoreboard.wrapper.ScoreboardListener;
import dev.voltic.helektra.plugin.model.scoreboard.wrapper.ScoreboardUpdater;
import dev.voltic.helektra.plugin.nms.strategy.NmsStrategies;
import dev.voltic.helektra.plugin.repository.MongoRepositoryFactory;
import dev.voltic.helektra.plugin.utils.BukkitUtils;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.LoggerUtils;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import dev.voltic.helektra.plugin.utils.menu.MenuFactory;
import fr.mrmicky.fastinv.FastInvManager;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.commandflow.CommandManager;
import team.unnamed.commandflow.annotated.AnnotatedCommandTreeBuilder;
import team.unnamed.commandflow.annotated.part.PartInjector;
import team.unnamed.commandflow.annotated.part.defaults.DefaultsModule;
import team.unnamed.commandflow.bukkit.BukkitMapCommandManager;
import team.unnamed.commandflow.bukkit.factory.BukkitModule;

@Getter
public final class Helektra extends JavaPlugin {

  @Getter
  private static Helektra instance;

  private FileConfig settingsConfig;
  private FileConfig kitsConfig;
  private FileConfig menusConfig;
  private FileConfig arenasConfig;
  private FileConfig scoreboardsConfig;
  private FileConfig hotbarConfig;

  private Injector injector;
  private RepositoryFactory repositoryFactory;
  private MongoDatabase database;
  private IKitService kitService;
  private IProfileService profileService;
  private IFriendService friendService;
  private IScoreboardService scoreboardService;
  private IMatchService matchService;
  private MenuFactory menuFactory;
  private BukkitAudiences adventure;
  private ScoreboardUpdater scoreboardUpdater;
  private HelektraAPI api;

  static {
    LoggerUtils.blockMongoDBLogs();
  }

  @Override
  public void onEnable() {
    instance = this;

    log("&7&m----------------------------------");
    log("&fStarting &eHelektra Practice&f...");
    log("&7Server version: &b" + Bukkit.getBukkitVersion());
    log("&7&m----------------------------------");

    try {
      this.adventure = BukkitAudiences.create(this);
      FastInvManager.register(this);

      initConfigs();
      initTranslations();
      initGuice();
      initStrategies();
      registerListeners();
      registerCommands();
      startScoreboard();

      api.setReady();

      log("&aHelektra started successfully.");
      log("&aAPI is now ready for use (v" + api.getVersion() + ")");
    } catch (Exception e) {
      log("&cError starting Helektra: " + e.getMessage());
      e.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
    }

    log("&7&m----------------------------------");
  }

  @Override
  public void onDisable() {
    stopScoreboard();
    log("&cHelektra stopped successfully.");
    if (this.adventure != null) this.adventure.close();
  }

  private void initConfigs() {
    this.settingsConfig = new FileConfig(this, "settings.yml");
    this.kitsConfig = new FileConfig(this, "kits.yml");
    this.menusConfig = new FileConfig(this, "menus.yml");
    this.arenasConfig = new FileConfig(this, "arenas.yml");
    this.scoreboardsConfig = new FileConfig(this, "scoreboards.yml");
    this.hotbarConfig = new FileConfig(this, "hotbar.yml");
  }

  private void initTranslations() {
    TranslationUtils.initialize();
    log("&aTranslation system initialized.");
  }

  private void initGuice() {
    injector = Guice.createInjector(
      new MongoModule(settingsConfig),
      new KitModule(kitsConfig),
      new FriendModule(),
      new ProfileModule(),
      new ProfileStateModule(),
      new UtilsModule(),
      new ArenaModule(),
      new ScoreboardModule(),
      new MatchModule(),
      binder -> {
        binder.bind(Helektra.class).toInstance(this);
        binder.bind(JavaPlugin.class).toInstance(this);
      }
    );

    database = injector.getInstance(MongoDatabase.class);
    repositoryFactory = new MongoRepositoryFactory(database);
    friendService = injector.getInstance(IFriendService.class);
    profileService = injector.getInstance(IProfileService.class);
    menuFactory = injector.getInstance(MenuFactory.class);
    kitService = injector.getInstance(IKitService.class);
    scoreboardService = injector.getInstance(IScoreboardService.class);
    matchService = injector.getInstance(MatchServiceImpl.class);
    scoreboardUpdater = injector.getInstance(ScoreboardUpdater.class);
    api = injector.getInstance(HelektraAPI.class);

    kitService.loadAll();

    IArenaService arenaService = injector.getInstance(IArenaService.class);
    arenaService.loadAll();

    log("&aMongoDB connected and services injected successfully.");
    log("&aHelektra API initialized successfully.");
  }

  private void initStrategies() {
    NmsStrategies.registerAll();
    log("&aNMS strategies registered successfully.");
  }

  private void registerListeners() {
    BukkitUtils.registerGoldenHeads(this);
    log("&aRegistered golden heads successfully.");

    List.of(
      PlayerListeners.class,
      ArenaSelectionListener.class,
      ArenaProtectionListener.class,
      ArenaBlockTrackingListener.class,
      ScoreboardListener.class,
      HotbarInteractListener.class,
      MatchListener.class,
      FriendAddPromptListener.class
    ).forEach(listenerClass -> {
      Object listener = injector.getInstance(listenerClass);
      Bukkit.getPluginManager().registerEvents((Listener) listener, this);
    });
    log("&aListeners registered successfully (via Guice).");
  }

  private void registerCommands() {
    CommandManager manager = new BukkitMapCommandManager(this);
    PartInjector partInjector = PartInjector.create();
    partInjector.install(new DefaultsModule());
    partInjector.install(new BukkitModule());
    AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(
      partInjector
    );

    manager.registerCommands(
      builder.fromClass(injector.getInstance(KitCommand.class))
    );
    manager.registerCommands(
      builder.fromClass(injector.getInstance(SettingsCommand.class))
    );
    manager.registerCommands(
      builder.fromClass(injector.getInstance(ArenaCommand.class))
    );
    manager.registerCommands(
      builder.fromClass(injector.getInstance(QueueCommand.class))
    );
    manager.registerCommands(
      builder.fromClass(injector.getInstance(DuelCommand.class))
    );
    manager.registerCommands(
      builder.fromClass(injector.getInstance(FriendCommand.class))
    );

    log(
      "&aCommands registered successfully (Kits, Settings, Arena, Queue, Duel)."
    );
  }

  private void startScoreboard() {
    scoreboardUpdater.start();
    log("&aScoreboard system started successfully.");
  }

  private void stopScoreboard() {
    if (scoreboardUpdater != null) {
      scoreboardUpdater.stop();
    }
  }

  public IHelektraAPI getAPI() {
    return api;
  }

  private void log(String message) {
    getLogger().info(ColorUtils.stripColor(ColorUtils.translate(message)));
  }
}
