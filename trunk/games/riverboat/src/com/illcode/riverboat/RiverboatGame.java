package com.illcode.riverboat;

import com.illcode.meterman2.Game;
import com.illcode.meterman2.GameUtils;
import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.handler.*;
import com.illcode.meterman2.loader.WorldLoader;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiverboatGame implements Game
{
    static final String RIVERBOAT_NAME = "The Riverboat (test game)";

    static final String BASIC_HANDLER_ID = "basic-handler";
    static final String TIME_HANDLER_ID = "time-handler";
    static final String LOOK_HANDLER_ID = "looker";
    static final String FRAME_IMAGE_HANDLER_ID = "frame-imager";
    static final String ENTITY_IMAGE_HANDLER_ID = "entity-imager";
    static final String SCRIPTED_HANDLER_ID = "scripted-handler";

    XBundle b;
    WorldLoader worldLoader;
    BasicWorldHandler basicWorldHandler;
    TimeOfDayHandler timeHandler;
    LookHandler lookHandler;
    UiImageHandler frameImageHandler;
    UiImageHandler entityImageHandler;
    ScriptedHandler scriptedHandler;

    Map<String,Room> roomIdMap;
    Map<String,Entity> entityIdMap;
    Map<String,Object> gameStateMap;

    GameUtils.DialogPassage aboutDialogPassage;
    MMScript.ScriptedMethod startMethod;

    public String getName() {
        return RIVERBOAT_NAME;
    }

    public void init() {
        b = XBundle.loadFromPath(Meterman2.assets.pathForGameAsset("riverboat-bundle.xml"));
        Meterman2.bundles.addFirst(b);
        GameUtils.registerActions(b.getElement("riverboat-actions"));
        Meterman2.ui.loadImageMap(b.getElement("riverboat-images"));
        List<MMScript.ScriptedMethod> methods = Meterman2.script.
            getScriptedMethods("game-start-script", GameUtils.readGameAsset("game-start.bsh"));
        if (!methods.isEmpty())
            startMethod = methods.get(0);

    }

    public void dispose() {
        // empty
    }

    public Map<String,Object> getInitialGameStateMap() {
        if (gameStateMap == null) {
            gameStateMap = new HashMap<>();
            gameStateMap.put("state", new RiverboatState());
            gameStateMap.put("hash", new HashMap());
        }
        return gameStateMap;
    }

    public void setGameStateMap(Map<String,Object> gameStateMap) {
        this.gameStateMap = gameStateMap;
    }

    public void constructWorld(boolean newGame) {
        worldLoader = new WorldLoader(Meterman2.bundles);
        worldLoader.loadAllGameObjects(newGame);
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
        timeHandler = (TimeOfDayHandler) getEventHandler(TIME_HANDLER_ID);
        timeHandler.setTime(11, 00, 00);
        timeHandler.setTimePerTurn(0, 5, 45);
        timeHandler.register();
        lookHandler = (LookHandler) getEventHandler(LOOK_HANDLER_ID);
        lookHandler.register();
        frameImageHandler = (UiImageHandler) getEventHandler(FRAME_IMAGE_HANDLER_ID);
        frameImageHandler.register();
        entityImageHandler = (UiImageHandler) getEventHandler(ENTITY_IMAGE_HANDLER_ID);
        entityImageHandler.register();
        scriptedHandler = (ScriptedHandler) getEventHandler(SCRIPTED_HANDLER_ID);
        scriptedHandler.register();
    }

    public GameEventHandler getEventHandler(String id) {
        switch (id) {
        case BASIC_HANDLER_ID:
            if (basicWorldHandler == null) {
                basicWorldHandler = new BasicWorldHandler(BASIC_HANDLER_ID);
                basicWorldHandler.setStatusLabelPos(UIConstants.RIGHT_LABEL);
            }
            return basicWorldHandler;
        case TIME_HANDLER_ID:
            if (timeHandler == null) {
                timeHandler = new TimeOfDayHandler(TIME_HANDLER_ID);
                timeHandler.setFormat24h(false);
                timeHandler.setShowSeconds(true);
                timeHandler.setStatusLabelPos(UIConstants.CENTER_LABEL);
            }
            return timeHandler;
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
        case SCRIPTED_HANDLER_ID:
            if (scriptedHandler == null) {
                scriptedHandler = new ScriptedHandler(SCRIPTED_HANDLER_ID);
                scriptedHandler.loadFromElement(b, "scripted-handler");
            }
            return scriptedHandler;
        default:
            return null;
        }
    }

    public void start(boolean newGame) {
        Meterman2.gm.setStatusBarProvider(new CompositeStatusBarProvider(null, timeHandler, basicWorldHandler));
        if (startMethod != null)
            startMethod.invoke(newGame);
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
        if (args[0].equals("sh")) {
            b.reloadElement("scripted-handler");
            scriptedHandler.loadFromElement(b, "scripted-handler");
        }
    }
}
