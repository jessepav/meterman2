package com.illcode.meterman2;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Game;
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
        GameUtils.setActionShortcuts(Meterman2.bundles.getElement("system-actions"));
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
                            Meterman2.gm.roomChanged(r);
                            Meterman2.gm.refreshUI();
                        } else {
                            Meterman2.ui.showTextDialog("Reload", "Reloading room ID " + args[2] + " failed!", "Rats");
                        }
                        break;
                    case "entity":
                    case "e":
                        Entity e = g.reloadEntity(args[2]);
                        if (e != null) {
                            Meterman2.gm.entityChanged(e);
                            Meterman2.gm.refreshUI();
                        } else {
                            Meterman2.ui.showTextDialog("Reload", "Reloading entity ID " + args[2] + " failed!", "Rats");
                        }
                        break;
                    case "topicmap":
                    case "tm":
                        TopicMap tm = g.reloadTopicMap(args[2]);
                        if (tm == null)
                            Meterman2.ui.showTextDialog("Reload", "Reloading topic map ID " + args[2] + " failed!", "Rats");
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

    public UIHandler transcribe(String text) {
        if (isGameActive())
            gm.print(text);
        return this;
    }
}
