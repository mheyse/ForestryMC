/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.core.utils;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.common.registry.EntityRegistry;
import buildcraft.api.tools.IToolWrench;

import com.mojang.authlib.GameProfile;

import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.arboriculture.ITree;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IArmorNaturalist;
import forestry.api.core.ITileStructure;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IPollinatable;
import forestry.core.config.Config;
import forestry.core.gadgets.TileForestry;
import forestry.core.proxy.Proxies;
import forestry.plugins.PluginArboriculture;

public class Utils {

	private static EntityPlayer modPlayer;

	public static EntityPlayer getForestryPlayer(World world, int x, int y, int z) {
		if (modPlayer == null) {
			modPlayer = new EntityPlayer(world, new GameProfile(UUID.nameUUIDFromBytes(Config.fakeUserLogin.getBytes()), Config.fakeUserLogin)) {
				@Override
				public boolean canCommandSenderUseCommand(int var1, String var2) {
					return false;
				}

				@Override
				public ChunkCoordinates getPlayerCoordinates() {
					return null;
				}

				@Override
				public void addChatMessage(IChatComponent var1) {
				}
			};
			modPlayer.posX = x;
			modPlayer.posY = y;
			modPlayer.posZ = z;
			Proxies.log.info("Created player '%s' for Forestry.", modPlayer.getGameProfile().getId().toString().replace("-", ""));
			if (Config.fakeUserAutoop) {
				MinecraftServer.getServer().getConfigurationManager().func_152605_a(modPlayer.getGameProfile());
				Proxies.log.info("Opped player '%s'.", modPlayer.getGameProfile().getId().toString().replace("-", ""));
			}
		}

		return modPlayer;
	}
	private static Random rand;

	public static int getUID() {
		if (rand == null)
			rand = new Random();

		int uid = rand.nextInt();
		return uid;
	}

