package com.illcode.riverboat;

import com.illcode.meterman2.GameUtils;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.handler.BasicWorldHandler;
import com.illcode.meterman2.loader.WorldLoader;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Game;
import com.illcode.meterman2.model.Player;
import com.illcode.meterman2.model.Room;

import java.util.Collections;
import java.util.Map;

public class RiverboatGame implements Game
{
    public static final String RIVERBOAT_NAME = "The Riverboat";

    static final String RIVERBOAT_BASIC_HANDLER_ID = "riverboat-basic-handler";

    private WorldLoader worldLoader;
    private BasicWorldHandler basicWorldHandler;

    public String getName() {
        return RIVERBOAT_NAME;
    }

    public void init() {
        XBundle b = XBundle.loadFromPath(Meterman2.assets.pathForGameAsset("riverboat-bundle.xml"));
        Meterman2.bundles.addFirst(b);
        Meterman2.script.evalGameScript("import com.illcode.riverboat.*;");
    }

    public void dispose() {

    }

    public Map<String,Object> getGameStateMap() {
        return Collections.emptyMap();
    }

    public void setGameStateMap(Map<String,Object> gameStateMap) {

    }

    public void constructWorld() {
        worldLoader = new WorldLoader(Meterman2.bundles);
        worldLoader.loadAllGameObjects();
    }

    public Map<String,Entity> getEntityIdMap() {
        return worldLoader.getEntityIdMap();
    }

    public Map<String,Room> getRoomIdMap() {
        return worldLoader.getRoomIdMap();
    }

    public Player getPlayer() {
        return worldLoader.getPlayer();
    }

    public Room getStartingRoom() {
        return worldLoader.getStartingRoom();
    }

    public void registerInitialGameHandlers() {
        basicWorldHandler = new BasicWorldHandler(RIVERBOAT_BASIC_HANDLER_ID);
        basicWorldHandler.register();
    }

    public GameEventHandler getEventHandler(String id) {
        switch (id) {
        case RIVERBOAT_BASIC_HANDLER_ID:
            if (basicWorldHandler == null)
                basicWorldHandler = new BasicWorldHandler(RIVERBOAT_BASIC_HANDLER_ID);
            return basicWorldHandler;
        default:
            return null;
        }
    }

    public void start(boolean newGame) {

    }

    public void about() {
        GameUtils.showPassage("about");
    }

    public boolean reloadEntity(String id) {
        return worldLoader.reloadEntity(id);
    }

    public boolean reloadRoom(String id) {
        return worldLoader.reloadRoom(id);
    }

    public void debugCommand(String command) {

    }
}
