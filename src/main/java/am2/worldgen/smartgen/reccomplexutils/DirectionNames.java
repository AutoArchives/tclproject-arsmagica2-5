/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.reccomplexutils;

import ivorius.ivtoolkit.blocks.Directions;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 30.03.15.
 */
public class DirectionNames
{
    public static String of(ForgeDirection direction)
    {
        return of(direction, "none");
    }

    public static String of(ForgeDirection direction, String nullTitle)
    {
        return StatCollector.translateToLocal("reccomplex.direction." + (direction == null ? nullTitle : Directions.serialize(direction).toLowerCase()));
    }
}
