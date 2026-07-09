package com.tterrag.registrate.providers.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {
	private final List<OverrideBuilder> overrides = new ArrayList<>();

	public ItemModelBuilder(Identifier location, BiConsumer<Identifier, ModelInstance> output) {
		super(location, output);
	}

	public OverrideBuilder override() {
		OverrideBuilder builder = new OverrideBuilder(this);
		overrides.add(builder);
		return builder;
	}

	@Override
	protected JsonObject toJson() {
		JsonObject root = super.toJson();
		if (!overrides.isEmpty()) {
			JsonArray overridesJson = new JsonArray();
			for (OverrideBuilder override : overrides) {
				overridesJson.add(override.toJson());
			}
			root.add("overrides", overridesJson);
		}
		return root;
	}

	public static class OverrideBuilder {
		private final ItemModelBuilder parent;
		private final JsonObject predicates = new JsonObject();
		private Identifier model;

		OverrideBuilder(ItemModelBuilder parent) {
			this.parent = parent;
		}

		public OverrideBuilder predicate(Identifier id, float value) {
			predicates.addProperty(id.toString(), value);
			return this;
		}

		public OverrideBuilder model(ModelFile model) {
			this.model = model.getLocation();
			return this;
		}

		public ItemModelBuilder end() {
			return parent.flush();
		}

		JsonObject toJson() {
			JsonObject root = new JsonObject();
			root.add("predicate", predicates);
			if (model != null) {
				root.addProperty("model", model.toString());
			}
			return root;
		}
	}
}
