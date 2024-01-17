/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.registry;

import am2.AMCore;
import am2.worldgen.smartgen.reccomplexutils.FMLRemapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 30.06.14.
 */
public class MCRegistrySpecial implements MCRegistry
{
    public static final String HIDDEN_ITEM_TAG = "RC_HIDDEN_ITEM";
    private static final Item DUMMY_ITEM = Items.coal;

    protected final BiMap<String, Item> itemMap = HashBiMap.create();
    protected final BiMap<String, Block> blockMap = HashBiMap.create();
    protected final Map<String, Class<? extends TileEntity>> tileEntityMap = new HashMap<>();

    protected MCRegistry parent;
    protected FMLRemapper remapper;
    protected ItemHidingRegistry itemHidingRegistry = new ItemHidingRegistry();

    public MCRegistrySpecial(MCRegistry parent, FMLRemapper remapper)
    {
        this.parent = parent;
        this.remapper = remapper;
    }

    public void register(String id, Item item)
    {
        itemMap.put(id, item);
    }

    public void register(String id, Block block)
    {
        blockMap.put(id, block);
    }

    public void register(String id, Class<? extends TileEntity> tileEntity)
    {
        tileEntityMap.put(id, tileEntity);
    }

    public ItemHidingRegistry itemHidingMode()
    {
        return itemHidingRegistry;
    }

    @Override
    public Item itemFromID(String itemID)
    {
        itemID = remapper.mapItem(itemID);
        Item item = itemMap.get(itemID);
        return item != null ? item : parent.itemFromID(itemID);
    }

    @Override
    public String idFromItem(Item item)
    {
        String id = itemMap.inverse().get(item);
        return id != null ? id : parent.idFromItem(item);
    }

    @Override
    public void modifyItemStackCompound(NBTTagCompound compound, String itemID)
    {
        parent.modifyItemStackCompound(compound, itemID);
    }

    public boolean isSafe(Item item)
    {
        return itemMap.isEmpty() || !itemMap.containsValue(item);
    }

    public boolean isItemSafe(String itemID)
    {
        return itemMap.isEmpty() || !itemMap.containsKey(itemID);
    }

    @Override
    public Block blockFromID(String blockID)
    {
        blockID = remapper.mapBlock(blockID);
        Block block = blockMap.get(blockID);
        return block != null ? block : parent.blockFromID(blockID);
    }

    @Override
    public String idFromBlock(Block block)
    {
        String id = blockMap.inverse().get(block);
        return id != null ? id : parent.idFromBlock(block);
    }

    public boolean isSafe(Block block)
    {
        return blockMap.isEmpty() || !blockMap.containsValue(block);
    }

    public boolean isBlockSafe(String blockID)
    {
        return blockMap.isEmpty() || !blockMap.containsKey(blockID);
    }

    @Override
    public TileEntity loadTileEntity(NBTTagCompound compound)
    {
        String id = compound.getString("id");
        if (id != null) {
            if (id.startsWith("RCSpawnScript")) return null;
        }
        // From TileEntity
        try
        {
            Class oclass = tileEntityMap.get(remapper.mapTileEntity(compound.getString("id")));

            if (oclass != null)
            {
                TileEntity tileEntity = (TileEntity) oclass.newInstance();
                tileEntity.readFromNBT(compound);
                return tileEntity;
            }
        }
        catch (Throwable e)
        {
            AMCore.logger.error("Error loading special TileEntity", e);
        }

        return parent.loadTileEntity(compound);
    }

    public boolean isSafe(TileEntity tileEntity)
    {
        return tileEntityMap.isEmpty() || !tileEntityMap.containsValue(tileEntity.getClass());
    }

    public class ItemHidingRegistry implements MCRegistry
    {
        @Override
        public Item itemFromID(String itemID)
        {
            itemID = remapper.mapItem(itemID);
            return !MCRegistrySpecial.this.isItemSafe(itemID) ? DUMMY_ITEM : parent.itemFromID(itemID);
        }

        @Override
        public String idFromItem(Item item)
        {
            return MCRegistrySpecial.this.idFromItem(item);
        }

        public String containedItemID(ItemStack stack)
        {
            return MCRegistrySpecial.this.idFromItem(containedItem(stack));
        }

        public Item containedItem(ItemStack stack)
        {
            Item hidden = hiddenItem(stack);
            return hidden != null ? hidden : stack.getItem();
        }

        @Nullable
        public Item hiddenItem(ItemStack stack)
        {
            return stack.hasTagCompound() && stack.getTagCompound().hasKey(HIDDEN_ITEM_TAG, Constants.NBT.TAG_STRING)
                    ? MCRegistrySpecial.this.itemFromID(stack.getTagCompound().getString(HIDDEN_ITEM_TAG))
                    : null;
        }

        public ItemStack constructItemStack(String itemID, int stackSize, int metadata)
        {
            return constructItemStack(MCRegistrySpecial.this.itemFromID(itemID), stackSize, metadata);
        }

        public ItemStack constructItemStack(Item item, int stackSize, int metadata)
        {
            String hiddenID = MCRegistrySpecial.this.itemMap.inverse().get(item);
            if (hiddenID != null)
            {
                ItemStack stack = new ItemStack(DUMMY_ITEM, stackSize, metadata);
                stack.setTagInfo(HIDDEN_ITEM_TAG, new NBTTagString(hiddenID));
                return stack;
            }
            else
                return new ItemStack(item, stackSize, metadata);
        }

        @Override
        public void modifyItemStackCompound(NBTTagCompound compound, String itemID)
        {
            Item item = MCRegistrySpecial.this.itemMap.get(MCRegistrySpecial.this.remapper.mapItem(itemID));
            if (item != null)
            {
                NBTTagCompound stackNBT;
                if (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND))
                    stackNBT = compound.getCompoundTag("tag");
                else
                    compound.setTag("tag", stackNBT = new NBTTagCompound());

                stackNBT.setString(HIDDEN_ITEM_TAG, itemID);
            }
        }

        @Override
        public Block blockFromID(String blockID)
        {
            return MCRegistrySpecial.this.blockFromID(blockID);
        }

        @Override
        public String idFromBlock(Block block)
        {
            return MCRegistrySpecial.this.idFromBlock(block);
        }

        @Override
        public TileEntity loadTileEntity(NBTTagCompound compound)
        {
            return MCRegistrySpecial.this.loadTileEntity(compound);
        }
    }
}
