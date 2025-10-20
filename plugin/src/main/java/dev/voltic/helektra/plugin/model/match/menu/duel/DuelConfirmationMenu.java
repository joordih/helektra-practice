package dev.voltic.helektra.plugin.model.match.menu.duel;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DuelConfirmationMenu extends FastInv {
    private final IMatchService matchService;
    private final IKit selectedKit;
    private final Arena selectedArena;
    private final UUID senderUuid;
    private final int rounds;

    public DuelConfirmationMenu(IMatchService matchService, IKit selectedKit, Arena selectedArena,
                                UUID senderUuid, int rounds) {
        super(27, ColorUtils.translate(TranslationUtils.translate("duel.confirmation-title")));
        this.matchService = matchService;
        this.selectedKit = selectedKit;
        this.selectedArena = selectedArena;
        this.senderUuid = senderUuid;
        this.rounds = rounds;
    }

    public void setup(Player player) {
        Player sender = Bukkit.getPlayer(senderUuid);
        String senderName = sender != null ? sender.getName() : "Unknown";
        
        setItem(11,
            new ItemBuilder(XMaterial.LIME_WOOL.get())
                .name(TranslationUtils.translate("duel.accept"))
                .lore(Arrays.asList(
                    "",
                    TranslationUtils.translate("duel.accept-description")
                ))
                .build(),
            event -> {
                if (event.getWhoClicked() instanceof Player p) {
                    acceptDuel(p, sender);
                }
            });
        
        setItem(15,
            new ItemBuilder(XMaterial.RED_WOOL.get())
                .name(TranslationUtils.translate("duel.decline"))
                .lore(Arrays.asList(
                    "",
                    TranslationUtils.translate("duel.decline-description")
                ))
                .build(),
            event -> {
                if (event.getWhoClicked() instanceof Player p) {
                    declineDuel(p, sender);
                }
            });
        
        setItem(13,
            new ItemBuilder(XMaterial.PAPER.get())
                .name(TranslationUtils.translate("duel.request-info"))
                .lore(Arrays.asList(
                    "",
                    TranslationUtils.translate("duel.from", "player", senderName),
                    TranslationUtils.translate("duel.kit", "kit", selectedKit.getDisplayName()),
                    TranslationUtils.translate("duel.arena", "arena", selectedArena.getDisplayName()),
                    TranslationUtils.translate("duel.rounds", "count", rounds)
                ))
                .build());
    }

    private void acceptDuel(Player accepter, Player sender) {
        if (sender == null || !sender.isOnline()) {
            accepter.sendMessage(TranslationUtils.translate("duel.sender-offline"));
            accepter.closeInventory();
            return;
        }
        
        List<UUID> participants = Arrays.asList(sender.getUniqueId(), accepter.getUniqueId());
        matchService.createMatch(MatchType.DUEL, selectedArena, selectedKit, participants);
        
        accepter.sendMessage(TranslationUtils.translate("duel.accepted", "player", sender.getName()));
        sender.sendMessage(TranslationUtils.translate("duel.was-accepted", "player", accepter.getName()));
        
        accepter.closeInventory();
    }

    private void declineDuel(Player decliner, Player sender) {
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(TranslationUtils.translate("duel.was-declined", "player", decliner.getName()));
        }
        
        decliner.sendMessage(TranslationUtils.translate("duel.declined", "player", sender != null ? sender.getName() : "Unknown"));
        decliner.closeInventory();
    }
}
