/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.reccomplexutils.rcpackets;

import am2.worldgen.smartgen.generic.GenericStructureInfo;
import am2.worldgen.smartgen.struct.info.StructureRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    private GenericStructureInfo structureInfo;
    private String structureID;
    private boolean saveAsActive;

    private boolean deleteOtherOrStructureInActive;
    private boolean structureInInactive;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, boolean deleteOther)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.saveAsActive = saveAsActive;
        this.deleteOtherOrStructureInActive = deleteOther;
    }

    public PacketEditStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, boolean structureInActive, boolean structureInInactive)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.saveAsActive = saveAsActive;
        this.deleteOtherOrStructureInActive = structureInActive;
        this.structureInInactive = structureInInactive;
    }

    public String getStructureID()
    {
        return structureID;
    }

    public void setStructureID(String structureID)
    {
        this.structureID = structureID;
    }

    public boolean isSaveAsActive()
    {
        return saveAsActive;
    }

    public void setSaveAsActive(boolean saveAsActive)
    {
        this.saveAsActive = saveAsActive;
    }

    public GenericStructureInfo getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructureInfo structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public boolean isDeleteOther()
    {
        return deleteOtherOrStructureInActive;
    }

    public boolean isStructureInActive()
    {
        return deleteOtherOrStructureInActive;
    }

    public void setDeleteOther(boolean deleteOtherOrStructureInActive)
    {
        this.deleteOtherOrStructureInActive = deleteOtherOrStructureInActive;
    }

    public void setStructureInActive(boolean deleteOtherOrStructureInActive)
    {
        this.deleteOtherOrStructureInActive = deleteOtherOrStructureInActive;
    }

    public boolean isStructureInInactive()
    {
        return structureInInactive;
    }

    public void setStructureInInactive(boolean structureInInactive)
    {
        this.structureInInactive = structureInInactive;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        String json = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureRegistry.INSTANCE.createStructureFromJSON(json);
        saveAsActive = buf.readBoolean();
        deleteOtherOrStructureInActive = buf.readBoolean();
        structureInInactive = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureRegistry.INSTANCE.createJSONFromStructure(structureInfo));
        buf.writeBoolean(saveAsActive);
        buf.writeBoolean(deleteOtherOrStructureInActive);
        buf.writeBoolean(structureInInactive);
    }
}
