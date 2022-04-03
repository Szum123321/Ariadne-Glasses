package net.szum123321.ariadne_glasses.data;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

class OctreeImplTest {
    @Test
    void test_oct_tree() {
        Octreee octreee = Octreee.newOctree();

        Scanner scanner = new Scanner(System.in);
        int command = scanner.nextInt();
        int cnt = 0;

        while (true) {
            if(command == 1) { //query
                BlockPos p = readBlockPod(scanner);
                int r  = scanner.nextInt();

                System.out.print("Found: ");
                octreee.query(p, r).forEach(i -> System.out.printf("%d ", i));
                System.out.println();
            } else if(command == 2) { // push
                BlockPos p = readBlockPod(scanner);
                octreee.insert(p, cnt++);
            } else if (command == 3) {
                BlockPos p = readBlockPod(scanner);
                octreee.remove(p);
            }
        }
    }

    BlockPos readBlockPod(Scanner scanner) {
        int x, y, z;
        x = scanner.nextInt();
        y = scanner.nextInt();
        z = scanner.nextInt();

        return new BlockPos(x, y, z);
    }
}