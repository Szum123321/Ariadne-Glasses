package net.szum123321.ariadne_glasses;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class VecfUtil {
    /*MAGIK*/
    public static BlockPos round(Vec3f in) {
        return new BlockPos(Math.round(in.getX()), Math.round(in.getY()), Math.round(in.getZ()));
    }

    public static Vec3f cast(Vec3d in) {
        return new Vec3f((float) in.x, (float) in.y, (float) in.z);
    }

    public static float distanceSquared(Vec3f a, Vec3f b) {
        float dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), dz = b.getZ() - a.getZ();

        return dx*dx + dy*dy + dz*dz;
    }

    public static void writeToByteBuf(Vec3f v, PacketByteBuf buf) {
        buf.writeFloat(v.getX());
        buf.writeFloat(v.getY());
        buf.writeFloat(v.getZ());
    }

    public static Vec3f readFromByteBuf(PacketByteBuf buf) {
        float x = buf.readFloat(), y = buf.readFloat(), z = buf.readFloat();
        return new Vec3f(x, y, z);
    }
}
