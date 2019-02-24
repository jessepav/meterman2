package com.illcode.meterman2;

import com.illcode.meterman2.ui.UIHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class MMHandler implements UIHandler
{
    public boolean isGameActive() {
        return false;
    }

    public void aboutMenuClicked() {

    }

    public void debugCommand(String cmd) {

    }

    public List<String> getGameNames() {
        List<String> gameNames = new LinkedList<>();
        gameNames.add("Archotron's Lazerbeam");
        gameNames.add("Giant Old Sandwich Game");
        return gameNames;
    }

    public String getGameDescription(String gameName) {
        return "A regular game";
    }

    public void newGame(String gameName) {

    }

    public void loadGameState(InputStream in) {

    }

    public void saveGameState(OutputStream out) {

    }

    public void lookCommand() {

    }

    public void waitCommand() {

    }

    public void entitySelected(String id) {

    }

    public void entityActionSelected(String action) {

    }

    public void exitSelected(int buttonPos) {

    }

    public void setMusicEnabled(boolean enabled) {

    }

    public boolean isMusicEnabled() {
        return false;
    }

    public void setSoundEnabled(boolean enabled) {

    }

    public boolean isSoundEnabled() {
        return false;
    }

    public void setAlwaysLook(boolean alwaysLook) {

    }

    public boolean isAlwaysLook() {
        return false;
    }
}
