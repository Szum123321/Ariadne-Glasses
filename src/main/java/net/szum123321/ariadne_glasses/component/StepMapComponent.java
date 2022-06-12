package net.szum123321.ariadne_glasses.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3f;
import net.szum123321.ariadne_glasses.AriadneGlasses;
import net.szum123321.ariadne_glasses.VecfUtil;
import net.szum123321.ariadne_glasses.data.Octreee;

import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

/*
    This class keep track of all the data for the given player
    The list of steps is stored in stepList as Vec3f, while stepMap quickly finds repeats and does client-side occlusion
 */
public class StepMapComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    private final static int fullSyncInterval = 1200; //ticks

    private final PlayerEntity player;

    private Octreee stepMap = Octreee.newOctree();
    private final List<Vec3f> stepList = new Vector<>();

    private int popIndex = -1;
    private boolean doFullSync = true;
    private int fullSyncCountdown = fullSyncInterval;
    private boolean clean = false;

    public StepMapComponent(PlayerEntity player) {
        this.player = player;
    }

    public Octreee getStepMap() {
        return stepMap;
    }

    public List<Vec3f> getStepList() {
        return stepList;
    }

    public void setDoFullSync() {
        doFullSync = true;
    }

    public void clear() {
        if(clean) return;

        clean = true;
        stepMap = Octreee.newOctree();
        stepList.clear();
        popIndex = -1;
        doFullSync = true;

        AriadneGlasses.STEP_MAP.sync(player);
    }

    @Override
    public void serverTick() {
        if(!player.getInventory().getArmorStack(3).isOf(AriadneGlasses.GLASSES)) {
            //TODO: here, no need to call all the time
            if(!clean) clear();
            return;
        }

        clean = false;

        if (--fullSyncCountdown <= 0) {
            fullSyncCountdown = fullSyncInterval;
            doFullSync = true;
        }

        Vec3f pos = VecfUtil.cast(player.getPos());
        Vec3f lastPos = (stepList.size() > 0 ? stepList.get(stepList.size() - 1): new Vec3f(0, -100000, 0));

        if ( VecfUtil.distanceSquared(lastPos, pos) >= squared(AriadneGlasses.STEP_RADIUS.get()) ) {
            stepMap.query(pos, AriadneGlasses.STEP_RADIUS.get())
                    .filter(i -> i < (stepList.size() - 2))
                    .reduce(Math::min)
                    .ifPresentOrElse(v -> {
                        for (int i = stepList.size() - 1; i > v; i--) stepMap.remove(stepList.remove(i));
                        popIndex = Math.min(popIndex, v);
                    }, () -> {
                        stepMap.insert(pos, stepList.size());
                        stepList.add(pos);
                    });

            AriadneGlasses.STEP_MAP.sync(player);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        if(!recipient.equals(player)) return;

        if(doFullSync) {
            buf.writeByte(SyncPacketType.COMPLETE.getID());

            buf.writeVarInt(stepList.size());
            for(Vec3f i: stepList) VecfUtil.writeToByteBuf(i, buf);
            doFullSync = false;
        } else {
            buf.writeByte(SyncPacketType.PARTIAL.getID());

            //Drop all the points greater then popIndex
            buf.writeVarInt(popIndex);

            //We'll push this many
            buf.writeVarInt(Math.max(0, stepList.size() - popIndex - 1));

            for (int i = popIndex + 1; i < stepList.size(); i++) VecfUtil.writeToByteBuf(stepList.get(i), buf);

            popIndex = stepList.size() - 1;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void applySyncPacket(PacketByteBuf buf) {
        switch (SyncPacketType.fromByte(buf.readByte())) {
            case PARTIAL -> {
                int k = buf.readVarInt();
                int n = buf.readVarInt();

                for (int i = stepList.size() - 1; i > k; i--) stepMap.remove(stepList.remove(i));

                for (int i = 0; i < n; i++) {
                    Vec3f pos = VecfUtil.readFromByteBuf(buf);
                    stepMap.insert(pos, stepList.size());
                    stepList.add(pos);
                }
            }

            case COMPLETE -> {
                stepList.clear();
                stepMap = Octreee.newOctree();

                int n = buf.readVarInt();

                for(int i = 0; i < n; i++) {
                    stepList.add(VecfUtil.readFromByteBuf(buf));
                    stepMap.insert(stepList.get(stepList.size() - 1), i);
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        int[] arr = stepList.stream()
                .flatMap(v -> Stream.of(v.getX(), v.getY(), v.getZ()))
                .mapToInt(Float::floatToIntBits)
                .toArray();

        tag.put("stepList", new NbtIntArray(arr));
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        int[] arr = tag.getIntArray("stepList");

        stepList.clear();

        for(int i = 0; i < arr.length / 3; i++) {
            stepList.add(new Vec3f(
                    Float.intBitsToFloat(arr[3*i]),
                    Float.intBitsToFloat(arr[3*i + 1]),
                    Float.intBitsToFloat(arr[3*i + 2])
            ));
        }

        stepMap = Octreee.newOctree();
        for(int i = 0; i < stepList.size(); i++) stepMap.insert(stepList.get(i), i);
    }


    private static int squared(int n) {
        return n*n;
    }

    private enum SyncPacketType {
        PARTIAL(1),
        COMPLETE(2),
        HASH_SYNC(3),
        NULL(0);

        private final byte type;

        SyncPacketType(int type) {
            this.type = (byte) type;
        }

        public byte getID() {
            return type;
        }

        public static SyncPacketType fromByte(byte n) {
            return switch (n) {
                case 1 -> PARTIAL;
                case 2 -> COMPLETE;
                default -> NULL;
            };
        }
    }
}
