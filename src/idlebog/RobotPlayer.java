package idlebog;

import battlecode.common.*;
import java.util.Random;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
                rc.setIndicatorString("Idlebog");
                Clock.yield();
        }
    }
}
