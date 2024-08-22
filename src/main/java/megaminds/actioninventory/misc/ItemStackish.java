package megaminds.actioninventory.misc;

import megaminds.actioninventory.serialization.wrappers.Validated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class ItemStackish {
	public static final ItemStackish MATCH_ALL = new ItemStackish() {
		@Override public boolean specifiedEquals(ItemStack s) {return true;}
		@Override public boolean specifiedEquals(ItemStackish s) {return true;}
	};
	public static final ItemStackish MATCH_NONE = new ItemStackish() {
		@Override public boolean specifiedEquals(ItemStack s) {return false;}
		@Override public boolean specifiedEquals(ItemStackish s) {return false;}
	};

	private static final String HIDE_FLAG_KEY = "HideFlags";
	private static final String ATTRIBUTE_KEY = "AttributeModifiers";

	private Item item;
	private Integer count;
	private Integer damage; 
	private Optional<NbtCompound> customNbt;
	private Optional<Text> customName;	//displayMatches
	private List<Text> lore;	//displayMatches
	private Optional<Integer> color;//color in display
	private Map<RegistryEntry<Enchantment>, Integer> enchantments;	//enchantmentsMatch
	private Set<AttributeValues> attributes;	//attributesMatch

	public ItemStackish() {}

	@SuppressWarnings("java:S107")
	public ItemStackish(Item item, Integer count, Integer damage, Optional<NbtCompound> customNbt, Optional<Text> customName, List<Text> lore, Optional<Integer> color, Map<RegistryEntry<Enchantment>, Integer> enchantments, Set<AttributeValues> attributes) {
		this.item = item;
		this.count = count;
		this.damage = damage;
		this.customNbt = customNbt;
		this.customName = customName;
		this.lore = lore;
		this.color = color;
		this.enchantments = enchantments;
		this.attributes = attributes;
	}

	public ItemStackish(ItemStack i) {
		if (ItemStack.EMPTY.equals(i)) return;

		item = i.getItem();
		count = i.getCount();
		damage = i.getDamage();
		setComponentDefaults();
		customName = Optional.ofNullable(i.get(DataComponentTypes.CUSTOM_NAME));
		if (i.contains(DataComponentTypes.LORE)) {
			lore = i.get(DataComponentTypes.LORE).lines();
		}
		color = Optional.ofNullable(i.get(DataComponentTypes.DYED_COLOR)).map(DyedColorComponent::rgb);
		EnchantmentHelper.getEnchantments(i).getEnchantmentEntries().forEach(enchantment -> enchantments.put(enchantment.getKey(), enchantment.getIntValue()));
		if (i.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
			attributes = i.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().stream()
				.map(entry -> new AttributeValues(
					entry.attribute().value(),
					entry.modifier().operation(),
					entry.modifier().value(),
					entry.modifier().id(),
					entry.slot())
				).collect(Collectors.toSet());
		}
	}

	@SuppressWarnings("java:S2789")
	public ItemStack toStack() {
		ItemStack s = new ItemStack(Objects.requireNonNullElse(item, Items.AIR));
		if (customNbt!=null) {
            customNbt.ifPresent(nbtCompound -> s.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCompound)));
        }
		if (count!=null) s.setCount(count);
		if (damage!=null) s.setDamage(damage);
        if (customName != null) customName.ifPresent(text -> s.set(DataComponentTypes.CUSTOM_NAME, text));
		if (lore!=null) s.set(DataComponentTypes.LORE, new LoreComponent(lore));
		if (color != null) color.ifPresent(rgb -> s.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(rgb, true)));
		if (enchantments != null) {
			ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
			enchantments.forEach(builder::add);
			EnchantmentHelper.set(s, builder.build());
		}
		if (attributes!=null) attributes.forEach(a->a.apply(s));
		return s;
	}

	public void setComponentDefaults() {
		customNbt = Optional.empty();
		color = Optional.empty();
		customName = Optional.empty();
		lore = Collections.emptyList();
		attributes = Set.of();
	}

	private void addLore(ItemStack s) {
		s.set(DataComponentTypes.LORE, new LoreComponent(lore));
	}


	/**
	 * Checks contained elements for equality.<br>
	 * Unspecified elements are not checked.<br>
	 * o1.equals(o2) doesn't mean o2.equals(o1)
	 */
	@SuppressWarnings("java:S2789")
	public boolean specifiedEquals(ItemStackish i) {
		if (this==i) return true;
		if (i==null) return false;

		return nullOrEquals(item, i.item)
				&& nullOrEquals(count, i.count)
				&& nullOrEquals(damage, i.damage)
				&& nullOrEquals(color, i.color)
				&& (customName==null || i.customName!=null&&textEqual(customName.orElse(null), i.customName.orElse(null)))
				&& (lore==null || i.lore!=null&&loreEquals(i.lore))
				&& nullOrEquals(enchantments, i.enchantments)
				&& (customNbt==null || i.customNbt!=null&&NbtHelper.matches(customNbt.orElse(null), i.customNbt.orElse(null), true))	//NOSONAR Minecraft uses this other places so I think this is fine.
				&& nullOrEquals(attributes, i.attributes);
	}

	private static <E> boolean nullOrEquals(E e, E other) {
		return e==null || Objects.equals(e, other);
	}

	public boolean specifiedEquals(ItemStack s) {
		return this.specifiedEquals(new ItemStackish(s));
	}

	private static boolean textEqual(Text t1, Text t2) {
		return Objects.equals(t1, t2) || 
				t1!=null && t2!=null && t1.getString().equals(t2.getString());
	}

	private boolean loreEquals(List<Text> lore2) {
		int l = lore.size();
		for (int i=0; i<l; i++) {
			if (!textEqual(lore.get(i), lore2.get(i))) return false;
		}
		return true;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getDamage() {
		return damage;
	}

	public void setDamage(Integer damage) {
		this.damage = damage;
	}

	public Optional<NbtCompound> getCustomNbt() {
		return customNbt;
	}

	public void setCustomNbt(Optional<NbtCompound> customNbt) {
		this.customNbt = customNbt;
	}

	public Optional<Text> getCustomName() {
		return customName;
	}

	public void setCustomName(Optional<Text> customName) {
		this.customName = customName;
	}

	public List<Text> getLore() {
		return lore;
	}

	public void setLore(List<Text> lore) {
		this.lore = lore;
	}

	public Optional<Integer> getColor() {
		return color;
	}

	public void setColor(Optional<Integer> color) {
		this.color = color;
	}

	public Map<RegistryEntry<Enchantment>, Integer> getEnchantments() {
		return enchantments;
	}

	public void setEnchantments(Map<RegistryEntry<Enchantment>, Integer> enchantments) {
		this.enchantments = enchantments;
	}

	public Set<AttributeValues> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<AttributeValues> attributes) {
		this.attributes = attributes;
	}

	public class AttributeValues implements Validated {
		private EntityAttribute attribute;
		private Operation operation;
		private double value;
		private Identifier identifier;
		private AttributeModifierSlot slot;

		public AttributeValues() {}

		public AttributeValues(EntityAttribute attribute, Operation operation, double value, Identifier identifier, AttributeModifierSlot slot) {
			this.attribute = attribute;
			this.operation = operation;
			this.value = value;
			this.identifier = identifier;
			this.slot = slot;
		}

		@Override
		public void validate() {
			Validated.validate(attribute!=null, "Attribute modifiers need attribute to be non-null");
			Validated.validate(identifier!=null, "Attribute modifiers need identifier to be non-null");
			Validated.validate(operation!=null, "Attribute modifiers need operation to be non-null");
		}

		public void apply(ItemStack stack) {
			EntityAttributeModifier mod = new EntityAttributeModifier(identifier, value, operation);
			// TODO 1.20.5
			stack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT, component -> {
				List<AttributeModifiersComponent.Entry> modifiers = new LinkedList<>(component.modifiers());
				modifiers.add(new AttributeModifiersComponent.Entry(Registries.ATTRIBUTE.getEntry(attribute), mod, slot));
				return new AttributeModifiersComponent(modifiers, false);
			});
		}

		public EntityAttribute getAttribute() {
			return attribute;
		}

		public void setAttribute(EntityAttribute attribute) {
			this.attribute = attribute;
		}

		public Operation getOperation() {
			return operation;
		}

		public void setOperation(Operation operation) {
			this.operation = operation;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public AttributeModifierSlot getSlot() {
			return slot;
		}

		public void setSlot(AttributeModifierSlot slot) {
			this.slot = slot;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof AttributeValues other)) return false;
			return Objects.equals(attribute, other.attribute)
					&& Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value)
					&& operation == other.operation
					&& slot == other.slot
					&& Objects.equals(identifier, other.identifier);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ItemStackish.this.hashCode();
			result = prime * result + Objects.hash(attribute, identifier, operation, slot, value);
			return result;
		}
	}
}