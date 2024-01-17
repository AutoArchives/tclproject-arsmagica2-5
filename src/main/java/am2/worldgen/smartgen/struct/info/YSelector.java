/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.struct.info;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Random;

/**
 * Created by lukas on 10.04.15.
 */
public interface YSelector
{
    int DONT_GENERATE = -1;

    int selectY(World world, Random random, StructureBoundingBox boundingBox);

    boolean isYcoordOutsideRange(int ycoord);
}
