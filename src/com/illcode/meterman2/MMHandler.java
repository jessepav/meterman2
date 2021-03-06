package com.illcode.meterman2;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.model.TopicMap;
import com.illcode.meterman2.ui.UIHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;

public class MMHandler implements UIHandler
{
    public void uiInitialized() {
        GameUtils.setActionShortcuts(Meterman2.bundles.getElement("system-action-shortcuts"));
    }

    public boolean isGameActive() {
        return gm.getGame() != null;
    }

    public void aboutMenuClicked() {
        Game g = gm.getGame();
        if (g != null)
            g.about();
    }

    public void debugCommand(String cmd) {
        if (cmd == null || cmd.isEmpty())
            return;
        Game g = gm.getGame();
        if (g != null) {
            String[] args = StringUtils.split(cmd);
            switch (args[0]) {
            case "reload": // We handle reloads ourself.
                if (args.length == 3) {
                    switch (args[1]) {
                    case "room":
                    case "r":
                        Room r = g.reloadRoom(args[2]);
                        if (r != null) {
                            gm.roomChanged(r);
                            gm.refreshUI();
                        } else {
                            ui.showTextDialogImpl("Reload", "Reloading room ID " + args[2] + " failed!", "Rats");
                        }
                        break;
                    case "entity":
                    case "e":
                        Entity e = g.reloadEntity(args[2]);
                        if (e != null) {
                            gm.entityChanged(e);
                            gm.refreshUI();
                        } else {
                            ui.showTextDialogImpl("Reload", "Reloading entity ID " + args[2] + " failed!", "Rats");
                        }
                        break;
                    case "topicmap":
                    case "tm":
                        TopicMap tm = g.reloadTopicMap(args[2]);
                        if (tm == null)
                            ui.showTextDialogImpl("Reload", "Reloading topic map ID " + args[2] + " failed!", "Rats");
                        break;
                    }
                } else if (args.length == 2) {
                    switch (args[1]) {
                    case "glue":
                        Meterman2.gamesList.loadGamesFromGlue();
                        break;
                    }
                }
                break;
            default:
                g.debugCommand(args);
                break;
            }
        }
    }

    public List<String> getGameNames() {
        return Meterman2.gamesList.getGameNames();
    }

    public void newGame(String gameName) {
        if (!Meterman2.gamesList.gameExists(gameName)) {
            ui.showTextDialogImpl("Invalid Game", "\"" + gameName + "\"is not a valid game.", "Ayaa");
        } else {
            Game game = Meterman2.gamesList.createGame(gameName);
            if (game == null)
                ui.showTextDialogImpl("Error", "Error creating game!", "OK");
            else
                gm.newGame(game);
        }
    }

    public void loadGameState(InputStream in) {
        gm.loadGameState(in);
    }

    public void saveGameState(OutputStream out) {
        gm.saveGameState(out);
    }

    public void endGame() {
        gm.endGame();
        gm.nextTurn();
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

    public UIHandler transcribe(String text) {
        if (isGameActive())
            gm.print(text);
        return this;
    }

    public UIHandler transcribe(String text, boolean newPar) {
        if (isGameActive()) {
            if (newPar)
                gm.newPar();
            gm.print(text);
        }
        return this;
    }

    public String getTranscript() {
        if (isGameActive())
            return gm.getTranscript();
        else
            return null;
    }
}
