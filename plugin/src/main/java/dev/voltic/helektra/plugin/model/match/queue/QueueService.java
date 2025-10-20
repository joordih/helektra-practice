package dev.voltic.helektra.plugin.model.match.queue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import dev.voltic.helektra.api.model.arena.IArenaService;
import dev.voltic.helektra.api.model.kit.IKit;
import dev.voltic.helektra.api.model.kit.IKitService;
import dev.voltic.helektra.api.model.kit.QueueType;
import dev.voltic.helektra.api.model.match.IMatch;
import dev.voltic.helektra.api.model.match.IMatchService;
import dev.voltic.helektra.api.model.match.MatchType;
import dev.voltic.helektra.api.model.profile.IProfileService;
import dev.voltic.helektra.api.model.profile.ProfileState;
import dev.voltic.helektra.api.model.queue.IQueueService;
import dev.voltic.helektra.api.model.queue.QueueJoinResult;
import dev.voltic.helektra.api.model.queue.QueueTicket;
import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.match.MatchArenaTracker;
import dev.voltic.helektra.plugin.model.match.event.MatchStartedEvent;
import dev.voltic.helektra.plugin.model.profile.state.ProfileStateManager;
import dev.voltic.helektra.plugin.utils.ArenaLocationResolver;
import dev.voltic.helektra.plugin.utils.TranslationUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class QueueService implements IQueueService {

    private final Map<UUID, QueueTicket> tickets = new ConcurrentHashMap<>();
    private final Map<QueueKey, QueueBucket> buckets =
        new ConcurrentHashMap<>();
    private final Map<String, QueueMatchContext> activeMatches =
        new ConcurrentHashMap<>();
    private final IKitService kitService;
    private final IProfileService profileService;
    private final IArenaService arenaService;
    private final IMatchService matchService;
    private final ProfileStateManager profileStateManager;
    private final ArenaLocationResolver locationResolver;
    private final JavaPlugin plugin;
    private final MatchArenaTracker matchArenaTracker;

    @Inject
    public QueueService(
        IKitService kitService,
        IProfileService profileService,
        IArenaService arenaService,
        IMatchService matchService,
        ProfileStateManager profileStateManager,
        ArenaLocationResolver locationResolver,
        JavaPlugin plugin,
        MatchArenaTracker matchArenaTracker
    ) {
        this.kitService = kitService;
        this.profileService = profileService;
        this.arenaService = arenaService;
        this.matchService = matchService;
        this.profileStateManager = profileStateManager;
        this.locationResolver = locationResolver;
        this.plugin = plugin;
        this.matchArenaTracker = matchArenaTracker;
    }

    @Override
    public QueueJoinResult joinQueue(
        UUID playerId,
        String kitName,
        QueueType queueType
    ) {
        Player player = Bukkit.getPlayer(playerId);
        if (
            player == null ||
            !player.isOnline() ||
            profileService.getCachedProfile(playerId).isEmpty()
        ) {
            return QueueJoinResult.PROFILE_NOT_FOUND;
        }
        if (matchService.getMatchByParticipant(playerId).isPresent()) {
            return QueueJoinResult.ALREADY_IN_MATCH;
        }
        if (tickets.containsKey(playerId)) {
            return QueueJoinResult.ALREADY_IN_QUEUE;
        }
        Optional<IKit> kitOptional = kitService.getKit(kitName);
        if (kitOptional.isEmpty()) {
            return QueueJoinResult.KIT_NOT_FOUND;
        }
        IKit kit = kitOptional.get();
        if (arenaService.getArenasByKit(kit.getName()).isEmpty()) {
            return QueueJoinResult.ARENA_UNAVAILABLE;
        }
        QueueKey key = new QueueKey(kit.getName(), queueType);
        QueueTicket ticket = QueueTicket.builder()
            .playerId(playerId)
            .kitName(kit.getName())
            .queueType(queueType)
            .joinedAt(Instant.now())
            .build();
        tickets.put(playerId, ticket);
        QueueBucket bucket = buckets.computeIfAbsent(key, k ->
            new QueueBucket()
        );
        bucket.lock.lock();
        try {
            bucket.players.add(playerId);
        } finally {
            bucket.lock.unlock();
        }
        kit.incrementQueue(playerId, queueType);
        if (
            profileService
                .getCachedProfile(playerId)
                .map(p -> p.getProfileState() != ProfileState.IN_QUEUE)
                .orElse(false)
        ) {
            profileStateManager.setState(player, ProfileState.IN_QUEUE);
        }
        sendJoinMessage(player, kit, queueType);
        tryDispatch(key);
        return QueueJoinResult.SUCCESS;
    }

    @Override
    public boolean leaveQueue(UUID playerId) {
        QueueTicket ticket = removeTicketInternal(playerId);
        if (ticket == null) {
            return false;
        }
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage(TranslationUtils.translate("queue.leave-queue"));
            profileService
                .getCachedProfile(playerId)
                .filter(
                    profile ->
                        profile.getProfileState() == ProfileState.IN_QUEUE
                )
                .ifPresent(profile ->
                    profileStateManager.setState(player, ProfileState.LOBBY)
                );
        }
        return true;
    }

    @Override
    public void clearTicket(UUID playerId) {
        removeTicketInternal(playerId);
    }

    @Override
    public Optional<QueueTicket> getTicket(UUID playerId) {
        return Optional.ofNullable(tickets.get(playerId));
    }

    @Override
    public List<QueueTicket> getQueue(String kitName, QueueType queueType) {
        QueueKey key = new QueueKey(kitName, queueType);
        QueueBucket bucket = buckets.get(key);
        if (bucket == null) {
            return Collections.emptyList();
        }
        bucket.lock.lock();
        try {
            return bucket.players
                .stream()
                .map(tickets::get)
                .filter(ticket -> ticket != null)
                .collect(Collectors.toList());
        } finally {
            bucket.lock.unlock();
        }
    }

    public CompletableFuture<Void> completeMatch(String matchId) {
        QueueMatchContext context = activeMatches.remove(matchId);
        if (context == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> releaseFuture = matchArenaTracker.release(
            matchId
        );
        Optional<IKit> kitOptional = kitService.getKit(context.kitName());
        kitOptional.ifPresent(kit ->
            context
                .participants()
                .forEach(participant -> kit.decrementPlaying())
        );
        context
            .participants()
            .forEach(playerId -> {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    profileStateManager.setState(player, ProfileState.LOBBY);
                }
            });
        return releaseFuture;
    }

    private void sendJoinMessage(Player player, IKit kit, QueueType queueType) {
        if (queueType == QueueType.RANKED) {
            player.sendMessage(
                TranslationUtils.translate(
                    "queue.joined-ranked",
                    "kit",
                    kit.getDisplayName()
                )
            );
        } else {
            player.sendMessage(
                TranslationUtils.translate(
                    "queue.joined",
                    "kit",
                    kit.getDisplayName()
                )
            );
        }
    }

    private void tryDispatch(QueueKey key) {
        QueueBucket bucket = buckets.get(key);
        if (bucket == null) {
            return;
        }
        while (true) {
            List<QueueTicket> candidates;
            bucket.lock.lock();
            try {
                candidates = collectCandidates(bucket);
            } finally {
                bucket.lock.unlock();
            }
            if (candidates.isEmpty()) {
                break;
            }
            List<QueueTicket> confirmed = new ArrayList<>();
            for (QueueTicket ticket : candidates) {
                QueueTicket removed = removeTicketInternal(
                    ticket.getPlayerId()
                );
                if (removed != null) {
                    confirmed.add(removed);
                }
            }
            if (confirmed.size() < 2) {
                confirmed.forEach(this::restoreTicket);
                break;
            }
            dispatchMatch(key, confirmed);
        }
    }

    private List<QueueTicket> collectCandidates(QueueBucket bucket) {
        List<QueueTicket> selected = new ArrayList<>();
        List<UUID> pendingRestore = new ArrayList<>();
        for (UUID uuid : new ArrayList<>(bucket.players)) {
            if (selected.size() >= 2) {
                break;
            }
            QueueTicket ticket = tickets.get(uuid);
            if (ticket == null) {
                bucket.players.remove(uuid);
                continue;
            }
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                bucket.players.remove(uuid);
                removeTicketInternal(uuid);
                continue;
            }
            selected.add(ticket);
            pendingRestore.add(uuid);
            bucket.players.remove(uuid);
        }
        if (selected.size() < 2) {
            bucket.players.addAll(pendingRestore);
            return Collections.emptyList();
        }
        return selected;
    }

    private void dispatchMatch(QueueKey key, List<QueueTicket> participants) {
        Optional<IKit> kitOptional = kitService.getKit(key.kitName());
        if (kitOptional.isEmpty()) {
            handleDispatchFailure(participants, "queue.error-kit");
            return;
        }
        IKit kit = kitOptional.get();
        List<Arena> arenas = arenaService.getArenasByKit(key.kitName());
        if (arenas.isEmpty()) {
            handleDispatchFailure(participants, "queue.no-arena");
            return;
        }
        allocateArena(arenas, 0, key, kit, participants);
    }

    private void allocateArena(
        List<Arena> arenas,
        int index,
        QueueKey key,
        IKit kit,
        List<QueueTicket> participants
    ) {
        if (index >= arenas.size()) {
            handleDispatchFailure(participants, "queue.no-arena");
            return;
        }
        Arena arena = arenas.get(index);
        arenaService
            .assignArena(arena.getId())
            .whenComplete((instance, error) -> {
                if (error != null || instance == null) {
                    allocateArena(arenas, index + 1, key, kit, participants);
                    return;
                }
                prepareMatch(arena, instance, kit, key, participants);
            });
    }

    private void prepareMatch(
        Arena arena,
        ArenaInstance instance,
        IKit kit,
        QueueKey key,
        List<QueueTicket> participants
    ) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player first = Bukkit.getPlayer(participants.get(0).getPlayerId());
            Player second = Bukkit.getPlayer(participants.get(1).getPlayerId());
            if (
                first == null ||
                !first.isOnline() ||
                second == null ||
                !second.isOnline()
            ) {
                arenaService.releaseArena(instance);
                handleDispatchFailure(participants, "queue.match-cancelled");
                return;
            }
            Optional<org.bukkit.Location> spawnA = locationResolver.resolve(
                instance.getInstanceSpawnA()
            );
            Optional<org.bukkit.Location> spawnB = locationResolver.resolve(
                instance.getInstanceSpawnB()
            );
            if (spawnA.isEmpty() || spawnB.isEmpty()) {
                arenaService.releaseArena(instance);
                handleDispatchFailure(participants, "queue.match-cancelled");
                return;
            }
            List<UUID> participantIds = participants
                .stream()
                .map(QueueTicket::getPlayerId)
                .toList();
            IMatch match = matchService.createMatch(
                MatchType.QUEUE,
                arena,
                kit,
                participantIds
            );
            matchArenaTracker.register(match.getMatchId(), instance);
            activeMatches.put(
                match.getMatchId(),
                new QueueMatchContext(kit.getName(), instance, participantIds)
            );
            participants.forEach(ticket -> kit.incrementPlaying());
            profileStateManager.setState(first, ProfileState.IN_GAME);
            profileStateManager.setState(second, ProfileState.IN_GAME);
            first.teleport(spawnA.get());
            second.teleport(spawnB.get());
            if (kit instanceof Kit pluginKit) {
                pluginKit.applyLoadout(first);
                pluginKit.applyLoadout(second);
            }
            first.sendMessage(
                TranslationUtils.translate(
                    "queue.match-start",
                    "kit",
                    kit.getDisplayName()
                )
            );
            second.sendMessage(
                TranslationUtils.translate(
                    "queue.match-start",
                    "kit",
                    kit.getDisplayName()
                )
            );
            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.callEvent(new MatchStartedEvent(match));
        });
    }

    private void handleDispatchFailure(
        List<QueueTicket> tickets,
        String messageKey
    ) {
        for (QueueTicket ticket : tickets) {
            Player player = Bukkit.getPlayer(ticket.getPlayerId());
            if (player != null && player.isOnline()) {
                player.sendMessage(TranslationUtils.translate(messageKey));
                profileStateManager.setState(player, ProfileState.LOBBY);
            }
        }
    }

    private void restoreTicket(QueueTicket ticket) {
        tickets.put(ticket.getPlayerId(), ticket);
        QueueKey key = new QueueKey(ticket.getKitName(), ticket.getQueueType());
        QueueBucket bucket = buckets.computeIfAbsent(key, k ->
            new QueueBucket()
        );
        bucket.lock.lock();
        try {
            bucket.players.add(ticket.getPlayerId());
        } finally {
            bucket.lock.unlock();
        }
        kitService
            .getKit(ticket.getKitName())
            .ifPresent(kit ->
                kit.incrementQueue(ticket.getPlayerId(), ticket.getQueueType())
            );
    }

    private QueueTicket removeTicketInternal(UUID playerId) {
        QueueTicket ticket = tickets.remove(playerId);
        if (ticket == null) {
            return null;
        }
        QueueKey key = new QueueKey(ticket.getKitName(), ticket.getQueueType());
        QueueBucket bucket = buckets.get(key);
        if (bucket != null) {
            bucket.lock.lock();
            try {
                bucket.players.remove(playerId);
            } finally {
                bucket.lock.unlock();
            }
        }
        kitService
            .getKit(ticket.getKitName())
            .ifPresent(kit ->
                kit.decrementQueue(playerId, ticket.getQueueType())
            );
        return ticket;
    }

    private record QueueKey(String kitName, QueueType queueType) {}

    private static final class QueueBucket {

        private final Set<UUID> players = new LinkedHashSet<>();
        private final ReentrantLock lock = new ReentrantLock();
    }

    private record QueueMatchContext(
        String kitName,
        ArenaInstance instance,
        List<UUID> participants
    ) {}
}
