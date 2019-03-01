package com.illcode.meterman2.text;

import bsh.NameSpace;

/**
 * A {@code TextSource} whose output is determined by running a script.
 */
public class ScriptSource implements TextSource
{
    private String source;
    private NameSpace ns;
    
    public String getText() {
        return null;
    }
}
