package dev.voltic.helektra.plugin.nms.strategy.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import dev.voltic.helektra.plugin.nms.NmsStrategy;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

@SuppressWarnings("all")
public final class NmsHoverStrategy implements NmsStrategy {

    @Override
    public void execute(Player player, Object... args) {
        if (args.length == 0 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException("Expected hover text as first argument");
        }

        String hoverText = (String) args[0];
        HoverEvent hoverEvent = createHoverEvent(hoverText);

        if (args.length > 1 && args[1] instanceof Consumer) {
            ((Consumer<HoverEvent>) args[1]).accept(hoverEvent);
        }
    }

    public static HoverEvent createHoverEvent(String hoverText) {
        try {
            Class<?> textClass = Class.forName("net.md_5.bungee.api.chat.hover.content.Text");
            Object textContent = textClass.getConstructor(String.class).newInstance(hoverText);

            Object contentArray = Array.newInstance(textClass, 1);
            Array.set(contentArray, 0, textContent);

            Constructor<HoverEvent> constructor = HoverEvent.class.getConstructor(HoverEvent.Action.class, contentArray.getClass());
            return constructor.newInstance(HoverEvent.Action.SHOW_TEXT, contentArray);
        } catch (Throwable ex) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create());
        }
    }
}
