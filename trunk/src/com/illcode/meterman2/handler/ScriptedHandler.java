package com.illcode.meterman2.handler;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.*;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

import java.util.List;
import java.util.Map;

import static com.illcode.meterman2.Meterman2.gm;

/**
 * A class implementing a number of useful event handler interfaces
 * whose methods can be delegated to scripts.
 */
public final class ScriptedHandler implements 
    EntityActionsProcessor, GameActionListener, LookListener, PlayerMovementListener, TurnListener
{
    private String handlerId;
    
    private MMScript.ScriptedMethod
        processEntityActionsMethod,
        processActionMethod, postActionMethod, objectActionMethod,
        lookInRoomMethod,
        playerMoveMethod,
        turnMethod,
        getHandlerStateMethod, restoreHandlerStateMethod, gameHandlerStartingMethod;

    private boolean registered;

    public ScriptedHandler(String handlerId) {
        this.handlerId = handlerId;
    }

    /** Register ourselves in the appropriate listener lists with the game manager. */
    public void register() {
        if (!registered) {
            if (processEntityActionsMethod != null)
                gm.addEntityActionsProcessor(this);
            if (processActionMethod != null || postActionMethod != null || objectActionMethod != null)
                gm.addGameActionListener(this);
            if (lookInRoomMethod != null)
                gm.addLookListener(this);
            if (playerMoveMethod != null)
                gm.addPlayerMovementListener(this);
            if (turnMethod != null)
                gm.addTurnListener(this);
            registered = true;
        }
    }

    /** Remove ourselves from the appropriate listener lists in the game manager. */
    public void deregister() {
        if (registered) {
            if (processEntityActionsMethod != null)
                gm.removeEntityActionsProcessor(this);
            if (processActionMethod != null || postActionMethod != null || objectActionMethod != null)
                gm.removeGameActionListener(this);
            if (lookInRoomMethod != null)
                gm.removeLookListener(this);
            if (playerMoveMethod != null)
                gm.removePlayerMovementListener(this);
            if (turnMethod != null)
                gm.removeTurnListener(this);
            registered = false;
        }
    }

    public void clearScriptedMethods() {
        deregister();
        processEntityActionsMethod = null;
        processActionMethod = null;
        postActionMethod = null;
        objectActionMethod = null;
        lookInRoomMethod = null;
        playerMoveMethod = null;
        turnMethod = null;
        getHandlerStateMethod = null;
        restoreHandlerStateMethod = null;
        gameHandlerStartingMethod = null;
    }

    /**
     * Loads scripted methods from an XML element. The element should
     * have a child {@code <script>} element where the actual script source is contained.
     * <p/>
     * @param b XBundle where element is found
     * @param id ID of the element to load from
     */
    public void loadFromElement(XBundle b, String id) {
        final Element el = b.getElement(id);
        if (el == null)
            return;
        final Element script = el.getChild("script");
        if (script == null)
            return;
        boolean wasRegistered = registered;
        clearScriptedMethods();
        final List<MMScript.ScriptedMethod> methods =
            Meterman2.script.getScriptedMethods(id, b.getElementTextTrim(script));
        for (MMScript.ScriptedMethod sm : methods) {
            switch (sm.getName()) {
            case "processEntityActions":
                processEntityActionsMethod = sm;
                break;
            case "processAction":
                processActionMethod = sm;
                break;
            case "postAction":
                postActionMethod = sm;
                break;
            case "objectAction":
                objectActionMethod = sm;
                break;
            case "lookInRoom":
                lookInRoomMethod = sm;
                break;
            case "playerMove":
                playerMoveMethod = sm;
                break;
            case "turn":
                turnMethod = sm;
                break;
            case "getHandlerState":
                getHandlerStateMethod = sm;
                break;
            case "restoreHandlerState":
                restoreHandlerStateMethod = sm;
                break;
            case "gameHandlerStarting":
                gameHandlerStartingMethod = sm;
                break;
            }
        }
        if (wasRegistered)
            register();
    }

    public void processEntityActions(Entity e, List<MMActions.Action> actions) {
        if (processEntityActionsMethod != null)
            processEntityActionsMethod.invoke(e, actions);
    }

    public boolean processAction(MMActions.Action action, Entity e, boolean beforeAction) {
        if (processActionMethod != null)
            return processActionMethod
                .invokeWithResultOrError(Boolean.class, Boolean.FALSE, action, e, Boolean.valueOf(beforeAction));
        else
            return false;
    }

    public boolean postAction(MMActions.Action action, Entity e, boolean actionHandled) {
        if (postActionMethod != null)
            return postActionMethod
                .invokeWithResultOrError(Boolean.class, Boolean.FALSE, action, e, Boolean.valueOf(actionHandled));
        else
            return false;
    }

    public boolean objectAction(Entity object, MMActions.Action action, Entity selectedEntity) {
        if (objectActionMethod != null)
            return objectActionMethod
                .invokeWithResultOrError(Boolean.class, Boolean.FALSE, object, action, selectedEntity);
        else
            return false;
    }

    public void lookInRoom(Room currentRoom) {
        if (lookInRoomMethod != null)
            lookInRoomMethod.invoke(currentRoom);
    }

    public boolean playerMove(Room fromRoom, Room toRoom, boolean beforeMove) {
        if (playerMoveMethod != null)
            return playerMoveMethod
                .invokeWithResultOrError(Boolean.class, Boolean.FALSE, fromRoom, toRoom, Boolean.valueOf(beforeMove));
        else
            return false;
    }

    public void turn() {
        if (turnMethod != null)
            turnMethod.invoke();
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        if (getHandlerStateMethod != null)
            return getHandlerStateMethod.invokeWithResultOrError(Object.class, null);
        else
            return null;

    }

    public void restoreHandlerState(Object state) {
        if (restoreHandlerStateMethod != null)
            restoreHandlerStateMethod.invoke(state);
    }

    public void gameHandlerStarting(boolean newGame) {
        if (gameHandlerStartingMethod != null)
            gameHandlerStartingMethod.invoke(Boolean.valueOf(newGame));
    }
}
