package com.illcode.meterman2.util;

import com.illcode.meterman2.MMActions;

import java.util.BitSet;
import java.util.List;

/**
 * A utility class to avoid duplicating actions in a list.
 */
public final class ActionSet
{
    private BitSet actionBitSet;

    /** Construct a new, empty action set. */
    public ActionSet() {
        actionBitSet = new BitSet();
    }

    /** Called to initialize the action set from a list of actions. This will usually be used in
     *  methods that process or return entity actions. */
    public void init(List<MMActions.Action> actions) {
        actionBitSet.clear();
        for (MMActions.Action a : actions)
            actionBitSet.set(a.getActionNo());
    }

    /** Add an action to an action list, if not already present. */
    public void checkAddAction(MMActions.Action a, List<MMActions.Action> actions) {
        final int actionNo = a.getActionNo();
        if (!actionBitSet.get(actionNo)) {
            actions.add(a);
            actionBitSet.set(actionNo);
        }
    }

    /** Add an action to an action list at the given index, if not already present. */
    public void checkAddAction(MMActions.Action a, List<MMActions.Action> actions, int index) {
        final int actionNo = a.getActionNo();
        if (!actionBitSet.get(actionNo)) {
            actions.add(index, a);
            actionBitSet.set(actionNo);
        }
    }

}
