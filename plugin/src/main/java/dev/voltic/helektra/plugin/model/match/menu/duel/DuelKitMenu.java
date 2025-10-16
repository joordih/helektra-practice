package dev.voltic.helektra.plugin.model.match.menu.duel;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import jakarta.inject.Inject;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelKitMenu extends InjectableMenu {
    private final IKitService kitService;
    private final Helektra helektra;
    private final UUID targetPlayer;

    @Inject
    public DuelKitMenu(MenuConfigHelper menuConfig, IKitService kitService, Helektra helektra) {
        super(menuConfig, "duel-kit");
        this.kitService = kitService;
        this.helektra = helektra;
        this.targetPlayer = null;
    }

    public DuelKitMenu(MenuConfigHelper menuConfig, IKitService kitService, Helektra helektra, UUID targetPlayer) {
        super(menuConfig, "duel-kit");
        this.kitService = kitService;
        this.helektra = helektra;
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void setup(Player player) {
        List<IKit> kits = kitService.getAllKits();
        int slot = 0;
        
        for (IKit kit : kits) {
            if (slot >= getInventory().getSize()) break;
            
            Material material = XMaterial.matchXMaterial(kit.getName().toUpperCase())
                .orElse(XMaterial.IRON_SWORD)
                .get();
            
            List<String> lore = new ArrayList<>(kit.getDescription());
            lore.add("");
            lore.add(TranslationUtils.translate("duel.select-kit"));
            
            setItem(slot++,
                new ItemBuilder(material)
                    .name(TranslationUtils.translate("duel.kit-name", "name", kit.getDisplayName()))
                    .lore(lore)
                    .build(),
                event -> {
                    if (event.getWhoClicked() instanceof Player p) {
                        openArenaMenu(p, kit);
                    }
                });
        }
    }

    private void openArenaMenu(Player player, IKit kit) {
        DuelArenaMenu arenaMenu = new DuelArenaMenu(
            helektra.getInjector().getInstance(MenuConfigHelper.class),
            helektra.getInjector().getInstance(dev.voltic.helektra.api.model.arena.IArenaService.class),
            helektra,
            kit,
            targetPlayer
        );
        arenaMenu.setup(player);
        arenaMenu.open(player);
    }
}
