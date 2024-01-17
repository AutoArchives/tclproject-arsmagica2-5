/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.struct.villages;

import am2.worldgen.smartgen.StructureGenerationData;
import am2.worldgen.smartgen.StructureGenerator;
import am2.worldgen.smartgen.generic.gentypes.StructureGenerationInfo;
import am2.worldgen.smartgen.generic.gentypes.VanillaStructureGenerationInfo;
import am2.worldgen.smartgen.reccomplexutils.NBTStorable;
import am2.worldgen.smartgen.struct.info.StructureInfo;
import am2.worldgen.smartgen.struct.info.StructureLoadContext;
import am2.worldgen.smartgen.struct.info.StructurePrepareContext;
import am2.worldgen.smartgen.struct.info.StructureRegistry;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.blocks.Directions;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillagePiece extends StructureVillagePieces.Village
{
    public String structureID;
    public String generationID;

    public boolean mirrorX;
    public boolean startedGeneration;
    public NBTBase instanceData;

    public GenericVillagePiece()
    {
    }

    public GenericVillagePiece(StructureVillagePieces.Start start, int generationDepth)
    {
        super(start, generationDepth);
    }

    public void setIds(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public void setOrientation(int front, boolean mirrorX, StructureBoundingBox boundingBox)
    {
        coordBaseMode = front;
        this.mirrorX = mirrorX;
        this.boundingBox = boundingBox;
    }

    public void prepare(Random random)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, coordBaseMode, mirrorX);

                instanceData = structureInfo.prepareInstanceData(new StructurePrepareContext(random, transform, boundingBox, false)).writeToNBT();
            }
        }
    }

    public static AxisAlignedTransform2D getTransform(VanillaStructureGenerationInfo vanillaGenInfo, int front, boolean mirrorX)
    {
        return AxisAlignedTransform2D.from(getRotations(vanillaGenInfo, front, mirrorX), mirrorX);
    }

    public static int getRotations(VanillaStructureGenerationInfo vanillaGenInfo, int front, boolean mirrorX)
    {
        Integer rotations = Directions.getHorizontalClockwiseRotations(vanillaGenInfo.front, Directions.getDirectionFromVRotation(front), mirrorX);
        return rotations == null ? 0 : rotations;
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID);
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID, StructureVillagePieces.Start start, int generationDepth)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID, start, generationDepth);
    }

    public static boolean canVillageGoDeeperC(StructureBoundingBox box)
    {
        return canVillageGoDeeper(box);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox boundingBox)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, coordBaseMode, mirrorX);

                BlockCoord structureShift = transform.apply(vanillaGenInfo.spawnShift, new int[]{1, 1, 1});

                if (this.field_143015_k < 0)
                {
                    this.field_143015_k = this.getAverageGroundLevel(world, boundingBox);

                    if (this.field_143015_k < 0)
                        return true;

                    this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.minY + structureShift.y, 0);
                }

                BlockCoord lowerCoord = new BlockCoord(this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ);
                NBTStorable instanceData = structureInfo.loadInstanceData(new StructureLoadContext(transform, boundingBox, false), this.instanceData);

                StructureGenerator.partially(structureInfo, world, random, lowerCoord, transform, boundingBox, componentType, structureID, instanceData, !startedGeneration);

                if (structureID != null && !startedGeneration)
                    StructureGenerationData.get(world).addCompleteEntry(structureID, lowerCoord, transform);

                startedGeneration = true;

                return true;
            }
        }

        return false;
    }

    protected void func_143012_a(NBTTagCompound tagCompound)
    {
        super.func_143012_a(tagCompound);
        tagCompound.setString("RcSId", structureID);
        tagCompound.setString("RcGtId", structureID);
        tagCompound.setBoolean("RcMirror", mirrorX);
        tagCompound.setBoolean("RcStartGen", startedGeneration);
        tagCompound.setTag("RcInstDat", instanceData);
    }

    protected void func_143011_b(NBTTagCompound tagCompound)
    {
        super.func_143011_b(tagCompound);
        structureID = tagCompound.getString("RcSId");
        generationID = tagCompound.getString("RcGtId");
        mirrorX = tagCompound.getBoolean("RcMirror");
        startedGeneration = tagCompound.getBoolean("RcStartGen");
        instanceData = tagCompound.getTag("RcInstDat");
    }
}
