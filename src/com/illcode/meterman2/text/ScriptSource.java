package com.illcode.meterman2.text;

import bsh.NameSpace;
import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

import java.util.List;

/**
 * A {@code TextSource} whose output is determined by running a script.
 * <p/>
 * A ScriptSource can optionally reference an {@link XBundle}, in which case the text
 * output will be formatted according to the settings of that XBundle by calling
 * {@link XBundle#formatText(String)}. Also, the XBundle will be put into the script's
 * declaring namespace as a variable named "bundle".
 */
public class ScriptSource implements TextSource
{
    private final String id;
    private final String source;
    private ScriptedMethod method;
    private XBundle bundle;

    public ScriptSource(String id, String source) {
        this.id = id;
        this.source = source;
    }

    public ScriptSource(String id, String source, XBundle bundle) {
        this(id, source);
        this.bundle = bundle;
    }

    /**
     * Set the XBundle referenced by this ScriptSource.
     * @param bundle bundle used to format the template output; if null, no additional
     *               formatting will be performed.
     */
    public void setBundle(XBundle bundle) {
        this.bundle = bundle;
    }

    public String getText() {
        if (method == null) {  // this is the first time we're invoked
            StringBuilder sb = new StringBuilder(source.length() + 100);
            sb.append("void getScriptedText() {\n");
            sb.append(source);
            sb.append("\n}");
            List<ScriptedMethod> methods = Meterman2.script.evalScript(id, sb.toString());
            if (methods.isEmpty())
                return "Error in ScriptSource ID: " + id;
            method = methods.get(0);
            if (bundle != null)
                method.setVariable("bundle", bundle);
        }
        String output = method.invoke().getRight();
        if (bundle != null)
            output = bundle.formatText(output);
        return output;
    }
}
