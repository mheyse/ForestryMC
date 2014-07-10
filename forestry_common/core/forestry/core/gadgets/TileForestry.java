/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.core.gadgets;

import java.util.LinkedList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import buildcraft.api.gates.ITrigger;

import forestry.core.EnumErrorCode;
import forestry.core.config.Config;
import forestry.core.config.Defaults;
import forestry.core.interfaces.IErrorSource;
import forestry.core.interfaces.ILiquidTankContainer;
import forestry.core.interfaces.IOwnable;
import forestry.core.interfaces.IPowerHandler;
import forestry.core.network.ForestryPacket;
import forestry.core.network.INetworkedEntity;
import forestry.core.network.PacketPayload;
import forestry.core.network.PacketTileUpdate;
import forestry.core.proxy.Proxies;
import forestry.core.utils.EnumAccess;
import forestry.core.utils.ForestryTank;
import forestry.core.utils.InventoryAdapter;
import forestry.core.utils.Vect;

public abstract class TileForestry extends TileEntity implements INetworkedEntity, IOwnable, IErrorSource {

	protected boolean isInited = false;
	protected int energyConsumed;
	protected int energyLast;
	protected int energyReceived;

	public Vect Coords() {
		return new Vect(xCoord, yCoord, zCoord);
	}

	public World getWorld() {
		return this.getWorldObj();
	}

	public void openGui(EntityPlayer player) {
	}

	public void rotateAfterPlacement(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack stack) {

		int l = MathHelper.floor_double(((entityliving.rotationYaw * 4F) / 360F) + 0.5D) & 3;
		if (l == 0)
			setOrientation(ForgeDirection.NORTH);
		if (l == 1)
			setOrientation(ForgeDirection.EAST);
		if (l == 2)
			setOrientation(ForgeDirection.SOUTH);
		if (l == 3)
			setOrientation(ForgeDirection.WEST);

	}

	// / UPDATING
	@Override
	public void updateEntity() {
		if (!isInited) {
			initialize();
			isInited = true;
		}

		if (!Proxies.common.isSimulating(worldObj))
			return;

		if (this instanceof IPowerHandler) {
			IPowerHandler receptor = (IPowerHandler) this;
			if (receptor.getPowerHandler() != null) {
				receptor.getPowerHandler().update();
			}
		}
	}

	public abstract void initialize();

