package dev.voltic.helektra.plugin.model.profile.commands;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.model.profile.menu.SettingsMenu;
import jakarta.inject.Inject;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

@Command(names = { "settings", "s" })
public class SettingsCommand implements CommandClass {

    private final Helektra helektra;

    @Inject
    public SettingsCommand(Helektra helektra) {
        this.helektra = helektra;
    }

    @Command(names = { "" })
    public void mainCommand(@Sender Player sender) {
        helektra.getMenuFactory().openMenu(SettingsMenu.class, sender);
    }
}
