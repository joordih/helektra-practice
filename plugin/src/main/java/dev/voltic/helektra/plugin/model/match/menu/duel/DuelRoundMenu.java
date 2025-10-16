package dev.voltic.helektra.plugin.model.match.menu.duel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;

public class DuelRoundMenu extends InjectableMenu {
    private final IMatchService matchService;
    private final IKit selectedKit;
    private final Arena selectedArena;
    private final UUID targetPlayer;

    public DuelRoundMenu(MenuConfigHelper menuConfig, IMatchService matchService, 
                         IKit selectedKit, Arena selectedArena, UUID targetPlayer) {
        super(menuConfig, "duel-rounds");
        this.matchService = matchService;
        this.selectedKit = selectedKit;
        this.selectedArena = selectedArena;
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void setup(Player player) {
        int[] rounds = {1, 2, 3, 4, 5};
        int[] slots = {11, 12, 13, 14, 15};
        
        for (int i = 0; i < rounds.length; i++) {
            int roundCount = rounds[i];
            int slot = slots[i];
            
            Material material = getMaterialForRounds(roundCount);
            
            List<String> lore = new ArrayList<>();
            lore.add(TranslationUtils.translate("duel.best-of", "rounds", roundCount));
            lore.add("");
            lore.add(TranslationUtils.translate("duel.click-to-select"));
            
            setItem(slot,
                new ItemBuilder(material)
                    .name(TranslationUtils.translate("duel.rounds", "count", roundCount))
                    .lore(lore)
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player p) {
                        sendDuelRequest(p, roundCount);
                    }
                });
        }
    }

    private Material getMaterialForRounds(int rounds) {
        return switch (rounds) {
            case 1 -> XMaterial.LIME_WOOL.get();
            case 2 -> XMaterial.YELLOW_WOOL.get();
            case 3 -> XMaterial.ORANGE_WOOL.get();
            case 4 -> XMaterial.RED_WOOL.get();
            case 5 -> XMaterial.PURPLE_WOOL.get();
            default -> XMaterial.WHITE_WOOL.get();
        };
    }

    private void sendDuelRequest(Player sender, int rounds) {
        Player target = Bukkit.getPlayer(targetPlayer);
        
        if (target == null || !target.isOnline()) {
            sender.sendMessage(TranslationUtils.translate("duel.player-offline"));
            sender.closeInventory();
            return;
        }
        
        sender.sendMessage(TranslationUtils.translate("duel.request-sent", 
            "player", target.getName(),
            "kit", selectedKit.getDisplayName(),
            "arena", selectedArena.getDisplayName(),
            "rounds", rounds));
        
        target.sendMessage(TranslationUtils.translate("duel.request-received",
            "player", sender.getName(),
            "kit", selectedKit.getDisplayName(),
            "arena", selectedArena.getDisplayName(),
            "rounds", rounds));
        
        openConfirmationMenu(target, sender, rounds);
        sender.closeInventory();
    }

    private void openConfirmationMenu(Player target, Player sender, int rounds) {
        DuelConfirmationMenu confirmMenu = new DuelConfirmationMenu(
            matchService,
            selectedKit,
            selectedArena,
            sender.getUniqueId(),
            rounds
        );
        confirmMenu.setup(target);
        confirmMenu.open(target);
    }
}
