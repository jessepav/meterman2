package com.illcode.meterman2.model;

import com.illcode.meterman2.*;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;
import static com.illcode.meterman2.SystemAttributes.LOCKED;
import static com.illcode.meterman2.SystemAttributes.TAKEABLE;

/**
 * Implementation for container entities.
 */
public class ContainerImpl extends BaseEntityImpl
{
    protected String inPrep;
    protected String outPrep;
    protected Entity key;

    private List<MMActions.Action> actions;
    private MMActions.Action lookInAction, putInAction, takeOutAction;

    public ContainerImpl() {
        super();
    }

    /**
     * Return the preposition used when putting an object into the container
     * (ex. "in" for a box, and "on" for a shelf).
     */
    public final String getInPrep() {
        return inPrep != null ? inPrep : "in";
    }

    /** Set the preposition used when putting an object into the container. */
    public final void setInPrep(String inPrep) {
        this.inPrep = inPrep;
    }

    /**
     * Return the preposition used when taking an object out of the container
     * (ex. "from" for a box, and "off" for a shelf).
     */
    public final String getOutPrep() {
        return outPrep != null ? outPrep : "from";
    }

    /** Set the preposition used when taking an object out of the container. */
    public final void setOutPrep(String outPrep) {
        this.outPrep = outPrep;
    }

    /** Get the key used to lock/unlock this container. Null means no key is needed. */
    public final Entity getKey() {
        return key;
    }

    /** Set the key used to lock/unlock this container. Null means no key is needed. */
    public final void setKey(Entity key) {
        this.key = key;
    }

    @Override
    public String getDescription(Entity e) {
        String description = super.getDescription(e);
        if (e.getAttributes().get(LOCKED))
            return description + " " +
                bundles.getPassage(SystemMessages.LOCKED).getTextWithArgs(e.getDefName());
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
            lookInAction = SystemActions.CONTAINER_LOOK_IN.formattedTextCopy(WordUtils.capitalize(getInPrep()));
            putInAction = SystemActions.CONTAINER_PUT.formattedTextCopy(WordUtils.capitalize(getInPrep()));
            takeOutAction = SystemActions.CONTAINER_TAKE.formattedTextCopy(WordUtils.capitalize(getOutPrep()));
        }
        actions.clear();
        if (c.getAttributes().get(LOCKED)) {
            if (key != null)
                actions.add(SystemActions.UNLOCK);
        } else {
            actions.add(lookInAction);
            actions.add(putInAction);
            actions.add(takeOutAction);
            if (key != null)
                actions.add(SystemActions.LOCK);
        }
        return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        if (!(e instanceof Container))
            return super.processAction(e, action);
        Container c = (Container) e;
        final AttributeSet attr = e.getAttributes();
        if (action.equals(SystemActions.LOCK) || action.equals(SystemActions.UNLOCK)) {
            // key should not be null (LOCK and UNLOCK shouldn't have been added),
            // but if it is, we act as though you don't have the key
            if (key == null || !gm.isInInventory(key)) {
                gm.println(bundles.getPassage(SystemMessages.NOKEY).getTextWithArgs(e.getDefName()));
            } else {
                attr.toggle(LOCKED);
                final String message = attr.get(LOCKED) ? SystemMessages.LOCK : SystemMessages.UNLOCK;
                gm.println(bundles.getPassage(message).getTextWithArgs(e.getDefName(), key.getDefName()));
                gm.entityChanged(e);
            }
            return true;
        } else if (action.equals(SystemActions.CONTAINER_LOOK_IN)) {
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
            if (attr.get(TAKEABLE))
                takeables.remove(c);
            if (takeables.isEmpty()) {
                gm.println(bundles.getPassage(SystemMessages.CONTAINER_NO_CONTENTS_PUT).getTextWithArgs(getInPrep(), c.getDefName()));
            } else {
                Entity item = ui.showListDialog(c.getName(),
                    bundles.getPassage(SystemMessages.CONTAINER_PUT_PROMPT).getTextWithArgs(getInPrep(), c.getDefName()),
                    takeables, true);
                if (item != null && !gm.testObjectAction(action, item)) {
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
                if (item != null && !gm.testObjectAction(action, item)) {
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
