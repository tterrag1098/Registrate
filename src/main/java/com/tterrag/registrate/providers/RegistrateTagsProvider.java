package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.neoforged.fml.LogicalSide;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RegistrateTagsProvider<T> extends RegistrateLookupFillerProvider {

    CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter();

	ResourceKey<? extends Registry<T>> registry();

    TagBuilder rawBuilder(TagKey<T> key);

    class Impl<T> extends TagsProvider<T> implements RegistrateTagsProvider<T> {
        private final AbstractRegistrate<?> owner;
        private final ProviderType<? extends Impl<T>> type;
        private final String name;

        public Impl(AbstractRegistrate<?> owner, ProviderType<? extends Impl<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup) {
            super(packOutput, registryIn, registriesLookup, owner.getModid());

            this.owner = owner;
            this.type = type;
            this.name = name;
        }

        @Override
        public String getName() {
            return "Tags (%s)".formatted(name);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            owner.genData(type, this);
        }

        @Override
        public TagBuilder rawBuilder(final TagKey<T> key) {
            return super.getOrCreateRawBuilder(key);
        }

        @Override
        public TagAppender<T> tag(TagKey<T> key) {
            return super.tag(key);
        }

        @Override
        public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
            return createContentsProvider();
        }

        @Override
		public ResourceKey<? extends Registry<T>> registry() {
			return registryKey;
		}

	}
}
