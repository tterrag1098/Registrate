package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.KeyTagProvider;
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

    interface Key<T> extends RegistrateTagsProvider<T> {
        TagAppender<ResourceKey<T>, T> tag(TagKey<T> key);
    }

    interface Intrinsic<T> extends RegistrateTagsProvider<T> {
        TagAppender<T, T> tag(TagKey<T> key);
    }

    class Impl<T> extends KeyTagProvider<T> implements RegistrateTagsProvider.Key<T> {
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
        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        @Override
        public TagBuilder rawBuilder(final TagKey<T> key) {
            return super.getOrCreateRawBuilder(key);
        }

        @Override
        public TagAppender<ResourceKey<T>, T> tag(TagKey<T> key) {
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

    class IntrinsicImpl<T> extends IntrinsicHolderTagsProvider<T> implements RegistrateTagsProvider.Intrinsic<T> {
        private final AbstractRegistrate<?> owner;
        private final ProviderType<? extends IntrinsicImpl<T>> type;
        private final String name;

        public IntrinsicImpl(AbstractRegistrate<?> owner, ProviderType<? extends IntrinsicImpl<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, Function<T, ResourceKey<T>> keyExtractor) {
            super(packOutput, registryIn, registriesLookup, keyExtractor, owner.getModid());

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
        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        @Override
        public TagBuilder rawBuilder(TagKey<T> key) {
            return super.getOrCreateRawBuilder(key);
        }

        @Override
        public TagAppender<T, T> tag(final TagKey<T> key) {
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
