package megaminds.testmod.actions;

import megaminds.testmod.inventory.ActionInventory;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * This opens an {@link ActionInventory} screen
 */
public class OpenActionInventoryAction implements Action {
	private final String name;
	
	/**
	 * @param nameOfInventory
	 * The name of the {@link ActionInventory}
	 */
	public OpenActionInventoryAction(String nameOfInventory) {
		this.name = nameOfInventory;
	}

	@Override
	public void execute(ServerPlayerEntity player) {
		player.openHandledScreen(ActionInventory.getInventory(name));
	}
}