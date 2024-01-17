/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.generic.maze.rules;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.GuavaCollectors;
import ivorius.ivtoolkit.tools.Visitor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    private final Set<Pair<MazeRoom, Set<MazeRoom>>> traversalAbilities = new HashSet<>();

    private ConnectionPoint mainConnectionPoint;
    private final List<ConnectionPoint> connectionPoints = new ArrayList<>();
    private final TObjectIntMap<ConnectionPoint> stepsReached = new TObjectIntHashMap<>();

    private final Predicate<MazeRoom> confiner;
    private final Predicate<C> traverser;

    private boolean preventConnection;

    public ReachabilityStrategy(Predicate<MazeRoom> confiner, Predicate<C> traverser, boolean preventConnection)
    {
        this.confiner = confiner;
        this.traverser = traverser;
        this.preventConnection = preventConnection;
    }

    public static <M extends MazeComponent<C>, C> ReachabilityStrategy<M, C> connect(Collection<Collection<MazePassage>> points, Predicate<C> traverser, Predicate<MazeRoom> confiner, Set<Pair<MazeRoom, Set<MazeRoom>>> traversalAbilities)
    {
        ReachabilityStrategy<M, C> strategy = new ReachabilityStrategy<>(confiner, traverser, false);
        strategy.setConnection(points);
        strategy.traversalAbilities.addAll(traversalAbilities);
        return strategy;
    }

    public static <M extends MazeComponent<C>, C> ReachabilityStrategy<M, C> preventConnection(Collection<Collection<MazePassage>> points, Predicate<C> traverser, Predicate<MazeRoom> confiner)
    {
        ReachabilityStrategy<M, C> strategy = new ReachabilityStrategy<>(confiner, traverser, true);
        strategy.setConnection(points);
        return strategy;
    }

    public static <C> Set<Pair<MazeRoom, Set<MazeRoom>>> compileAbilities(Collection<? extends MazeComponent<C>> components, Predicate<C> traverser)
    {
        Set<Pair<MazeRoom, Set<MazeRoom>>> abilities = new HashSet<>();

        for (MazeComponent<C> component : components)
        {
            // Exits leading outside
            component.exits().forEach((passage, c) -> {
                if (traverser.test(c))
                    abilities.add(Pair.of(passage.normalize().getDest(), Collections.emptySet()));
            });

            // Walking within the component
            for (Map.Entry<MazePassage, MazePassage> entry : component.reachability().entries())
            {
                if (!traverser.test(component.exits().get(entry.getValue())))
                    continue;

                MazePassage passage = new MazePassage(entry.getKey().getSource(), entry.getValue().getSource());
                abilities.add(Pair.of(passage.normalize().getDest(), component.rooms().stream().map(r -> r.sub(passage.getSource())).collect(Collectors.toSet())));
            }
        }

        // Remove inferrable abilities
        for (Iterator<Pair<MazeRoom, Set<MazeRoom>>> iterator = abilities.iterator(); iterator.hasNext(); )
        {
            Pair<MazeRoom, Set<MazeRoom>> ability = iterator.next();
            MazeRoom nullRoom = new MazeRoom(new int[ability.getLeft().getDimensions()]);

            if (canReach(ability.getRight(),
                    abilities.stream().filter(a -> !a.equals(ability)).collect(Collectors.toSet()),
                    Collections.singleton(nullRoom),
                    Collections.singleton(ability.getLeft())
                    , null))
                iterator.remove();
        }

        return abilities;
    }

    public static <C> Predicate<C> connectorTraverser(final Set<C> blockingConnections)
    {
        return input -> !blockingConnections.contains(input);
    }

    protected static <C> Set<MazePassage> traverse(Collection<MazeComponent<C>> mazes, @Nullable Collection<MazePassage> traversed, boolean addToTraversed, Set<MazePassage> connections, Predicate<C> traverser, @Nullable Visitor<MazePassage> visitor)
    {
        if (addToTraversed) Objects.requireNonNull(traversed);

        Deque<MazePassage> dirty = Lists.newLinkedList(connections);
        Set<MazePassage> added = new HashSet<>();

        MazePassage traversing;
        while ((traversing = dirty.pollFirst()) != null)
        {
            for (MazeComponent<C> maze : mazes)
            {
                maze.reachability().get(traversing).forEach(dest -> {
                    if ((traversed == null || !traversed.contains(dest))
                            && (addToTraversed || !added.contains(dest))
                            && (visitor == null || visitor.visit(dest)))
                    {
                        if (traverser.test(maze.exits().get(dest)))
                        {
                            MazePassage rDest = dest.inverse(); // We are now on the other side of the connection/'wall'

                            if (added.add(rDest))
                            {
                                if (addToTraversed) traversed.add(rDest);
                                dirty.addLast(rDest);
                            }
                        }

                        if (addToTraversed) traversed.add(dest);
                        dirty.addLast(dest);
                        added.add(dest);
                    }
                });
            }
        }
        return added;
    }

    private static boolean canReach(Set<MazeRoom> rooms, Set<Pair<MazeRoom, Set<MazeRoom>>> abilities, Set<MazeRoom> left, Set<MazeRoom> right, Predicate<MazeRoom> confiner)
    {
        return canReach(rooms, abilities, Collections.emptyList(), left, right, Collections.emptyList(), confiner, null);
    }

    private static <C> boolean canReach(Set<MazeRoom> rooms, Set<Pair<MazeRoom, Set<MazeRoom>>> abilities, Collection<MazeComponent<C>> mazes, Set<MazeRoom> left, Set<MazeRoom> right, Collection<MazePassage> pTraversed, Predicate<MazeRoom> confiner, Predicate<C> traverser)
    {
        if (left.size() <= 0 || right.size() <= 0)
            return false;

        final Collection<MazePassage> traversed = Sets.newHashSet(pTraversed); // Editable

        Predicate<MazeRoom> predicate = confiner != null ? confiner.and((o) -> !rooms.contains(o)) : rooms::contains;
        Predicate<MazePassage> passagePredicate = p -> predicate.test(p.getDest()) && !traversed.contains(p);

        Multimap<MazeRoom, MazePassage> entryReachability = compileEntryReachability(mazes, passagePredicate, traverser);

        Set<MazeRoom> visited = Sets.newHashSet(left);
        TreeSet<MazeRoom> dirty = Sets.newTreeSet((o1, o2) -> {
            int compare = Double.compare(minDistanceSQ(o1, right), minDistanceSQ(o2, right));
            return compare != 0 ? compare : compare(o1.getCoordinates(), o2.getCoordinates());
        });
        dirty.addAll(left);
        visited.addAll(left);

        Deque<MazeRoom> tryAdd = Lists.newLinkedList();

        while (!dirty.isEmpty())
        {
            MazeRoom cur = dirty.pollFirst();
            for (MazeRoom next : (Iterable<MazeRoom>) abilities.stream()
                    .filter(e -> e.getValue().stream().map(p -> p.add(cur)).allMatch(predicate))
                    .map(p -> p.getKey().add(cur))::iterator)
            {
                do
                {
                    if (right.contains(next))
                        return true;

                    if (predicate.test(next) && visited.add(next))
                    {
                        for (MazePassage passage : entryReachability.removeAll(next))
                        {
                            Set<MazeRoom> roomExits = traverse(mazes, traversed, true, Collections.singleton(passage), traverser, null).stream().map(MazePassage::getDest).collect(Collectors.toSet());
                            tryAdd.addAll(roomExits);
                            roomExits.forEach(entryReachability::removeAll);
                        }

                        dirty.add(next);
                    }
                }
                while (!tryAdd.isEmpty() && (next = tryAdd.pollFirst()) != null);
            }
        }

        return false;
    }

    private static <C> Multimap<MazeRoom, MazePassage> compileEntryReachability(Collection<MazeComponent<C>> mazes, Predicate<MazePassage> passagePredicate, Predicate<C> traverser)
    {
        Multimap<MazeRoom, MazePassage> iReachability = HashMultimap.create();
        for (MazeComponent<C> maze : mazes)
            iReachability.putAll(maze.reachability().keySet().stream()
                    .filter(passagePredicate.and(p -> traverser.test(maze.exits().get(p))))
                    .collect(GuavaCollectors.toMultimap(MazePassage::getDest, maze.reachability()::get))
            );
        return iReachability;
    }

    private static int compare(int[] left, int[] right)
    {
        for (int i = 0; i < left.length; i++)
        {
            int cmp = Integer.compare(left[i], right[i]);
            if (cmp != 0)
                return cmp;
        }

        return 0;
    }

    private static double minDistanceSQ(MazeRoom room, Collection<MazeRoom> rooms)
    {
        return rooms.stream().mapToDouble(r -> room.distanceSQ(room)).min().orElse(0);
    }

    protected void setConnection(Collection<Collection<MazePassage>> points)
    {
        connectionPoints.addAll(points.stream().map(p -> new ConnectionPoint(p, p.stream().map(MazePassage::inverse).collect(Collectors.toList()))).collect(Collectors.toList()));

        mainConnectionPoint = connectionPoints.size() > 0 ? connectionPoints.remove(0) : null;
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<M, C> component)
    {
        if (preventConnection && !stepsReached.isEmpty())
            return true; // Already Connected: Give Up

        if (stepsReached.size() == connectionPoints.size())
            return true; // Done

        place(maze, component, true);

        final Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
        Predicate<MazePassage> isDirty = input -> confiner.test(input.getSource()) && !roomsFromBoth.contains(input.getSource());
        boolean canPlace = preventConnection
                ? stepsReached.isEmpty()
                : connectionPoints.stream().allMatch(point -> stepsReached.containsKey(point) || canReach(roomsFromBoth,
                traversalAbilities,
                Arrays.asList(maze, component),
                point.traversed.stream().filter(isDirty).map(MazePassage::getDest).collect(Collectors.toSet()),
                mainConnectionPoint.traversed.stream().filter(isDirty).map(MazePassage::getDest).collect(Collectors.toSet()),
                point.traversed,
                confiner,
                traverser));

        unplace(maze, component, true);

        return canPlace;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        place(maze, component, false);
    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {

    }

    protected void place(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component, boolean simulate)
    {
        if (stepsReached.size() == connectionPoints.size())
            stepsReached.transformValues(i -> i + 1);
        else
        {
            for (ConnectionPoint point : connectionPoints)
            {
                if (stepsReached.containsKey(point))
                    stepsReached.adjustValue(point, 1);
                else
                    point.order.add(traverse(maze, component, point.traversed, mainConnectionPoint.traversed, p -> stepsReached.put(point, 0)));
            }

            mainConnectionPoint.order.add(traverse(maze,
                    component,
                    mainConnectionPoint.traversed,
                    connectionPoints.stream().filter(point -> !stepsReached.containsKey(point)).flatMap(point -> point.traversed.stream()).collect(Collectors.toList()),
                    p -> connectionPoints.stream().filter(point -> point.traversed.contains(p)).forEach(point -> stepsReached.put(point, 0))));
        }
    }

    protected Set<MazePassage> traverse(MazeComponent<C> maze, MazeComponent<C> component, Set<MazePassage> traversed, final Collection<MazePassage> goal, Consumer<MazePassage> goalConsumer)
    {
        return traverse(Arrays.asList(maze, component), traversed, true, Sets.intersection(component.exits().keySet(), traversed), traverser, connection -> {
            if (goal.contains(connection))
                goalConsumer.accept(connection);

            return true;
        });
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        unplace(maze, component, false);
    }

    protected void unplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component, boolean simulate)
    {
        stepsReached.transformValues(i -> i - 1);
        stepsReached.retainEntries((a, i) -> i >= 0);

        if (stepsReached.size() < connectionPoints.size())
        {
            mainConnectionPoint.reverseStep();

            for (ConnectionPoint point : connectionPoints)
            {
                if (point.order.size() > mainConnectionPoint.order.size())
                    point.reverseStep();
            }
        }
    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }

    private static class ConnectionPoint
    {
        public final Set<MazePassage> traversed = new HashSet<>();
        public final List<Set<MazePassage>> order = new ArrayList<>();

        @SafeVarargs
        public ConnectionPoint(Collection<MazePassage>... points)
        {
            for (Collection<MazePassage> point : points)
                traversed.addAll(point);
        }

        public void reverseStep()
        {
            traversed.removeAll(order.remove(order.size() - 1));
        }
    }
}
