package com.illcode.meterman2.util;

import com.illcode.meterman2.bundle.BundleGroup;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * A helper class to make a bundle group's passages available to templates as a hash.
 */
public class PassageHash implements TemplateHashModel
{
    private BundleGroup group;

    public PassageHash(BundleGroup group) {
        this.group = group;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        return new SimpleScalar(group.getPassage(key).getText());
    }

    public boolean isEmpty() throws TemplateModelException {
        return false;
    }
}
