package com.illcode.meterman2.model;

import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.event.TurnListener;

import java.util.Map;

/**
 * An entity that allows a scripted method to be run each turn.
 */
public class EachTurnEntityImpl extends BaseEntityImpl implements TurnListener
{
    protected MMScript.ScriptedMethod eachTurnMethod;

    private Entity e;

    public EachTurnEntityImpl(Entity e) {
        super();
        this.e = e;
    }

    /**
     * Set scripted methods to be used by this each-turn entity.
     * <p/>
     * It looks for methods with these names (and implicit signatures) in the passed method map:
     * <dl>
     *     <dt>{@code void eachTurn(Entity e)}</dt>
     *     <dd>Called each turn</dd>
     * </dl>
     * @param methodMap map from method name to scripted method. If null, all of our scripted methods
     * will be cleared
     */
    public void setScriptedMethods(Map<String,MMScript.ScriptedMethod> methodMap) {
        if (methodMap != null) {
            eachTurnMethod = methodMap.get("eachTurn");
        } else {
            eachTurnMethod = null;
        }
    }

    public void gameStarting(Entity e) {
        super.gameStarting(e);
        Meterman2.gm.addTurnListener(this);
    }

    public void turn() {
        if (eachTurnMethod != null)
            eachTurnMethod.invoke(e);
    }

    public String getHandlerId() {
        return "#e:" + e.getId();
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
