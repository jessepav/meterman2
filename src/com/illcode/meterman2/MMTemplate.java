package com.illcode.meterman2;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;

import static com.illcode.meterman2.MMLogging.logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class handles Meterman's interaction with a scripting engine, in our case FreeMarker.
 */
public class MMTemplate
{
    private Configuration cfg;
    private StringTemplateLoader strLoader;

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

    }

    public void putTemplate(String name, String templateSource) {
        strLoader.putTemplate(name, templateSource);
    }

    public void removeTemplate(String name) {
        strLoader.removeTemplate(name);
    }

    public void clearTemplateCache() {
        cfg.clearTemplateCache();
    }

    public void removeTemplateFromCache(String name) {
        try {
            cfg.removeTemplateFromCache(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
