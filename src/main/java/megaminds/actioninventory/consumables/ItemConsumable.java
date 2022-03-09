package megaminds.actioninventory.consumables;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import megaminds.actioninventory.misc.ItemStackish;
import megaminds.actioninventory.misc.Enums.TagOption;
import megaminds.actioninventory.util.annotations.PolyName;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@PolyName("Item")
public final class ItemConsumable extends NumberConsumable {
	private ItemStackish stack;
	private Set<Identifier> tags;
	private TagOption tagOption;

	public ItemConsumable(boolean requireFull, long amount, ItemStackish stack, Set<Identifier> tags, TagOption tagOption) {
		super(requireFull, amount);
		this.stack = stack!=null ? stack : ItemStackish.MATCH_ALL;
		this.tags = tags;
		this.tagOption = tagOption!=null ? tagOption : TagOption.ANY;
	}

	@Override
	public boolean canConsume(MinecraftServer server, UUID player, long left) {
		var p = server.getPlayerManager().getPlayer(player);
		Objects.requireNonNull(p, ()->"No Player Exists for UUID: "+player);

		var inv = p.getInventory();
		var available = 0;
		for (int i = 0, size = inv.size(); i < size; i++) {
			var s = inv.getStack(i);
			if (stack.specifiedEquals(s) && (tags==null || tagOption.matches(tags, s.streamTags()))) {
				available += s.getCount();
				if (available >= left) return true;
			}
		}
		return false;
	}

	@Override
	public long consume(MinecraftServer server, UUID player, long left) {
		var p = server.getPlayerManager().getPlayer(player);
		Objects.requireNonNull(p, ()->"No Player Exists for UUID: "+player);

		var inv = p.getInventory();
		for (int i = 0, size = inv.size(); i < size; i++) {
			var s = inv.getStack(i);
			if (stack.specifiedEquals(s) && (tags==null || tagOption.matches(tags, s.streamTags()))) {
				var count = Math.min(s.getCount(), left);
				left -= count;
				s.setCount(s.getCount()-(int)count);
				if (left<=0) break;
			}
		}
		return left;
	}

	@Override
	public void validate() {
		if (stack==null) stack = ItemStackish.MATCH_ALL;
		if (tagOption==null) tagOption = TagOption.ANY;
	}

	@Override
	public String getStorageName() {
		return "Item";
	}

	@Override
	public BasicConsumable copy() {
		return new ItemConsumable(stack, Set.copyOf(tags), tagOption);
	}
}