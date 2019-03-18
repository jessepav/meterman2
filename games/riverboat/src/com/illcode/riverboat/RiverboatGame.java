package com.illcode.riverboat;

import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Game;
import com.illcode.meterman2.model.Player;
import com.illcode.meterman2.model.Room;

import java.util.Map;

public class RiverboatGame implements Game
{
    public static final String RIVERBOAT_NAME = "The Riverboat";

    public String getName() {
        return RIVERBOAT_NAME;
    }

    public void init() {

    }

    public void dispose() {

    }

    public Map<String,Object> getGameStateMap() {
        return null;
    }

    public void setGameStateMap(Map<String,Object> gameStateMap) {

    }

    public void constructWorld() {

    }

    public Map<String,Entity> getEntityIdMap() {
        return null;
    }

    public Map<String,Room> getRoomIdMap() {
        return null;
    }

    public Player getPlayer() {
        return null;
    }

    public Room getStartingRoom() {
        return null;
    }

    public void registerInitialGameHandlers() {

    }

    public GameEventHandler getEventHandler(String id) {
        return null;
    }

    public void start(boolean newGame) {

    }

    public void about() {

    }

    public void debugCommand(String command) {

    }
}
