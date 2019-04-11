package com.illcode.meterman2.handler;

import com.illcode.meterman2.ui.UIConstants;
import static com.illcode.meterman2.ui.UIConstants.NUM_LABELS;

/**
 * A status bar provider that composes other providers, one for each label position.
 * <p/>
 * It can optionally reroute the positions handled by each composed provider (ex. make
 * a provider's left-label text be displayed in the middle).
 * <p/>
 * We do not check for valid label positions, so always use one of the <tt>LABEL</tt>
 * position constants in {@code UIConstants}!
 */
public final class CompositeStatusBarProvider implements StatusBarProvider
{
    private StatusBarProvider[] providers;
    
    /**
     * For each status bar position <em>pos</em>, the two entries in <tt>routeTable</tt> that determine
     * what will be shown there are:
     * <ul>
     * <li>routeTable[<em>pos</em>*2] = which of our three composed providers will be used</li>
     * <li>routeTable[<em>pos</em>*2 + 1] = which position in that provider will be used
     * </ul>
     */
    private int[] routeTable;

    /** Create a new composite status bar provider with no composed providers. */
    public CompositeStatusBarProvider() {
        providers = new StatusBarProvider[NUM_LABELS];
        routeTable = new int[NUM_LABELS];
        resetRouting();
    }

    /** Create a new composite status bar provider with the given providers. If a given
     * provider parameter is <tt>null</tt>, no text will be returned for that position. */
    public CompositeStatusBarProvider(StatusBarProvider left,
                                      StatusBarProvider center,
                                      StatusBarProvider right) {
        this();
        providers[UIConstants.LEFT_LABEL] = left;
        providers[UIConstants.CENTER_LABEL] = center;
        providers[UIConstants.RIGHT_LABEL] = right;
    }

    /** Return the status bar provider for the given label position, or null if none is set. */
    public StatusBarProvider getProvider(int labelPos) {
        return providers[labelPos];
    }

    /** Set the status bar provider for the given label position. */
    public void setProvider(int labelPos, StatusBarProvider provider) {
        providers[labelPos] = provider;
    }

    /** Reset label position routing. */
    public void resetRouting() {
        for (int i = 0; i < NUM_LABELS; i++)
            routeTable[i] = i;
    }

    /**
     * Route a label position to a differnt provider.
     * @param labelPos label position
     * @param providerPos the provider that will now supply the given label position. That is, the value of
     *      <blockquote>
     *      {@code providers[providerPos].getStatusText(providerPos)}
     *      </blockquote>
     *      will be used at <tt>labelPos</tt>.<br/>
     *      If {@code providerPos} equals -1, the label at {@code labelPos} will be left blank.
     */
    public void route(int labelPos, int providerPos) {
        routeTable[labelPos] = providerPos;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * We do this by delegating the call to the appropriate composed provider, if present.
     */
    public String getStatusText(int labelPos) {
        final int providerPos = routeTable[labelPos];
        if (providerPos != -1) {
            final StatusBarProvider sbp = providers[providerPos];
            if (sbp != null)
                return sbp.getStatusText(providerPos);
        }
        return null;
    }
}
