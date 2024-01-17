/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.maze;

import am2.worldgen.smartgen.reccomplexutils.NBTStorable;
import am2.worldgen.smartgen.struct.info.StructureInfo;
import am2.worldgen.smartgen.struct.info.StructureInfos;
import am2.worldgen.smartgen.struct.info.StructureLoadContext;
import am2.worldgen.smartgen.struct.info.StructureRegistry;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 16.04.15.
 */
public class PlacedStructure implements NBTCompoundObject
{
    public String structureID;
    public AxisAlignedTransform2D transform;
    public BlockCoord lowerCoord;

    public NBTStorable instanceData;

    public PlacedStructure(String structureID, AxisAlignedTransform2D transform, BlockCoord lowerCoord, NBTStorable instanceData)
    {
        this.structureID = structureID;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.instanceData = instanceData;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structureID = compound.getString("structureID");
        transform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));
        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);

        instanceData = compound.hasKey("instanceData", Constants.NBT.TAG_COMPOUND) && structureInfo != null
                ? structureInfo.loadInstanceData(new StructureLoadContext(transform, StructureInfos.structureBoundingBox(lowerCoord, StructureInfos.structureSize(structureInfo, transform)), false), compound.getTag("instanceData"))
                : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("structureID", structureID);
        compound.setInteger("rotation", transform.getRotation());
        compound.setBoolean("mirrorX", transform.isMirrorX());
        BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);
        if (instanceData != null)
            compound.setTag("instanceData", instanceData.writeToNBT());
    }
}
