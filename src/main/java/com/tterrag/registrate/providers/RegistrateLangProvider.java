package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class RegistrateLangProvider extends LanguageProvider implements RegistrateProvider {

    private static class AccessibleLanguageProvider extends LanguageProvider {

        public AccessibleLanguageProvider(DataGenerator gen, String modid, String locale) {
            super(gen, modid, locale);
        }

        @Override
        public void add(@Nullable String key, @Nullable String value) {
            super.add(key, value);
        }

        @Override
        protected void addTranslations() {
        }
    }

    private final AbstractRegistrate<?> owner;

    private final AccessibleLanguageProvider upsideDown;

    public RegistrateLangProvider(AbstractRegistrate<?> owner, DataGenerator gen) {
        super(gen, owner.getModid(), "en_us");
        this.owner = owner;
        this.upsideDown = new AccessibleLanguageProvider(gen, owner.getModid(), "en_ud");
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public String getName() {
        return "Lang (en_us/en_ud)";
    }

    @Override
    protected void addTranslations() {
        owner.genData(ProviderType.LANG, this);
    }

    public static String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    public String getAutomaticName(NonNullSupplier<? extends IForgeRegistryEntry<?>> sup) {
        return toEnglishName(sup.get().getRegistryName().getPath());
    }

    public void addBlock(NonNullSupplier<? extends Block> block) {
        addBlock(block, getAutomaticName(block));
    }

    public void addBlockWithTooltip(NonNullSupplier<? extends Block> block, String tooltip) {
        addBlock(block);
        addTooltip(block, tooltip);
    }

    public void addBlockWithTooltip(NonNullSupplier<? extends Block> block, String name, String tooltip) {
        addBlock(block, name);
        addTooltip(block, tooltip);
    }

    public void addItem(NonNullSupplier<? extends Item> item) {
        addItem(item, getAutomaticName(item));
    }

    public void addItemWithTooltip(NonNullSupplier<? extends Item> block, String name, List<@NonnullType String> tooltip) {
        addItem(block, name);
        addTooltip(block, tooltip);
    }

    public void addTooltip(NonNullSupplier<? extends ItemLike> item, String tooltip) {
        add(item.get().asItem().getDescriptionId() + ".desc", tooltip);
    }

    public void addTooltip(NonNullSupplier<? extends ItemLike> item, List<@NonnullType String> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            add(item.get().asItem().getDescriptionId() + ".desc." + i, tooltip.get(i));
        }
    }

    public void add(CreativeModeTab group, String name) {
        add(((TranslatableComponent) group.getDisplayName()).getKey(), name);
    }

    public void addEntityType(NonNullSupplier<? extends EntityType<?>> entity) {
        addEntityType(entity, getAutomaticName(entity));
    }

    // Automatic en_ud generation
    private static final Map<Character, Character> NORMAL_TO_UPSIDE_DOWN_CHARS = Map.ofEntries(
            entry('a', '\u0250'),
            entry('b', 'q'),
            entry('c', '\u0254'),
            entry('d', 'p'),
            entry('e', '\u01DD'),
            entry('f', '\u025F'),
            entry('g', 'b'),
            entry('h', '\u0265'),
            entry('i', '\u0131'),
            entry('j', '\u0638'),
            entry('k', '\u029E'),
            entry('l', '\u05DF'),
            entry('m', '\u026F'),
            entry('n', 'u'),
            entry('\u00F1', 'u'),
            entry('o', 'o'),
            entry('p', 'd'),
            entry('q', 'b'),
            entry('r', '\u0279'),
            entry('s', 's'),
            entry('t', '\u0287'),
            entry('u', 'n'),
            entry('v', '\u028C'),
            entry('w', '\u028D'),
            entry('x', 'x'),
            entry('y', '\u028E'),
            entry('z', 'z'),
            entry('A', 'Ɐ'),
            entry('B', 'ᗺ'),
            entry('C', 'Ↄ'),
            entry('D', 'ᗡ'),
            entry('E', 'Ǝ'),
            entry('F', 'Ⅎ'),
            entry('G', '⅁'),
            entry('H', 'H'),
            entry('I', 'I'),
            entry('J', 'ſ'),
            entry('K', 'ʞ'),
            entry('L', 'Ꞁ'),
            entry('M', 'W'),
            entry('N', 'N'),
            entry('O', 'O'),
            entry('P', 'Ԁ'),
            entry('Q', 'Ὁ'),
            entry('R', 'ᴚ'),
            entry('S', 's'),
            entry('T', '⟘'),
            entry('U', '∩'),
            entry('V', 'Λ'),
            entry('W', 'M'),
            entry('X', 'X'),
            entry('Y', 'ʎ'),
            entry('Z', 'Z'),
            entry('0', '0'),
            entry('1', 'Ɩ'),
            entry('2', 'ᄅ'),
            entry('3', 'Ɛ'),
            entry('4', 'ㄣ'),
            entry('5', 'ϛ'),
            entry('6', '9'),
            entry('7', 'ㄥ'),
            entry('8', '8'),
            entry('9', '6'),
            entry('_', '‾'),
            entry(',', '\''),
            entry(';', '؛'),
            entry('.', '˙'),
            entry('?', '¿'),
            entry('!', '¡'),
            entry('/', '/'),
            entry('\\', '\\'),
            entry('\'', '\'')
    );

    private String toUpsideDown(String normal) {
        char[] ud = new char[normal.length()];
        for (int i = 0; i < normal.length(); i++) {
            char c = normal.charAt(i);
            if (c == '%') {
                StringBuilder fmtArg = new StringBuilder();
                while (Character.isDigit(c) || c == '%' || c == '$' || c == 's' || c == 'd') { // TODO this is a bit lazy
                    fmtArg.append(c);
                    i++;
                    c = i == normal.length() ? 0 : normal.charAt(i);
                }
                i--;
                for (int j = 0; j < fmtArg.length(); j++) {
                    ud[normal.length() - 1 - i + j] = fmtArg.charAt(j);
                }
                continue;
            }
            c = NORMAL_TO_UPSIDE_DOWN_CHARS.get(c);
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
    public void run(HashCache cache) throws IOException {
        super.run(cache);
        upsideDown.run(cache);
    }
}
