package dev.voltic.helektra.plugin.model.kit;

import lombok.Getter;

@Getter
public enum KitRule {
    BUILD("build"),
    BREAK("break"),
    COMBO("combo"),
    ENDER_PEARL("ender_pearl"),
    FALL_DAMAGE("fall_damage"),
    HUNGER("hunger"),
    INTERACT("interact"),
    KNOCKBACK("knockback"),
    REGENERATION("regeneration"),
    PROJECTILES("projectiles"),
    ARMOR("armor"),
    PARKOUR("parkour"),
    SUMO("sumo"),
    SPEED("speed"),
    DROP_ITEMS("drop_items"),
    BOXING("boxing"),
    SHIELD("shield"),
    LAVA_DAMAGE("lava_damage"),
    VOID_DAMAGE("void_damage"),
    BED_DAMAGE("bed_damage"),
    SATURATION("saturation");
    
    private final String key;
    
    KitRule(String key) {
        this.key = key;
    }
    
    public String getTranslationKey() {
        return "kit.rule." + key + ".name";
    }
    
    public String getDescriptionKey() {
        return "kit.rule." + key + ".description";
    }
    
    public String getDisplayName() {
        return key.substring(0, 1).toUpperCase() + 
               key.substring(1).replace("_", " ");
    }
    
    public static KitRule fromKey(String key) {
        if (key == null) return null;
        for (KitRule rule : values()) {
            if (rule.key.equalsIgnoreCase(key)) {
                return rule;
            }
        }
        return null;
    }
    
    public static KitRule fromName(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
