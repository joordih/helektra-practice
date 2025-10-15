package dev.voltic.helektra.plugin.model.kit;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.voltic.helektra.api.model.kit.IKit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Kit implements IKit {

    @JsonProperty("_id")
    private String id;
    
    private String name;
    private String displayName;
    private List<ItemStack> items;
    private Set<String> arenaIds;
    private ItemStack icon;
    private Map<KitRule, Boolean> rules;
    private int queue;
    private int playing;
    private int slot;
    private int kitEditorSlot;
    private double health;
    private List<PotionEffect> potionEffects;
    private double damageMultiplier;
    private List<String> description;

    private Kit(Builder builder) {
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.items = builder.items;
        this.arenaIds = builder.arenaIds;
        this.icon = builder.icon;
        this.rules = builder.rules;
        this.queue = 0;
        this.playing = 0;
        this.slot = builder.slot;
        this.health = builder.health;
        this.kitEditorSlot = builder.kitEditorSlot;
        this.potionEffects = builder.potionEffects;
        this.damageMultiplier = builder.damageMultiplier;
        this.description = builder.description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromPlayer(String name, Player player) {
        List<ItemStack> items = Arrays.stream(player.getInventory().getContents())
            .filter(Objects::nonNull)
            .toList();
            
        return new Builder()
            .name(name)
            .displayName(name)
            .items(new ArrayList<>(items))
            .potionEffects(new ArrayList<>(player.getActivePotionEffects()))
            .health(player.getHealth());
    }

    public static class Builder {
        private String name;
        private String displayName;
        private List<ItemStack> items = new ArrayList<>();
        private Set<String> arenaIds = new HashSet<>();
        private ItemStack icon = new ItemStack(Material.DIAMOND_SWORD);
        private Map<KitRule, Boolean> rules = createDefaultRules();
        private int slot = 0;
        private int kitEditorSlot = 0;
        private double health = 20.0;
        private List<PotionEffect> potionEffects = new ArrayList<>();
        private double damageMultiplier = 1.0;
        private List<String> description = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder items(List<ItemStack> items) {
            this.items = items != null ? items : new ArrayList<>();
            return this;
        }

        public Builder addItem(ItemStack item) {
            if (item != null) {
                this.items.add(item);
            }
            return this;
        }

        public Builder arenaIds(Set<String> arenaIds) {
            this.arenaIds = arenaIds != null ? arenaIds : new HashSet<>();
            return this;
        }

        public Builder addArenaId(String arenaId) {
            if (arenaId != null) {
                this.arenaIds.add(arenaId);
            }
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon != null && icon.getType() != Material.AIR 
                ? icon 
                : new ItemStack(Material.DIAMOND_SWORD);
            return this;
        }

        public Builder rules(Map<KitRule, Boolean> rules) {
            this.rules = rules != null ? rules : createDefaultRules();
            return this;
        }

        public Builder rule(KitRule rule, boolean enabled) {
            this.rules.put(rule, enabled);
            return this;
        }

        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public Builder kitEditorSlot(int kitEditorSlot) {
            this.kitEditorSlot = kitEditorSlot;
            return this;
        }

        public Builder health(double health) {
            this.health = health;
            return this;
        }

        public Builder potionEffects(List<PotionEffect> potionEffects) {
            this.potionEffects = potionEffects != null ? potionEffects : new ArrayList<>();
            return this;
        }

        public Builder addPotionEffect(PotionEffect effect) {
            if (effect != null) {
                this.potionEffects.add(effect);
            }
            return this;
        }

        public Builder damageMultiplier(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
            return this;
        }

        public Builder description(List<String> description) {
            this.description = description != null ? description : new ArrayList<>();
            return this;
        }

        public Builder addDescription(String line) {
            if (line != null) {
                this.description.add(line);
            }
            return this;
        }

        public Kit build() {
            Objects.requireNonNull(name, "Kit name cannot be null");
            if (displayName == null) {
                displayName = name;
            }
            return new Kit(this);
        }
    }

    private static Map<KitRule, Boolean> createDefaultRules() {
        Map<KitRule, Boolean> rules = new EnumMap<>(KitRule.class);
        for (KitRule rule : KitRule.values()) {
            rules.put(rule, false);
        }
        return rules;
    }

    public boolean hasRule(KitRule rule) {
        return rules.getOrDefault(rule, false);
    }

    public void toggleRule(KitRule rule) {
        rules.put(rule, !hasRule(rule));
    }

    public boolean hasArena(String arenaId) {
        return arenaIds.contains(arenaId);
    }

    public void toggleArena(String arenaId) {
        if (arenaIds.contains(arenaId)) {
            arenaIds.remove(arenaId);
        } else {
            arenaIds.add(arenaId);
        }
    }

    public void incrementQueue() {
        queue++;
    }

    public void decrementQueue() {
        if (queue > 0) {
            queue--;
        }
    }

    public void incrementPlaying() {
        playing++;
    }

    public void decrementPlaying() {
        if (playing > 0) {
            playing--;
        }
    }

    public void applyLoadout(Player player) {
        applyLoadout(player, null);
    }

    public void applyLoadout(Player player, List<ItemStack> customLoadout) {
        Objects.requireNonNull(player, "Player cannot be null");
        
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        List<ItemStack> loadout = customLoadout != null && !customLoadout.isEmpty() 
            ? customLoadout 
            : items;
            
        player.getInventory().setContents(loadout.toArray(new ItemStack[0]));
        
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType())
        );
        
        if (!potionEffects.isEmpty()) {
            player.addPotionEffects(potionEffects);
        }
        
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
            : 20.0;
        player.setHealth(Math.min(health, maxHealth));
        player.updateInventory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Kit kit)) return false;
        return Objects.equals(name, kit.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Kit{name='" + name + "', queue=" + queue + ", playing=" + playing + "}";
    }
}
