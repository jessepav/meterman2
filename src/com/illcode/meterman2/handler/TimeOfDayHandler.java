package com.illcode.meterman2.handler;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.event.TurnListener;
import com.illcode.meterman2.ui.UIConstants;

/**
 * A class that keeps track of the time of day.
 */
public class TimeOfDayHandler implements TurnListener, StatusBarProvider
{
    public static final int SECONDS_PER_DAY = 86400;

    private String handlerId;

    protected int secondsSinceMidnight;
    protected int secondsPerTurn;
    protected boolean format24h;

    private int statusLabelPos;

    /**
     * Construct a new time-of-day handler, with the current time set to midnight.
     * @param handlerId handler ID
     * @param secondsPerTurn the number of seconds that elapse each turn.
     */
    public TimeOfDayHandler(String handlerId, int secondsPerTurn) {
        this.handlerId = handlerId;
        this.secondsPerTurn = secondsPerTurn;
        statusLabelPos = UIConstants.RIGHT_LABEL;
    }

    /** Registers this time-of-day handler with the game manager.  */
    public void register() {
        Meterman2.gm.addTurnListener(this);
    }

    /** Deregisters this time-of-day handler from the game manager.  */
    public void deregister() {
        Meterman2.gm.removeTurnListener(this);
    }

    /** Get the number of seconds that elapse each turn. */
    public int getSecondsPerTurn() {
        return secondsPerTurn;
    }

    /** Set the number of seconds that elapse each turn. */
    public void setSecondsPerTurn(int secondsPerTurn) {
        this.secondsPerTurn = secondsPerTurn;
    }

    /** Set the number of minutes that elapse each turn. */
    public void setMinutesPerTurn(int minutesPerTurn) {
        this.secondsPerTurn = minutesPerTurn * 60;
    }

    /** Returns the number of seconds elapsed since midnight. */
    public int getTime() {
        return secondsSinceMidnight;
    }

    /** Returns the hour of day (0-23). */
    public int getHourOfDay() {
        return secondsSinceMidnight / 3600;
    }

    /**
     * Set the current time.
     * @param hours hour of day (0-23)
     * @param minutes minute of hour (0-59)
     * @param seconds second of minutes (0-59)
     */
    public void setTime(int hours, int minutes, int seconds) {
        secondsSinceMidnight = (hours * 3600 + minutes * 60 + seconds) % SECONDS_PER_DAY;
    }

    /** Set whether we used 24H format. */
    public void setFormat24h(boolean format24h) {
        this.format24h = format24h;
    }

    /**
     * Return the current time as a string. This will be in 24H format (ex. "06:30") or in
     * AM/PM format (ex. "12:39 AM") depending on the current setting.
     * @return current time as a string
     */
    public String getTimeString() {
        StringBuilder sb = new StringBuilder(10);
        final int hours = getHourOfDay();
        final int minutes = (secondsSinceMidnight % 3600) / 60;
        if (format24h) {
            if (hours < 10)
                sb.append('0');
            sb.append(hours).append(':');
            if (minutes < 10)
                sb.append('0');
            sb.append(minutes);
        } else {
            if (hours == 0)
                sb.append("12");
            else if (hours > 12)
                sb.append(hours - 12);
            else
                sb.append(hours);
            sb.append(':');
            if (minutes < 10)
                sb.append('0');
            sb.append(minutes).append(' ');
            sb.append(hours < 12 ? "AM" : "PM");
        }
        return sb.toString();
    }

    public String getStatusText(int labelPos) {
        if (labelPos == statusLabelPos)
            return getTimeString();
        else
            return null;
    }

    /**
     * Set the position for which we should return text indicating the time of day.
     * By default the value is {@code UIConstants.RIGHT_LABEL}.
     * @param statusLabelPos one of the <tt>LABEL</tt> positions in {@code UIConstants}
     */
    public void setStatusLabelPos(int statusLabelPos) {
        this.statusLabelPos = statusLabelPos;
    }

    public void turn() {
        secondsSinceMidnight = (secondsSinceMidnight + secondsPerTurn) % SECONDS_PER_DAY;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        return Integer.valueOf(secondsSinceMidnight);
    }

    public void restoreHandlerState(Object state) {
        secondsSinceMidnight = ((Integer) state).intValue();
    }

    public void gameHandlerStarting(boolean newGame) {
        // empty
    }
}
