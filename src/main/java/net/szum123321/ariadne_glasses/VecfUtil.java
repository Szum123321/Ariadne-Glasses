package net.szum123321.ariadne_glasses;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3d;

public class VecfUtil {
    /*MAGIK*/
    public static BlockPos round(Vec3d in) {
        return new BlockPos((int) Math.round(in.getX()), (int) Math.round(in.getY()), (int) Math.round(in.getZ()));
    }

    public static double distanceSquared(Vec3d a, Vec3d b) {
        double dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), dz = b.getZ() - a.getZ();

        return dx*dx + dy*dy + dz*dz;
    }

    public static void writeToByteBuf(Vec3d v, PacketByteBuf buf) {
        buf.writeDouble(v.getX());
        buf.writeDouble(v.getY());
        buf.writeDouble(v.getZ());
    }

    public static Vec3d readFromByteBuf(PacketByteBuf buf) {
        double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
        return new Vec3d(x, y, z);
    }
}
