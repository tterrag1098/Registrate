package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public class RegistrateIntrinsicHolderTagsProvider<T> extends RegistrateTagsProvider<T> {
    private final Function<T, ResourceKey<T>> keyExtractor;

    public RegistrateIntrinsicHolderTagsProvider(AbstractRegistrate<?> owner, ProviderType<? extends RegistrateTagsProvider<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, CompletableFuture<TagLookup<T>> parentProvider, Function<T, ResourceKey<T>> keyExtractor, ExistingFileHelper existingFileHelper) {
        super(owner, type, name, packOutput, registryIn, registriesLookup, parentProvider, existingFileHelper);

        this.keyExtractor = keyExtractor;
    }

    public RegistrateIntrinsicHolderTagsProvider(AbstractRegistrate<?> owner, ProviderType<? extends RegistrateTagsProvider<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, Function<T, ResourceKey<T>> keyExtractor, ExistingFileHelper existingFileHelper) {
        super(owner, type, name, packOutput, registryIn, registriesLookup, existingFileHelper);

        this.keyExtractor = keyExtractor;
    }

    @Override
    public IntrinsicTagAppender<T> tag(TagKey<T> tag) {
        var tagbuilder = getOrCreateRawBuilder(tag);
        return new IntrinsicTagAppender<>(tagbuilder, keyExtractor, modId);
    }

    // copied from IntrinsicHolderTagsProvider.IntrinsicTagAppender
    // constructor is not public, so we can not make use of the existing class
    // potential solution would be to have an AT to make the constructor public
    //
    // includes all methods from IForgeIntrinsicHolderTagAppender
    // unfortunately we can not implement this interface due to
    // all the methods wanting to return a IntrinsicHolderTagsProvider.IntrinsicTagAppender
    // which we are not an instance of (due to the above contractor issue)
    public static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> {
        private final Function<T, ResourceKey<T>> keyExtractor;

        IntrinsicTagAppender(TagBuilder tagBuilder, Function<T, ResourceKey<T>> keyExtractor, String modId) {
            super(tagBuilder, modId);

            this.keyExtractor = keyExtractor;
        }

        public IntrinsicTagAppender<T> addTag(TagKey<T> tag) {
            super.addTag(tag);
            return this;
        }

        public final IntrinsicTagAppender<T> add(T value) {
            add(keyExtractor.apply(value));
            return this;
        }

        @SafeVarargs
        public final IntrinsicTagAppender<T> add(T... values) {
            Stream.of(values).map(keyExtractor).forEach(this::add);
            return this;
        }

        public final ResourceKey<T> getKey(T value) {
            return keyExtractor.apply(value);
        }

        public IntrinsicTagAppender<T> remove(final T entry) {
            return remove(getKey(entry));
        }

        public IntrinsicTagAppender<T> remove(final T first, final T...entries) {
            remove(first);

            for(var entry : entries) {
                remove(entry);
            }

            return this;
        }

        public IntrinsicTagAppender<T> addTags(TagKey<T>... values) {
            super.addTags(values);
            return this;
        }

        public IntrinsicTagAppender<T> replace() {
            super.replace();
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> replace(boolean value) {
            super.replace(value);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(final ResourceLocation location) {
            super.remove(location);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(final ResourceLocation first, final ResourceLocation... locations) {
            super.remove(first, locations);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(final ResourceKey<T> resourceKey) {
            super.remove(resourceKey);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(final ResourceKey<T> firstResourceKey, final ResourceKey<T>... resourceKeys) {
            super.remove(firstResourceKey, resourceKeys);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(TagKey<T> tag) {
            super.remove(tag);
            return this;
        }

        @Override
        public IntrinsicTagAppender<T> remove(TagKey<T> first, TagKey<T>...tags) {
            super.remove(first, tags);
            return this;
        }
    }
}
