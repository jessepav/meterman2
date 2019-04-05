package com.illcode.riverboat;

import com.illcode.meterman2.GameUtils;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.handler.BasicWorldHandler;
import com.illcode.meterman2.handler.LookHandler;
import com.illcode.meterman2.loader.WorldLoader;
import com.illcode.meterman2.model.*;

import java.util.HashMap;
import java.util.Map;

public class RiverboatGame implements Game
{
    static final String RIVERBOAT_NAME = "The Riverboat";

    static final String RIVERBOAT_BASIC_HANDLER_ID = "riverboat-basic-handler";
    static final String RIVERBOAT_LOOK_HANDLER_ID = "riverboat-looker";

    XBundle b;
    WorldLoader worldLoader;
    BasicWorldHandler basicWorldHandler;
    LookHandler lookHandler;

    Map<String,Room> roomIdMap;
    Map<String,Entity> entityIdMap;
    Map<String,Object> gameStateMap;

    public String getName() {
        return RIVERBOAT_NAME;
    }

    public void init() {
        b = XBundle.loadFromPath(Meterman2.assets.pathForGameAsset("riverboat-bundle.xml"));
        Meterman2.bundles.addFirst(b);
        GameUtils.registerActions(b.getElement("riverboat-actions"));
    }

    public void dispose() {
        // empty
    }

    public Map<String,Object> getInitialGameStateMap() {
        if (gameStateMap == null) {
            gameStateMap = new HashMap<>();
            gameStateMap.put("state", new RiverboatState());
        }
        return gameStateMap;
    }

    public void setGameStateMap(Map<String,Object> gameStateMap) {
        this.gameStateMap = gameStateMap;
    }

    public void constructWorld(boolean processContainment) {
        worldLoader = new WorldLoader(Meterman2.bundles);
        worldLoader.loadAllGameObjects(processContainment);
        roomIdMap = worldLoader.getRoomIdMap();
        entityIdMap = worldLoader.getEntityIdMap();
    }

    public Map<String,Entity> getEntityIdMap() {
        return entityIdMap;
    }

    public Map<String,Room> getRoomIdMap() {
        return roomIdMap;
    }

    public Player getStartingPlayer() {
        return worldLoader.getPlayer();
    }

    public Room getStartingRoom() {
        return worldLoader.getStartingRoom();
    }

    public void registerInitialGameHandlers() {
        basicWorldHandler = (BasicWorldHandler) getEventHandler(RIVERBOAT_BASIC_HANDLER_ID);
        basicWorldHandler.register();
        lookHandler = (LookHandler) getEventHandler(RIVERBOAT_LOOK_HANDLER_ID);
        lookHandler.register();
    }

    public GameEventHandler getEventHandler(String id) {
        switch (id) {
        case RIVERBOAT_BASIC_HANDLER_ID:
            if (basicWorldHandler == null) {
                basicWorldHandler = new BasicWorldHandler(RIVERBOAT_BASIC_HANDLER_ID);
                basicWorldHandler.setMaxInventoryItems(4);
            }
            return basicWorldHandler;
        case RIVERBOAT_LOOK_HANDLER_ID:
            if (lookHandler == null) {
                lookHandler = new LookHandler(RIVERBOAT_LOOK_HANDLER_ID);
                lookHandler.loadFromElement(b, "riverlooker");
            }
            return lookHandler;
        default:
            return null;
        }
    }

    public void start(boolean newGame) {
        // empty
    }

    public void about() {
        GameUtils.showPassage("about");
        Meterman2.gm.outputText();
    }

    public Entity reloadEntity(String id) {
        return worldLoader.reloadEntity(id);
    }

    public Room reloadRoom(String id) {
        return worldLoader.reloadRoom(id);
    }

    public TopicMap reloadTopicMap(String id) {
        return worldLoader.reloadTopicMap(id);
    }

    public void debugCommand(String[] args) {
        // empty
    }
}
