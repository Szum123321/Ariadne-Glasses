package net.szum123321.ariadne_glasses.data;

import net.minecraft.util.math.BlockPos;

import java.util.stream.IntStream;

/**
 * Relatively simple Octree(https://en.wikipedia.org/wiki/Octree) implementation optimized for low memory footprint
 */
class LinkedOctree implements Octreee {
    private Cuboid baseCuboid = null;
    private Node root = null;

    @Override
    synchronized public void insert(BlockPos pos, int id) {
        if(root == null) _init(pos, id);
        expandBaseCuboid(pos);

        _insert(root, baseCuboid.copy(), pos, id);
    }

    @Override
    public IntStream query(BlockPos pos, double radius) {
        IntStream.Builder builder = IntStream.builder();

        if(root != null && baseCuboid.intersectsSphere(pos, radius)) _queryStream(builder, root, baseCuboid.copy(), pos, radius);

        return builder.build();
    }

    @Override
    synchronized public void remove(BlockPos pos) {
        if(root != null && baseCuboid.within(pos)) _remove(root, baseCuboid.copy(), pos);

        shrinkBaseCuboid();
    }

    //recursively go down until cuboid is reduces to singular point
    private void _insert(Node n, Cuboid cub, BlockPos p, int val) {
        if(cub.singular()) {
            n.value = val;
            return;
        }

        byte mask = cub.getMaskForPoint(p);
        if(n.sons[mask] == null) n.sons[mask] = new Node();

        cub.setSub(mask);

        _insert(n.sons[mask], cub, p, val);
    }

    private void _queryStream(IntStream.Builder builder, Node n, Cuboid cub, BlockPos p, double r) {
        if(cub.singular()) {
            if (n.value != -1) builder.add(n.value);
            return;
        }

        for(byte i = 0; i < 8; i++) {
            if(n.sons[i] != null) {
                Cuboid sc = cub.getSub(i);

                if(sc.intersectsSphere(p, r)) _queryStream(builder, n.sons[i], sc, p, r);
            }
        }
    }

    private boolean _remove(Node n, Cuboid cub, BlockPos p) {
        if(cub.singular()) return true;

        byte mask = cub.getMaskForPoint(p);
        if(n.sons[mask] != null) {
            cub.setSub(mask);

            if(cub.within(p) && _remove(n.sons[mask], cub, p)) {
                n.sons[mask] = null;

                return n.getSons() == 0;
            }
        }

        return false;
    }

    private void _init(BlockPos pos, int id) {
        baseCuboid = new Cuboid(pos.mutableCopy(), pos.mutableCopy());
        root = new Node();
        root.value = id;
    }

    //Keep the tree as short a possible by slicing off the root as long as it has exactly a single son
    synchronized private void shrinkBaseCuboid() {
        while(true) {
            int n = 0, id = -1;

            for (int i = 0; i < 8; i++) {
                if (root.sons[i] != null) {
                    n++;
                    id = i;
                }
            }

            if(n == 1) {
                //baseCuboid = baseCuboid.getSub((byte)id);
                baseCuboid.setSub((byte)id);
                root = root.sons[id];
            } else {
                break;
            }
        }
    }

