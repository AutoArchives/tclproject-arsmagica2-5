/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package am2.worldgen.smartgen.reccomplexutils;

/**
 * Created by lukas on 12.03.15.
 */
public class RCBoolAlgebra
{
    private static Algebra<Boolean> algebra;

    public static Algebra<Boolean> algebra()
    {
        return algebra != null ? algebra : (algebra = new Algebra<>(
                BoolAlgebra.parentheses("(", ")"),
                BoolAlgebra.or("|"), BoolAlgebra.and("&"),
                BoolAlgebra.not("!")
        ));
    }
}
