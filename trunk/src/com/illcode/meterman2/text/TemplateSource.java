package com.illcode.meterman2.text;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

/**
 * A {@code TextSource} whose output is rendered by processing a (FreeMarker) template.
 * <p/>
 * A TextSource can optionally reference an {@link XBundle}, in which case the text
 * output will be formatted according to the settings of that XBundle by calling
 * {@link XBundle#formatText(String)}.
 */

public class TemplateSource implements TextSource
{
    private final String name;
    private XBundle bundle;

    /**
     * Construct a TextSource with a given name.
     * @param name template name (aka ID) under which the source will be stored.
     * @param source template source
     */
    public TemplateSource(String name, String source) {
        this.name = name;
        Meterman2.template.putTemplate(name, source);
    }

    /**
     * Construct a TextSource with a given name, with a referenced XBundle.
     * @param name template name (aka ID) under which the source will be stored.
     * @param source template source
     * @param bundle XBundle used to format the template output
     */
    public TemplateSource(String name, String source, XBundle bundle) {
        this(name, source);
        this.bundle = bundle;
    }

    public String getName() {
        return name;
    }

    public XBundle getBundle() {
        return bundle;
    }

    /**
     * Set the XBundle referenced by this TemplateSource.
     * @param bundle bundle used to format the template output; if null, no additional
     *               formatting will be performed.
     */
    public void setBundle(XBundle bundle) {
        this.bundle = bundle;
    }

    public String getText() {
        String output = Meterman2.template.renderTemplate(name);
        if (bundle != null)
            output = bundle.formatText(output);
        return output;
    }
}
