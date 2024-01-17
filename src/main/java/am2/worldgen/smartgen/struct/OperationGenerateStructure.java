package am2.worldgen.smartgen.struct;

import am2.worldgen.smartgen.StructureGenerator;
import am2.worldgen.smartgen.StructurePresets;
import am2.worldgen.smartgen.generic.GenericStructureInfo;
import am2.worldgen.smartgen.struct.info.StructureInfos;
import am2.worldgen.smartgen.struct.info.StructureRegistry;
import am2.worldgen.smartgen.struct.info.StructureSpawnContext;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.BlockQuadCache;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.opengl.GL11;

public class OperationGenerateStructure implements StructurePresets.Operation
{
    public GenericStructureInfo structure;

    public AxisAlignedTransform2D transform;
    public BlockCoord lowerCoord;

    public boolean generateAsSource;

    public String structureIDForSaving;

    protected GridQuadCache cachedShapeGrid;

    public OperationGenerateStructure()
    {
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockCoord lowerCoord, boolean generateAsSource)
    {
        this.structure = structure;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockCoord lowerCoord, boolean generateAsSource, String structureIDForSaving)
    {
        this.structure = structure;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
        this.structureIDForSaving = structureIDForSaving;
    }

    public String getStructureIDForSaving()
    {
        return structureIDForSaving;
    }

    public void setStructureIDForSaving(String structureIDForSaving)
    {
        this.structureIDForSaving = structureIDForSaving;
    }

    @Override
    public void perform(World world)
    {
        if (generateAsSource)
            StructureGenerator.directly(structure, StructureSpawnContext.complete(world, world.rand, transform, lowerCoord, structure, 0, generateAsSource));
        else
            StructureGenerator.instantly(structure, world, world.rand, lowerCoord, transform, 0, false, structureIDForSaving, generateAsSource);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("structureInfo", StructureRegistry.INSTANCE.createJSONFromStructure(structure));
        compound.setTag("structureData", structure.worldDataCompound);

        compound.setInteger("rotation", transform.getRotation());
        compound.setBoolean("mirrorX", transform.isMirrorX());

        BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);

        compound.setBoolean("generateAsSource", generateAsSource);

        if (structureIDForSaving != null)
            compound.setString("structureIDForSaving", structureIDForSaving);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structure = StructureRegistry.INSTANCE.createStructureFromJSON(compound.getString("structureInfo"));
        structure.worldDataCompound = compound.getCompoundTag("structureData");

        transform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

        generateAsSource = compound.getBoolean("generateAsSource");

        structureIDForSaving = compound.hasKey("structureIDForSaving", Constants.NBT.TAG_STRING)
                ? compound.getString("structureIDForSaving")
                : null;
    }

    public void invalidateCache()
    {
        cachedShapeGrid = null;
    }

    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        int[] size = structure.structureBoundingBox();
        if (previewType == PreviewType.SHAPE)
        {
            GL11.glColor3f(0.8f, 0.75f, 1.0f);
//            OperationRenderer.renderGridQuadCache(
//                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = BlockQuadCache.createQuadCache(structure.constructWorldData(world).blockCollection, new float[]{1, 1, 1})),
//                    transform, lowerCoord, ticks, partialTicks);
        }

//        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
//            OperationRenderer.maybeRenderBoundingBox(lowerCoord, StructureInfos.structureSize(size, transform), ticks, partialTicks);
    }
}