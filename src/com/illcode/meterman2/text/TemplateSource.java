package com.illcode.meterman2.text;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

/**
 * A {@code TextSource} whose output is rendered by processing a (FreeMarker) template.
 * <p/>
 * The text output will be formatted according to the settings of the template's XBundle by calling
 * {@link XBundle#formatText(String)}.
 */

public class TemplateSource implements TextSource
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

    public String getName() {
        return name;
    }

    public XBundle getBundle() {
        return bundle;
    }

    public String getText() {
        String output = Meterman2.template.renderTemplate(name);
        output = bundle.formatText(output);
        return output;
    }

    public String toString() {
        return getText();
    }
}
