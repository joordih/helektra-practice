package dev.voltic.helektra.plugin.model.match.listeners;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.KitRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@Singleton
public class MatchRuleListener implements Listener {

    private final IMatchService matchService;

    @Inject
    public MatchRuleListener(IMatchService matchService) {
        this.matchService = matchService;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(
            player.getUniqueId()
        );
        if (matchOpt.isEmpty()) {
            return;
        }
        IMatch match = matchOpt.get();
        if (match.hasEnded()) {
            return;
        }
        IKit kit = match.getKit();
        if (kit instanceof Kit pluginKit && pluginKit.hasRule(KitRule.HUNGER)) {
            return;
        }
        event.setCancelled(true);
        player.setFoodLevel(20);
        player.setSaturation(20);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Optional<IMatch> matchOpt = matchService.getMatchByParticipant(
            player.getUniqueId()
        );
        if (matchOpt.isEmpty()) {
            return;
        }
        IMatch match = matchOpt.get();
        if (match.hasEnded()) {
            return;
        }
        IKit kit = match.getKit();
        if (
            kit instanceof Kit pluginKit &&
            pluginKit.hasRule(KitRule.REGENERATION)
        ) {
            return;
        }
        EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        String name = reason.name();
        if (
            "NATURAL".equalsIgnoreCase(name) ||
            "SATIATED".equalsIgnoreCase(name) ||
            "REGEN".equalsIgnoreCase(name)
        ) {
            event.setCancelled(true);
        }
    }
}
