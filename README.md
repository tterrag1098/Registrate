# Registrate [![Build Status](https://img.shields.io/jenkins/build/https/ci.tterrag.com/Registrate)](https://ci.tterrag.com/job/Registrate)

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
public static final Registrate REGISTRATE = new Registrate(MOD_ID);
```

Using a constant field is not necessary, it can be passed around and thrown away after registration is setup.

Next, begin adding objects.

```java
public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.block(MyBlock::new).register();
```

This simple declaration will create a block, with a default simple blockstate, model, loot table, and lang entry. However all of these can be configured easily to use whatever custom data you may want.

```java
public static final RegistryObject<MyStairsBlock> MY_STAIRS = REGISTRATE.block(MyStairsBlock::new)
        .tag(BlockTags.STAIRS)
        .blockstate(ctx -> ctx.getProvider()
                .stairsBlock(ctx.getEntry(), ctx.getProvider().modLoc(ctx.getName())))
        .lang("Special Stairs")
        .register();
```

To get an overview of the different APIs and methods, check out the [Javadocs](https://ci.tterrag.com/job/Registrate/javadoc/). For more advanced usage, read the [wiki](https://github.com/tterrag1098/Registrate/wiki) (WIP).
