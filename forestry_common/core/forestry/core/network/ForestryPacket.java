/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.core.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.common.registry.GameData;
import io.netty.buffer.Unpooled;
import forestry.core.proxy.Proxies;

public class ForestryPacket {

	protected int id;
	protected static String channel = "FOR";

	public ForestryPacket() {
	}

	public ForestryPacket(int id) {
		this.id = id;
	}

	public FMLProxyPacket getPacket() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			data.writeByte(getID());
			writeData(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new FMLProxyPacket(Unpooled.wrappedBuffer(bytes.toByteArray()), channel);
	}

	public int getID() {
		return id;
	}

	protected ItemStack readItemStack(DataInputStream data) throws IOException {
		ItemStack itemstack = null;
		String itemName = data.readUTF();

		if (!itemName.isEmpty()) {
			Item item = GameData.itemRegistry.getObject(itemName);
			byte stackSize = data.readByte();
			short meta = data.readShort();
			itemstack = new ItemStack(item, stackSize, meta);

			if (item.isDamageable() || Proxies.common.needsTagCompoundSynched(item))
				itemstack.stackTagCompound = this.readNBTTagCompound(data);
		}

		return itemstack;
	}

	protected void writeItemStack(ItemStack itemstack, DataOutputStream data) throws IOException {

		if (itemstack == null)
			data.writeUTF("");
		else {
			data.writeUTF(GameData.itemRegistry.getNameForObject(itemstack.getItem()));
			data.writeByte(itemstack.stackSize);
			data.writeShort(itemstack.getItemDamage());

			if (itemstack.getItem().isDamageable() || Proxies.common.needsTagCompoundSynched(itemstack.getItem()))
				this.writeNBTTagCompound(itemstack.stackTagCompound, data);
		}
	}

	protected NBTTagCompound readNBTTagCompound(DataInputStream data) throws IOException {

		short length = data.readShort();

		if (length < 0)
			return null;
		else {
			byte[] compressed = new byte[length];
			data.readFully(compressed);
			return CompressedStreamTools.func_152457_a(compressed, NBTSizeTracker.field_152451_a);
		}

	}

	protected void writeNBTTagCompound(NBTTagCompound nbttagcompound, DataOutputStream data) throws IOException {

		if (nbttagcompound == null)
			data.writeShort(-1);
		else {
			byte[] compressed = CompressedStreamTools.compress(nbttagcompound);
			data.writeShort((short) compressed.length);
			data.write(compressed);
		}

	}

	public void writeData(DataOutputStream data) throws IOException {
	}

	public void readData(DataInputStream data) throws IOException {
	}

}