	// / SAVING & LOADING
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("Access"))
			access = EnumAccess.values()[nbttagcompound.getInteger("Access")];
		else
			access = EnumAccess.SHARED;
		if (nbttagcompound.hasKey("Owner"))
			owner = nbttagcompound.getString("Owner");

		if (nbttagcompound.hasKey("Orientation"))
			orientation = ForgeDirection.values()[nbttagcompound.getInteger("Orientation")];
		else
			orientation = ForgeDirection.WEST;

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Access", access.ordinal());
		if (owner != null)
			nbttagcompound.setString("Owner", owner);
		if (orientation != null)
			nbttagcompound.setInteger("Orientation", orientation.ordinal());
	}

	// / SMP
	@Override
	public void sendNetworkUpdate() {
		PacketTileUpdate packet = new PacketTileUpdate(this);
		Proxies.net.sendNetworkPacket(packet, xCoord, yCoord, zCoord);
	}

	@Override
	public Packet getDescriptionPacket() {
		PacketTileUpdate packet = new PacketTileUpdate(this);
		return packet.getPacket();
	}

	public abstract PacketPayload getPacketPayload();

	public abstract void fromPacketPayload(PacketPayload payload);

	@Override
	public void fromPacket(ForestryPacket packetRaw) {
		PacketTileUpdate packet = (PacketTileUpdate) packetRaw;
		if (orientation != packet.getOrientation()) {
			orientation = packet.getOrientation();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		errorState = packet.getErrorState();
		owner = packet.getOwner();
		access = packet.getAccess();
		fromPacketPayload(packet.payload);
	}

	public LinkedList<ITrigger> getCustomTriggers() {
		return null;
	}

	public void onRemoval() {
	}

	// / REDSTONE INFO
	/**
	 * @return true if tile is activated by redstone current.
	 */
	public boolean isActivated() {
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}
	// / ORIENTATION
	private ForgeDirection orientation = ForgeDirection.WEST;

	public ForgeDirection getOrientation() {
		return this.orientation;
	}

	public void setOrientation(ForgeDirection orientation) {
		if (this.orientation == orientation)
			return;
		this.orientation = orientation;
		this.sendNetworkUpdate();
	}
	// / ERROR HANDLING
	public EnumErrorCode errorState = EnumErrorCode.OK;

	public void setErrorState(EnumErrorCode state) {
		if (this.errorState == state)
			return;
		this.errorState = state;
		this.sendNetworkUpdate();
	}

	@Override
	public boolean throwsErrors() {
		return true;
	}

	@Override
	public EnumErrorCode getErrorState() {
		return errorState;
	}
	// / OWNERSHIP
	public String owner = null;
	private EnumAccess access = EnumAccess.SHARED;

	@Override
	public boolean allowsRemoval(EntityPlayer player) {
		if (!isOwnable())
			return true;
		if (!isOwned())
			return true;
		if (isOwner(player))
			return true;
		if (Proxies.common.isOp(player))
			return true;

		return getAccess() == EnumAccess.SHARED;
	}

	@Override
	public boolean allowsInteraction(EntityPlayer player) {
		if (Config.disablePermissions)
			return true;
		if (!isOwnable())
			return true;
		if (!isOwned())
			return true;
		if (isOwner(player))
			return true;
		if (Proxies.common.isOp(player))
			return true;

		return getAccess() == EnumAccess.SHARED || getAccess() == EnumAccess.VIEWABLE;
	}

	@Override
	public EnumAccess getAccess() {
		return access;
	}

	@Override
	public boolean isOwnable() {
		return false;
	}

	@Override
	public boolean isOwned() {
		return owner != null && !owner.isEmpty();
	}

	@Override
	public String getOwnerName() {
		return owner;
	}

	public EntityPlayer getOwnerEntity() {
		if (owner != null)
			return worldObj.getPlayerEntityByName(owner);
		else
			return null;
	}

	@Override
	public void setOwner(EntityPlayer player) {
		this.owner = player.getGameProfile().getId().toString().replace("-", "");
	}

	@Override
	public boolean isOwner(EntityPlayer player) {
		if (owner != null)
			return owner.equals(player.getGameProfile().getId().toString().replace("-", ""));
		else
			return false;
	}

	@Override
	public boolean switchAccessRule(EntityPlayer player) {
		if (owner != null && !owner.isEmpty() && !owner.equals(player.getGameProfile().getId().toString().replace("-", "")))
			return false;

		if (access.ordinal() < EnumAccess.values().length - 1)
			access = EnumAccess.values()[access.ordinal() + 1];
		else
			access = EnumAccess.values()[0];

		return true;
	}

	/* NAME */
	public abstract String getInventoryName();

	public boolean hasCustomInventoryName() {
		return true;
	}

	/* ACCESS */
	public abstract boolean isUseableByPlayer(EntityPlayer player);

	/* INVENTORY BASICS */
	public InventoryAdapter getInternalInventory() {
		return null;
	}

	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) {
		return true;
	}

	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return canPutStackFromSide(i, itemstack, j);
	}

	protected boolean canTakeStackFromSide(int slotIndex, ItemStack itemstack, int side) {
		if (getAccess() == EnumAccess.PRIVATE)
			return false;

		return true;
	}

	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return canTakeStackFromSide(i, itemstack, j);
	}

	protected boolean canPutStackFromSide(int slotIndex, ItemStack itemstack, int side) {
		if (getAccess() == EnumAccess.PRIVATE)
			return false;

		return true;
	}

	public int[] getAccessibleSlotsFromSide(int side) {
		if (getInternalInventory() == null)
			return Defaults.FACINGS_NONE;
		else
			return getInternalInventory().getSizeInventorySide(side);

	}

	/* IFLUIDHANDLER BASICS */
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if (this instanceof ILiquidTankContainer) {
			ForestryTank[] tanks = ((ILiquidTankContainer) this).getTanks();
			FluidTankInfo[] info = new FluidTankInfo[tanks.length];
			for (int i = 0; i < info.length; i++) {
				info[i] = tanks[i].getInfo();
			}
			return info;
		}
		return ForestryTank.DUMMY_TANKINFO_ARRAY;
	}

	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return resource != null ? drain(from, resource.amount, doDrain) : null;
	}

	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}
}
