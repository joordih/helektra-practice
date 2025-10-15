package dev.voltic.helektra.plugin.model.scoreboard.wrapper;

import dev.voltic.helektra.api.model.scoreboard.IScoreboard;
import dev.voltic.helektra.plugin.utils.ColorUtils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FastBoardAdapter implements IScoreboard {

  private final FastBoard fastBoard;

  public FastBoardAdapter(Player player) {
    this.fastBoard = new FastBoard(player);
  }

  @Override
  public Player getPlayer() {
    return fastBoard.getPlayer();
  }

  @Override
  public void updateTitle(String title) {
    fastBoard.updateTitle(ColorUtils.translate(title));
  }

  @Override
  public void updateLines(String... lines) {
    List<String> translatedLines = Arrays.stream(lines)
        .map(ColorUtils::translate)
        .collect(Collectors.toList());
    fastBoard.updateLines(translatedLines);
  }

  @Override
  public void updateLines(List<String> lines) {
    List<String> translatedLines = lines.stream()
        .map(ColorUtils::translate)
        .collect(Collectors.toList());
    fastBoard.updateLines(translatedLines);
  }

  @Override
  public boolean isDeleted() {
    return fastBoard.isDeleted();
  }

  @Override
  public void delete() {
    fastBoard.delete();
  }
}
