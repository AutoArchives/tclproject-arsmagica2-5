/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic;

import am2.worldgen.smartgen.reccomplexutils.json.JsonUtils;
import com.google.gson.*;
import net.minecraft.world.biome.BiomeGenBase;

import java.lang.reflect.Type;
import java.util.regex.Matcher;

/**
 * Created by lukas on 24.05.14.
 */
public class BiomeGenerationInfo
{
    private BiomeMatcher biomeMatcher;
    private Double generationWeight;

    public BiomeGenerationInfo(String expression, Double generationWeight)
    {
        this.biomeMatcher = new BiomeMatcher(expression);
        this.generationWeight = generationWeight;
    }

    public Double getGenerationWeight()
    {
        return generationWeight;
    }

    public void setGenerationWeight(Double generationWeight)
    {
        this.generationWeight = generationWeight;
    }

    public double getActiveGenerationWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return generationWeight == null;
    }

    public boolean matches(BiomeGenBase biome)
    {
        return biomeMatcher.apply(biome);
    }

    public BiomeMatcher getBiomeMatcher()
    {
        return biomeMatcher;
    }

    public String getDisplayString()
    {
        return biomeMatcher.getDisplayString();
    }

    public static class Serializer implements JsonDeserializer<BiomeGenerationInfo>, JsonSerializer<BiomeGenerationInfo>
    {
        @Override
        public BiomeGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "generationInfo");

            String expression;
            if (jsonobject.has("biome"))
            {
                // Legacy
                expression = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "biome");
                if (expression.startsWith("Type:"))
                    expression = "$" + expression.substring(5).replaceAll(",", Matcher.quoteReplacement(" & $"));
            }
            else
                expression = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "biomes");

            Double weight = jsonobject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonobject, "weight") : null;

            return new BiomeGenerationInfo(expression, weight);
        }

        @Override
        public JsonElement serialize(BiomeGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("biomes", generationInfo.getBiomeMatcher().getExpression());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
