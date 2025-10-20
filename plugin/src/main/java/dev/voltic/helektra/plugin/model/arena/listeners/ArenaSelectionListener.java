package dev.voltic.helektra.plugin.model.arena.listeners;

import com.google.inject.Inject;
import dev.voltic.helektra.api.model.arena.IArenaSelectionService;
import dev.voltic.helektra.api.model.arena.Location;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectionListener implements Listener {
    private final IArenaSelectionService selectionService;

    @Inject
    public ArenaSelectionListener(IArenaSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.STICK) {
            return;
        }

        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = item.getItemMeta().getDisplayName();
        String wandName = TranslationUtils.translate("arena.wand-name");

        if (!displayName.equals(wandName)) {
            return;
        }

        if (!player.hasPermission("helektra.arena.wand")) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Location location = Location.builder()
                .world(block.getWorld().getName())
                .x(block.getX())
                .y(block.getY())
                .z(block.getZ())
                .build();

            selectionService.setPosition1(player.getUniqueId(), location);
            player.sendMessage(TranslationUtils.translate("arena.pos1-set",
                "x", block.getX(),
                "y", block.getY(),
                "z", block.getZ()
            ));

            long volume = selectionService.estimateVolume(player.getUniqueId());
            if (volume > 0) {
                player.sendMessage(TranslationUtils.translate("arena.volume-estimate", "volume", volume));
            }

        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Location location = Location.builder()
                .world(block.getWorld().getName())
                .x(block.getX())
                .y(block.getY())
                .z(block.getZ())
                .build();

            selectionService.setPosition2(player.getUniqueId(), location);
            player.sendMessage(TranslationUtils.translate("arena.pos2-set",
                "x", block.getX(),
                "y", block.getY(),
                "z", block.getZ()
            ));

            long volume = selectionService.estimateVolume(player.getUniqueId());
            if (volume > 0) {
                player.sendMessage(TranslationUtils.translate("arena.volume-estimate", "volume", volume));
            }
        }
    }
}
