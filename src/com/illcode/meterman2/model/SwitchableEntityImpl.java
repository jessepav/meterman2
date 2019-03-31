package com.illcode.meterman2.model;

import com.illcode.meterman2.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class for entities that can be switched on and off, like flashlights, machines, etc.
 * <p/>
 * Functionality is implement either by overriding the {@link #switchedAction} method, or by
 * providing a scripted method (via {@link #setScriptedMethods}).
 */
public class SwitchableEntityImpl extends BaseEntityImpl
{
    protected MMScript.ScriptedMethod switchedMethod;

    private List<MMActions.Action> actions;

    public SwitchableEntityImpl() {
        super();
        actions = new ArrayList<>(4);
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        actions.clear();
        actions.addAll(super.getActions(e));
        if (e.getAttributes().get(SystemAttributes.ON))
            actions.add(SystemActions.SWITCH_OFF);
        else
            actions.add(SystemActions.SWITCH_ON);
        return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        final AttributeSet attr = e.getAttributes();
        if (action.equals(SystemActions.SWITCH_ON) || action.equals(SystemActions.SWITCH_OFF)) {
            final boolean on = attr.get(SystemAttributes.ON);
            if (switchedMethod != null)
                return switchedMethod.invokeWithResultOrError(Boolean.class, false, e, on);
            else
                return switchedAction(e, on);
        } else {
            return false;
        }
    }

    /**
     * Set scripted methods to be used by this switchable entity.
     * <p/>
     * It looks for methods with these names (and implicit signatures) in the passed method map:
     * <dl>
     *     <dt>{@code boolean switchedAction(Entity e, boolean on)}</dt>
     *     <dd>See {@link #switchedAction}</dd>
     * </dl>
     * @param methodMap map from method name to scripted method. If null, all of our scripted methods
     * will be cleared
     */
    public void setScriptedMethods(Map<String,MMScript.ScriptedMethod> methodMap) {
        if (methodMap != null) {
            switchedMethod = methodMap.get("switchedAction");
        } else {
            switchedMethod = null;
        }
    }

    /**
     * Called when we're switched on or off. The default implementation is just to toggle the
     * <tt>ON</tt> attribute and return false.
     * <p/>
     * Subclasses would override this method to actually do something.
     * @param e the entity that received the action
     * @param on true if we are currently on
     * @return true if the action has been handled.
     */
    protected boolean switchedAction(Entity e, boolean on) {
        e.getAttributes().toggle(SystemAttributes.ON);
        Meterman2.gm.entityChanged(e);
        return false;
    }
}
