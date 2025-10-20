package dev.voltic.helektra.plugin.model.match.menu;

import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.xseries.XMaterial;
import fr.mrmicky.fastinv.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

@Singleton
public class QueueMenuItemFactory {

  public ItemBuilder create(IKit kit, QueueType queueType) {
    XMaterial match = XMaterial.matchXMaterial(kit.getIcon().getType());
    Material material = match != null ? match.get() : null;
    if (material == null) {
      material = XMaterial.STONE.get();
    }
    List<String> lore = new ArrayList<>(kit.getDescription());
    lore.add("");
    if (queueType == QueueType.RANKED) {
      int rating = 1000;
      lore.add(
        TranslationUtils.translate("queue.your-rating", "rating", rating)
      );
    }
    lore.add(
      TranslationUtils.translate(
        "queue.in-queue",
        "count",
        kit.getQueue(queueType)
      )
    );
    lore.add(
      TranslationUtils.translate("queue.playing", "count", kit.getPlaying())
    );
    lore.add("");
    lore.add(TranslationUtils.translate("queue.click-to-join"));
    return new ItemBuilder(material)
      .name(
        TranslationUtils.translate(
          "queue.kit-name",
          "name",
          kit.getDisplayName()
        )
      )
      .lore(lore);
  }
}
