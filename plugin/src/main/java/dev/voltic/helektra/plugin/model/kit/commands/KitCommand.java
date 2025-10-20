package dev.voltic.helektra.plugin.model.kit.commands;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.api.model.profile.IProfile;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.KitRule;
import dev.voltic.helektra.plugin.model.kit.KitRuleHelper;
import dev.voltic.helektra.plugin.model.kit.serialization.SerializedInventory;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsHoverStrategy;
import dev.voltic.helektra.plugin.utils.BukkitUtils;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "kit", "k" })
public class KitCommand implements CommandClass {

  private final IKitService kitService;
  private final IProfileService profileService;

  @Inject
  public KitCommand(IKitService kitService, IProfileService profileService) {
    this.kitService = kitService;
    this.profileService = profileService;
  }

  @Command(names = "")
  public void mainCommand(@Sender CommandSender sender) {
    sender.sendMessage(TranslationUtils.translate("kit.command.help.header"));
    List<String> lines = TranslationUtils.translateList(
      "kit.command.help.lines"
    );
    lines.forEach(sender::sendMessage);
  }

  @Command(names = "buildmode")
  public void buildModeCommand(@Sender Player player) {
    Optional<IProfile> optProfile = profileService.getCachedProfile(
      player.getUniqueId()
    );

    optProfile.ifPresent(profile -> {
      profile.getSettings().setKitMode(!profile.getSettings().isKitMode());
      profileService.saveProfile(profile);
      player.sendMessage(TranslationUtils.translate("kit.command.buildmode"));
    });
  }

  @Command(names = "goldenhead")
  public void goldenHeadCommand(@Sender Player player) {
    player.getInventory().addItem(BukkitUtils.GOLDEN_HEAD);
    player.sendMessage(TranslationUtils.translate("kit.command.goldenhead"));
  }

  @Command(names = "create")
  public void createCommand(@Sender Player player, String kitName) {
    if (kitService.getKit(kitName).isPresent()) {
      player.sendMessage(
        TranslationUtils.translate("kit.command.exists", "kit", kitName)
      );
      return;
    }

    Kit kit = Kit.fromPlayer(kitName, player).build();
    kitService.saveKit(kit);

    player.sendMessage(
      TranslationUtils.translate("kit.command.created", "kit", kitName)
    );
  }

