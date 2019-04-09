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
 * It also manages the display of information in the status bar.
 */
public class BasicWorldHandler
    implements StatusBarProvider, GameActionListener, EntityActionsProcessor, TurnListener
{
    private String handlerId;
    private StatusBarProvider statusBarProvider;

    /**
     * Create a basic world handler.
     * @param handlerId handler ID
     */
    public BasicWorldHandler(String handlerId) {
        this.handlerId = handlerId;
        statusBarProvider = this;
    }

    /** Registers this basic world handler with the game manager.  */
    public void register() {
        gm.addGameActionListener(this);
        gm.addEntityActionsProcessor(this);
        gm.addTurnListener(this);
    }

    /** De-registers this basic world handler from the game manager. */
    public void deregister() {
        gm.removeGameActionListener(this);
        gm.removeEntityActionsProcessor(this);
        gm.removeTurnListener(this);
    }

    /** Set the provider that will determine which is displayed in the status bar labels.
     *  The default is to use the BasicWorldHandler's built-in provider. */
    public void setStatusBarProvider(StatusBarProvider statusBarProvider) {
        this.statusBarProvider = statusBarProvider;
    }

    // Implement StatusBarProvider
    public String getStatusText(int labelPos) {
        if (labelPos == UIConstants.RIGHT_LABEL)
            return "Current Turn: " + (gm.getNumTurns() + 1);
        else
            return null;
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

    // Implement TurnListener
    @Override
    public void turn() {
        refreshStatusBar();
    }

    /** Refreshes the status bar labels. */
    public void refreshStatusBar() {
        if (statusBarProvider != null) {
            for (int pos = 0; pos < UIConstants.NUM_LABELS; pos++)
                ui.setStatusLabel(pos, statusBarProvider.getStatusText(pos));
        }
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
        if (!newGame)
            refreshStatusBar();  // since no turn occurs at the start of a loaded game
    }
}