package com.illcode.riverboat;

import com.illcode.meterman2.GameUtils;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.handler.BasicWorldHandler;
import com.illcode.meterman2.handler.LookHandler;
import com.illcode.meterman2.handler.UiImageHandler;
import com.illcode.meterman2.loader.WorldLoader;
import com.illcode.meterman2.model.*;

import java.util.HashMap;
import java.util.Map;

public class RiverboatGame implements Game
{
    static final String RIVERBOAT_NAME = "The Riverboat";

    static final String BASIC_HANDLER_ID = "basic-handler";
    static final String LOOK_HANDLER_ID = "looker";
    static final String FRAME_IMAGE_HANDLER_ID = "frame-imager";
    static final String ENTITY_IMAGE_HANDLER_ID = "entity-imager";

    XBundle b;
    WorldLoader worldLoader;
    BasicWorldHandler basicWorldHandler;
    LookHandler lookHandler;
    UiImageHandler frameImageHandler;
    UiImageHandler entityImageHandler;

    Map<String,Room> roomIdMap;
    Map<String,Entity> entityIdMap;
    Map<String,Object> gameStateMap;

    GameUtils.DialogPassage aboutDialogPassage;

    public String getName() {
        return RIVERBOAT_NAME;
    }

    public void init() {
        b = XBundle.loadFromPath(Meterman2.assets.pathForGameAsset("riverboat-bundle.xml"));
        Meterman2.bundles.addFirst(b);
        GameUtils.registerActions(b.getElement("riverboat-actions"));
        Meterman2.ui.loadImageMap(b.getElement("riverboat-images"));
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
        basicWorldHandler = (BasicWorldHandler) getEventHandler(BASIC_HANDLER_ID);
        basicWorldHandler.register();
        lookHandler = (LookHandler) getEventHandler(LOOK_HANDLER_ID);
        lookHandler.register();
        frameImageHandler = (UiImageHandler) getEventHandler(FRAME_IMAGE_HANDLER_ID);
        frameImageHandler.register();
        entityImageHandler = (UiImageHandler) getEventHandler(ENTITY_IMAGE_HANDLER_ID);
        entityImageHandler.register();
    }

    public GameEventHandler getEventHandler(String id) {
        switch (id) {
        case BASIC_HANDLER_ID:
            if (basicWorldHandler == null) {
                basicWorldHandler = new BasicWorldHandler(BASIC_HANDLER_ID);
                basicWorldHandler.setMaxInventoryItems(4);
            }
            return basicWorldHandler;
        case LOOK_HANDLER_ID:
            if (lookHandler == null) {
                lookHandler = new LookHandler(LOOK_HANDLER_ID);
                lookHandler.loadFromElement(b, "riverlooker");
            }
            return lookHandler;
        case FRAME_IMAGE_HANDLER_ID:
            if (frameImageHandler == null) {
                frameImageHandler = UiImageHandler.createFrameImageHandler(FRAME_IMAGE_HANDLER_ID);
                frameImageHandler.loadFromElement(b, "riverboat-frame-images");
            }
            return frameImageHandler;
        case ENTITY_IMAGE_HANDLER_ID:
            if (entityImageHandler == null) {
                entityImageHandler = UiImageHandler.createEntityImageHandler(ENTITY_IMAGE_HANDLER_ID);
                entityImageHandler.loadFromElement(b, "riverboat-entity-images");
            }
            return entityImageHandler;
        default:
            return null;
        }
    }

    public void start(boolean newGame) {
        // empty
    }

    public void about() {
        if (aboutDialogPassage == null)
            aboutDialogPassage = GameUtils.loadDialogPassage("about");
        aboutDialogPassage.show();
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
