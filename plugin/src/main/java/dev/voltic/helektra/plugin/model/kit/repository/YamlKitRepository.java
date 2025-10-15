package dev.voltic.helektra.plugin.model.kit.repository;

import dev.voltic.helektra.plugin.model.kit.Kit;
import dev.voltic.helektra.plugin.model.kit.KitRule;
import dev.voltic.helektra.plugin.utils.config.FileConfig;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class YamlKitRepository implements KitRepository {

    private final FileConfig kitsConfig;
    private final Map<String, Kit> cache = new LinkedHashMap<>();

    @Override
    public Optional<Kit> findByName(String name) {
        return Optional.ofNullable(cache.get(name.toLowerCase()));
    }

    @Override
    public List<Kit> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void save(Kit kit) {
        cache.put(kit.getName().toLowerCase(), kit);
        saveToYaml(kit);
    }

    @Override
    public void deleteByName(String name) {
        String key = name.toLowerCase();
        cache.remove(key);
        kitsConfig.getConfig().set("kits." + key, null);
        kitsConfig.save();
    }

    @Override
    public List<String> findAllNames() {
        return cache.values().stream()
                .map(Kit::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Kit> findByArenaId(String arenaId) {
        return cache.values().stream()
                .filter(kit -> kit.hasArena(arenaId))
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<Kit> kits) {
        cache.clear();
        kits.forEach(kit -> cache.put(kit.getName().toLowerCase(), kit));
        
        kitsConfig.getConfig().set("kits", null);
        kits.forEach(this::saveToYaml);
    }

    @Override
    public void loadAll() {
        cache.clear();
        ConfigurationSection kitsSection = kitsConfig.getConfig().getConfigurationSection("kits");
        
        if (kitsSection == null) {
            return;
        }

        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
            if (kitSection == null) continue;

            try {
                Kit kit = loadFromYaml(key, kitSection);
                cache.put(key, kit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToYaml(Kit kit) {
        String path = "kits." + kit.getName().toLowerCase();
        ConfigurationSection section = kitsConfig.getConfig().createSection(path);
        
        section.set("name", kit.getName());
        section.set("displayName", kit.getDisplayName());
        section.set("items", kit.getItems());
        section.set("arenaIds", new ArrayList<>(kit.getArenaIds()));
        section.set("icon", kit.getIcon());
        section.set("slot", kit.getSlot());
        section.set("kitEditorSlot", kit.getKitEditorSlot());
        section.set("health", kit.getHealth());
        section.set("potionEffects", kit.getPotionEffects());
        section.set("damageMultiplier", kit.getDamageMultiplier());
        section.set("description", kit.getDescription());
        
        ConfigurationSection rulesSection = section.createSection("rules");
        kit.getRules().forEach((rule, enabled) -> 
            rulesSection.set(rule.name(), enabled)
        );
        
        kitsConfig.save();
    }

    @SuppressWarnings("unchecked")
    private Kit loadFromYaml(String key, ConfigurationSection section) {
        Kit.Builder builder = Kit.builder()
                .name(section.getString("name", key))
                .displayName(section.getString("displayName", key))
                .slot(section.getInt("slot", 0))
                .kitEditorSlot(section.getInt("kitEditorSlot", 0))
                .health(section.getDouble("health", 20.0))
                .damageMultiplier(section.getDouble("damageMultiplier", 1.0));

        if (section.contains("items")) {
            List<ItemStack> items = (List<ItemStack>) section.getList("items", new ArrayList<>());
            builder.items(items);
        }

        if (section.contains("arenaIds")) {
            List<String> arenaList = section.getStringList("arenaIds");
            builder.arenaIds(new HashSet<>(arenaList));
        }

        if (section.contains("icon")) {
            builder.icon((ItemStack) section.get("icon"));
        }

        if (section.contains("potionEffects")) {
            List<PotionEffect> effects = (List<PotionEffect>) section.getList("potionEffects", new ArrayList<>());
            builder.potionEffects(effects);
        }

        if (section.contains("description")) {
            builder.description(section.getStringList("description"));
        }

        if (section.contains("rules")) {
            ConfigurationSection rulesSection = section.getConfigurationSection("rules");
            if (rulesSection != null) {
                rulesSection.getKeys(false).forEach(ruleKey -> {
                    try {
                        KitRule rule = 
                            KitRule.valueOf(ruleKey);
                        builder.rule(rule, rulesSection.getBoolean(ruleKey));
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
        }

        return builder.build();
    }
}