	public static void dropInventory(TileForestry tile, World world, int x, int y, int z) {
		if (tile == null)
			return;

		// Release inventory
		if (tile instanceof ITileStructure) {

			IInventory inventory = ((ITileStructure) tile).getInventory();
			if (inventory != null)
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					if (inventory.getStackInSlot(i) == null)
						continue;

					StackUtils.dropItemStackAsEntity(inventory.getStackInSlot(i), world, x, y, z);
					inventory.setInventorySlotContents(i, null);
				}

		} else {

			IInventory inventory;
			if (tile instanceof IInventory)
				inventory = (IInventory) tile;
			else
				inventory = tile.getInternalInventory();

			if (inventory != null) {
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {

					ItemStack itemstack = inventory.getStackInSlot(slot);

					if (itemstack == null)
						continue;

					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

					while (itemstack.stackSize > 0) {

						int stackPartial = world.rand.nextInt(21) + 10;
						if (stackPartial > itemstack.stackSize)
							stackPartial = itemstack.stackSize;
						ItemStack drop = itemstack.splitStack(stackPartial);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, drop);
						float accel = 0.05F;
						entityitem.motionX = (float) world.rand.nextGaussian() * accel;
						entityitem.motionY = (float) world.rand.nextGaussian() * accel + 0.2F;
						entityitem.motionZ = (float) world.rand.nextGaussian() * accel;
						world.spawnEntityInWorld(entityitem);

					}

					inventory.setInventorySlotContents(slot, null);
				}
			}
		}

	}

	public static boolean hasNaturalistEye(EntityPlayer player) {
		ItemStack armorItem = player.inventory.armorInventory[3];
		return armorItem != null && armorItem.getItem() instanceof IArmorNaturalist
				&& ((IArmorNaturalist) armorItem.getItem()).canSeePollination(player, armorItem, true);
	}

	@SuppressWarnings("rawtypes")
	public static void broadcastMessage(World world, String message) {
		for (Iterator iterator = world.playerEntities.iterator(); iterator.hasNext();) {
			EntityPlayer entityplayer = (EntityPlayer) iterator.next();
			entityplayer.addChatMessage(new ChatComponentText(message));
		}
	}

	public static IInventory getChest(IInventory inventory) {
		if (!(inventory instanceof TileEntityChest))
			return inventory;

		TileEntityChest chest = (TileEntityChest) inventory;

		Vect[] adjacent = new Vect[] { new Vect(chest.xCoord + 1, chest.yCoord, chest.zCoord), new Vect(chest.xCoord - 1, chest.yCoord, chest.zCoord),
				new Vect(chest.xCoord, chest.yCoord, chest.zCoord + 1), new Vect(chest.xCoord, chest.yCoord, chest.zCoord - 1) };

		for (Vect pos : adjacent) {
			TileEntity otherchest = chest.getWorldObj().getTileEntity(pos.x, pos.y, pos.z);
			if (otherchest instanceof TileEntityChest)
				return new InventoryLargeChest("", chest, (TileEntityChest) otherchest);
		}

		return inventory;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static int[] concat(int[] first, int[] second) {
		int[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static short[] concat(short[] first, short[] second) {
		short[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static float[] concat(float[] first, float[] second) {
		float[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static boolean canWrench(EntityPlayer player, int x, int y, int z) {

		ItemStack itemstack = player.getCurrentEquippedItem();
		if (itemstack == null)
			return false;

		if (!(itemstack.getItem() instanceof IToolWrench))
			return false;

		IToolWrench wrench = (IToolWrench) itemstack.getItem();
		if (!wrench.canWrench(player, x, y, z))
			return false;

		return true;
	}

	public static void useWrench(EntityPlayer player, int x, int y, int z) {
		ItemStack itemstack = player.getCurrentEquippedItem();

		if (itemstack == null)
			return;

		if (!(itemstack.getItem() instanceof IToolWrench))
			return;

		((IToolWrench) itemstack.getItem()).wrenchUsed(player, x, y, z);
	}

	public static EnumTankLevel rateTankLevel(int scaled) {

		if (scaled < 5)
			return EnumTankLevel.EMPTY;
		else if (scaled < 30)
			return EnumTankLevel.LOW;
		else if (scaled < 60)
			return EnumTankLevel.MEDIUM;
		else if (scaled < 90)
			return EnumTankLevel.HIGH;
		else
			return EnumTankLevel.MAXIMUM;
	}

	public static boolean isReplaceableBlock(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);

		return block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block == Blocks.snow_layer
				|| block.isReplaceable(world, x, y, z);
	}

	public static boolean isLiquidBlock(World world, int x, int y, int z) {
		return isLiquidBlock(world.getBlock(x, y, z));
	}

	public static boolean isLiquidBlock(Block block) {
		return block == Blocks.water || block == Blocks.flowing_water || block == Blocks.lava || block == Blocks.flowing_lava;
	}

	public static boolean isUseableByPlayer(EntityPlayer player, TileEntity tile, World world, int x, int y, int z) {
		if (world.getTileEntity(x, y, z) != tile)
			return false;

		return player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) <= 64.0D;

	}

	public static BiomeGenBase getBiomeAt(World world, int x, int z) {
		return world.getBiomeGenForCoordsBody(x, z);
	}

	public static Entity spawnEntity(World world, Class<? extends Entity> entityClass, double x, double y, double z) {
		if (!EntityList.classToStringMapping.containsKey(entityClass))
			return null;

		return spawnEntity(world, EntityList.createEntityByName((String) EntityList.classToStringMapping.get(entityClass), world), x, y, z);
	}

	public static Entity spawnEntity(World world, Entity spawn, double x, double y, double z) {

		if (spawn != null && spawn instanceof EntityLiving) {

			EntityLiving living = (EntityLiving) spawn;
			spawn.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0f), 0.0f);
			living.rotationYawHead = living.rotationYaw;
			living.renderYawOffset = living.rotationYaw;
			living.onSpawnWithEgg((IEntityLivingData) null);
			world.spawnEntityInWorld(spawn);
			living.playLivingSound();
		}

		return spawn;
	}

	public static void registerEntity(Class<? extends Entity> entityClass, String ident, int id, int eggForeground, int eggBackground, int trackingRange, int updateFrequency, boolean sendVelocity) {
		EntityRegistry.registerModEntity(entityClass, ident, id, ForestryAPI.instance, trackingRange, updateFrequency, sendVelocity);
		Proxies.log.finer("Registered entity %s (%s) with id %s.", ident, entityClass.toString(), id);
	}

	public static int addRGBComponents(int colour, int r, int g, int b) {
		r = ((colour & 0xff0000) >> 16) + r;
		g = ((colour & 0xff00) >> 8) + b;
		b = ((colour & 0xff)) + b;

		r = r <= 255 ? r : 255;
		g = g <= 255 ? g : 255;
		b = b <= 255 ? b : 255;

		return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
	}

	public static int multiplyRGBComponents(int colour, float factor) {
		int r = (int) (((colour & 0xff0000) >> 16) * factor);
		int g = (int) (((colour & 0xff00) >> 8) * factor);
		int b = (int) (((colour & 0xff)) * factor);

		r = r <= 255 ? r : 255;
		g = g <= 255 ? g : 255;
		b = b <= 255 ? b : 255;

		return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
	}

	public static String getFingerprint(Certificate certificate) {
		if (certificate == null)
			return "Certificate invalid";

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] der = certificate.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			return tohex(digest);
		} catch (Exception ex) {
			return null;
		}
	}
	private static final String HEX = "0123456789abcdef";

	private static String tohex(byte[] checksum) {

		final StringBuilder hex = new StringBuilder(2 * checksum.length);
		for (byte bty : checksum) {
			hex.append(HEX.charAt((bty & 0xf0) >> 4)).append(HEX.charAt((bty & 0x0f)));
		}
		return hex.toString();
	}

	public static ItemStack convertSaplingToGeneticEquivalent(ItemStack foreign) {

		ItemStack ersatz = null;
		ITree tree = null;

		for (Map.Entry<ItemStack, IIndividual> entry : AlleleManager.ersatzSaplings.entrySet()) {
			if (entry.getKey().getItem() != foreign.getItem())
				continue;
			if (entry.getKey().getItemDamage() != foreign.getItemDamage())
				continue;

			if (entry.getValue() instanceof ITree)
				tree = (ITree) entry.getValue();

			ersatz = PluginArboriculture.treeInterface.getMemberStack(tree, EnumGermlingType.SAPLING.ordinal());
			ersatz.stackSize = foreign.stackSize;
		}

		return ersatz;
	}

	public static IPollinatable getOrCreatePollinatable(String owner, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);

		IPollinatable receiver = null;
		if (tile instanceof IPollinatable)
			receiver = (IPollinatable) tile;
		else if (!world.isAirBlock(x, y, z)) {

			// Test for ersatz genomes
			for (Map.Entry<ItemStack, IIndividual> entry : AlleleManager.ersatzSpecimen.entrySet()) {
				if (!StackUtils.equals(world.getBlock(x, y, z), entry.getKey()))
					continue;

				int meta = world.getBlockMetadata(x, y, z);
				if (StackUtils.equals(Blocks.leaves, entry.getKey()))
					meta = meta & 3;
				if (entry.getKey().getItemDamage() != meta)
					continue;

				// We matched, replace the leaf block with ours and set the ersatz genome
				PluginArboriculture.treeInterface.setLeaves(world, entry.getValue(), owner, x, y, z);
				// Now let's pollinate
				receiver = (IPollinatable) world.getTileEntity(x, y, z);
			}

		}

		return receiver;
	}
}
