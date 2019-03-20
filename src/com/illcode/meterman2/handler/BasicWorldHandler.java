package com.illcode.meterman2.handler;

import com.illcode.meterman2.*;
import com.illcode.meterman2.event.EntityActionsProcessor;
import com.illcode.meterman2.event.GameActionListener;
import com.illcode.meterman2.event.TurnListener;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.ui.UIConstants;

import java.util.List;

import static com.illcode.meterman2.Meterman2.ui;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.GameUtils.hasAttr;

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
    public static final int DEFAULT_MAX_INVENTORY = 100;

    private String handlerId;
    private int maxInventoryItems;
    private StatusBarProvider statusBarProvider;

    /**
     * Create a basic world handler.
     * @param handlerId handler ID
     */
    public BasicWorldHandler(String handlerId) {
        this.handlerId = handlerId;
        maxInventoryItems = DEFAULT_MAX_INVENTORY;
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

    /** Returns the maximum # of inventory items the player can carry. */
    public int getMaxInventoryItems() {
        return maxInventoryItems;
    }

    /** Sets the maximum # of inventory items the player can carry. */
    public void setMaxInventoryItems(int maxInventoryItems) {
        this.maxInventoryItems = maxInventoryItems;
    }

    /** Set the provider that will determine which is displayed in the status bar labels.
     *  The default is to use the BasicWorldHandler's built-in provider. */
    public void setStatusBarProvider(StatusBarProvider statusBarProvider) {
        this.statusBarProvider = statusBarProvider;
    }

    // Implement StatusBarProvider
    public String getStatusText(int labelPos) {
        if (labelPos == UIConstants.RIGHT_LABEL)
            return "Turn No: " + (gm.getNumTurns() + 1);
        else
            return null;
    }

    // Implement EntityActionsProcessor
    @Override
    public void processEntityActions(Entity e, List<MMActions.Action> actions) {
        actions.add(SystemActions.EXAMINE);
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
        } else if (action.equals(SystemActions.TAKE)) {
            if (gm.getPlayer().getEntities().size() < maxInventoryItems)
                gm.moveEntity(e, gm.getPlayer());
            else
                gm.println(Meterman2.bundles.getPassage(SystemMessages.MAX_INVENTORY));
        } else if (action.equals(SystemActions.EQUIP)) {
            gm.setEquipped(e, true);
        } else if (action.equals(SystemActions.UNEQUIP)) {
            gm.setEquipped(e, false);
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
    public boolean objectAction(MMActions.Action action, Entity e, Entity object) {
        return false;
    }

    // Implement TurnListener
    @Override
    public void turn() {
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
}
