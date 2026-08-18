# Registrate [![GitHub branch status](https://img.shields.io/github/check-runs/tterrag1098/Registrate/26.2%2Fdev?label=build)](https://github.com/tterrag1098/Registrate/actions) [![License](https://img.shields.io/github/license/tterrag1098/Registrate?cacheSeconds=36000)](https://www.tldrlegal.com/l/mpl-2.0) [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.gegy.dev%2Freleases%2Fcom%2Ftterrag%2Fregistrate%2FRegistrate%2Fmaven-metadata.xml&filter=MC26.2*)](https://maven.tterrag.com/com/tterrag/registrate/Registrate) ![Minecraft Version](https://img.shields.io/badge/minecraft-26.2-blue) [![Discord](https://img.shields.io/discord/175740881389879296?label=discord&logo=discord&color=7289da)](https://discord.gg/gZqYcEj)

A powerful wrapper for creating and registering objects in your mod.

## Why Registrate?

- Allows you to organize your mod content however you like, rather than having pieces of each object defined in scattered places
- Simple fluent API
- Open to extension, build and register custom objects and data
- Automatic data generation with sane defaults
- Shadeable, contains no mod, only code

## How to Use

First, create a `Registrate` object which will be used across your entire project.

```java
public static final Registrate REGISTRATE = Registrate.create(MOD_ID);
```

Using a constant field is not necessary, it can be passed around and thrown away after registration is setup.

If declared static in your `@Mod` class, you must create the `Registrate` object lazily so it is not created too early during loading. This can be done easily like so:

```java
public static final NonNullSupplier<Registrate> REGISTRATE = NonNullSupplier.lazy(() -> Registrate.create(MOD_ID));
```

Next, begin adding objects.

If you have a block class such as

```java
public class MyBlock extends Block {

    public MyBlock(Block.Properties properties) {
        super(properties);
    }
    
    ...
}
```

then register it like so,

```java
public static final RegistryEntry<MyBlock> MY_BLOCK = REGISTRATE.block("my_block", MyBlock::new).register();
```

Registrate will create a block, with a default simple blockstate, model, loot table, and lang entry. However, all of these facets can be configured easily to use whatever custom data you may want. Example:

```java
public static final RegistryEntry<MyStairsBlock> MY_STAIRS = REGISTRATE.block("my_block", MyStairsBlock::new)
            .defaultItem()
            .tag(BlockTags.STAIRS)
            .blockstate(ctx -> ctx.getProvider()
                .stairsBlock(ctx.getEntry(), ctx.getProvider().modLoc(ctx.getName())))
            .lang("Special Stairs")
            .register();
```

This customized version will create a BlockItem (with its own default model and lang entry), add the block to a tag, configure the blockstate for stair properties, and add a custom localization.

To get an overview of the different APIs and methods, check out the [Javadocs](https://ci.tterrag.com/job/Registrate/job/26.2/javadoc/). For more advanced usage, read the [wiki](https://github.com/tterrag1098/Registrate/wiki) (WIP).

## Project Setup

Registrate is a game library rather than a standalone mod. Minecraft 26.2 requires Java 25.
Most mods should compile against Registrate and embed it with NeoForge's Jar-in-Jar system.

[NeoForge Jar-in-Jar documentation](https://docs.neoforged.net/toolchain/docs/dependencies/jarinjar/)

Add Gegy's Maven repository:

```groovy
repositories {
    maven {
        name = 'Gegy'
        url = uri('https://maven.gegy.dev/releases')
    }
}
```

Then compile against and embed a negotiated Registrate version:

```groovy
dependencies {
    jarJar(implementation(group: 'com.tterrag.registrate', name: 'Registrate')) {
        version {
            strictly '[MC26.2,MC26.3)'
            prefer 'MC26.2-1.6.0'
        }
    }
}
```
