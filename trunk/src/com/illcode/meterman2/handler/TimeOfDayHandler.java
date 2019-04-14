package com.illcode.meterman2.handler;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.StatusBarProvider;
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
    protected boolean format24h, showSeconds;

    private int statusLabelPos;
    private int[] timeArray;

    /**
     * Construct a new time-of-day handler, with the current time set to midnight and one minute
     * elapsing per turn.
     * @param handlerId handler ID
     */
    public TimeOfDayHandler(String handlerId) {
        this.handlerId = handlerId;
        secondsPerTurn = 60;
        statusLabelPos = UIConstants.RIGHT_LABEL;
        timeArray = new int[3];
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
    final public int getSecondsPerTurn() {
        return secondsPerTurn;
    }

    /** Set the amount of time that elapses each turn. */
    final public void setTimePerTurn(int hours, int minutes, int seconds) {
        this.secondsPerTurn = (hours * 3600 + minutes * 60 + seconds) % SECONDS_PER_DAY;
    }

    /**
     * Set the current time.
     * @param hours hour of day (0-23)
     * @param minutes minute of hour (0-59)
     * @param seconds second of minutes (0-59)
     */
    final public void setTime(int hours, int minutes, int seconds) {
        secondsSinceMidnight = (hours * 3600 + minutes * 60 + seconds) % SECONDS_PER_DAY;
    }

    /**
     * Get the time of day.
     * @return an array of size 3:
     *    <blockquote>
     *    [ <em>hours (0-23)</em>, <em>minutes (0-59)</em>, <em>seconds (0-59)</em> ]
     *    </blockquote>
     * Note that this returned array is pre-allocated, and whenever this method is called the values in
     * the array will be overwritten.
     */
    final public int[] getTime() {
        int t = secondsSinceMidnight;
        final int seconds = t % 60;
        t /= 60; // t = minutes since midnight
        final int minutes = t % 60;
        t /= 60; // t = hours since midnight
        final int hours = t;
        timeArray[0] = hours;
        timeArray[1] = minutes;
        timeArray[2] = seconds;
        return timeArray;
    }

    /** Set whether we used 24H format. */
    final public void setFormat24h(boolean format24h) {
        this.format24h = format24h;
    }

    /** Set whether we should show the seconds in {@link #getTimeString}. */
    final public void setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
    }

    /**
     * Return the current time as a string. This will be in 24H format (ex. "06:30") or in
     * AM/PM format (ex. "12:39 AM") depending on the current setting.
     * @return current time as a string
     */
    public String getTimeString() {
        final int[] ta = getTime();
        final int hours = ta[0];
        final int minutes = ta[1];
        final int seconds = ta[2];
        StringBuilder sb = new StringBuilder(16);
        if (format24h) {
            append2(hours, sb).append(':');
            append2(minutes, sb);
            if (showSeconds) {
                sb.append(':');
                append2(seconds, sb);
            }
        } else {
            if (hours == 0)
                sb.append("12");
            else if (hours > 12)
                sb.append(hours - 12);
            else
                sb.append(hours);
            sb.append(':');
            append2(minutes, sb);
            if (showSeconds) {
                sb.append(':');
                append2(seconds, sb);
            }
            sb.append(' ').append(hours < 12 ? "AM" : "PM");
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
    final public void setStatusLabelPos(int statusLabelPos) {
        this.statusLabelPos = statusLabelPos;
    }

    public void turn() {
        secondsSinceMidnight = (secondsSinceMidnight + secondsPerTurn) % SECONDS_PER_DAY;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        final Object[] oa = new Object[2];
        oa[0] = Integer.valueOf(secondsSinceMidnight);
        oa[1] = Integer.valueOf(secondsPerTurn);
        return oa;
    }

    public void restoreHandlerState(Object state) {
        final Object[] oa = (Object[]) state;
        secondsSinceMidnight = ((Integer) oa[0]).intValue();
        secondsPerTurn = ((Integer) oa[1]).intValue();
    }

    public void gameHandlerStarting(boolean newGame) {
        // empty
    }

    private StringBuilder append2(int val, StringBuilder sb) {
        if (val < 10)
            sb.append('0');
        return sb.append(val);
    }
}
