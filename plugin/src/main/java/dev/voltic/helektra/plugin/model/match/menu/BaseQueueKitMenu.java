package dev.voltic.helektra.plugin.model.match.menu;

import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.api.model.queue.QueueJoinResult;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.MenuConfigHelper;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import dev.voltic.helektra.plugin.utils.menu.InjectableMenu;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.entity.Player;

public abstract class BaseQueueKitMenu extends InjectableMenu {
    protected final IKitService kitService;
    protected final IQueueService queueService;
    protected final IProfileService profileService;
    protected final ProfileStateManager profileStateManager;
    protected final QueueMenuItemFactory itemFactory;

    protected BaseQueueKitMenu(MenuConfigHelper menuConfig,
                               String menuPath,
                               IKitService kitService,
                               IQueueService queueService,
                               IProfileService profileService,
                               ProfileStateManager profileStateManager,
                               QueueMenuItemFactory itemFactory) {
        super(menuConfig, menuPath);
        this.kitService = kitService;
        this.queueService = queueService;
        this.profileService = profileService;
        this.profileStateManager = profileStateManager;
        this.itemFactory = itemFactory;
    }

    @Override
    public void setup(Player player) {
        List<IKit> kits = kitService.getAllKits();
        AtomicInteger slot = new AtomicInteger();
        for (IKit kit : kits) {
            if (slot.get() >= getInventory().getSize()) {
                break;
            }
            setItem(slot.getAndIncrement(), itemFactory.create(kit, queueType()).build(), event -> {
                if (event.getWhoClicked() instanceof Player p) {
                    handleJoin(p, kit);
                }
            });
        }
    }

    protected abstract QueueType queueType();

    private void handleJoin(Player player, IKit kit) {
        if (!canJoin(player, kit)) {
            return;
        }
        QueueJoinResult result = queueService.joinQueue(player.getUniqueId(), kit.getName(), queueType());
        if (result == QueueJoinResult.SUCCESS) {
            player.closeInventory();
            return;
        }
        player.sendMessage(resolveJoinMessage(result));
    }

    protected boolean canJoin(Player player, IKit kit) {
        return profileService.getCachedProfile(player.getUniqueId())
            .map(profile -> {
                if (profile.getProfileState() != ProfileState.LOBBY) {
                    player.sendMessage(TranslationUtils.translate("queue.already-in-queue"));
                    player.closeInventory();
                    return false;
                }
                return true;
            })
            .orElseGet(() -> {
                player.sendMessage(TranslationUtils.translate("queue.profile-not-found"));
                player.closeInventory();
                return false;
            });
    }

    private String resolveJoinMessage(QueueJoinResult result) {
        return switch (result) {
            case ALREADY_IN_QUEUE -> TranslationUtils.translate("queue.already-in-queue");
            case ALREADY_IN_MATCH -> TranslationUtils.translate("queue.already-in-match");
            case KIT_NOT_FOUND -> TranslationUtils.translate("queue.error-kit");
            case PROFILE_NOT_FOUND -> TranslationUtils.translate("queue.profile-not-found");
            case ARENA_UNAVAILABLE -> TranslationUtils.translate("queue.no-arena");
            default -> TranslationUtils.translate("queue.error-general");
        };
    }
}
