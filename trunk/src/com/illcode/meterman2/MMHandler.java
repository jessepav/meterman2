package com.illcode.meterman2;

import com.illcode.meterman2.model.Game;
import com.illcode.meterman2.ui.UIHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;

public class MMHandler implements UIHandler
{
    public boolean isGameActive() {
        return gm.getGame() != null;
    }

    public void aboutMenuClicked() {
        Game g = gm.getGame();
        if (g != null)
            g.about();
    }

    public void debugCommand(String cmd) {
        Game g = gm.getGame();
        if (g != null)
            g.debugCommand(cmd);
    }

    public List<String> getGameNames() {
        return Meterman2.gamesList.getGameNames();
    }

    public void newGame(String gameName) {
        Game game = Meterman2.gamesList.createGame(gameName);
        if (game == null)
            ui.showTextDialog("Error", "Error creating game!", "OK");
        else
            gm.newGame(game);
    }

    public void loadGameState(InputStream in) {
        gm.loadGameState(in);
    }

    public void saveGameState(OutputStream out) {
        gm.saveGameState(out);
    }

    public void lookCommand() {
        gm.lookCommand();
    }

    public void waitCommand() {
        gm.waitCommand();
    }

    public void entitySelected(String id) {
        gm.entitySelected(id);
    }

    public void entityActionSelected(MMActions.Action action) {
        gm.entityActionSelected(action);
    }

    public void exitSelected(int buttonPos) {
        gm.exitSelected(buttonPos);
    }

    public void setMusicEnabled(boolean enabled) {
        Meterman2.sound.setMusicEnabled(enabled);
    }

    public boolean isMusicEnabled() {
        return Meterman2.sound.isMusicEnabled();
    }

    public void setSoundEnabled(boolean enabled) {
        Meterman2.sound.setSoundEnabled(enabled);
    }

    public boolean isSoundEnabled() {
        return Meterman2.sound.isSoundEnabled();
    }

    public void setAlwaysLook(boolean alwaysLook) {
        gm.setAlwaysLook(alwaysLook);
    }

    public boolean isAlwaysLook() {
        return gm.isAlwaysLook();
    }
}
