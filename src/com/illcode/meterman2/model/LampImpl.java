package com.illcode.meterman2.model;

import com.illcode.meterman2.*;
import com.illcode.meterman2.event.TurnListener;

import static com.illcode.meterman2.SystemAttributes.LIGHTSOURCE;
import static com.illcode.meterman2.SystemAttributes.ON;
import static com.illcode.meterman2.Meterman2.gm;
/**
 * A class for entities like flashlights, lamps, etc.
 */
public class LampImpl extends SwitchableEntityImpl implements TurnListener
{
    protected boolean burnsFuel;
    protected int fuelRemaining;
    protected int lowFuelAmount;
    protected String onText, offText;
    protected String baseName;

    private Entity e;
    private AttributeSet attr;
    private MMActions.Action lightAction, douseAction;

    public LampImpl() {
        super();
        lightAction = SystemActions.SWITCH_ON;
        douseAction = SystemActions.SWITCH_OFF;
    }

    public void gameStarting(Entity e) {
        super.gameStarting(e);
        this.e = e;
        attr = e.getAttributes();
        attr.set(LIGHTSOURCE, attr.get(ON));  // synchronize LIGHTSOURCE and ON
        setLampName();
        if (isLit())
            gm.addTurnListener(this);
    }

    /** Returns true if this lamp burns fuel. */
    public boolean getBurnsFuel() {
        return burnsFuel;
    }

    /** Set whether this lamp burns fuel. */
    public void setBurnsFuel(boolean burnsFuel) {
        this.burnsFuel = burnsFuel;
    }

    /** Get the fuel remaining. */
    public int getFuelRemaining() {
        return fuelRemaining;
    }

    /** Set the fuel remaining. A lamp burns fuel at the rate of one unit per turn. */
    public void setFuelRemaining(int fuelRemaining) {
        if (fuelRemaining >= 0)
            this.fuelRemaining = fuelRemaining;
    }

    /** Get the low fuel amount. */
    public int getLowFuelAmount() {
        return lowFuelAmount;
    }

    /** Set the low fuel amount. If the fuel remaining reaches this amount, a message
     *  will be printed about the lamp getting dim. */
    public void setLowFuelAmount(int lowFuelAmount) {
        this.lowFuelAmount = lowFuelAmount;
    }

    /** Set the name of the lamp without the parenthesized status text. */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /** Set the text to display indicating the lamp is on. (ex. "on" or "lit") */
    public void setOnText(String onText) {
        this.onText = onText;
    }

    /** Set the text to display indicating the lamp is off. (ex. "off" or "doused") */
    public void setOffText(String offText) {
        this.offText = offText;
    }

    /** Set an alternate name for the "Switch On" (aka light) action. */
    public void setLightActionName(String name) {
        if (name != null && !name.isEmpty())
            lightAction = SystemActions.SWITCH_ON.fixedTextCopy(name);
    }

    /** Set an alternate name for the "Switch Off" (aka douse) action. */
    public void setDouseActionName(String name) {
        if (name != null && !name.isEmpty())
            douseAction = SystemActions.SWITCH_OFF.fixedTextCopy(name);
    }

    /**
     * Light the lamp, switching it on.
     * @return true if the lamp is lit when the method returns, false otherwise.
     */
    public boolean light() {
        if (burnsFuel && fuelRemaining <= 0)
            return false;  // it cannot be lit
        if (!isLit()) {
            attr.set(ON);
            attr.set(LIGHTSOURCE);
            gm.addTurnListener(this);
            setLampName();
            gm.entityChanged(e);
        }
        return true;
    }

    /** Douse the lamp, switching it off. */
    public void douse() {
        if (isLit()) {
            attr.clear(ON);
            attr.clear(LIGHTSOURCE);
            gm.removeTurnListener(this);
            setLampName();
            gm.entityChanged(e);
        }
    }

    public String getDescription(Entity e) {
        return super.getDescription(e) + " " +
            Meterman2.bundles.getPassage("lamp-status").getTextWithArgs(getStatusText());
    }

    /** Return true if the lamp is lit. */
    public boolean isLit() {
        return attr.get(ON);
    }

    private String getStatusText() {
        if (isLit())
            return onText != null ? onText : "on";
        else
            return offText != null ? offText : "off";
    }

    private void setLampName() {
        e.setName(baseName + " (" + getStatusText() + ")");
    }

    private String getDefBaseName(boolean capitalize) {
        if (attr.get(SystemAttributes.PROPER_NAME))
            return baseName;
        final String defArt = capitalize ? "The " : "the ";
        return defArt + baseName;
    }

    protected MMActions.Action getSwitchOnAction() {
        return lightAction;
    }

    protected MMActions.Action getSwitchOffAction() {
        return douseAction;
    }

    protected boolean switchedAction(Entity e, boolean on) {
        boolean switched = true;
        if (on) {  // we're switching off!
            douse();
        } else {
            if (!light()) {
                gm.println(Meterman2.bundles.getPassage("lamp-no-fuel")
                    .getTextWithArgs(getDefBaseName(true), lightAction.getText().toLowerCase()));
                switched = false;
            }
        }
        if (switched) {
            final String actionText = on ? douseAction.getText() : lightAction.getText();
            gm.println(Meterman2.bundles.getPassage("lamp-switched")
                .getTextWithArgs(actionText.toLowerCase(), getDefBaseName(false)));
        }
        return true;
    }

    public Object getState(Entity e) {
        Object[] oa = new Object[2];
        oa[0] = super.getState(e);
        oa[1] = Integer.valueOf(fuelRemaining);
        return oa;
    }

    public void restoreState(Entity e, Object state) {
        Object[] oa = (Object[]) state;
        super.restoreState(e, oa[0]);
        fuelRemaining = ((Integer) oa[1]).intValue();
    }

    // This will only be called if we are lit.
    public void turn() {
        if (burnsFuel) {
            fuelRemaining--;
            if (fuelRemaining <= 0) {
                douse();
                gm.newPar();
                gm.println(Meterman2.bundles.getPassage("lamp-out").getTextWithArgs(getDefBaseName(true)));
            } else if (fuelRemaining == lowFuelAmount) {
                gm.newPar();
                gm.println(Meterman2.bundles.getPassage("lamp-low").getTextWithArgs(getDefBaseName(true)));
            }
        }
    }

    public String getHandlerId() {
        return "#e:" + e.getId();
    }
}
