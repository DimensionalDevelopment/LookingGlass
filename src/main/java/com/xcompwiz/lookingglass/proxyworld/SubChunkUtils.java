package com.xcompwiz.lookingglass.proxyworld;

import net.minecraft.util.math.BlockPos;

public final class SubChunkUtils {
    public static boolean withinDistance(BlockPos c1, int x, int y, int z, int distance) {
        return distance * distance >= c1.distanceSq(x, y, z);
    }

    public static boolean withinDistance(int x, int y, int z, int x2, int y2, int z2, int distance) {
        int x3 = x - x2;
        int y3 = y - y2;
        int z3 = z - z2;
        return distance * distance >= x3 * x3 + y3 * y3 + z3 * z3;
    }

    public static boolean withinRange(BlockPos c1, int x, int y, int z, int d1, int d2) {
        double cDistance = c1.distanceSq(x, y, z);
        return d2 * d2 >= cDistance && d1 * d1 <= cDistance;
    }

    public static boolean withinDistance2D(int x, int z, int x2, int z2, int distance) {
        int x3 = x - x2;
        int z3 = z - z2;
        int distance2 = x3 * x3 + z3 * z3;
        return distance * distance >= distance2;
    }

    public static boolean withinRange2D(int x, int z, int x2, int z2, int d1, int d2) {
        int x3 = x - x2;
        int z3 = z - z2;
        int distance = x3 * x3 + z3 * z3;
        return d2 * d2 >= distance && d1 * d1 <= distance;
    }
}
