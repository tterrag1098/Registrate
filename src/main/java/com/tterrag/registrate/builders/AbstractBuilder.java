package com.tterrag.registrate.builders;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

@RequiredArgsConstructor
public abstract class AbstractBuilder<R extends IForgeRegistryEntry<R>, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {
    
    @Getter(onMethod = @__({ @Override }))
    private final Registrate owner;
    @Getter(onMethod = @__({ @Override }))
    private final P parent;
    @Getter(onMethod = @__({ @Override }))
    private final String name;
    @Getter(AccessLevel.PROTECTED)
    private final BuilderCallback callback;
    private final Class<? super R> registryType;

    protected abstract T createEntry();
    
    @Override
    public RegistryObject<T> register() {
        return callback.accept(name, registryType, this::createEntry);
    }
    
    public Supplier<T> get() {
        return get(registryType);
    }
    
    public <D extends RegistrateProvider> S addData(ProviderType<D> type, Consumer<DataGenContext<D, R, T>> cons) {
        return addData(type, registryType, cons);
    }
    
    public S lang(Function<T, String> langKeyProvider) {
        return lang(langKeyProvider, RegistrateLangProvider::getAutomaticName);
    }
    
    public S lang(Function<T, String> langKeyProvider, String name) {
        return lang(langKeyProvider, (p, s) -> name);
    }
    
    private S lang(Function<T, String> langKeyProvider, BiFunction<RegistrateLangProvider, Supplier<T>, String> localizedNameProvider) {
        return addData(ProviderType.LANG, ctx -> ctx.getProvider().add(langKeyProvider.apply(ctx.getEntry()), localizedNameProvider.apply(ctx.getProvider(), ctx::getEntry)));
    }
}
