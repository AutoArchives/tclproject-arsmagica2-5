/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.maze;

import am2.worldgen.smartgen.GenerationConstants;
import am2.worldgen.smartgen.generic.Selection;
import am2.worldgen.smartgen.generic.maze.rules.BlockedConnectorStrategy;
import am2.worldgen.smartgen.generic.maze.rules.LimitAABBStrategy;
import am2.worldgen.smartgen.generic.maze.rules.MazeRule;
import am2.worldgen.smartgen.generic.maze.rules.MazeRuleRegistry;
import am2.worldgen.smartgen.reccomplexutils.IntAreas;
import am2.worldgen.smartgen.reccomplexutils.IvTranslations;
import am2.worldgen.smartgen.reccomplexutils.NBTStorable;
import am2.worldgen.smartgen.struct.info.StructureLoadContext;
import am2.worldgen.smartgen.struct.info.StructurePrepareContext;
import am2.worldgen.smartgen.struct.info.StructureRegistry;
import am2.worldgen.smartgen.struct.info.StructureSpawnContext;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptMazeGenerator implements WorldScript<WorldScriptMazeGenerator.InstanceData>
{

    // TODO Turn into SavedMazeComponent
    public final List<SavedMazePathConnection> exitPaths = new ArrayList<>();
    public String mazeID = "";
    public Selection rooms = Selection.zeroSelection(3);
    public BlockCoord structureShift = new BlockCoord(0, 0, 0);
    public int[] roomSize = new int[]{3, 5, 3};
    public final List<MazeRule> rules = new ArrayList<>();

    public static <C> void addRandomPaths(Random random, int[] size, MorphingMazeComponent<C> maze, List<? extends MazeComponent<C>> components, C roomConnector, int number)
    {
        Map<MazePassage, C> exits = new HashMap<>();
        for (MazeComponent<C> component : components)
            for (Map.Entry<MazePassage, C> entry : component.exits().entrySet())
                exits.put(entry.getKey(), entry.getValue());

        for (int i = 0; i < number; i++)
        {
            int[] randomCoords = new int[size.length];
            for (int c = 0; c < randomCoords.length; c++)
                randomCoords[c] = MathHelper.getRandomIntegerInRange(random, 0, size[c]);
            MazeRoom randomRoom = new MazeRoom(randomCoords);
            MazePassage randomConnection = new MazePassage(randomRoom, randomRoom.addInDimension(random.nextInt(size.length), random.nextBoolean() ? 1 : -1));
            if (Objects.equals(exits.get(randomConnection), roomConnector))
                maze.exits().put(randomConnection, roomConnector);
        }
    }

    public static void addExits(ConnectorFactory factory, MorphingMazeComponent<Connector> maze, List<SavedMazePathConnection> mazeExits)
    {
        SavedMazePaths.putAll(maze.exits(), mazeExits.stream().map(SavedMazePaths.buildFunction(factory)).map(e -> maze.rooms().contains(e.getKey().getSource()) ? e : Pair.of(e.getKey().inverse(), e.getValue())).collect(Collectors.toList()));
    }

    public static <C> void blockRooms(MorphingMazeComponent<C> component, Set<MazeRoom> rooms, C wallConnector)
    {
        component.add(WorldGenMaze.createCompleteComponent(rooms, Collections.<MazePassage, C>emptyMap(), wallConnector));
    }

    public static <C> void enclose(MorphingMazeComponent<C> component, MazeRoom lower, MazeRoom higher, C wallConnector)
    {
        if (lower.getDimensions() != higher.getDimensions())
            throw new IllegalArgumentException();

        final Set<MazeRoom> rooms = new HashSet<>();
        int[] coords = lower.getCoordinates();
        for (int i = 0; i < coords.length; i++)
        {
            final int lowest = lower.getCoordinate(i);
            final int highest = higher.getCoordinate(i);

            final int finalI = i;
            IntAreas.visitCoordsExcept(lower.getCoordinates(), higher.getCoordinates(), TIntArrayList.wrap(new int[]{i}), ints -> {
                ints[finalI] = lowest;
                rooms.add(new MazeRoom(ints));
                ints[finalI] = highest;
                rooms.add(new MazeRoom(ints));

                return true;
            });
        }

        blockRooms(component, rooms, wallConnector);
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockCoord coord)
    {
        List<PlacedStructure> placedStructures = instanceData.placedStructures;
        if (placedStructures == null)
            return;
        WorldGenMaze.generatePlacedStructures(placedStructures, context);
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.mazeGen");
    }

//    @Override
//    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
//    {
//        return new TableDataSourceWorldScriptMazeGenerator(this, tableDelegate, navigator);
//    }

    public String getMazeID()
    {
        return mazeID;
    }

    public void setMazeID(String mazeID)
    {
        this.mazeID = mazeID;
    }

    public BlockCoord getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockCoord structureShift)
    {
        this.structureShift = structureShift;
    }

    public int[] getRoomSize()
    {
        return roomSize.clone();
    }

    public void setRoomSize(int[] roomSize)
    {
        this.roomSize = roomSize;
    }

    public Selection getRooms()
    {
        return rooms;
    }

    public void setRooms(Selection rooms)
    {
        this.rooms = rooms;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        mazeID = compound.getString("mazeID");

        NBTTagCompound rooms = compound.getCompoundTag("rooms");
        this.rooms.readFromNBT(rooms, 3);

        // Legacy
        if (compound.hasKey("roomNumbers", Constants.NBT.TAG_INT_ARRAY))
            this.rooms.add(new Selection.Area(true, new int[]{0, 0, 0}, IvVecMathHelper.sub(IvNBTHelper.readIntArrayFixedSize("roomNumbers", 3, compound), new int[]{1, 1, 1})));
        if (compound.hasKey("blockedRoomAreas", Constants.NBT.TAG_LIST))
        {
            NBTTagList blockedRoomsList = compound.getTagList("blockedRoomAreas", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < blockedRoomsList.tagCount(); i++)
            {
                NBTTagCompound blockedRoomTag = blockedRoomsList.getCompoundTagAt(i);
                this.rooms.add(new Selection.Area(false, IvNBTHelper.readIntArrayFixedSize("min", 3, blockedRoomTag), IvNBTHelper.readIntArrayFixedSize("max", 3, blockedRoomTag)));
            }
        }

        exitPaths.clear();
        exitPaths.addAll(NBTCompoundObjects.readListFrom(compound, "mazeExits", SavedMazePathConnection.class));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", compound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, compound);

        rules.clear();
        rules.addAll(NBTTagLists.compoundsFrom(compound, "rules").stream().map(MazeRuleRegistry.INSTANCE::read).collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("mazeID", mazeID);

        NBTTagCompound rooms = new NBTTagCompound();
        this.rooms.writeToNBT(rooms);
        compound.setTag("rooms", rooms);

        NBTCompoundObjects.writeListTo(compound, "mazeExits", exitPaths);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, compound);

        compound.setIntArray("roomSize", roomSize);

        NBTTagLists.writeCompoundsTo(compound, "rules", rules.stream().map(MazeRuleRegistry.INSTANCE::write).collect(Collectors.toList()));
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.placedStructures.addAll(WorldGenMaze.convertToPlacedStructures(context.random, coord, structureShift, getPlacedRooms(context.random, context.transform), roomSize, context.transform));
        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    public List<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>> getPlacedRooms(Random random, AxisAlignedTransform2D transform)
    {
        if (rooms.isEmpty())
            return null;

        ConnectorFactory factory = new ConnectorFactory();

        Connector roomConnector = factory.get("Path");
        Connector wallConnector = factory.get("Wall");
        Set<Connector> blockedConnections = Collections.singleton(wallConnector); // TODO Make configurable

        int[] boundsHigher = rooms.boundsHigher();
        int[] boundsLower = rooms.boundsLower();

        int[] oneArray = new int[boundsHigher.length];
        Arrays.fill(oneArray, 1);

        final int[] outsideBoundsHigher = IvVecMathHelper.add(boundsHigher, oneArray);
        final int[] outsideBoundsLower = IvVecMathHelper.sub(boundsLower, oneArray);

        List<MazeComponentStructure<Connector>> transformedComponents = WorldGenMaze.transformedComponents(StructureRegistry.INSTANCE.getStructuresInMaze(mazeID), factory, transform, blockedConnections);

        MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();

        WorldScriptMazeGenerator.enclose(maze, new MazeRoom(outsideBoundsLower), new MazeRoom(outsideBoundsHigher), wallConnector);
        WorldScriptMazeGenerator.blockRooms(maze, rooms.mazeRooms(false), wallConnector);
        WorldScriptMazeGenerator.addExits(factory, maze, exitPaths);
        WorldScriptMazeGenerator.addRandomPaths(random, outsideBoundsHigher, maze, transformedComponents, roomConnector, outsideBoundsHigher[0] * outsideBoundsHigher[1] * outsideBoundsHigher[2] / (5 * 5 * 5) + 1);

        List<MazePredicate<MazeComponentStructure<Connector>, Connector>> predicates = rules.stream().map(r -> r.build(this, blockedConnections, factory, transformedComponents)).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        predicates.add(new LimitAABBStrategy<>(outsideBoundsHigher));
        predicates.add(new BlockedConnectorStrategy<>(blockedConnections));

        ConnectorStrategy connectionStrategy = new ConnectorStrategy();

        int totalRooms = rooms.mazeRooms(true).size();

        return MazeComponentConnector.randomlyConnect(maze,
                transformedComponents, connectionStrategy,
                new MazePredicateMany<>(predicates),
                random,
                GenerationConstants.mazePlacementReversesPerRoom >= 0 ? MathHelper.floor_float(totalRooms * GenerationConstants.mazePlacementReversesPerRoom + 0.5f) : MazeComponentConnector.INFINITE_REVERSES
        );
    }

    public static class InstanceData implements NBTStorable
    {
        public final List<PlacedStructure> placedStructures = new ArrayList<>();

        public InstanceData()
        {

        }

        public InstanceData(NBTTagCompound compound)
        {
            placedStructures.addAll(NBTCompoundObjects.readListFrom(compound, "placedStructures", PlacedStructure.class));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            NBTCompoundObjects.writeListTo(compound, "placedStructures", placedStructures);
            return compound;
        }
    }
}
