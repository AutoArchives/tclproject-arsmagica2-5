/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic;

import am2.AMCore;
import am2.worldgen.smartgen.reccomplexutils.BlockStates;
import am2.worldgen.smartgen.reccomplexutils.IBlockState;
import am2.worldgen.smartgen.reccomplexutils.json.JsonUtils;
import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.random.WeightedSelector;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Type;

/**
 * Created by lukas on 03.03.15.
 */
public class WeightedBlockState implements WeightedSelector.Item
{
    private static Gson gson = createGson();

    public Double weight;

    public IBlockState state;

    public String tileEntityInfo;

    public WeightedBlockState(Double weight, IBlockState state, String tileEntityInfo)
    {
        this.weight = weight;
        this.state = state;
        this.tileEntityInfo = tileEntityInfo;
    }

    public WeightedBlockState(MCRegistry registry, NBTTagCompound compound)
    {
        weight = compound.hasKey("weight") ? compound.getDouble("weight") : null;
        Block block = compound.hasKey("block") ? registry.blockFromID(compound.getString("block")) : null;
        state = block != null ? BlockStates.fromMetadata(block, compound.getInteger("meta")) : null;
        tileEntityInfo = compound.getString("tileEntityInfo");
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(WeightedBlockState.class, new Serializer(AMCore.specialRegistry));

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public NBTTagCompound writeToNBT(MCRegistry registry)
    {
        NBTTagCompound compound = new NBTTagCompound();

        if (weight != null) compound.setDouble("weight", weight);
        if (state != null) compound.setString("block", registry.idFromBlock(state.getBlock()));
        compound.setInteger("meta", BlockStates.getMetadata(state));
        compound.setString("tileEntityInfo", tileEntityInfo);

        return compound;
    }

    public static class Serializer implements JsonDeserializer<WeightedBlockState>, JsonSerializer<WeightedBlockState>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public WeightedBlockState deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "blockState");

            Double weight = jsonObject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "weight") : null;

            IBlockState state = BlockStates.fromMetadata(registry.blockFromID(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "block", "air")), JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "metadata", 0));

            String tileEntityInfo = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "tileEntityInfo", "");

            return new WeightedBlockState(weight, state, tileEntityInfo);
        }

        @Override
        public JsonElement serialize(WeightedBlockState source, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (source.weight != null)
                jsonObject.addProperty("weight", source.weight);

            jsonObject.addProperty("block", registry.idFromBlock(source.state.getBlock()));
            jsonObject.addProperty("metadata", BlockStates.getMetadata(source.state));

            jsonObject.addProperty("tileEntityInfo", source.tileEntityInfo);

            return jsonObject;
        }
    }
}
