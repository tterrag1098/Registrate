# Registrate [![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.tterrag.com%2Fjob%2FRegistrate%2Fjob%2F1.21%2F)](https://ci.tterrag.com/job/Registrate/job/1.21) [![License](https://img.shields.io/github/license/tterrag1098/Registrate?cacheSeconds=36000)](https://www.tldrlegal.com/l/mpl-2.0) [![Maven Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.tterrag.com%2Fcom%2Ftterrag%2Fregistrate%2FRegistrate%2Fmaven-metadata.xml)](https://maven.tterrag.com/com/tterrag/registrate/Registrate) ![Minecraft Version](https://img.shields.io/badge/minecraft-1.21.8-blue) [![Discord](https://img.shields.io/discord/175740881389879296?label=discord&logo=discord&color=7289da)](https://discord.gg/gZqYcEj)

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

To get an overview of the different APIs and methods, check out the [Javadocs](https://ci.tterrag.com/job/Registrate/job/1.21/javadoc/). For more advanced usage, read the [wiki](https://github.com/tterrag1098/Registrate/wiki) (WIP).

## Project Setup

Registrate can be installed in the mods folder as a typical dependency, but since it does not have a mod, it can also be pre-packaged into your mod. You can do this by making use of Forges Jar-in-Jar system.

[See here for more info on Forges Jar-in-Jar system](https://forge.gemwire.uk/wiki/Jar-in-jar).

To get started you **MUST** enable the Jar-in-Jar system, you can do this by adding the following code anywhere in your build script:

```gradle
jarJar.enable()
```

Then, make sure the jarJar artifact is reobfuscated.

```groovy
reobf {
    jarJar { }
}

tasks.jarJar.finalizedBy('reobfJarJar')
```

Finally, the dependency itself must be added. First add my maven repository,

```groovy
repositories {
    maven { // Registrate
        url "https://maven.tterrag.com/"
    }
    mavenLocal()
}
```

and then the Registrate dependency to the implementation and jarJar configurations.

```groovy
dependencies {
    minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}" // This should alread
    
    // MC<minecraft_version>-<registrate_version>
    implementation fg.deobf("com.tterrag.registrate:Registrate:MC1.19.3-1.1.6")
    // [MC<minecraft_version>,MC<next_minecraft_version>)
    jarJar(group: 'com.tterrag.registrate', name: 'Registrate', version: "[MC1.19.3,MC1.20)")
}
```
<details>

<summary>Additional JarJar Note</summary>

By default the jar containing your mod & registrate will have a `-all` suffix and the normal jar file will not contain registrate.
You would want to share around this `-all` jar, as that contains registrate and any other libs you have pre-packaged.

You can change this though with the following code, this changes the `-all` jar to no longer have a suffix, and the default main jar to be given a `-slim` suffix.
Essentially swapping the 2 jars [_you now would want to share the jar with no suffix appended_].

```groovy
tasks.jarJar.configure {
    // remove '-all' from jarJar jar file
	classifier ''
}

jar {
    // this now conflicts with jarJar as filenames are the same
    // append a `-slim` to this jar, as this jar contains no pre-packaged libs
    classifier 'slim'
}
```

</details>
