package megaminds.actioninventory.serialization;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eu.pb4.sgui.api.elements.GuiElementInterface.ClickCallback;
import megaminds.actioninventory.actions.BasicAction;
import megaminds.actioninventory.gui.ActionInventoryBuilder;
import megaminds.actioninventory.misc.ItemStackish;
import megaminds.actioninventory.openers.BasicOpener;
import megaminds.actioninventory.serialization.wrappers.InstancedAdapterWrapper;
import megaminds.actioninventory.serialization.wrappers.ValidatedAdapterWrapper;
import megaminds.actioninventory.serialization.wrappers.WrapperAdapterFactory;
import megaminds.actioninventory.util.ValidationException;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleType;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Serializer {
	public static final Gson GSON;

	private Serializer() {}

	public static ActionInventoryBuilder builderFromJson(Reader json) throws ValidationException {
		return GSON.fromJson(json, ActionInventoryBuilder.class);
	}

	public static ActionInventoryBuilder builderFromJson(String json) throws ValidationException {
		return GSON.fromJson(json, ActionInventoryBuilder.class);
	}

	public static BasicOpener openerFromJson(Reader json) throws ValidationException {
		return GSON.fromJson(json, BasicOpener.class);
	}

	public static BasicOpener openerFromJson(String json) throws ValidationException {
		return GSON.fromJson(json, BasicOpener.class);
	}

	static {
		GSON = new GsonBuilder()
				.disableHtmlEscaping()
				.setPrettyPrinting()
				.enableComplexMapKeySerialization()
				.setExclusionStrategies(new ExcludeStrategy())

				.registerTypeHierarchyAdapter(NbtElement.class, new NbtElementAdapter().nullSafe())
				.registerTypeHierarchyAdapter(Text.class, basic(Text.Serializer::fromJson, Text.Serializer::toJsonTree))

				.registerTypeAdapter(ClickCallback.class, delegate(BasicAction.class, ClickCallback.class::cast, BasicAction.class::cast))
				.registerTypeAdapter(ItemStack.class, delegate(ItemStackish.class, ItemStackish::toStack, ItemStackish::new))
				.registerTypeAdapter(Identifier.class, delegate(String.class, s->new Identifier(s.toLowerCase()), Identifier::toString))
				.registerTypeAdapter(TriState.class, new TriStateAdapter())

				.registerTypeAdapter(Item.class, registryDelegate(Registries.ITEM))
				.registerTypeAdapter(Enchantment.class, registryDelegate(Registries.ENCHANTMENT))
				.registerTypeAdapter(EntityAttribute.class, registryDelegate(Registries.ATTRIBUTE))
				.registerTypeAdapter(Block.class, registryDelegate(Registries.BLOCK))
				.registerTypeAdapter(BlockEntity.class, registryDelegate(Registries.BLOCK_ENTITY_TYPE))
				.registerTypeAdapter(EntityType.class, registryDelegate(Registries.ENTITY_TYPE))
				.registerTypeAdapter(SoundEvent.class, registryDelegate(Registries.SOUND_EVENT))
				.registerTypeAdapter(ScreenHandlerType.class, registryDelegate(Registries.SCREEN_HANDLER))
				.registerTypeAdapter(StatusEffect.class, registryDelegate(Registries.STATUS_EFFECT))
				.registerTypeAdapter(ParticleType.class, registryDelegate(Registries.PARTICLE_TYPE))
				.registerTypeAdapter(EntityPredicate.class, basic(j -> EntityPredicate.fromJson(j).orElse(null), EntityPredicate::toJson))
				.registerTypeAdapterFactory(new WrapperAdapterFactory(new InstancedAdapterWrapper(), new ValidatedAdapterWrapper()))
				.registerTypeAdapterFactory(new PolyAdapterFactory())
				.registerTypeAdapterFactory(new OptionalAdapterFactory())

				.create();
	}

	private static <T> Both<T> basic(Function<JsonElement, T> from, Function<T, JsonElement> to) {
		return new Both<T>() {
			@Override public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {return from.apply(json);}
			@Override public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {return to.apply(src);}
		};
	}

	private static <T, D> Both<T> delegate(Class<D> delegate, Function<D, T> from, Function<T, D> to) {
		return new Both<T>(){
			@Override public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				D d = context.deserialize(json, delegate);
				return d==null ? null : from.apply(d);
			}
			@Override public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
				return src==null ? JsonNull.INSTANCE : context.serialize(to.apply(src), delegate);
			}
		};
	}

	private static <T> Both<T> registryDelegate(Registry<T> registry) {
		return delegate(Identifier.class, registry::get, registry::getId);
	}

	private static interface Both<T> extends JsonDeserializer<T>, JsonSerializer<T> {}
}