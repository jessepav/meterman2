package com.illcode.meterman2.handler;

import com.illcode.meterman2.*;
import com.illcode.meterman2.event.EntityActionsProcessor;
import com.illcode.meterman2.event.GameActionListener;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.ui.UIConstants;
import com.illcode.meterman2.util.ActionSet;
import com.illcode.meterman2.util.EquipTable;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.GameUtils.getPassageWithArgs;
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

    protected int statusLabelPos;
    protected List<Room> roomList;  // temprary list to avoid allocation
    protected ActionSet actionSet;

    protected EquipTable equipTable;

    /**
     * Create a basic world handler.
     * @param handlerId handler ID
     */
    public BasicWorldHandler(String handlerId) {
        this.handlerId = handlerId;
        statusLabelPos = UIConstants.RIGHT_LABEL;
        roomList = new ArrayList<>(UIConstants.NUM_EXIT_BUTTONS);
        actionSet = new ActionSet();
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
    public final void setStatusLabelPos(int statusLabelPos) {
        this.statusLabelPos = statusLabelPos;
    }

    /**
     * Set the equip-table to be used in determining the number of entities that can
     * be equipped for each category.
     * @param equipTable equip-table
     */
    public void setEquipTable(EquipTable equipTable) {
        this.equipTable = equipTable;
    }

    // Implement EntityActionsProcessor
    @Override
    public void processEntityActions(Entity e, List<MMActions.Action> actions) {
        actionSet.init(actions);
        actionSet.checkAddAction(SystemActions.EXAMINE, actions, 0);  // Examine should always be first.
        final AttributeSet attr = e.getAttributes();
        if (attr.get(SystemAttributes.TAKEABLE)) {
            if (gm.isInInventory(e))
                actionSet.checkAddAction(SystemActions.DROP, actions);
            else
                actionSet.checkAddAction(SystemActions.TAKE, actions);
        }
        if (attr.get(SystemAttributes.EQUIPPABLE) && gm.isInInventory(e)) {
            if (gm.isEquipped(e))
                actionSet.checkAddAction(SystemActions.UNEQUIP, actions);
            else
                actionSet.checkAddAction(SystemActions.EQUIP, actions);
        }
        if (attr.get(SystemAttributes.MOVEABLE))
            actionSet.checkAddAction(SystemActions.MOVE, actions);
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
            boolean limitReached = false;
            if (equipTable != null) {
                final EquipTable.Category cat = equipTable.getCategory(e);
                if (cat != null) {
                    // We check how many items in the entity's category are already equipped, and
                    // refuse to equip the entity if the limit has been reached.
                    int numEquipped = 0;
                    for (Entity item : gm.getPlayer().getEquippedEntities()) {
                        final EquipTable.Category itemCat = equipTable.getCategory(item);
                        if (itemCat == cat)
                            numEquipped++;
                    }
                    if (numEquipped >= cat.limit) {
                        limitReached = true;
                        gm.println(cat.limitMessage);
                    }
                }
            }
            if (!limitReached) {
                gm.setEquipped(e, true);
                printPassageWithArgs("equip-message", e.getDefName());
            }
        } else if (action.equals(SystemActions.UNEQUIP)) {
            gm.setEquipped(e, false);
            printPassageWithArgs("unequip-message", e.getDefName());
        } else if (action.equals(SystemActions.MOVE)) {
            final String actionText = action.getText().toLowerCase();
            GameUtils.gatherExitRooms(gm.getCurrentRoom(), false, roomList);
            if (roomList.isEmpty()) {
                printPassageWithArgs("move-no-rooms-message", actionText, e.getDefName());
            } else {
                final Room r = ui.showListDialog(action.getText(),
                    getPassageWithArgs("move-prompt-message", actionText, e.getDefName()), roomList, true);
                if (r != null) {
                    gm.moveEntity(e, r);
                    printPassageWithArgs("move-message", actionText, e.getDefName(), r.getDefName());
                }
                roomList.clear();
            }
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
