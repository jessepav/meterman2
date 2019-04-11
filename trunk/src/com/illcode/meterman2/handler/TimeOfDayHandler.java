package com.illcode.meterman2.handler;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.event.TurnListener;

/**
 * A class that keeps track of the time of day.
 */
public class TimeOfDayHandler implements TurnListener
{
    public static final int SECONDS_PER_DAY = 86400;

    private String handlerId;

    protected int secondsSinceMidnight;
    protected int secondsPerTurn;
    protected boolean format24h;

    /**
     * Construct a new time-of-day handler, with the current time set to midnight.
     * @param handlerId handler ID
     * @param secondsPerTurn the number of seconds that elapse each turn.
     */
    public TimeOfDayHandler(String handlerId, int secondsPerTurn) {
        this.handlerId = handlerId;
        this.secondsPerTurn = secondsPerTurn;
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
        return null;
    }

    public void turn() {
        secondsSinceMidnight = (secondsSinceMidnight + secondsPerTurn) % SECONDS_PER_DAY;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        return null;
    }

    public void restoreHandlerState(Object state) {

    }

    public void gameHandlerStarting(boolean newGame) {

    }
}
