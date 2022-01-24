package comms;

import battlecode.common.*;

import java.util.Random;

public class MiscFuncs {
    static int turnCount = 0;
    /*
    SHARED ARRAY TABLE OF CONTENTS
    0 - 3 Archon locs
    4 - 7 Archon build counts


     */

    // WRITE  ARCHONS LOCS AND GUESS ENEMIES

    // include this near the turn count
    static int archon_id = 4;

    static void write_archon_locs(RobotController rc, int turns) throws GameActionException {
        MapLocation loc = rc.getLocation();
        if (turns > 4) {
            return;
        }
        if ((rc.readSharedArray(0) & 0b111111111111) == 0) {
            rc.writeSharedArray(0, (rc.getID() & 0b1111) + ((loc.x >> 2) << 4) + ((loc.y >> 2) << 8));
            archon_id = 0;
        }
        else if ((rc.readSharedArray(1) & 0b111111111111) == 0) {
            rc.writeSharedArray(1, (rc.getID() & 0b1111) + ((loc.x >> 2) << 4) + ((loc.y >> 2) << 8));
            archon_id = 1;
        }
        else if ((rc.readSharedArray(2) & 0b111111111111) == 0) {
            rc.writeSharedArray(2, (rc.getID() & 0b1111) + ((loc.x >> 2) << 4) + ((loc.y >> 2) << 8));
            archon_id = 2;
        }
        else {
            rc.writeSharedArray(3, (rc.getID() & 0b1111) + ((loc.x >> 2) << 4) + ((loc.y >> 2) << 8));
            archon_id = 3;
        }
    }

    // 0 - 3
    static MapLocation getArchonPos(RobotController rc, int num) throws GameActionException {
        int pos = rc.readSharedArray(num);
        return new MapLocation(((pos & 0b11110000) >> 2 + 2),  ((pos & 0b111100000000) >> 6 + 2));
        // slightly hacky version to use that saves 2 bytecodes at the expense of a marginal amount of accuracy
        // return new MapLocation(((pos & 0b11111000) >> 2),  ((pos & 0b111110000000) >> 6));
    }

    static MapLocation guess_location(RobotController rc, int num, String guess_type) throws GameActionException {

        int max_x = rc.getMapWidth();
        int max_y = rc.getMapHeight();

        MapLocation reference_location =  getArchonPos(rc, num);
        int ref_x = reference_location.x;
        int ref_y = reference_location.y;

        if (guess_type.equals("rotational")) {
            MapLocation guess = new MapLocation(max_x-ref_x, max_y-ref_y);
            return guess;
        }

        else if (guess_type.equals("vertical")) {
            MapLocation guess = new MapLocation(max_x-ref_x, ref_y);
            return guess;
        }

        else { //horizontal
            MapLocation guess = new MapLocation(ref_x, max_y-ref_y);
            return guess;
        }
    }

    // COUNT SOLDIERS AND MINERS

    // include these near the turn count
    // whenever you build a miner / soldier simply add a soldiers++; or miners++; afterwards
    static int soldiers = 0;
    static int miners = 0;

    static void updateSoldierMiner(RobotController rc) throws GameActionException{
        if ((turnCount ^ rc.getID()) % 8 == 0) {
            if (archon_id != 4) {
                rc.writeSharedArray(4 + archon_id, Math.min(0b11111111, soldiers) + (Math.min(0b11111111, miners) << 8));
            }
        }
    }

    // MapLocation.x is soldiers MapLocation.y is Miners
    static MapLocation downloadSoldierMiner(RobotController rc, int num) throws GameActionException{
        int data = rc.readSharedArray(4 + num);
        return new MapLocation(data & 0b11111111, data >> 8);
    }
}
