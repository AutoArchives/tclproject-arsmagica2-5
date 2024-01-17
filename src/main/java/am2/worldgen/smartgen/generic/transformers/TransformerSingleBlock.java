/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.transformers;

import am2.worldgen.smartgen.reccomplexutils.BlockStates;
import am2.worldgen.smartgen.reccomplexutils.IBlockState;
import am2.worldgen.smartgen.reccomplexutils.NBTStorable;
import am2.worldgen.smartgen.struct.info.StructureSpawnContext;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by lukas on 17.09.14.
 */
public abstract class TransformerSingleBlock<S extends NBTStorable> implements Transformer<S>
{
    @Override
    public boolean skipGeneration(S instanceData, IBlockState state)
    {
        return matches(instanceData, state);
    }

    @Override
    public void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockCoord lowerCoord = context.lowerCoord();

        for (BlockCoord sourceCoord : blockCollection)
        {
            BlockCoord worldCoord = context.transform.apply(sourceCoord, areaSize).add(lowerCoord);
            if (context.includes(worldCoord))
            {
                IBlockState state = BlockStates.at(blockCollection, sourceCoord);
                if (matches(instanceData, state)) {
                    transformBlock(instanceData, Phase.BEFORE, context, worldCoord, state);
                }
            }
        }
    }

    public abstract boolean matches(S instanceData, IBlockState state);

    public abstract void transformBlock(S instanceData, Phase phase, StructureSpawnContext context, BlockCoord coord, IBlockState sourceState);
}
