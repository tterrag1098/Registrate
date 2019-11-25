package com.tterrag.registrate.providers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.tterrag.registrate.Registrate;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistrateLangProvider extends LanguageProvider implements RegistrateProvider {
    
    private static class AccessibleLanguageProvider extends LanguageProvider {

        public AccessibleLanguageProvider(DataGenerator gen, String modid, String locale) {
            super(gen, modid, locale);
        }

        @Override
        public void add(String key, String value) {
            super.add(key, value);
        }

        @Override
        protected void addTranslations() {}
    }
    
    private final Registrate owner;
    
    private final AccessibleLanguageProvider upsideDown;

    public RegistrateLangProvider(Registrate owner, DataGenerator gen) {
        super(gen, owner.getModid(), "en_us");
        this.owner = owner;
        this.upsideDown = new AccessibleLanguageProvider(gen, owner.getModid(), "en_ud");
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    protected void addTranslations() {
        owner.genData(ProviderType.LANG, this);
    }
    
    public static final String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }
    
    public String getAutomaticName(Supplier<? extends IForgeRegistryEntry<?>> sup) {
        return toEnglishName(sup.get().getRegistryName().getPath());
    }
    
    public void addBlock(Supplier<? extends Block> block) {
        addBlock(block, getAutomaticName(block));
    }
    
    public void addBlockWithTooltip(Supplier<? extends Block> block, String tooltip) {
        addBlock(block);
        addTooltip(block, tooltip);
    }
    
    public void addBlockWithTooltip(Supplier<? extends Block> block, String name, String tooltip) {
        addBlock(block, name);
        addTooltip(block, tooltip);
    }
    
    public void addItem(Supplier<? extends Item> item) {
        addItem(item, getAutomaticName(item));
    }
    
    public void addItemWithTooltip(Supplier<? extends Item> block, String name, List<String> tooltip) {
        addItem(block, name);
        addTooltip(block, tooltip);
    }
    
    public void addTooltip(Supplier<? extends IItemProvider> item, String tooltip) {
        add(item.get().asItem().getTranslationKey() + ".desc", tooltip);
    }
    
    public void addTooltip(Supplier<? extends IItemProvider> item, List<String> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            add(item.get().asItem().getTranslationKey() + ".desc." + i, tooltip.get(i));
        }
    }
    
    public void add(ItemGroup group, String name) {
        add(group.getTranslationKey(), name);
    }
    
    public void addEntityType(Supplier<? extends EntityType<?>> entity) {
        addEntityType(entity, getAutomaticName(entity));
    }
    
    public void addBiome(Supplier<? extends Biome> biome) {
        addBiome(biome, getAutomaticName(biome));
    }
    
    // @formatter:off
    // GENERATED START

    @Override
    public void addBlock(Supplier<? extends Block> key, String name) { super.addBlock(key, name); }

    @Override
    public void add(Block key, String name) { super.add(key, name); }

    @Override
    public void addItem(Supplier<? extends Item> key, String name) { super.addItem(key, name); }

    @Override
    public void add(Item key, String name) { super.add(key, name); }

    @Override
    public void addItemStack(Supplier<ItemStack> key, String name) { super.addItemStack(key, name); }

    @Override
    public void add(ItemStack key, String name) { super.add(key, name); }

    @Override
    public void addEnchantment(Supplier<? extends Enchantment> key, String name) { super.addEnchantment(key, name); }

    @Override
    public void add(Enchantment key, String name) { super.add(key, name); }

    @Override
    public void addBiome(Supplier<? extends Biome> key, String name) { super.addBiome(key, name); }

    @Override
    public void add(Biome key, String name) { super.add(key, name); }

    @Override
    public void addEffect(Supplier<? extends Effect> key, String name) { super.addEffect(key, name); }

    @Override
    public void add(Effect key, String name) { super.add(key, name); }

    @Override
    public void addEntityType(Supplier<? extends EntityType<?>> key, String name) { super.addEntityType(key, name); }

    @Override
    public void add(EntityType<?> key, String name) { super.add(key, name); }

    // GENERATED END
    
    // Automatic en_ud generation

    private static final String NORMAL_CHARS = 
            /* lowercase */ "abcdefghijklmn\u00F1opqrstuvwxyz" +
            /* uppercase */ "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            /*  numbers  */ "0123456789" +
            /*  special  */ "_,;.?!/\\'";
    private static final String UPSIDE_DOWN_CHARS = 
            /* lowercase */ "\u0250q\u0254p\u01DD\u025Fb\u0265\u0131\u0638\u029E\u05DF\u026Fuuodb\u0279s\u0287n\u028C\u028Dx\u028Ez" +
            /* uppercase */ "\u2C6F\u15FA\u0186\u15E1\u018E\u2132\u2141HI\u017F\u029E\uA780WNO\u0500\u1F49\u1D1AS\u27D8\u2229\u039BMX\u028EZ" +
            /*  numbers  */ "0\u0196\u1105\u0190\u3123\u03DB9\u312586" +
            /*  special  */ "\u203E'\u061B\u02D9\u00BF\u00A1/\\,";
    
    static {
        if (NORMAL_CHARS.length() != UPSIDE_DOWN_CHARS.length()) {
            throw new AssertionError("Char maps do not match in length!");
        }
    }

    private String toUpsideDown(String normal) {
        char[] ud = new char[normal.length()];
        for (int i = 0; i < normal.length(); i++) {
            char c = normal.charAt(i);
            if (c == '%') {
                String fmtArg = "";
                while (Character.isDigit(c) || c == '%' || c == '$' || c == 's' || c == 'd') { // TODO this is a bit lazy
                    fmtArg += c;
                    i++;
                    c = i == normal.length() ? 0 : normal.charAt(i);
                }
                i--;
                for (int j = 0; j < fmtArg.length(); j++) {
                    ud[normal.length() - 1 - i + j] = fmtArg.charAt(j);
                }
                continue;
            }
            int lookup = NORMAL_CHARS.indexOf(c);
            if (lookup >= 0) {
                c = UPSIDE_DOWN_CHARS.charAt(lookup);
            }
            ud[normal.length() - 1 - i] = c;
        }
        return new String(ud);
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
        upsideDown.add(key, toUpsideDown(value));
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        super.act(cache);
        upsideDown.act(cache);
    }
}
