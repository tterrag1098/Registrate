package com.tterrag.registrate.providers.generators;

import net.minecraft.resources.Identifier;

public class ModelFile {
	private final Identifier location;

	public ModelFile(Identifier location) {
		this.location = location;
	}

	public Identifier getLocation() {
		return location;
	}

	public static class ExistingModelFile extends ModelFile {
		public ExistingModelFile(Identifier location) {
			super(location);
		}
	}

	public static class UncheckedModelFile extends ModelFile {
		public UncheckedModelFile(String location) {
			super(location.contains(":") ? Identifier.parse(location) : Identifier.withDefaultNamespace(location));
		}

		public UncheckedModelFile(Identifier location) {
			super(location);
		}
	}
}
