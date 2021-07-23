package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * A builder for enchantments, allows for customization of the {@link Enchantment.Rarity enchantment rarity} and {@link
 * EquipmentSlot equipment slots}, and configuration of data associated with enchantments (lang).
 *
 * @param <T> The type of enchantment being built
 * @param <P> Parent object type
 */
public class EnchantmentBuilder<T extends Enchantment, P> extends AbstractBuilder<Enchantment, T, P, EnchantmentBuilder<T, P>> {

    private final EnchantmentCategory type;
    @SuppressWarnings("null")
    private final EnumSet<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

    private Enchantment.Rarity rarity = Enchantment.Rarity.COMMON;

    protected EnchantmentBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        super(owner, parent, name, callback, Enchantment.class);
        this.factory = factory;
        this.type = type;
    }

    /**
     * Create a new {@link EnchantmentBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The enchantment will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * </ul>
     *
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param type
     *            The {@link EnchantmentCategory type} of the enchantment
     * @param factory
     *            Factory to create the enchantment
     * @return A new {@link EnchantmentBuilder} with reasonable default data generators.
     */
    public static <T extends Enchantment, P> EnchantmentBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return new EnchantmentBuilder<>(owner, parent, name, callback, type, factory)
                .defaultLang();
    }

    private final EnchantmentFactory<T> factory;

    /**
     * Add the armor {@link EquipmentSlot slots} as valid slots for this enchantment, i.e. {@code HEAD}, {@code CHEST},
     * {@code LEGS}, and {@code FEET}.
     *
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> addArmorSlots() {
        return addSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }

    /**
     * Set the rarity of this enchantment. Defaults to {@link Enchantment.Rarity#COMMON}.
     *
     * @param rarity The rarity to assign
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> rarity(Enchantment.Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    /**
     * Add valid slots for this enchantment. Defaults to none. Subsequent calls are additive.
     *
     * @param slots
     *            The slots to add
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> addSlots(EquipmentSlot... slots) {
        this.slots.addAll(Arrays.asList(slots));
        return this;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     *
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> defaultLang() {
        return lang(Enchantment::getDescriptionId);
    }

    /**
     * Set the translation for this enchantment.
     *
     * @param name
     *            A localized English name
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> lang(String name) {
        return lang(Enchantment::getDescriptionId, name);
    }

    @Override
    protected @NonnullType T createEntry() {
        return factory.create(rarity, type, slots.toArray(new EquipmentSlot[0]));
    }

    @FunctionalInterface
    public interface EnchantmentFactory<T extends Enchantment> {

        T create(Enchantment.Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots);
    }
}
