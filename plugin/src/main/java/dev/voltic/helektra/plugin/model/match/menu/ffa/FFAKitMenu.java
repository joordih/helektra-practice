package dev.voltic.helektra.plugin.model.match.menu.ffa;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.match.menu.ffa.FFAMainMenu.FFAType;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FFAKitMenu extends InjectableMenu {
    private final IKitService kitService;
    private final Helektra helektra;
    private final FFAType ffaType;

    public FFAKitMenu(MenuConfigHelper menuConfig, IKitService kitService, Helektra helektra, FFAType ffaType) {
        super(menuConfig, "ffa-kit");
        this.kitService = kitService;
        this.helektra = helektra;
        this.ffaType = ffaType;
    }

    @Override
    public void setup(Player player) {
        List<IKit> kits = kitService.getAllKits();
        int slot = 0;
        
        for (IKit kit : kits) {
            if (slot >= getInventory().getSize()) break;
            
            Material material = XMaterial.matchXMaterial(kit.getName().toUpperCase())
                .orElse(XMaterial.GOLDEN_SWORD)
                .get();
            
            List<String> lore = new ArrayList<>(kit.getDescription());
            lore.add("");
            lore.add(TranslationUtils.translate("ffa.select-kit"));
            
            setItem(slot++,
                new ItemBuilder(material)
                    .name(TranslationUtils.translate("ffa.kit-name", "name", kit.getDisplayName()))
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
        FFAArenaMenu arenaMenu = new FFAArenaMenu(
            helektra.getInjector().getInstance(MenuConfigHelper.class),
            helektra.getInjector().getInstance(dev.voltic.helektra.api.model.arena.IArenaService.class),
            helektra.getInjector().getInstance(dev.voltic.helektra.api.model.match.IMatchService.class),
            kit,
            ffaType
        );
        arenaMenu.setup(player);
        arenaMenu.open(player);
    }
}
