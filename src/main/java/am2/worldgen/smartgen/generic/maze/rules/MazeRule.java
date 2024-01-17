/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.maze.rules;

import am2.worldgen.smartgen.generic.maze.*;
import ivorius.ivtoolkit.maze.components.MazeComponent;
import ivorius.ivtoolkit.maze.components.MazePredicate;
import ivorius.ivtoolkit.tools.NBTCompoundObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 21.03.16.
 */
public abstract class MazeRule implements NBTCompoundObject
{
    public abstract String displayString();

//    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, List<SavedMazePathConnection> expected, int[] boundsLower, int[] boundsHigher);

    public abstract MazePredicate<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory, Collection<? extends MazeComponent<Connector>> components);
}
