package dev.voltic.helektra.plugin.nms;

import dev.voltic.helektra.api.strategy.StrategyFactory;
import dev.voltic.helektra.plugin.nms.strategy.impl.*;
import org.bukkit.entity.Player;

public final class NmsStrategies {

    private NmsStrategies() {}

    public static final Executor TITLE = new Executor(
        NmsSendTitleStrategy.class
    );
    public static final Executor ACTION_BAR = new Executor(
        NmsActionBarStrategy.class
    );
    public static final Executor PING = new Executor(NmsPingStrategy.class);
    public static final Executor PACKET = new Executor(
        NmsSendPacketStrategy.class
    );
    public static final Executor BOSS_BAR = new Executor(
        NmsBossBarStrategy.class
    );

    public static class Executor {

        private final Class<? extends NmsStrategy> strategyClass;

        private Executor(Class<? extends NmsStrategy> strategyClass) {
            this.strategyClass = strategyClass;
        }

        public void execute(Player player, Object... args) {
            NmsStrategy strategy = StrategyFactory.getStrategy(
                NmsStrategy.class,
                strategyClass
            );
            strategy.execute(player, args);
        }

        @SuppressWarnings("unchecked")
        public <T extends NmsStrategy> T get() {
            return (T) StrategyFactory.getStrategy(
                NmsStrategy.class,
                strategyClass
            );
        }
    }

    public static void registerAll() {
        StrategyFactory.registerStrategy(
            NmsStrategy.class,
            NmsSendTitleStrategy.class,
            new NmsSendTitleStrategy()
        );
        StrategyFactory.registerStrategy(
            NmsStrategy.class,
            NmsActionBarStrategy.class,
            new NmsActionBarStrategy()
        );
        StrategyFactory.registerStrategy(
            NmsStrategy.class,
            NmsPingStrategy.class,
            new NmsPingStrategy()
        );
        StrategyFactory.registerStrategy(
            NmsStrategy.class,
            NmsSendPacketStrategy.class,
            new NmsSendPacketStrategy()
        );
        StrategyFactory.registerStrategy(
            NmsStrategy.class,
            NmsBossBarStrategy.class,
            new NmsBossBarStrategy()
        );
    }
}
