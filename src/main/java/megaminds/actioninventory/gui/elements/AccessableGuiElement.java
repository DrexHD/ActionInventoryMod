package megaminds.actioninventory.gui.elements;

import org.jetbrains.annotations.NotNull;

import lombok.NoArgsConstructor;
import lombok.Setter;
import megaminds.actioninventory.actions.BasicAction;
import megaminds.actioninventory.util.annotations.Poly;
import net.minecraft.item.ItemStack;

/**
 * Adapted from {@link eu.pb4.sgui.api.elements.GuiElement}
 */
@Setter
@NoArgsConstructor
@Poly("Normal")
public final class AccessableGuiElement extends AccessableElement {
	private ItemStack item;

	public AccessableGuiElement(int index, BasicAction action, ItemStack item) {
		super(index, action);
		this.item = item;
	}

	@NotNull
	@Override
	public ItemStack getItemStack() {
		return this.item.copy();
	}

	@Override
	public void validate() {
		super.validate();
		if (item==null) item = ItemStack.EMPTY;
	}
}