    //As long as the new point doesn't fit within the base cuboid, expand it in the direction of the point
    synchronized private void expandBaseCuboid(BlockPos pos) {
        while(!baseCuboid.within(pos)) {
            BlockPos mid = baseCuboid.getCenterPoint();

            byte mask = 0;

            int minX, minY, minZ, maxX, maxY, maxZ;
            int dx, dy, dz;
            dx = baseCuboid.maxPos().getX() - baseCuboid.minPos().getX() + 1;
            dy = baseCuboid.maxPos().getY() - baseCuboid.minPos().getY() + 1;
            dz = baseCuboid.maxPos().getZ() - baseCuboid.minPos().getZ() + 1;

            if(mid.getX() < pos.getX()) {
                minX = baseCuboid.minPos().getX();
                maxX = baseCuboid.maxPos().getX() + dx;
            } else {
                mask |= 1;
                minX = baseCuboid.minPos().getX() - dx;
                maxX = baseCuboid.maxPos().getX();
            }

            if(mid.getY() < pos.getY()) {
                minY = baseCuboid.minPos().getY();
                maxY = baseCuboid.maxPos().getY() + dy;
            } else {
                mask |= 2;
                minY = baseCuboid.minPos().getY() - dy;
                maxY = baseCuboid.maxPos().getY();
            }

            if(mid.getZ() < pos.getZ()) {
                minZ = baseCuboid.minPos().getZ();
                maxZ = baseCuboid.maxPos().getZ() + dz;
            } else {
                mask |= 4;
                minZ = baseCuboid.minPos().getZ() - dz;
                maxZ = baseCuboid.maxPos().getZ();
            }

            Node newRoot = new Node();

            newRoot.sons[mask] = root;
            root = newRoot;
            baseCuboid.update(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    //Structure representing portion of 3D space
    record Cuboid(BlockPos.Mutable minPos, BlockPos.Mutable maxPos) {

        void update(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            minPos.set(minX, minY, minZ);
            maxPos.set(maxX, maxY, maxZ);
        }

        public boolean singular() {
            return minPos.equals(maxPos());
        }

        public boolean within(BlockPos p) {
            return (minPos.getX() <= p.getX() && p.getX() <= maxPos.getX()) &&
                    (minPos.getY() <= p.getY() && p.getY() <= maxPos.getY()) &&
                    (minPos.getZ() <= p.getZ() && p.getZ() <= maxPos().getZ());
        }

        boolean intersectsSphere(BlockPos p, double r) {
            if (within(p)) return true;
            r *= r;

            if (p.getX() < minPos().getX()) r -= squared(p.getX() - minPos().getX());
            else if (p.getX() > maxPos().getX()) r -= squared(p.getX() - maxPos().getX());

            if (p.getY() < minPos().getY()) r -= squared(p.getY() - minPos().getY());
            else if (p.getY() > maxPos().getY()) r -= squared(p.getY() - maxPos().getY());

            if (p.getZ() < minPos().getZ()) r -= squared(p.getZ() - minPos().getZ());
            else if (p.getZ() > maxPos().getZ()) r -= squared(p.getZ() - maxPos().getZ());

            return r >= 0;
        }

        //Returns integer round-down center point of the cuboid
        public BlockPos getCenterPoint() {
            return new BlockPos(
                    div_2_floor(minPos().getX() + maxPos().getX()),
                    div_2_floor(minPos().getY() + maxPos().getY()),
                    div_2_floor(minPos().getZ() + maxPos().getZ())
            );
        }

        //Returns a mask for the given point
        //Assumes the point lies within the cuboid
        public byte getMaskForPoint(BlockPos p) {
            BlockPos mid = getCenterPoint();

            byte mask = 0;
            if (p.getX() > mid.getX()) mask |= 1;
            if (p.getY() > mid.getY()) mask |= 2;
            if (p.getZ() > mid.getZ()) mask |= 4;

            return mask;
        }

        //Return a new Cuboid described by the mask
        public Cuboid getSub(byte mask) {
            BlockPos m = getCenterPoint();

            int minX, minY, minZ, maxX, maxY, maxZ;

            if ((mask & 1) != 0) {
                minX = m.getX() + 1;
                maxX = maxPos().getX();
            } else {
                minX = minPos().getX();
                maxX = m.getX();
            }

            if ((mask & 2) != 0) {
                minY = m.getY() + 1;
                maxY = maxPos().getY();
            } else {
                minY = minPos().getY();
                maxY = m.getY();
            }

            if ((mask & 4) != 0) {
                minZ = m.getZ() + 1;
                maxZ = maxPos().getZ();
            } else {
                minZ = minPos().getZ();
                maxZ = m.getZ();
            }

            return new Cuboid(new BlockPos.Mutable(minX, minY, minZ), new BlockPos.Mutable(maxX, maxY, maxZ));
        }

        public void setSub(byte mask) {
            BlockPos m = getCenterPoint();

            int minX, minY, minZ, maxX, maxY, maxZ;

            if ((mask & 1) != 0) {
                minX = m.getX() + 1;
                maxX = maxPos().getX();
            } else {
                minX = minPos().getX();
                maxX = m.getX();
            }

            if ((mask & 2) != 0) {
                minY = m.getY() + 1;
                maxY = maxPos().getY();
            } else {
                minY = minPos().getY();
                maxY = m.getY();
            }

            if ((mask & 4) != 0) {
                minZ = m.getZ() + 1;
                maxZ = maxPos().getZ();
            } else {
                minZ = minPos().getZ();
                maxZ = m.getZ();
            }

            minPos.set(minX, minY, minZ);
            maxPos.set(maxX, maxY, maxZ);
        }

        public Cuboid copy() {
            return new Cuboid(minPos.mutableCopy(), maxPos.mutableCopy());
        }
    }

    private static class Node {
        Node[] sons = {null, null, null, null, null, null, null, null};
        int value = -1;

        public int getSons() {
            int r = 0;
            for(int i = 0; i < 8; i++) if(sons[i] != null) r++;
            return r;
        }
    }

    /*
        Math helper functions
     */

    //  3 / 2 = 1
    // -3 / 2 = -2
    private static int div_2_floor(int n) {
        if(n >= 0) return n >> 1;
        else {
            return (n / 2) - (n & 1);
        }
    }

    private static int squared(int n) {
        return n*n;
    }

}
