/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.gentypes;

import am2.AMCore;
import am2.worldgen.smartgen.StructureGenerationData;
import am2.worldgen.smartgen.generic.BiomeGenerationInfo;
import am2.worldgen.smartgen.generic.DimensionGenerationInfo;
import am2.worldgen.smartgen.generic.GenericYSelector;
import am2.worldgen.smartgen.generic.presets.BiomeMatcherPresets;
import am2.worldgen.smartgen.generic.presets.DimensionMatcherPresets;
import am2.worldgen.smartgen.reccomplexutils.PresettedList;
import am2.worldgen.smartgen.reccomplexutils.json.JsonUtils;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by lukas on 07.10.14.
 */
public class NaturalGenerationInfo extends StructureGenerationInfo
{
    private static Gson gson = createGson();

    public String id = "";

    public final PresettedList<BiomeGenerationInfo> biomeWeights = new PresettedList<>(BiomeMatcherPresets.instance(), null);
    public final PresettedList<DimensionGenerationInfo> dimensionWeights = new PresettedList<>(DimensionMatcherPresets.instance(), null);

    public String generationCategory;
    public GenericYSelector ySelector;
    private Double generationWeight;

    public SpawnLimitation spawnLimitation;

    public NaturalGenerationInfo()
    {
        this(randomID("Natural"), "decoration", new GenericYSelector(GenericYSelector.SelectionMode.SURFACE, 0, 0, 0, 255));

        biomeWeights.setToDefault();
        dimensionWeights.setToDefault();
    }

    public NaturalGenerationInfo(String id, String generationCategory, GenericYSelector ySelector)
    {
        this.id = id;
        this.generationCategory = generationCategory;
        this.ySelector = ySelector;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(NaturalGenerationInfo.class, new Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());
        builder.registerTypeAdapter(GenericYSelector.class, new GenericYSelector.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static NaturalGenerationInfo deserializeFromVersion1(JsonObject jsonObject, JsonDeserializationContext context)
    {
        String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
        GenericYSelector ySelector = gson.fromJson(jsonObject.get("generationY"), GenericYSelector.class);

        NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo("", generationCategory, ySelector);
        if (jsonObject.has("generationBiomes"))
        {
            BiomeGenerationInfo[] infos = gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
            naturalGenerationInfo.biomeWeights.setContents(Arrays.asList(infos));
        }
        else
            naturalGenerationInfo.biomeWeights.setToDefault();

        naturalGenerationInfo.dimensionWeights.setToDefault();

        return naturalGenerationInfo;
    }

    public Double getGenerationWeight()
    {
        return generationWeight;
    }

    public void setGenerationWeight(Double generationWeight)
    {
        this.generationWeight = generationWeight;
    }

    public double getGenerationWeight(BiomeGenBase biome, WorldProvider provider)
    {
        return getActiveSpawnWeight()
                * generationWeightInBiome(biome)
                * generationWeightInDimension(provider);
    }

    public double generationWeightInDimension(WorldProvider provider)
    {
        for (DimensionGenerationInfo generationInfo : dimensionWeights.list)
        {
            if (generationInfo.matches(provider))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double generationWeightInBiome(BiomeGenBase biome)
    {
        for (BiomeGenerationInfo generationInfo : biomeWeights.list)
        {
            if (generationInfo.matches(biome))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double getActiveSpawnWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return generationWeight == null;
    }

    public boolean hasLimitations()
    {
        return spawnLimitation != null;
    }

    public SpawnLimitation getLimitations()
    {
        return spawnLimitation;
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public String displayString()
    {
        return StatCollector.translateToLocal("reccomplex.generationInfo.natural");
    }

//    @Override
//    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
//    {
//        return new TableDataSourceNaturalGenerationInfo(navigator, delegate, this);
//    }

    public static class SpawnLimitation
    {
        public int maxCount = 1;
        public Context context = Context.DIMENSION;

        public boolean areResolved(World world, String structureID)
        {
            return StructureGenerationData.get(world).getEntriesByID(structureID).size() < maxCount;
        }

        public enum Context
        {
            @SerializedName("dimension")
            DIMENSION,
        }
    }

    public static class Serializer implements JsonSerializer<NaturalGenerationInfo>, JsonDeserializer<NaturalGenerationInfo>
    {
        @Override
        public NaturalGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "naturalGenerationInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
            GenericYSelector ySelector;

            if (jsonObject.has("generationY"))
                ySelector = gson.fromJson(jsonObject.get("generationY"), GenericYSelector.class);
            else
            {
                AMCore.logger.warn("Structure JSON missing 'generationY'! Using 'surface'!");
                ySelector = new GenericYSelector(GenericYSelector.SelectionMode.SURFACE, 0, 0, 0, 255);
            }

            NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(id, generationCategory, ySelector);

            if (jsonObject.has("generationWeight"))
                naturalGenerationInfo.generationWeight = JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "generationWeight");

            loadPresettedList(jsonObject, naturalGenerationInfo.biomeWeights, "biomeWeightsPreset", "generationBiomes", BiomeGenerationInfo[].class);
            loadPresettedList(jsonObject, naturalGenerationInfo.dimensionWeights, "dimensionWeightsPreset", "generationDimensions", DimensionGenerationInfo[].class);

            if (jsonObject.has("spawnLimitation"))
                naturalGenerationInfo.spawnLimitation = context.deserialize(jsonObject.get("spawnLimitation"), SpawnLimitation.class);

            return naturalGenerationInfo;
        }

        @Override
        public JsonElement serialize(NaturalGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.addProperty("generationCategory", src.generationCategory);
            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            jsonObject.add("generationY", gson.toJsonTree(src.ySelector));

            writePresettedList(jsonObject, src.biomeWeights, "biomeWeightsPreset", "generationBiomes");
            writePresettedList(jsonObject, src.dimensionWeights, "dimensionWeightsPreset", "generationDimensions");

            if (src.spawnLimitation != null)
                jsonObject.add("spawnLimitation", context.serialize(src.spawnLimitation));

            return jsonObject;
        }

        protected static <T> void loadPresettedList(JsonObject jsonObject, PresettedList<T> list, String presetKey, String listKey, Class<T[]> clazz)
        {
            if (!list.setPreset(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, presetKey, null)))
            {
                if (jsonObject.has(listKey))
                    Collections.addAll(list.list, gson.fromJson(jsonObject.get(listKey), clazz));
                else
                    list.setToDefault();
            }
        }

        protected static <T> void writePresettedList(JsonObject jsonObject, PresettedList<T> list, String presetKey, String listKey)
        {
            if (list.getPreset() != null)
                jsonObject.addProperty(presetKey, list.getPreset());
            jsonObject.add(listKey, gson.toJsonTree(list.list));
        }
    }
}
