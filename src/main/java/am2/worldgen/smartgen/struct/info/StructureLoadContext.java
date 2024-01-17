/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.struct.info;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created by lukas on 30.03.15.
 */
public class StructureLoadContext
{
    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;

    public final boolean generateAsSource;

    public StructureLoadContext(@Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource)
    {
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generateAsSource = generateAsSource;
    }

    public int[] boundingBoxSize()
    {
        return new int[]{boundingBox.getXSize(), boundingBox.getYSize(), boundingBox.getZSize()};
    }

    public BlockCoord lowerCoord()
    {
        return new BlockCoord(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
    }

}
