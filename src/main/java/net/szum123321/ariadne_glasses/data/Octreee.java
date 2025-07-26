package net.szum123321.ariadne_glasses.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.szum123321.ariadne_glasses.VecfUtil;

import java.util.stream.IntStream;

public interface Octreee {
    void insert(BlockPos pos, int id);
    void remove(BlockPos pos);

    IntStream query(BlockPos pos, double radius);

    default void insert(Vec3d pos, int id) {
        insert(VecfUtil.round(pos), id);
    }

    default IntStream query(Vec3d pos, double radius) {
        return query(VecfUtil.round(pos), radius);
    }

    default void remove(Vec3d pos) {
        remove(VecfUtil.round(pos));
    }

    static Octreee newOctree() {
        return new OctreeImpl();
    }
}
