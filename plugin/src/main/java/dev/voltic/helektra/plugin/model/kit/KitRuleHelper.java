package dev.voltic.helektra.plugin.model.kit;

import dev.voltic.helektra.plugin.utils.TranslationUtils;

public final class KitRuleHelper {
    
    private KitRuleHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static String getName(KitRule rule) {
        return TranslationUtils.translate(rule.getTranslationKey());
    }
    
    public static String getDescription(KitRule rule) {
        return TranslationUtils.translate(rule.getDescriptionKey());
    }
    
    public static String getName(KitRule rule, String locale) {
        return TranslationUtils.translate(rule.getTranslationKey(), locale);
    }
    
    public static String getDescription(KitRule rule, String locale) {
        return TranslationUtils.translate(rule.getDescriptionKey(), locale);
    }
}
