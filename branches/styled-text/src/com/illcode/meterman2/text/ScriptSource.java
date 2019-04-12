package com.illcode.meterman2.text;

import com.illcode.meterman2.MMScript;
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
        return getTextWithArgs((Object[]) null);
    }

    /**
     * @param bindings an array of even length, conceptually grouped into pairs of variable name and value.<br/>
     *                 i.e. {@code [name1, value1, name2, value2, etc.]}
     */
    public String getTextWithBindings(String... bindings) {
        if (!ensureMethodInitialized())
            return errorMessage();

        int numVars = 0;
        if (bindings != null) {
            numVars = bindings.length / 2;
            for (int i = 0; i < numVars; i++)
                method.putBinding(bindings[i*2], bindings[i*2+1]);
        }
        final String output = getTextImpl();
        if (bindings != null)
            for (int i = 0; i < numVars; i++)
                method.removeBinding(bindings[i*2]);
        return output;
    }

    /**
     * @param args if non-null, put into the script namespace as an array variable "args".
     */
    public String getTextWithArgs(Object... args) {
        if (!ensureMethodInitialized())
            return errorMessage();

        if (args != null)
            method.putBinding("args", args);
        final String output = getTextImpl();
        if (args != null)
            method.removeBinding("args");
        return output;
    }

    private boolean ensureMethodInitialized() {
        if (method == null) {  // this is the first time we're invoked
            StringBuilder sb = new StringBuilder(source.length() + 50);
            sb.append("void getScriptedText() {\n");
            sb.append(source);
            sb.append("\n}");
            List<ScriptedMethod> methods = Meterman2.script.getScriptedMethods(id, sb.toString());
            if (methods.isEmpty())
                return false;
            method = methods.get(0);
        }
        return true;
    }

    private String errorMessage() {
        return "Error in ScriptSource ID: " + id;
    }

    private String getTextImpl() {
        final String output = method.invokeGetOutput();
        return bundle.formatText(output);
    }

    public String toString() {
        return getText();
    }
}
