package com.illcode.meterman2.model;

import com.illcode.meterman2.*;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;
import static com.illcode.meterman2.SystemAttributes.*;

/**
 * Implementation for container entities.
 */
public class ContainerImpl extends BaseEntityImpl
{
    protected String inPrep;
    protected String outPrep;
    protected Entity key;

    private List<MMActions.Action> actions;
    private MMActions.Action putInAction, takeOutAction;

    public ContainerImpl() {
        super();
    }

    /**
     * Return the preposition used when putting an object into the container
     * (ex. "in" for a box, and "on" for a shelf).
     */
    public String getInPrep() {
        return inPrep != null ? inPrep : "in";
    }

    /** Set the preposition used when putting an object into the container. */
    public void setInPrep(String inPrep) {
        this.inPrep = inPrep;
    }

    /**
     * Return the preposition used when taking an object out of the container
     * (ex. "from" for a box, and "off" for a shelf).
     */
    public String getOutPrep() {
        return outPrep != null ? outPrep : "from";
    }

    /** Set the preposition used when taking an object out of the container. */
    public void setOutPrep(String outPrep) {
        this.outPrep = outPrep;
    }

    /** Get the key used to lock/unlock this container. Null means no key is needed. */
    public Entity getKey() {
        return key;
    }

    /** Set the key used to lock/unlock this container. Null means no key is needed. */
    public void setKey(Entity key) {
        this.key = key;
    }

    @Override
    public String getDescription(Entity e) {
        String description = super.getDescription(e);
        if (e.getAttributes().get(LOCKED))
            return description + " " +
                bundles.getPassage(SystemMessages.CONTAINER_LOCKED).getTextWithArgs(e.getDefName());
        else
            return description;
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        if (!(e instanceof Container))
            return super.getActions(e);
        Container c = (Container) e;
        if (actions == null) {
            actions = new ArrayList<>(6);
            putInAction = SystemActions.CONTAINER_PUT.formattedTextCopy(getInPrep());
            takeOutAction = SystemActions.CONTAINER_TAKE.formattedTextCopy(getOutPrep());
        }
        actions.clear();
        if (c.getAttributes().get(LOCKED)) {
            actions.add(SystemActions.UNLOCK);
        } else {
            actions.add(SystemActions.CONTAINER_EXAMINE);
            actions.add(putInAction);
            actions.add(takeOutAction);
            if (getKey() != null)
                actions.add(SystemActions.LOCK);
        }
        return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        if (!(e instanceof Container))
            return super.processAction(e, action);
        Container c = (Container) e;
        if (action.equals(SystemActions.LOCK) || action.equals(SystemActions.UNLOCK)) {
            if (getKey() != null && !gm.isInInventory(getKey())) {
                gm.println(bundles.getPassage(SystemMessages.CONTAINER_NOKEY).getTextWithArgs(e.getDefName()));
            } else {
                e.getAttributes().toggle(LOCKED);
                gm.entityChanged(e);
            }
            return true;
        } else if (action.equals(SystemActions.CONTAINER_EXAMINE)) {
            List<Entity> contents = c.getEntities();
            if (contents.isEmpty()) {
                gm.println(bundles.getPassage(SystemMessages.CONTAINER_EMPTY).getTextWithArgs(getInPrep(), c.getDefName()));
            } else {
                Entity item = ui.showListDialog(
                    c.getName(), bundles.getPassage(SystemMessages.CONTAINER_EXAMINE).getText(), contents, true);
                if (item != null)
                    gm.println(item.getDescription());
            }
            return true;
        } else if (action.equals(SystemActions.CONTAINER_PUT)) {
            List<Entity> takeables = new ArrayList<>();
            GameUtils.getCurrentTakeableEntities(takeables);
            if (GameUtils.hasAttr(c, TAKEABLE))
                takeables.remove(c);
            if (takeables.isEmpty()) {
                gm.println(bundles.getPassage(SystemMessages.CONTAINER_NO_CONTENTS_PUT).getTextWithArgs(getInPrep(), c.getDefName()));
            } else {
                Entity item = ui.showListDialog(c.getName(),
                    bundles.getPassage(SystemMessages.CONTAINER_PUT_PROMPT).getTextWithArgs(getInPrep(), c.getDefName()),
                    takeables, true);
                if (item != null) {
                    gm.moveEntity(item, c);
                    gm.println(bundles.getPassage(SystemMessages.CONTAINER_PUT).
                        getTextWithArgs(item.getDefName(), getInPrep(), c.getDefName()));
                }
            }
            return true;
        } else if (action.equals(SystemActions.CONTAINER_TAKE)) {
            List<Entity> takeables = new ArrayList<>();
            GameUtils.filterByAttribute(c.getEntities(), SystemAttributes.TAKEABLE, true, takeables);
            if (takeables.isEmpty()) {
                gm.println(bundles.getPassage(SystemMessages.CONTAINER_NO_CONTENTS_TAKE).getTextWithArgs(getInPrep(), c.getDefName()));
            } else {
                Entity item = ui.showListDialog(c.getName(),
                    bundles.getPassage(SystemMessages.CONTAINER_TAKE_PROMPT).getTextWithArgs(getOutPrep(), c.getDefName()),
                    takeables, true);
                if (item != null) {
                    gm.moveEntity(item, gm.getPlayer());
                    gm.println(bundles.getPassage(SystemMessages.CONTAINER_TAKE).
                        getTextWithArgs(item.getDefName(), getOutPrep(), c.getDefName()));
                }
            }
            return true;
        }
        return false;
    }
}
