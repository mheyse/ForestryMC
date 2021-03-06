/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.farming.logic;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.core.config.Defaults;
import forestry.core.render.SpriteSheet;
import forestry.core.utils.Vect;

public abstract class FarmLogic implements IFarmLogic {

	World world;
	IFarmHousing housing;

	boolean isManual;

	public FarmLogic(IFarmHousing housing) {
		this.housing = housing;
	}

	public FarmLogic setManual(boolean flag) {
		isManual = flag;
		return this;
	}

	@Override
	public ResourceLocation getSpriteSheet() {
		return SpriteSheet.ITEMS.getLocation();
	}

	protected final boolean isAirBlock(Vect position) {
		return world.isAirBlock(position.x, position.y, position.z);
	}

	protected final boolean isWaterBlock(Vect position) {
		return world.getBlock(position.x, position.y, position.z) == Blocks.water;
	}

	protected final boolean isWoodBlock(Vect position) {
		Block block = getBlock(position);
		return block.isWood(world, position.x, position.y, position.z);
	}

	protected final Block getBlock(Vect position) {
		return world.getBlock(position.x, position.y, position.z);
	}

	protected final int getBlockMeta(Vect position) {
		return world.getBlockMetadata(position.x, position.y, position.z);
	}

	protected final ItemStack getAsItemStack(Vect position) {
		return new ItemStack(getBlock(position), 1, getBlockMeta(position));
	}

	protected final Vect translateWithOffset(int x, int y, int z, ForgeDirection direction, int step) {
		return new Vect(x + direction.offsetX * step, y + direction.offsetY * step, z + direction.offsetZ * step);
	}

	protected final void setBlock(Vect position, Block block, int meta) {
		world.setBlock(position.x, position.y, position.z, block, meta, Defaults.FLAG_BLOCK_SYNCH);
	}

}
