package com.illcode.meterman2.text;

import bsh.NameSpace;

/**
 * A {@code TextSource} whose output is determined by running a script.
 */
public class ScriptSource implements TextSource
{
    private final String id;
    private final String source;
    private NameSpace ns;

    public ScriptSource(String id, String source) {
        this.id = id;
        this.source = source;
    }

    public String getText() {
        if (ns == null) {  // this is the first time we're invoked
            //ns =
        }
        return null;
    }
}
