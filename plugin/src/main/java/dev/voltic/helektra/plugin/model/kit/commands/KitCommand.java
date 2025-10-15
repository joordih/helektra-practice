package dev.voltic.helektra.plugin.model.kit.commands;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.KitRule;
import dev.voltic.helektra.plugin.model.kit.KitRuleHelper;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import jakarta.inject.Inject;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "kit", "k" })
public class KitCommand implements CommandClass {
    
    private final IKitService kitService;
    
    @Inject
    public KitCommand(IKitService kitService) {
        this.kitService = kitService;
    }
    
    @Command(names = "")
    public void mainCommand(@Sender CommandSender sender) {
        sender.sendMessage(ColorUtils.translate("&e&lHelektra - Kit Commands"));
        sender.sendMessage(ColorUtils.translate("&7/kit create <nombre> &f- Crea un kit desde tu inventario"));
        sender.sendMessage(ColorUtils.translate("&7/kit delete <nombre> &f- Elimina un kit"));
        sender.sendMessage(ColorUtils.translate("&7/kit list &f- Lista todos los kits"));
        sender.sendMessage(ColorUtils.translate("&7/kit info <nombre> &f- Información de un kit"));
        sender.sendMessage(ColorUtils.translate("&7/kit give <jugador> <kit> &f- Aplica un kit a un jugador"));
        sender.sendMessage(ColorUtils.translate("&7/kit rule <kit> <regla> <true|false> &f- Configura una regla"));
        sender.sendMessage(ColorUtils.translate("&7/kit seticon <kit> &f- Establece el icono del kit"));
        sender.sendMessage(ColorUtils.translate("&7/kit setslot <kit> <slot> &f- Establece el slot del kit"));
        sender.sendMessage(ColorUtils.translate("&7/kit clone <origen> <nuevo> &f- Clona un kit"));
    }
    
    @Command(names = "create")
    public void createCommand(@Sender Player player, String kitName) {
        if (kitService.getKit(kitName).isPresent()) {
            player.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' ya existe."));
            return;
        }
        
        Kit kit = Kit.fromPlayer(kitName, player).build();
        kitService.saveKit(kit);
        
        player.sendMessage(ColorUtils.translate("&aKit '&e" + kitName + "&a' creado correctamente."));
    }
    
