package com.illcode.meterman2;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * This class handles Meterman's interaction with a scripting engine, in our case FreeMarker.
 */
public class MMTemplate
{
    private Configuration cfg;
    private StringTemplateLoader strLoader;

    private Map<String,Object> rootHash;
    private Map<String,Object> systemHash;

    // Used for push/pop binding.
    private Map<String,Deque<Object>> savedBindings;

    private Set<String> systemTemplates, gameTemplates;

    public MMTemplate() {
        cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLogTemplateExceptions(false);
        cfg.setTemplateExceptionHandler(new MMTemplateExceptionHandler());
        DefaultObjectWrapperBuilder b = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
        b.setForceLegacyNonListCollections(false);
        b.setIterableSupport(true);
        b.setExposeFields(true);
        cfg.setObjectWrapper(b.build());
        strLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(strLoader);
        cfg.setTemplateUpdateDelayMilliseconds(10000);
        cfg.setInterpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setCacheStorage(new MruCacheStorage(0, 50));
        cfg.setAPIBuiltinEnabled(true);

        systemTemplates = new HashSet<>(20);
        gameTemplates = new HashSet<>(40);
        rootHash = new HashMap<>();
        systemHash = new HashMap<>();
        savedBindings = new HashMap<>();

        initSystemHash();
    }

    /** Free any resources allocated by this MMTemplate instance. */
    public void dispose() {
        clearTemplateCache();
        clearGameTemplates();
        clearSystemTemplates();
        savedBindings = null;
        systemHash = null;
        rootHash = null;
        gameTemplates = null;
        systemTemplates = null;
        strLoader = null;
        cfg = null;
    }

    private void initSystemHash() {
        putSystemBinding("utils", new TemplateUtils());
        BeansWrapper bw = new BeansWrapperBuilder(Configuration.VERSION_2_3_28).build();;
        TemplateHashModel staticModels = bw.getStaticModels();
        try {
            putSystemBinding("attributes", staticModels.get("com.illcode.meterman2.SystemAttributes"));
        } catch (TemplateModelException ex) {
            logger.log(Level.WARNING, "MMTemplate.initRootHash()", ex);
        }
    }

    /**
     * Add a template to our loader, making it available for rendering.
     * @param name the name (aka ID) by which the template will be known.
     * @param templateSource the text source of the template
     * @param systemTemplate true if this is a system (as opposed to game) template.
     */
    public void putTemplate(String name, String templateSource, boolean systemTemplate) {
        strLoader.putTemplate(name, templateSource);
        if (systemTemplate)
            systemTemplates.add(name);
        else
            gameTemplates.add(name);
    }

    /**
     * Remove a template from our loader.
     * @param name the name (aka ID) under which it was previously put.
     */
    public void removeTemplate(String name) {
        if (gameTemplates.remove(name) || systemTemplates.remove(name))
            strLoader.removeTemplate(name);
    }

    /** Removes all system templates from our loader. */
    void clearSystemTemplates() {
        for (String name : systemTemplates)
            strLoader.removeTemplate(name);
        systemTemplates.clear();
    }

    /** Removes all game templates from our loader. */
    public void clearGameTemplates() {
        for (String name : gameTemplates)
            strLoader.removeTemplate(name);
        gameTemplates.clear();
    }

    /** Clear the template cache. */
    public void clearTemplateCache() {
        cfg.clearTemplateCache();
    }

