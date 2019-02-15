package com.illcode.meterman2;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;

import java.io.IOException;

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
}
