package com.illcode.meterman2.text;

import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

import java.util.List;

/**
 * A {@code TextSource} whose output is determined by running a script.
 * <p/>
 * The text output will be formatted according to the settings of the script's XBundle by calling
 * {@link XBundle#formatText(String)}. Also, the XBundle will be put into the script's
 * declaring namespace as a variable named "bundle".
 */
public class ScriptSource implements TextSource
{
    private final String id;
    private final String source;
    private final XBundle bundle;
    private ScriptedMethod method;

    public ScriptSource(String id, String source, XBundle bundle) {
        this.id = id;
        this.source = source;
        this.bundle = bundle;
    }

    public String getText() {
        if (method == null) {  // this is the first time we're invoked
            StringBuilder sb = new StringBuilder(source.length() + 100);
            sb.append("void getScriptedText() {\n");
            sb.append(source);
            sb.append("\n}");
            List<ScriptedMethod> methods = Meterman2.script.getScriptedMethods(id, sb.toString());
            if (methods.isEmpty())
                return "Error in ScriptSource ID: " + id;
            method = methods.get(0);
            method.setVariable("bundle", bundle);
        }
        String output = method.invokeGetOutput();
        output = bundle.formatText(output);
        return output;
    }

    public String toString() {
        return getText();
    }
}
