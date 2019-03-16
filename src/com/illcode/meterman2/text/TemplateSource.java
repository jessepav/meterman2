package com.illcode.meterman2.text;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

/**
 * A {@code TextSource} whose output is rendered by processing a (FreeMarker) template.
 * <p/>
 * The text output will be formatted according to the settings of the template's XBundle by calling
 * {@link XBundle#formatText(String)}.
 */

public final class TemplateSource implements TextSource
{
    private final String name;
    private final XBundle bundle;

    /**
     * Construct a TextSource with a given name, with a referenced XBundle.
     * @param name template name (aka ID) under which the source will be stored.
     * @param source template source
     * @param bundle XBundle used to format the template output
     */
    public TemplateSource(String name, String source, XBundle bundle) {
        this.name = name;
        this.bundle = bundle;
        Meterman2.template.putTemplate(name, source, bundle.isSystemBundle());
    }

    /** Release resources used by this TemplateSource. Attempting to render the template after
     *  this call will fail. */
    public void dispose() {
        Meterman2.template.removeTemplate(name);
    }

    public String getName() {
        return name;
    }

    public XBundle getBundle() {
        return bundle;
    }

    public String getText() {
        return getText((String[]) null);
    }

    /**
     * @param bindings an array of even length, conceptually grouped into pairs of variable name and value.<br/>
     *                 i.e. {@code [name1, value1, name2, value2, etc.]}
     */
    public String getText(String... bindings) {
        int numVars = 0;
        if (bindings != null) {
            numVars = bindings.length / 2;
            for (int i = 0; i < numVars; i++)
                Meterman2.template.putBinding(bindings[i*2], bindings[i*2+1]);
        }
        final String output = Meterman2.template.renderTemplate(name);
        if (bindings != null)
            for (int i = 0; i < numVars; i++)
                Meterman2.template.removeBinding(bindings[i*2]);
        return bundle.formatText(output);
    }

    public String toString() {
        return getText();
    }
}
