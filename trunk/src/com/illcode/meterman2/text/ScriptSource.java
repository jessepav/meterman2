package com.illcode.meterman2.text;

import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;

import java.util.List;

/**
 * A {@code TextSource} whose output is determined by running a script.
 * <p/>
 * The text output will be formatted according to the settings of the script's XBundle by calling
 * {@link XBundle#formatText(String)}.
 */
public final class ScriptSource implements TextSource
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
        return getText((String[])null);
    }

    /**
     * @param bindings an array of even length, conceptually grouped into pairs of variable name and value.<br/>
     *                 i.e. {@code [name1, value1, name2, value2, etc.]}
     */
    public String getText(String... bindings) {
        if (method == null) {  // this is the first time we're invoked
            StringBuilder sb = new StringBuilder(source.length() + 100);
            sb.append("void getScriptedText() {\n");
            sb.append(source);
            sb.append("\n}");
            List<ScriptedMethod> methods = Meterman2.script.getScriptedMethods(id, sb.toString());
            if (methods.isEmpty())
                return "Error in ScriptSource ID: " + id;
            method = methods.get(0);
        }
        int numVars = 0;
        if (bindings != null) {
            numVars = bindings.length / 2;
            for (int i = 0; i < numVars; i++)
                method.putBinding(bindings[i*2], bindings[i*2+1]);
        }
        final String output = method.invokeGetOutput();
        if (bindings != null)
            for (int i = 0; i < numVars; i++)
                method.removeBinding(bindings[i*2]);
        return bundle.formatText(output);
    }

    public String toString() {
        return getText();
    }
}