  @Command(names = { "delete", "remove" })
  public void deleteCommand(@Sender CommandSender sender, String kitName) {
    if (kitService.getKit(kitName).isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    kitService.deleteKit(kitName);
    sender.sendMessage(
      TranslationUtils.translate("kit.command.deleted", "kit", kitName)
    );
  }

  @Command(names = "list")
  public void listCommand(@Sender CommandSender sender) {
    List<IKit> kits = kitService.getAllKits();

    if (kits.isEmpty()) {
      sender.sendMessage(TranslationUtils.translate("kit.command.list-empty"));
      return;
    }

    sender.sendMessage(
      TranslationUtils.translate(
        "kit.command.list-header",
        "count",
        kits.size()
      )
    );
    for (IKit kit : kits) {
      sender.sendMessage(
        TranslationUtils.translate(
          "kit.command.list-entry",
          "kit",
          kit.getName(),
          "queue",
          kit.getQueue(QueueType.UNRANKED),
          "playing",
          kit.getPlaying()
        )
      );
    }
  }

  @Command(names = "info")
  public void infoCommand(@Sender Player player, String kitName) {
    Optional<IKit> optKit = kitService.getKit(kitName);

    if (optKit.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    Kit kit = (Kit) optKit.get();

    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.title",
        "display",
        kit.getDisplayName()
      )
    );
    player.sendMessage(
      TranslationUtils.translate("kit.command.info.name", "name", kit.getName())
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.health",
        "health",
        kit.getHealth()
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.damage",
        "damage",
        kit.getDamageMultiplier()
      )
    );
    player.sendMessage(
      TranslationUtils.translate("kit.command.info.slot", "slot", kit.getSlot())
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.queue",
        "queue",
        kit.getQueue(QueueType.UNRANKED)
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.playing",
        "playing",
        kit.getPlaying()
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.items",
        "count",
        kit.getInventory().itemCount()
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.potions",
        "count",
        kit.getPotionEffects().size()
      )
    );
    player.sendMessage(
      TranslationUtils.translate(
        "kit.command.info.arenas",
        "count",
        kit.getArenaIds().size()
      )
    );
    player.sendMessage(
      TranslationUtils.translate("kit.command.info.rules-header")
    );

    for (Map.Entry<KitRule, Boolean> entry : kit.getRules().entrySet()) {
      KitRule rule = entry.getKey();
      boolean enabled = entry.getValue();

      String statusKey = enabled
        ? "kit.command.info.rule-status.enabled"
        : "kit.command.info.rule-status.disabled";
      String status = TranslationUtils.translate(statusKey);
      String ruleName = KitRuleHelper.getName(rule);
      String text = TranslationUtils.translate(
        "kit.command.info.rule-format",
        "status",
        status,
        "rule",
        ruleName
      );

      TextComponent component = new TextComponent(text);

      String command =
        "/kit rule " + kit.getName() + " " + rule.name() + " " + !enabled;
      component.setClickEvent(
        new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
      );

      String hoverKey = enabled
        ? "kit.command.rule.hover.disable"
        : "kit.command.rule.hover.enable";

      String hoverText = TranslationUtils.translate(hoverKey);
      HoverEvent hoverEvent = NmsHoverStrategy.createHoverEvent(hoverText);
      component.setHoverEvent(hoverEvent);

      player.spigot().sendMessage(component);
    }
  }

  @Command(names = { "give", "apply" })
  public void giveCommand(
    @Sender CommandSender sender,
    Player target,
    String kitName
  ) {
    Optional<IKit> optKit = kitService.getKit(kitName);

    if (optKit.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    Kit kit = (Kit) optKit.get();
    kit.applyLoadout(target);
    sender.sendMessage(
      TranslationUtils.translate(
        "kit.command.give.sender",
        "kit",
        kitName,
        "player",
        target.getName()
      )
    );
    target.sendMessage(
      TranslationUtils.translate("kit.received", "kit", kit.getDisplayName())
    );
  }

  @Command(names = "rule")
  public void ruleCommand(
    @Sender CommandSender sender,
    String kitName,
    String ruleName,
    boolean enabled
  ) {
    Optional<IKit> optKit = kitService.getKit(kitName);

    if (optKit.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    KitRule rule = KitRule.fromName(ruleName);
    if (rule == null) {
      sender.sendMessage(
        TranslationUtils.translate("kit.command.rule.invalid")
      );
      for (KitRule r : KitRule.values()) {
        sender.sendMessage(
          TranslationUtils.translate(
            "kit.command.rule.available-format",
            "rule",
            r.name(),
            "name",
            KitRuleHelper.getName(r)
          )
        );
      }
      return;
    }

    Kit kit = (Kit) optKit.get();
    kit.getRules().put(rule, enabled);
    kitService.saveKit(kit);

    String statusKey = enabled
      ? "kit.command.rule.status.enabled"
      : "kit.command.rule.status.disabled";
    String status = TranslationUtils.translate(statusKey);
    String ruleDisplayName = KitRuleHelper.getName(rule);
    sender.sendMessage(
      TranslationUtils.translate(
        "kit.command.rule.updated",
        "rule",
        ruleDisplayName,
        "status",
        status,
        "kit",
        kitName
      )
    );
  }

  @Command(names = "seticon")
  public void setIconCommand(@Sender Player player, String kitName) {
    Optional<IKit> optKit = kitService.getKit(kitName);

    if (optKit.isEmpty()) {
      player.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    ItemStack item = player.getInventory().getItemInMainHand();
    if (item.getType() == Material.AIR) {
      player.sendMessage(TranslationUtils.translate("kit.command.no-item"));
      return;
    }

    Kit kit = (Kit) optKit.get();
    kit.setIcon(item.clone());
    kitService.saveKit(kit);

    player.sendMessage(
      TranslationUtils.translate("kit.command.icon-updated", "kit", kitName)
    );
  }

  @Command(names = "setslot")
  public void setSlotCommand(
    @Sender CommandSender sender,
    String kitName,
    int slot
  ) {
    Optional<IKit> optKit = kitService.getKit(kitName);

    if (optKit.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", kitName)
      );
      return;
    }

    Kit kit = (Kit) optKit.get();
    kit.setSlot(slot);
    kitService.saveKit(kit);
    sender.sendMessage(
      TranslationUtils.translate(
        "kit.command.slot-updated",
        "kit",
        kitName,
        "slot",
        slot
      )
    );
  }

  @Command(names = "clone")
  public void cloneCommand(
    @Sender CommandSender sender,
    String sourceName,
    String newName
  ) {
    Optional<IKit> sourceOpt = kitService.getKit(sourceName);
    if (sourceOpt.isEmpty()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.not-found", "kit", sourceName)
      );
      return;
    }

    if (kitService.getKit(newName).isPresent()) {
      sender.sendMessage(
        TranslationUtils.translate("kit.command.exists", "kit", newName)
      );
      return;
    }

    Kit source = (Kit) sourceOpt.get();
    SerializedInventory inventoryCopy = source.getInventory().copy();
    Kit cloned = Kit.builder()
      .name(newName)
      .displayName(source.getDisplayName())
      .inventory(inventoryCopy)
      .arenaIds(new HashSet<>(source.getArenaIds()))
      .icon(source.getIcon().clone())
      .rules(new EnumMap<>(source.getRules()))
      .slot(source.getSlot())
      .health(source.getHealth())
      .kitEditorSlot(source.getKitEditorSlot())
      .potionEffects(new ArrayList<>(source.getPotionEffects()))
      .damageMultiplier(source.getDamageMultiplier())
      .description(new ArrayList<>(source.getDescription()))
      .build();

    kitService.saveKit(cloned);
    sender.sendMessage(
      TranslationUtils.translate(
        "kit.command.cloned",
        "source",
        sourceName,
        "target",
        newName
      )
    );
  }
}