    @Command(names = { "delete", "remove" })
    public void deleteCommand(@Sender CommandSender sender, String kitName) {
        if (kitService.getKit(kitName).isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        kitService.deleteKit(kitName);
        sender.sendMessage(ColorUtils.translate("&aKit '&e" + kitName + "&a' eliminado."));
    }
    
    @Command(names = "list")
    public void listCommand(@Sender CommandSender sender) {
        List<IKit> kits = kitService.getAllKits();
        
        if (kits.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cNo hay kits disponibles."));
            return;
        }
        
        sender.sendMessage(ColorUtils.translate("&e&lKits disponibles &7(" + kits.size() + ")&e:"));
        for (IKit kit : kits) {
            sender.sendMessage(ColorUtils.translate(
                "  &7- &e" + kit.getName() + 
                " &8| &7Queue: &b" + kit.getQueue() + 
                " &7Playing: &a" + kit.getPlaying()
            ));
        }
    }
    
    @Command(names = "info")
    public void infoCommand(@Sender CommandSender sender, String kitName) {
        Optional<IKit> optKit = kitService.getKit(kitName);
        
        if (optKit.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        Kit kit = (Kit) optKit.get();
        sender.sendMessage(ColorUtils.translate("&e&l" + kit.getDisplayName()));
        sender.sendMessage(ColorUtils.translate("&7Name: &f" + kit.getName()));
        sender.sendMessage(ColorUtils.translate("&7Health: &c" + kit.getHealth()));
        sender.sendMessage(ColorUtils.translate("&7Damage Multiplier: &6" + kit.getDamageMultiplier()));
        sender.sendMessage(ColorUtils.translate("&7Slot: &b" + kit.getSlot()));
        sender.sendMessage(ColorUtils.translate("&7Queue: &e" + kit.getQueue()));
        sender.sendMessage(ColorUtils.translate("&7Playing: &a" + kit.getPlaying()));
        sender.sendMessage(ColorUtils.translate("&7Items: &f" + kit.getItems().size()));
        sender.sendMessage(ColorUtils.translate("&7Potion Effects: &d" + kit.getPotionEffects().size()));
        sender.sendMessage(ColorUtils.translate("&7Arenas: &9" + kit.getArenaIds().size()));
        
        sender.sendMessage(ColorUtils.translate("&7Rules:"));
        for (Map.Entry<KitRule, Boolean> entry : kit.getRules().entrySet()) {
            String status = entry.getValue() ? "&a✔" : "&c✘";
            String ruleName = KitRuleHelper.getName(entry.getKey());
            sender.sendMessage(ColorUtils.translate("  " + status + " &7" + ruleName));
        }
    }
    
    @Command(names = { "give", "apply" })
    public void giveCommand(@Sender CommandSender sender, Player target, String kitName) {
        Optional<IKit> optKit = kitService.getKit(kitName);
        
        if (optKit.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        Kit kit = (Kit) optKit.get();
        kit.applyLoadout(target);
        sender.sendMessage(ColorUtils.translate("&aKit '&e" + kitName + "&a' aplicado a &e" + target.getName() + "&a."));
        target.sendMessage(ColorUtils.translate("&aHas recibido el kit &e" + kit.getDisplayName() + "&a."));
    }
    
    @Command(names = "rule")
    public void ruleCommand(@Sender CommandSender sender, String kitName, String ruleName, boolean enabled) {
        Optional<IKit> optKit = kitService.getKit(kitName);
        
        if (optKit.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        KitRule rule = KitRule.fromName(ruleName);
        if (rule == null) {
            sender.sendMessage(ColorUtils.translate("&cRegla no válida. Reglas disponibles:"));
            for (KitRule r : KitRule.values()) {
                sender.sendMessage(ColorUtils.translate("  &7- &e" + r.name()));
            }
            return;
        }
        
        Kit kit = (Kit) optKit.get();
        kit.getRules().put(rule, enabled);
        kitService.saveKit(kit);
        
        String status = enabled ? "&ahabilitada" : "&cdeshabilitada";
        String ruleDisplayName = KitRuleHelper.getName(rule);
        sender.sendMessage(ColorUtils.translate("&aRegla '&e" + ruleDisplayName + "&a' " + status + " &aen el kit '&e" + kitName + "&a'."));
    }
    
    @Command(names = "seticon")
    public void setIconCommand(@Sender Player player, String kitName) {
        Optional<IKit> optKit = kitService.getKit(kitName);
        
        if (optKit.isEmpty()) {
            player.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ColorUtils.translate("&cDebes tener un item en la mano."));
            return;
        }
        
        Kit kit = (Kit) optKit.get();
        kit.setIcon(item.clone());
        kitService.saveKit(kit);
        
        player.sendMessage(ColorUtils.translate("&aIcono del kit '&e" + kitName + "&a' actualizado."));
    }
    
    @Command(names = "setslot")
    public void setSlotCommand(@Sender CommandSender sender, String kitName, int slot) {
        Optional<IKit> optKit = kitService.getKit(kitName);
        
        if (optKit.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + kitName + "&c' no existe."));
            return;
        }
        
        Kit kit = (Kit) optKit.get();
        kit.setSlot(slot);
        kitService.saveKit(kit);
        sender.sendMessage(ColorUtils.translate("&aSlot del kit '&e" + kitName + "&a' establecido en &e" + slot + "&a."));
    }
    
    @Command(names = "clone")
    public void cloneCommand(@Sender CommandSender sender, String sourceName, String newName) {
        Optional<IKit> sourceOpt = kitService.getKit(sourceName);
        if (sourceOpt.isEmpty()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + sourceName + "&c' no existe."));
            return;
        }
        
        if (kitService.getKit(newName).isPresent()) {
            sender.sendMessage(ColorUtils.translate("&cEl kit '&e" + newName + "&c' ya existe."));
            return;
        }
        
        Kit source = (Kit) sourceOpt.get();
        Kit cloned = Kit.builder()
            .name(newName)
            .displayName(source.getDisplayName())
            .items(new ArrayList<>(source.getItems()))
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
        sender.sendMessage(ColorUtils.translate("&aKit '&e" + sourceName + "&a' clonado como '&e" + newName + "&a'."));
    }
}
