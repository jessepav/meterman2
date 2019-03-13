package com.illcode.meterman2;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;

import static com.illcode.meterman2.MMLogging.logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class handles Meterman's interaction with a scripting engine, in our case FreeMarker.
 */
public class MMTemplate
{
    private Configuration cfg;
    private StringTemplateLoader strLoader;

    /* We do not maintain a "system namespace" for templates in the way that we do for scripts:
       the rootHash contains references only to the game state objects specific to the loaded
       game. It is cleared when a game is closed and populated when one is started. */
    private Map<String,Object> rootHash;

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

        systemTemplates = new HashSet<>(20);
        gameTemplates = new HashSet<>(40);
        rootHash = new HashMap<>();
    }

    /** Free any resources allocated by this MMTemplate instance. */
    public void dispose() {
        clearTemplateCache();
        clearGameTemplates();
        clearSystemTemplates();
        clearBindings();
        systemTemplates = null;
        gameTemplates = null;
        strLoader = null;
        cfg = null;
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
     * @param value value
     */
    public void putBinding(String name, Object value) {
        rootHash.put(name, value);
    }

    /**
     * Remove a binding from our template data model.
     * @param name key name to remove
     */
    public void removeBinding(String name) {
        rootHash.remove(name);
    }

    /**
     * Add a map of game-state bindings to our template data model.
     * @param bindings name to game-state object mapping
     */
    public void putBindings(Map<String,Object> bindings) {
        rootHash.putAll(bindings);
    }

    /** Clear all game-state bindings from our template data model. */
    public void clearBindings() {
        rootHash.clear();
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
}
