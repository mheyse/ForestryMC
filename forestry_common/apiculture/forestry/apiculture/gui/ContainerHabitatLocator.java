/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.apiculture.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.items.ItemBiomefinder;
import forestry.apiculture.items.ItemBiomefinder.BiomefinderInventory;
import forestry.core.config.ForestryItem;
import forestry.core.gui.ContainerItemInventory;
import forestry.core.gui.slots.SlotCustom;
import forestry.core.proxy.Proxies;

public class ContainerHabitatLocator extends ContainerItemInventory {

	public BiomefinderInventory inventory;

	public ContainerHabitatLocator(InventoryPlayer inventoryplayer, BiomefinderInventory inventory) {
		super(inventory, inventoryplayer.player);

		this.inventory = inventory;

		// Energy
		this.addSlot(new SlotCustom(inventory, 2, 152, 8, new Object[] { ForestryItem.honeydew, ForestryItem.honeyDrop }));

		// Bee to analyze
		this.addSlot(new SlotCustom(inventory, 0, 152, 32, new Object[] { ItemBeeGE.class }));
		// Analyzed bee
		this.addSlot(new SlotCustom(inventory, 1, 152, 75, new Object[] { ItemBeeGE.class }));

		// Player inventory
		for (int i1 = 0; i1 < 3; i1++)
			for (int l1 = 0; l1 < 9; l1++)
				addSecuredSlot(inventoryplayer, l1 + i1 * 9 + 9, 8 + l1 * 18, 102 + i1 * 18);
		// Player hotbar
		for (int j1 = 0; j1 < 9; j1++)
			addSecuredSlot(inventoryplayer, j1, 8 + j1 * 18, 160);
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer) {

		if (!Proxies.common.isSimulating(entityplayer.worldObj))
			return;

		((ItemBiomefinder) ForestryItem.biomeFinder.item()).startBiomeSearch(entityplayer.worldObj, entityplayer, inventory.biomesToSearch);

		for (int i = 0; i < inventory.getSizeInventory() - 1; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null)
				continue;

			Proxies.common.dropItemPlayer(entityplayer, stack);
			inventory.setInventorySlotContents(i, null);
		}

		inventory.onGuiSaved(entityplayer);

	}

	@Override
	protected boolean isAcceptedItem(EntityPlayer player, ItemStack stack) {
		return false;
	}

}
