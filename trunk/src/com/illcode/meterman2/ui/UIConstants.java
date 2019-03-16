package com.illcode.meterman2.ui;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Constants for interacting with the UI, and methods for operating on those constants.
 */
public final class UIConstants
{
    /** Series of constants indicating the status bar label position */
    public static final int LEFT_LABEL = 0,
                            CENTER_LABEL = 1,
                            RIGHT_LABEL = 2;

    /** Series of constants indicating the exit button position in the UI */
    public static final int NW_BUTTON = 0,
                            N_BUTTON = 1,
                            NE_BUTTON = 2,
                            E_BUTTON = 6,
                            SE_BUTTON = 10,
                            S_BUTTON = 9,
                            SW_BUTTON = 8,
                            W_BUTTON = 4,
                            MID_BUTTON = 5,
                            X1_BUTTON = 3,
                            X2_BUTTON = 7,
                            X3_BUTTON = 11;

    /** The number of exit buttons in the UI */
    public static final int NUM_EXIT_BUTTONS = 12;

    /** Parameter to {@link MMUI#setFrameImage} to show the default frame image. */
    public static final String DEFAULT_FRAME_IMAGE = "default";

    /** Parameter to {@link MMUI#setFrameImage}, {@link MMUI#setEntityImage},
     *  or {@link MMUI#showImageDialog} to show no image. */
    public static final String NO_IMAGE = "empty";

    private static final String[] BUTTON_NAMES =
        {"NW", "N", "NE", "X1",
         "W", "MID", "E", "X2",
         "SW", "S", "SE", "X3"};

    /**
     * Returns a string representation of the direction indicated by a given button position
     * @param position button position
     * @return string representation, or "(none)" if {@code position} is invalid
     */
    public static String buttonPositionToText(int position) {
        if (position < NUM_EXIT_BUTTONS)
            return BUTTON_NAMES[position];
        else
            return "(none)";
    }

    /**
     * Returns the corresponding button position for a given textual representation
     * @param text textual representation of a button position ("N", "SW", "MID", "X1", etc.)
     * @return the corresponding button position, or -1 if the text does not indicate a valid
     * position
     */
    public static int buttonTextToPosition(String text) {
        if (text == null)
            return -1;
        else
            return ArrayUtils.indexOf(BUTTON_NAMES, text);
    }
}
