package com.illcode.meterman2.handler;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.event.EntityActionsProcessor;
import com.illcode.meterman2.event.GameActionListener;
import com.illcode.meterman2.event.TurnListener;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.ui.UIConstants;

import java.util.List;

import static com.illcode.meterman2.GameUtils.hasAttr;
import static com.illcode.meterman2.GameUtils.printPassageWithArgs;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;

/**
 * A handler (global listener for various game events) that manages basic
 * world interactions. Namely
 * <ul>
 *     <li>Examining entities.</li>
 *     <li>Taking and dropping items.</li>
 *     <li>Equipping and unequipping equippables.</li>
 * </ul>
 * It also implements StatusBarProvider to show the # of turns.
 */
public class BasicWorldHandler
    implements GameActionListener, EntityActionsProcessor, StatusBarProvider
{
    private String handlerId;
    private int statusLabelPos;

    /**
     * Create a basic world handler.
     * @param handlerId handler ID
     */
    public BasicWorldHandler(String handlerId) {
        this.handlerId = handlerId;
        statusLabelPos = UIConstants.RIGHT_LABEL;
    }

    /** Registers this basic world handler with the game manager.  */
    public void register() {
        gm.addGameActionListener(this);
        gm.addEntityActionsProcessor(this);
    }

    /** De-registers this basic world handler from the game manager. */
    public void deregister() {
        gm.removeGameActionListener(this);
        gm.removeEntityActionsProcessor(this);
    }

    // Implement StatusBarProvider
    public String getStatusText(int labelPos) {
        if (labelPos == statusLabelPos)
            return "Current Turn: " + (gm.getNumTurns() + 1);
        else
            return null;
    }

    /**
     * Set the position for which we should return text indicating the number of turns elapsed.
     * By default the value is {@code UIConstants.RIGHT_LABEL}.
     * @param statusLabelPos one of the <tt>LABEL</tt> positions in {@code UIConstants}
     */
    public void setStatusLabelPos(int statusLabelPos) {
        this.statusLabelPos = statusLabelPos;
    }

    // Implement EntityActionsProcessor
    @Override
    public void processEntityActions(Entity e, List<MMActions.Action> actions) {
        actions.add(0, SystemActions.EXAMINE);  // Examine should always be first.
        if (hasAttr(e, SystemAttributes.TAKEABLE)) {
            if (gm.isInInventory(e))
                actions.add(SystemActions.DROP);
            else
                actions.add(SystemActions.TAKE);
        }
        if (hasAttr(e, SystemAttributes.EQUIPPABLE) && gm.isInInventory(e)) {
            if (gm.isEquipped(e))
                actions.add(SystemActions.UNEQUIP);
            else
                actions.add(SystemActions.EQUIP);
        }
    }

    // Implement GameActionListener
    @Override
    public boolean processAction(MMActions.Action action, Entity e, boolean beforeAction) {
        if (beforeAction)
            return false;  // we don't want to block the entity from handling the action itself

        boolean handled = true;
        if (action.equals(SystemActions.EXAMINE)) {
            gm.println(e.getDescription());
        } else if (action.equals(SystemActions.DROP)) {
            gm.moveEntity(e, gm.getCurrentRoom());
            printPassageWithArgs("drop-message", e.getDefName());
        } else if (action.equals(SystemActions.TAKE)) {
            gm.moveEntity(e, gm.getPlayer());
            printPassageWithArgs("take-message", e.getDefName());
        } else if (action.equals(SystemActions.EQUIP)) {
            gm.setEquipped(e, true);
            printPassageWithArgs("equip-message", e.getDefName());
        } else if (action.equals(SystemActions.UNEQUIP)) {
            gm.setEquipped(e, false);
            printPassageWithArgs("unequip-message", e.getDefName());
        } else {
            handled = false;
        }
        return handled;
    }

    @Override
    public boolean postAction(MMActions.Action action, Entity e, boolean actionHandled) {
        return false;
    }

    @Override
    public boolean objectAction(Entity object, MMActions.Action action, Entity selectedEntity) {
        return false;
    }

    // Implement GameActionHandler
    @Override
    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        return null;
    }

    public void restoreHandlerState(Object state) {
        // empty
    }

    public void gameHandlerStarting(boolean newGame) {
        // empty
    }
}
