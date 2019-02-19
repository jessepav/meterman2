package com.illcode.meterman2;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import static com.illcode.meterman2.MMLogging.logger;

import java.io.IOException;
import java.io.Writer;
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
        b.setExposeFields(true);
        cfg.setObjectWrapper(b.build());
        strLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(strLoader);
        cfg.setTemplateUpdateDelayMilliseconds(86400000);  // 24 hours
        cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setCacheStorage(new MruCacheStorage(0, 50));

    }

    public void clearTemplateCache() {
        cfg.clearTemplateCache();
    }

    public void removeTemplateFromCache(String templateName) {
        try {
            cfg.removeTemplateFromCache(templateName);
        } catch (IOException e) {
            e.printStackTrace();
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
