/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic;

import am2.worldgen.smartgen.struct.info.YSelector;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import am2.worldgen.smartgen.reccomplexutils.json.JsonUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericYSelector implements YSelector
{
    public static final int MIN_DIST_TO_VOID = 3;

    public enum SelectionMode
    {
        @SerializedName("bedrock")
        BEDROCK,
        @SerializedName("surface")
        SURFACE,
        @SerializedName("sealevel")
        SEALEVEL,
        @SerializedName("underwater")
        UNDERWATER,
        @SerializedName("top")
        TOP,
        @SerializedName("lowestedge")
        LOWEST_EDGE;

        public String serializedName()
        {
            return IvGsonHelper.serializedName(this);
        }

        public static SelectionMode selectionMode(String serializedName)
        {
            return IvGsonHelper.enumForName(serializedName, values());
        }
    }

    public SelectionMode selectionMode;

    public int minYShift;
    public int maxYShift;
    public int minAbsY;
    public int maxAbsY;

    public GenericYSelector(SelectionMode selectionMode, int minYShift, int maxYShift, int minAbsY, int maxAbsY)
    {
        this.selectionMode = selectionMode;
        this.minYShift = minYShift;
        this.maxYShift = maxYShift;
        this.minAbsY = minAbsY;
        this.maxAbsY = maxAbsY;
    }

    @Override
    public int selectY(final World world, Random random, StructureBoundingBox boundingBox)
    {
        final int yShift = minYShift + random.nextInt(maxYShift - minYShift + 1);

        switch (selectionMode)
        {
            case BEDROCK:
                return selectByConstant(world, boundingBox, yShift);
            case TOP:
                return selectByConstant(world, boundingBox, world.getHeight() + yShift - 1);
            case SEALEVEL:
                return selectByConstant(world, boundingBox, 63 + yShift);
            case SURFACE:
                return selectByFunction(world, boundingBox, surfaceSelector(world), averageReducer(yShift));
            case UNDERWATER:
                return selectByFunction(world, boundingBox, surfaceUnderwaterSelector(world), averageReducer(yShift));
            case LOWEST_EDGE:
                return selectByFunction(world, boundingBox, surfaceUnderwaterSelector(world), minReducer(yShift));
        }

        throw new RuntimeException("Unrecognized selection mode " + selectionMode);
    }

    @Override
    public boolean isYcoordOutsideRange(int ycoord) {
        return ycoord < this.minAbsY || ycoord > this.maxAbsY;
    }

    protected static SingleYSelector surfaceSelector(final World world)
    {
        return (x, z) -> surfaceHeight(world, x, z);
    }

    protected static SingleYSelector surfaceUnderwaterSelector(final World world)
    {
        return (x, z) -> surfaceHeightUnderwater(world, x, z);
    }

    protected static IntListReducer minReducer(final int y)
    {
        return list -> {
            int average = list.min();
            return average > MIN_DIST_TO_VOID ? average + y : DONT_GENERATE;
        };
    }

    protected static IntListReducer averageReducer(final int y)
    {
        return list -> {
            int average = averageIgnoringErrors(list.toArray());
            return average > MIN_DIST_TO_VOID ? average + y : DONT_GENERATE;
        };
    }

    protected static int surfaceHeightUnderwater(World world, int x, int z)
    {
        int curYWater = world.getTopSolidOrLiquidBlock(x, z);

        while (curYWater > 0)
        {
            Block block = world.getBlock(x, curYWater, z);
            if (!(block instanceof BlockLiquid || block.getMaterial() == Material.ice))
            {
                curYWater++;
                break;
            }

            curYWater--;
        }
        return curYWater;
    }

    protected static int surfaceHeight(World world, int x, int z)
    {
        int curY = world.getTopSolidOrLiquidBlock(x, z);

        while (curY > 0)
        {
            Block block = world.getBlock(x, curY, z);
            if (!(block.isFoliage(world, x, curY, z) || block.getMaterial() == Material.leaves || block.getMaterial() == Material.plants || block.getMaterial() == Material.wood))
            {
                break;
            }

            curY--;
        }
        while (curY < world.getHeight())
        {
            if (!(world.getBlock(x, curY, z) instanceof BlockLiquid))
            {
                break;
            }

            curY++;
        }

        return curY;
    }

    protected static int selectByConstant(World world, StructureBoundingBox boundingBox, int y)
    {
        return y;
    }

    protected static int selectByFunction(World world, StructureBoundingBox boundingBox, SingleYSelector selector, IntListReducer reducer)
    {
        TIntList intList = selectAll(world, boundingBox, selector);

        if (intList.size() == 0)
            return DONT_GENERATE;

        return reducer.reduce(intList);
    }

    protected static TIntList selectAll(World world, StructureBoundingBox boundingBox, SingleYSelector selector)
    {
        TIntList list = new TIntArrayList();
        for (int x = boundingBox.minX; x <= boundingBox.maxX; x++)
            for (int z = boundingBox.minZ; z <= boundingBox.maxZ; z++)
            {
                if (world.blockExists(x, 0, z))
                    list.add(selector.select(x, z));
            }
        return list;
    }

    protected static int averageIgnoringErrors(int... values)
    {
        int average = 0;
        for (int val : values)
            average += val;
        average /= values.length;

        int averageDist = 0;
        for (int val : values)
            averageDist += dist(val, average);
        averageDist /= values.length;

        int newAverage = 0;
        int ignored = 0;
        for (int val : values)
        {
            if (dist(val, average) <= averageDist * 2)
                newAverage += val;
            else
                ignored++;
        }

        return newAverage / (values.length - ignored);
    }

    protected static int dist(int val1, int val2)
    {
        return (val1 > val2) ? val1 - val2 : val2 - val1;
    }

    protected interface IntListReducer
    {
        int reduce(TIntList list);
    }

    protected interface SingleYSelector
    {
        int select(int x, int z);
    }

    public static class Serializer implements JsonSerializer<GenericYSelector>, JsonDeserializer<GenericYSelector>
    {
        @Override
        public GenericYSelector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "ySelector");

            SelectionMode selectionMode = jsonObject.has("selectionMode")
                    ? (SelectionMode) context.deserialize(jsonObject.get("selectionMode"), SelectionMode.class)
                    : SelectionMode.SURFACE;

            int minYShift = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minY", 0);
            int maxYShift = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxY", 0);

            int minYAbsolute = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minAbsY", 0);
            int maxYAbsolute = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxAbsY", 255);

            return new GenericYSelector(selectionMode, minYShift, maxYShift, minYAbsolute, maxYAbsolute);
        }

        @Override
        public JsonElement serialize(GenericYSelector src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("selectionMode", context.serialize(src.selectionMode));

            jsonObject.addProperty("minY", src.minYShift);
            jsonObject.addProperty("maxY", src.maxYShift);

            jsonObject.addProperty("minAbsY", src.minAbsY);
            jsonObject.addProperty("maxAbsY", src.maxAbsY);

            return jsonObject;
        }
    }
}
