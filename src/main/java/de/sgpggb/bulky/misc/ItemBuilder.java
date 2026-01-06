package de.sgpggb.bulky.misc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ItemBuilder {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private final List<Component> lore = new ArrayList<>();
    private final EnumSet<ItemFlag> flags = EnumSet.noneOf(ItemFlag.class);
    private Material material;
    private int amount = 1;
    private int damage = 0;
    private Component displayName;
    private String b64texture;

    private Boolean unbreakable;
    private Boolean glintOverride;

    private boolean allowUnsafeStackSize = false;

    /**
     * Create a builder with a default item.
     */
    public ItemBuilder() {
        this(Material.AIR);
    }

    /**
     * Create a builder with a material. If material is null, AIR is used.
     */
    public ItemBuilder(Material material) {
        this.material = (material == null) ? Material.AIR : material;
    }

    /**
     * Create a builder with a material and amount.
     */
    public ItemBuilder(Material material, int amount) {
        this(material);
        this.amount(amount);
    }

    /**
     * Create a builder from an existing ItemStack.
     */
    public ItemBuilder(ItemStack base) {
        Objects.requireNonNull(base, "ItemStack must not be null.");

        this.material = base.getType();
        this.amount = base.getAmount();

        ItemMeta meta = base.getItemMeta();
        if (meta instanceof Damageable dmg)
            this.damage = dmg.getDamage();

        // Copy enchantments
        this.enchantments.putAll(base.getEnchantments());

        // Copy display name / lore
        if (meta != null) {
            if (meta.hasDisplayName())
                this.displayName = meta.displayName();

            if (meta.hasLore()) {
                List<Component> existingLore = meta.lore();
                if (existingLore != null)
                    this.lore.addAll(existingLore);
            }

            this.flags.addAll(meta.getItemFlags());
            this.unbreakable = meta.isUnbreakable();
        }
    }

    /**
     * Read an ItemStack from config at the given path.
     */
    public static ItemBuilder fromConfig(FileConfiguration cfg, String path) {
        Objects.requireNonNull(cfg, "cfg must not be null.");
        Objects.requireNonNull(path, "path must not be null.");

        ItemStack stack = cfg.getItemStack(path);
        if (stack == null)
            stack = new ItemStack(Material.AIR);
        return new ItemBuilder(stack);
    }

    /**
     * Deserialize from JSON (created by {@link #toJson()}).
     */
    public static ItemBuilder fromJson(String json) {
        Objects.requireNonNull(json, "json must not be null.");
        Map<String, Object> map = GSON.fromJson(json, MAP_TYPE);
        if (map == null)
            return new ItemBuilder(Material.AIR);

        ItemStack stack = ItemStack.deserialize(map);
        return new ItemBuilder(stack);
    }

    /**
     * Write the built ItemStack to config at the given path.
     */
    public void toConfig(FileConfiguration cfg, String path) {
        Objects.requireNonNull(cfg, "cfg must not be null.");
        Objects.requireNonNull(path, "path must not be null.");
        cfg.set(path, build());
    }

    /**
     * Serialize the built ItemStack to JSON using Bukkit serialization.
     */
    public String toJson() {
        Map<String, Object> serialized = build().serialize();
        return GSON.toJson(serialized);
    }

    /**
     * Allow stack sizes outside the material max stack size and <= 0.
     */
    public ItemBuilder unsafeStackSize(boolean allow) {
        this.allowUnsafeStackSize = allow;
        return this;
    }

    /**
     * Set amount (validated unless unsafe stack sizes are enabled).
     */
    public ItemBuilder amount(int amount) {
        if (!allowUnsafeStackSize) {
            int max = material.getMaxStackSize();
            if (amount <= 0 || amount > max)
                amount = 1;
        }
        this.amount = amount;
        return this;
    }

    /**
     * Set durability/damage (0 = undamaged).
     */
    public ItemBuilder damage(int damage) {
        this.damage = Math.max(0, damage);
        return this;
    }

    /**
     * Change material. If null, AIR is used.
     */
    public ItemBuilder material(Material material) {
        this.material = (material == null) ? Material.AIR : material;
        this.amount(this.amount);
        return this;
    }

    /**
     * Set the display name as an Adventure Component.
     */
    public ItemBuilder displayName(Component name) {
        this.displayName = Objects.requireNonNull(name, "displayName must not be null.");
        return this;
    }

    /**
     * Convenience: set display name from legacy string (e.g. "&aHello").
     */
    public ItemBuilder displayName(String legacyText) {
        Objects.requireNonNull(legacyText, "legacyText must not be null.");
        this.displayName = legacySerializer().deserialize(legacyText);
        return this;
    }

    /**
     * Replace lore with the given list.
     */
    public ItemBuilder lore(List<Component> lore) {
        Objects.requireNonNull(lore, "lore must not be null.");
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    /**
     * Add one lore line.
     */
    public ItemBuilder addLore(Component line) {
        this.lore.add(Objects.requireNonNull(line, "lore line must not be null."));
        return this;
    }

    /**
     * Convenience: add lore lines from legacy strings (e.g. "&7Line").
     */
    public ItemBuilder addLore(String... legacyLines) {
        Objects.requireNonNull(legacyLines, "legacyLines must not be null.");
        for (String s : legacyLines) {
            if (s == null)
                continue;
            this.lore.add(legacySerializer().deserialize(s));
        }
        return this;
    }

    /**
     * Add an enchantment level.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        Objects.requireNonNull(enchantment, "enchantment must not be null.");
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Add all enchantments from a map.
     */
    public ItemBuilder enchantAll(Map<Enchantment, Integer> enchants) {
        Objects.requireNonNull(enchants, "enchants must not be null.");
        this.enchantments.putAll(enchants);
        return this;
    }

    /**
     * Add an item flag.
     */
    public ItemBuilder flag(ItemFlag flag) {
        this.flags.add(Objects.requireNonNull(flag, "flag must not be null."));
        return this;
    }

    /**
     * Add multiple item flags.
     */
    public ItemBuilder flags(Collection<ItemFlag> flags) {
        Objects.requireNonNull(flags, "flags must not be null.");
        this.flags.addAll(flags);
        return this;
    }

    /**
     * Mark item unbreakable.
     */
    public ItemBuilder unbreakable(boolean value) {
        this.unbreakable = value;
        return this;
    }

    /**
     * Set enchantment glint override.
     */
    public ItemBuilder glow(boolean value) {
        this.glintOverride = value;
        return this;
    }

    /**
     * Set a custom base64 texture.
     */
    public ItemBuilder playerHead(String b64texture) {
        Objects.requireNonNull(b64texture, "b64texture must not be null.");
        this.material(Material.PLAYER_HEAD);
        if (b64texture.isBlank()) {
            this.b64texture = null;
            return this;
        }

        this.b64texture = b64texture;
        return this;
    }

    /**
     * Build a fresh ItemStack each call (no shared mutable state).
     */
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof Damageable dmg)
            dmg.setDamage(damage);

        if (!enchantments.isEmpty())
            //unsafe to allow higher levels
            item.addUnsafeEnchantments(enchantments);

        if (displayName != null)
            meta.displayName(displayName);

        if (!lore.isEmpty())
            meta.lore(new ArrayList<>(lore));

        if (!flags.isEmpty())
            meta.addItemFlags(flags.toArray(ItemFlag[]::new));

        if (unbreakable != null)
            meta.setUnbreakable(unbreakable);

        if (glintOverride != null)
            meta.setEnchantmentGlintOverride(glintOverride);

        if (b64texture != null && meta instanceof SkullMeta skullMeta) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", b64texture));
            skullMeta.setPlayerProfile(profile);
            meta = skullMeta;
        }

        item.setItemMeta(meta);
        return item;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public int getDamage() {
        return damage;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public List<Component> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Collections.unmodifiableMap(enchantments);
    }

    public Set<ItemFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    private LegacyComponentSerializer legacySerializer() {
        return LegacyComponentSerializer.legacySection();
    }
}
