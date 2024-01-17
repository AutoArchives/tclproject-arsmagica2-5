/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.reccomplexutils;

import am2.AMCore;
import com.google.common.collect.ObjectArrays;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import java.lang.reflect.Constructor;

/**
 * Created by lukas on 01.04.15.
 */
public class FMLUtils
{
    public static String addPrefix(String name)
    {
        // From FML
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index);
        String prefix;
        ModContainer mc = Loader.instance().activeModContainer();

        if (mc != null)
            prefix = mc.getModId();
        else // no mod container, assume minecraft
            prefix = "minecraft";

        if (!oldPrefix.equals(prefix))
            name = prefix + ":" + name;

        return name;
    }

    public static ItemBlock constructItem(Block block, Class<? extends ItemBlock> itemClass, Object... itemArgs)
    {
        // From FML
        try
        {
            Class<?>[] ctorArgClasses = new Class<?>[itemArgs.length + 1];
            ctorArgClasses[0] = Block.class;
            for (int idx = 1; idx < ctorArgClasses.length; idx++)
                ctorArgClasses[idx] = itemArgs[idx - 1].getClass();
            Constructor<? extends ItemBlock> itemCtor = itemClass.getConstructor(ctorArgClasses);

            return itemCtor.newInstance(ObjectArrays.concat(block, itemArgs));
        }
        catch (Throwable e)
        {
            AMCore.logger.warn("Error constructing secret item", e);
        }

        return null;
    }
}