    /**
     * Remove a template from the cache. The next time it is requested, the template source
     * will be reparsed.
     * @param name the name (aka ID) under which it was put into the loader.
     */
    public void removeTemplateFromCache(String name) {
        try {
            cfg.removeTemplateFromCache(name);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "MMTemplate cfg.removeTemplateFromCache", ex);
        }
    }

    /**
     * Put a binding into our template data model.
     * @param name key name
     * @param value value; if null, the binding will be removed.
     */
    public void putBinding(String name, Object value) {
        if (value == null)
            removeBinding(name);
        else
            rootHash.put(name, value);
    }

    /**
     * Remove a binding from our template data model. If a system binding with the same name
     * exists, it will be put into the data model in place of the removed binding.
     * @param name key name to remove
     */
    public void removeBinding(String name) {
        Object systemVal = systemHash.get(name);
        if (systemVal != null)
            rootHash.put(name, systemVal);
        else
            rootHash.remove(name);
    }

    /**
     * Saves the current value of a binding in root hash and sets a new one.
     * @param name variable name
     * @param value value; if null, the binding will be removed.
     */
    public void pushBinding(String name, Object value) {
        final Object oldVal = rootHash.get(name);
        Deque<Object> savedVals = savedBindings.get(name);
        if (savedVals == null) {
            savedVals = new LinkedList<>();
            savedBindings.put(name, savedVals);
        }
        savedVals.push(oldVal);
        putBinding(name, value);
    }

    /**
     * Restores the value of a binding in the root hash saved with {@link #pushBinding(String, Object)}.
     * @param name variable name
     */
    public void popBinding(String name) {
        Object previousVal = null;
        final Deque<Object> savedVals = savedBindings.get(name);
        if (savedVals != null && !savedVals.isEmpty())
            previousVal = savedVals.pop();
        putBinding(name, previousVal);
    }

    /**
     * Add a map of game-state bindings to our template data model.
     * @param bindings name to game-state object mapping
     */
    public void putBindings(Map<String,Object> bindings) {
        rootHash.putAll(bindings);
    }

    /** Put a binding into our system hash. It is only these bindings that will remain
     *  when {@link #clearBindings()} is called. */
    void putSystemBinding(String name, Object value) {
        if (value == null)
            systemHash.remove(name);
        else {
            systemHash.put(name, value);
            if (!rootHash.containsKey(name))
                rootHash.put(name, value);
        }
    }

    /** Remove a binding from our system hash.
     *  The binding will still persist in the data model, however */
    void removeSystemBinding(String name) {
        Object val = systemHash.remove(name);
    }

    /** Clear all game-state bindings from our template data model, resetting it
     * to the contents of the system hash. */
    void clearBindings() {
        rootHash.clear();
        rootHash.putAll(systemHash);
    }

    /**
     * Render a template using our game-state data model.
     * @param templateName name (aka id) of the template
     * @return the template output, as a string.
     */
    public String renderTemplate(String templateName) {
        return renderTemplate(templateName, rootHash);
    }

    /**
     * Render a template using our game-state data model.
     * @param templateName name (aka id) of the template
     * @param out the writer to which the template output is written. The writer is flushed, but not closed.
     */
    public void renderTemplate(String templateName, Writer out) {
        renderTemplate(templateName, rootHash, out);
    }

    /**
     * Combines a template with a root data model.
     * @param templateName name of the template, as passed to {@link Configuration#getTemplate}.
     * @param root map used as the root data model
     * @return the template output, as a string.
     */
    public String renderTemplate(String templateName, Map<String,Object> root) {
        StringWriter w = new StringWriter();
        renderTemplate(templateName, root, w);
        return w.toString();
    }

    /**
     * Combines a template with a root data model.
     * @param templateName name of the template, as passed to {@link Configuration#getTemplate}.
     * @param root map used as the root data model
     * @param out the writer to which the template output is written. The writer is flushed, but not closed.
     */
    public void renderTemplate(String templateName, Map<String,Object> root, Writer out) {
        try {
            Template t = cfg.getTemplate(templateName);
            t.process(root, out);
        } catch (IOException|TemplateException e) {
            logger.log(Level.WARNING, "Template rendering error: ", e);
        }
    }

    private static class MMTemplateExceptionHandler implements TemplateExceptionHandler
    {
        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
            try {
                out.write("[ERROR: " + te.getMessage() + "]");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not write TemplateException: ", te);
            }
        }
    }

    /**
     * Used to inject useful methods into template data models.
     */
    public final static class TemplateUtils
    {
        public int randInt(int min, int max) {
            return Utils.randInt(min, max);
        }

        public boolean hasAttr(Entity e, int attrNum) {
            return GameUtils.hasAttr(e, attrNum);
        }

        public boolean hasAttr(Room r, int attrNum) {
            return GameUtils.hasAttr(r, attrNum);
        }

    }
}
