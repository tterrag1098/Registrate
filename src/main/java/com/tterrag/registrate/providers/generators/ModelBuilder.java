package com.tterrag.registrate.providers.generators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.gson.JsonObject;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unchecked")
public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {
	protected final BiConsumer<Identifier, ModelInstance> output;
	protected Identifier parent;
	protected final Map<String, String> textures = new LinkedHashMap<>();
	private boolean registered;

	public ModelBuilder(Identifier location, BiConsumer<Identifier, ModelInstance> output) {
		super(location);
		this.output = output;
		flush();
	}

	public T parent(ModelFile parent) {
		this.parent = parent.getLocation();
		return flush();
	}

	public T parent(Identifier parent) {
		this.parent = parent;
		return flush();
	}

	public T parent(String parent) {
		return parent(parent.contains(":") ? Identifier.parse(parent) : Identifier.withDefaultNamespace(parent));
	}

	public T texture(String key, Identifier texture) {
		textures.put(key, texture.toString());
		return flush();
	}

	public T texture(String key, String texture) {
		textures.put(key, texture);
		return flush();
	}

	public ElementBuilder<T> element() {
		return new ElementBuilder<>((T) this);
	}

	protected T flush() {
		if (output != null && !registered) {
			registered = true;
			output.accept(getLocation(), this::toJson);
		}
		return (T) this;
	}

	protected JsonObject toJson() {
		JsonObject root = new JsonObject();
		if (parent != null) {
			root.addProperty("parent", parent.toString());
		}
		if (!textures.isEmpty()) {
			JsonObject textureObj = new JsonObject();
			textures.forEach(textureObj::addProperty);
			root.add("textures", textureObj);
		}
		return root;
	}

	public static class ElementBuilder<P extends ModelBuilder<P>> {
		private final P parent;

		ElementBuilder(P parent) {
			this.parent = parent;
		}

		public ElementBuilder<P> from(float x, float y, float z) {
			return this;
		}

		public ElementBuilder<P> to(float x, float y, float z) {
			return this;
		}

		public FaceBuilder<ElementBuilder<P>> face(net.minecraft.core.Direction direction) {
			return new FaceBuilder<>(this);
		}

		public ElementBuilder<P> faces(BiConsumer<net.minecraft.core.Direction, FaceBuilder<ElementBuilder<P>>> action) {
			for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
				action.accept(direction, new FaceBuilder<>(this));
			}
			return this;
		}

		public P end() {
			return parent.flush();
		}
	}

	public static class FaceBuilder<P> {
		private final P parent;

		FaceBuilder(P parent) {
			this.parent = parent;
		}

		public FaceBuilder<P> uvs(float u1, float v1, float u2, float v2) {
			return this;
		}

		public FaceBuilder<P> texture(String texture) {
			return this;
		}

		public P end() {
			return parent;
		}
	}
}
