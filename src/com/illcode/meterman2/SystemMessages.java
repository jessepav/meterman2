package com.illcode.meterman2;

/**
 * Constants for passage IDs for system messages.
 */
public final class SystemMessages
{
    //  Used by the basic game system.
    public static final String OUTPUT_SEPARATOR = "output-separator";
    public static final String ACTION_NOT_HANDLED = "action-not-handled-message";
    public static final String WAIT = "wait-message";
    public static final String EXIT_BLOCKED = "exit-blocked-message";
    public static final String MAX_INVENTORY = "max-inventory-message";

    //  Used by doors and containers.
    public static final String LOCKED = "locked-message";
    public static final String NOKEY = "nokey-message";
    public static final String UNLOCK = "unlock-message";
    public static final String LOCK = "lock-message";
    public static final String OPEN = "open-message";
    public static final String CLOSE = "close-message";

    //  Used by containers.
    public static final String CONTAINER_EMPTY = "container-empty-message";
    public static final String CONTAINER_EXAMINE = "container-examine-message";
    public static final String CONTAINER_NO_CONTENTS_PUT = "container-no-contents-put-message";
    public static final String CONTAINER_PUT_PROMPT = "container-put-prompt-message";
    public static final String CONTAINER_PUT = "container-put-message";
    public static final String CONTAINER_NO_CONTENTS_TAKE = "container-no-contents-take-message";
    public static final String CONTAINER_TAKE_PROMPT = "container-take-prompt-message";
    public static final String CONTAINER_TAKE = "container-take-message";

    // Used by dark rooms.
    public static final String DARKROOM_DESCRIPTION = "darkroom-description";

    // Used by talkers.
    public static final String NO_TALK_TOPICS = "no-talk-topics-message";
    public static final String TALK_PROMPT = "talk-prompt-message";
}
