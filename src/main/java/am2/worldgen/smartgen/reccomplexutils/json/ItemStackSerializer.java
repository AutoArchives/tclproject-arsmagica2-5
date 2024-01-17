/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.reccomplexutils.json;

import am2.AMCore;
import am2.worldgen.smartgen.registry.MCRegistrySpecial;
import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack>
{
    private MCRegistrySpecial registry;

    public ItemStackSerializer(MCRegistrySpecial registry)
    {
        this.registry = registry;
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", registry.itemHidingMode().containedItemID(src));
        jsonObject.addProperty("damage", src.getItemDamage());
        jsonObject.addProperty("count", src.stackSize);

        if (src.hasTagCompound())
        {
            if (AMCore.USE_JSON_FOR_NBT)
                jsonObject.add("tag", context.serialize(src.getTagCompound()));
            else
                jsonObject.addProperty("tagBase64", NbtToJson.getBase64FromNBT(src.getTagCompound()));
        }

        return jsonObject;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "ItemStack");

        String id = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "id");
        int damage = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "damage");
        int count = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "count");

        ItemStack stack = registry.itemHidingMode().constructItemStack(id, count, damage);

        if (jsonObject.has("tag"))
        {
            NBTTagCompound compound = context.deserialize(jsonObject.get("tag"), NBTTagCompound.class);
            stack.setTagCompound(compound);
        }
        else if (jsonObject.has("tagBase64"))
        {
            NBTTagCompound compound = NbtToJson.getNBTFromBase64(JsonUtils.getJsonObjectStringFieldValue(jsonObject, "tagBase64"));
            stack.setTagCompound(compound);
        }

        return stack;
    }
}
