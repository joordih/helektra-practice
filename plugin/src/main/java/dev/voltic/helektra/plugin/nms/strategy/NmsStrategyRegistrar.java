package dev.voltic.helektra.plugin.nms.strategy;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.voltic.helektra.api.strategy.StrategyFactory;
import dev.voltic.helektra.plugin.nms.NmsStrategy;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsActionBarStrategy;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsPingStrategy;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsSendPacketStrategy;
import dev.voltic.helektra.plugin.nms.strategy.impl.NmsSendTitleStrategy;

public final class NmsStrategyRegistrar {
    private NmsStrategyRegistrar() {
    }

    public static void registerAll() {
        Map<Class<?>, NmsStrategy> strategies = new LinkedHashMap<>();

        strategies.put(String.class, new NmsActionBarStrategy());
        strategies.put(Integer.class, new NmsPingStrategy());
        strategies.put(Object[].class, new NmsSendTitleStrategy());
        strategies.put(Object.class, new NmsSendPacketStrategy());

        strategies.forEach(
                (parameter, strategy) -> StrategyFactory.registerStrategy(NmsStrategy.class, parameter, strategy));
    }
}
