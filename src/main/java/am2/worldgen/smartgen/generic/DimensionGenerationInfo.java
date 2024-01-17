/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic;

import am2.worldgen.smartgen.reccomplexutils.json.JsonUtils;
import com.google.gson.*;
import net.minecraft.world.WorldProvider;

import java.lang.reflect.Type;
import java.util.regex.Matcher;

/**
 * Created by lukas on 24.05.14.
 */
public class DimensionGenerationInfo
{
    private DimensionMatcher dimensionMatcher;
    private Double generationWeight;

    public DimensionGenerationInfo(String expression, Double generationWeight)
    {
        this.dimensionMatcher = new DimensionMatcher(expression);
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

    public boolean matches(WorldProvider provider)
    {
        return dimensionMatcher.apply(provider);
    }

    public DimensionMatcher getDimensionMatcher()
    {
        return dimensionMatcher;
    }

    public String getDisplayString()
    {
        return dimensionMatcher.getDisplayString();
    }

    public static class Serializer implements JsonDeserializer<DimensionGenerationInfo>, JsonSerializer<DimensionGenerationInfo>
    {
        @Override
        public DimensionGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "generationInfo");

            String expression;
            if (jsonobject.has("dimensionID"))
            {
                // Legacy
                expression = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dimensionID");
                if (expression.startsWith("Type:"))
                    expression = "$" + expression.substring(5).replaceAll(",", Matcher.quoteReplacement(" & $"));
            }
            else
                expression = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dimensions");

            Double weight = jsonobject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonobject, "weight") : null;

            return new DimensionGenerationInfo(expression, weight);
        }

        @Override
        public JsonElement serialize(DimensionGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("dimensions", generationInfo.getDimensionMatcher().getExpression());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
