package strikeforce0;

import battlecode.common.*;
import com.sun.glass.ui.Robot;

import java.awt.*;
import java.util.Map;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /**
     * Array containing all the possible movement directions.
     */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final Direction[] cardinalDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };

    static final Direction[] cross_map_x = {
            Direction.SOUTHWEST,
            Direction.NORTH,
            Direction.SOUTHEAST,
            Direction.WEST,
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")


    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:
                        runArchon(rc);
                        break;
                    case MINER:
                        runMiner(rc);
                        break;
                    case SOLDIER:
                        runSoldier(rc);
                        break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                    case SAGE:
                        break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
                rc.resign();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */

    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rng.nextBoolean()) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
                rc.writeSharedArray(6, rc.readSharedArray(6) + 1);
            }
        } else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
            }
        }
    }
    /*
    !!!!!!MESSAGE TO THADDAEUS!!!!!!!!
    USE THIS CODE WHENEVER YOU BUILD A ROBOT OF THE TYPE
    IE, ONCE THE ARCHON HAS CODE FOR BUILDING BUILDERS MAKE SURE IT CALLS
    "rc.writeSharedArray(7, rc.readSharedArray(7) + 1);" AFTER BUILDING THE BUILDER

    rc.buildRobot(RobotType.BUILDER, dir);
    rc.writeSharedArray(7, rc.readSharedArray(7) + 1);

    rc.buildRobot(RobotType.SAGE, dir);
    rc.writeSharedArray(7, rc.readSharedArray(7) + 0b1000000);

    rc.buildRobot(RobotType.WATCHTOWER, dir);
    rc.writeSharedArray(8, rc.readSharedArray(8) + 1);

    rc.buildRobot(RobotType.LABORATORY, dir);
    rc.writeSharedArray(8, rc.readSharedArray(8) + 0b10000);
    */
    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */

    static void runMiner(RobotController rc) throws GameActionException {


        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }

    /**
     FIX SOLDIER ROUTINE!!! ONLY CHECKS FIRST ARCHON LOCATION!!!!!
     Attack buildings before droids while on offense??
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        MapLocation start_loc = rc.getLocation();
        // downloadWID: {actual x, actual y, actual time, mode, arcID}
        if (rc.readSharedArray(60) != 0) { // if there's info about archon in shared array
            System.out.println("FOUND ARCHON. DOWNLOADING INFO.");
            int [] archon_info = downloadWID(rc, 0);
            int archon_id = archon_info[4];
            MapLocation read_archon_loc = new MapLocation(archon_info[0], archon_info[1]);
            System.out.println("IT'S LOCATED AT " +read_archon_loc+"!");
            if (rc.canSenseRobot(archon_id)) {
                RobotInfo local_archon_info = rc.senseRobot(archon_id);
                MapLocation archon_loc = local_archon_info.getLocation();
                if (rc.canAttack(archon_loc)) {
                    rc.attack(archon_loc);
                    System.out.println("Attacked an ARCHON!");
                }
            }
            else {
                Direction dir_to_archon = start_loc.directionTo(read_archon_loc);
                if (fuzzyGoTo(rc, dir_to_archon)) {
                    System.out.println("MOVED TOWARDS ARCHON!");
                }
            }
        }

        else { // couldn't find archon so other things will happen

            int attack_id = -1;
            int lowest_health = 2000;
            int out_of_range_id = -1;
            int out_of_range_dist = 25;

            for (RobotInfo robot_test : rc.senseNearbyRobots(20, rc.getTeam().opponent())) {
                int dist_to_test = start_loc.distanceSquaredTo(robot_test.getLocation());
                if (dist_to_test<=13) {
                    if (robot_test.getHealth()<lowest_health) {
                        attack_id = robot_test.getID();
                        lowest_health = robot_test.getHealth();
                    }
                }

                else {
                    if (dist_to_test<out_of_range_dist) {
                        out_of_range_id = robot_test.getID();
                        out_of_range_dist = dist_to_test;
                    }
                }
            }

            // maybe loop thru full vision radius, and if it couldn't attack the closest enemy move towards it if archon loc is unknown
            if (attack_id!=-1) {
                MapLocation to_attack = rc.senseRobot(attack_id).getLocation();
                if (rc.canAttack(to_attack)) {
                    rc.attack(to_attack);
                    System.out.println("ATTACKED A ROBOT!");
                }

            }

            else {
                if (out_of_range_id != -1) {
                    if (rc.canSenseRobot(out_of_range_id)) {
                        if (fuzzyGoTo(rc, start_loc.directionTo(rc.senseRobot(out_of_range_id).getLocation()))) {
                            System.out.println("MOVED TOWARDS ENEMY!");
                        }
                    }

                } else {

                    RobotInfo [] local_robots = rc.senseNearbyRobots(20, rc.getTeam());

                    boolean moved_from_friendly_archon = false;
                    for (int i = 0; i<local_robots.length; i++) {
                        if (local_robots[i].getType().equals(RobotType.ARCHON)) {
                            Direction move_dir = start_loc.directionTo(local_robots[i].getLocation()).opposite();
                            if (fuzzyGoTo(rc, move_dir)) {
                                moved_from_friendly_archon = true;
                                System.out.println("MOVED FROM HQ!");
                            }
                        }
                    }

                    if (!moved_from_friendly_archon) {
                        if (fan_out(rc)) {
                            System.out.println("FANNED OUT!");
                        }
                    }

                }
            }
        }

        System.out.println("HAD ENOUGH BYTECODES LEFT TO SCAN");
        generalScout(rc);
        System.out.println("SCANNED FOR ARCHON");

    }

    /*my funcs*/

    /* fan out ... can get written more efficiently lol*/
    // perhaps to make it more efficient: if a test robot dist^2 is < 8, break out of loop and move in opposite direction to it
    static boolean fan_out(RobotController rc) throws  GameActionException {
        boolean moved = false;
        Direction dir_to_move = Direction.CENTER;
        boolean mean_was_center = false;

        int x_sum = 0;
        int y_sum = 0;
        int counter = 0;
        RobotInfo [] nearby_robots = rc.senseNearbyRobots();

        for (int i=1; i<nearby_robots.length; i++) {
            RobotInfo nearby_bot = nearby_robots[i];
            if (nearby_bot.getType().equals(RobotType.ARCHON) && nearby_bot.getTeam().isPlayer()) {
                for (int j = 1; j<nearby_robots.length + 1 && j<6; j++) {
                    MapLocation nearby_robot_loc = nearby_robots[j-1].getLocation();
                    x_sum += nearby_robot_loc.x;
                    y_sum += nearby_robot_loc.y;
                    counter++;
                }
                break;
            }
        }

        if (counter!=0) {
            int x_mean = x_sum / counter;
            int y_mean = y_sum / counter;
            System.out.println("TESTTEST:" + x_mean+","+y_mean);
            MapLocation mean_loc = new MapLocation(x_mean, y_mean);
            dir_to_move = rc.getLocation().directionTo(mean_loc).opposite();
            System.out.println(dir_to_move);
            if (dir_to_move!=Direction.CENTER) {
                if (fuzzyGoTo(rc, dir_to_move)) {
                    System.out.println("MOVED AWAY FROM ALLIES");
                    moved=true;
                }
            }

            else {
                mean_was_center=true;
                System.out.println("MEAN WAS CENTER");
            }
        }

        else {
            for (int i = 0; i<7; i++) {
                dir_to_move = cross_map_x[i];
                if (fuzzyGoTo(rc, dir_to_move)) {
                    System.out.println("I moved intentionally!");
                    moved = true;
                    break;
                }
            }
        }

        if (!mean_was_center&&!moved) {
            for (int i = 0; i<7; i++) {
                dir_to_move = cross_map_x[i];
                if (fuzzyGoTo(rc, dir_to_move)) {
                    System.out.println("I moved intentionally!");
                    moved = true;
                    break;
                }
            }
        }
        return moved;
    }

    /* try to move randomly */
    static boolean moveRandomly(RobotController rc) throws GameActionException {
        boolean moved = false;
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        return moved;
    }

    /* return random dir */
    static Direction randomDir(RobotController rc) {
        Direction dir = directions[rng.nextInt(directions.length)];
        return dir;
    }

    /* fuzzy. if it can't move in given direction, try to move in closest angle dirs to desired one */
    /* rip bytecodes :YEET: */
    static boolean fuzzyGoTo(RobotController rc, Direction desired_dir) throws  GameActionException {
        boolean moved = false;
        Direction attempt_dir_left = desired_dir;
        Direction attempt_dir_right = desired_dir;
        firstloop:
        for (int n=0; n<5; n++) {
            for (int i = n; i<=n; i++) {
                attempt_dir_left.rotateLeft();
                attempt_dir_right.rotateRight();
            }

            if (rc.canMove(attempt_dir_right)) {
                rc.move(attempt_dir_right);
                moved = true;
                break firstloop;
            }

            else if (rc.canMove(attempt_dir_left)) {
                rc.move(attempt_dir_left);
                moved = true;
                break firstloop;
            }
        }
        return moved;
    }


    /* COMS STUFF FROM DISCORD 1/8 2022 */
    /**
     * x Use the actual x location
     * y Use the actual y location
     * time Use the actual time (getRoundNum())
     * mode 0 for turret / prototype 1 for portable
     * arcNum 0 through 3
     */

    static void uploadArchon(RobotController rc, int x, int y, int time, int mode, int arcNum) throws  GameActionException{
        rc.writeSharedArray(0 + arcNum, (x >> 2) + ((y >> 2) << 4) + ((time>>7) << 8) + (mode << 12));
    }

    static void scoutArchon(RobotController rc, RobotInfo[] nearby, int arc4)  throws  GameActionException{
        int arc1 = rc.readSharedArray(60);
        int arc2 = rc.readSharedArray(61);
        int arc3 = rc.readSharedArray(62);

        for (RobotInfo robotInfo : nearby) {
            if (robotInfo.getType().equals(RobotType.ARCHON)) {
                if (robotInfo.getID() != arc1 && robotInfo.getID() != arc2 && robotInfo.getID() != arc3 && robotInfo.getID() != arc4) {
                    if (arc1 == 0) {
                        uploadArchon(rc, robotInfo.getLocation().x, robotInfo.getLocation().y, rc.getRoundNum(), (robotInfo.getMode().equals(RobotMode.PORTABLE) ? 1 : 0), 0, robotInfo.getID());
                    } else if (arc2 == 0) {
                        uploadArchon(rc, robotInfo.getLocation().x, robotInfo.getLocation().y, rc.getRoundNum(), (robotInfo.getMode().equals(RobotMode.PORTABLE) ? 1 : 0), 0, robotInfo.getID());
                    } else if (arc3 == 0) {
                        uploadArchon(rc, robotInfo.getLocation().x, robotInfo.getLocation().y, rc.getRoundNum(), (robotInfo.getMode().equals(RobotMode.PORTABLE) ? 1 : 0), 0, robotInfo.getID());
                    } else {
                        uploadArchon(rc, robotInfo.getLocation().x, robotInfo.getLocation().y, rc.getRoundNum(), (robotInfo.getMode().equals(RobotMode.PORTABLE) ? 1 : 0), 0, robotInfo.getID());
                    }
                }
            }
        }
    }


    // idk if we want to use 13 bytes on storing an id, probably good for now
    static void uploadArchon(RobotController rc, int x, int y, int time, int mode, int arcNum, int id) throws  GameActionException{
        rc.writeSharedArray(0 + arcNum, (x >> 2) + ((y >> 2) << 4) + ((time>>7) << 8) + (mode << 12));
        rc.writeSharedArray(60 + arcNum, id);
    }

    /**
     * arcNum (0-3) which arc you want
     * return int[] thats {actual x, actual y, actual time, mode}
     */
    static int[] downloadArchon(RobotController rc, int arcNum)  throws  GameActionException{
        int val = rc.readSharedArray(arcNum);

        return new int[]{(val & 0b1111) << 2, (val & 0b11110000) >> 2, (val & 0b111100000000) >> 1, (val >> 12) & 0b1};
    }

    /**
     * arcNum (0-3) which arc you want
     * return int[] thats {actual x, actual y, actual time, mode, arcID}
     */
    static int[] downloadWID(RobotController rc, int arcNum)  throws  GameActionException{
        int val = rc.readSharedArray(arcNum);
        return new int[]{(val & 0b1111) << 2, (val & 0b11110000) >> 2, (val & 0b111100000000) >> 1, (val >> 12) & 0b1, rc.readSharedArray(60 + arcNum)};
    }

    /**
     * x Use the actual x location
     * y Use the actual y location
     * sNum 0 through 3
     */
    static void uploadSoldier(RobotController rc, int x, int y, int sNum) throws  GameActionException{
        int ind = 4 + (sNum & 0b1);
        rc.writeSharedArray(ind, (rc.readSharedArray(ind) & (0b11111111 << (8 & (-(sNum >> 1))))) + (((x >> 2) + ((y >> 2) << 4)) << ((1 - (sNum >> 1)) << 3)));
    }

    /**
     * x Use the actual x location
     * y Use the actual y location
     */
    static int[] downloadSoldier(RobotController rc, int sNum) throws  GameActionException{
        int val = (rc.readSharedArray(4 + (sNum & 0b1)) >> ((1 - (sNum >> 1)) << 3)) & 0b11111111;
        return new int[]{(val & 0b1111) << 2, (val & 0b11110000) >> 2};
    }

    static void scoutSoldier(RobotController rc, RobotInfo[] nearby, int s24)  throws  GameActionException{
        int s13 = rc.readSharedArray(4);

        int s1 = s13 & 0b11111111;
        int s3 = s13 >> 8;
        int s2 = s24 & 0b11111111;
        int s4 = s24 >> 8;
        int robotx;
        int roboty;
        int robotCombined;
        for (RobotInfo robotInfo : nearby) {
            if (robotInfo.getType().equals(RobotType.SOLDIER)) {
                robotx = robotInfo.getLocation().x;
                roboty = robotInfo.getLocation().y;
                robotCombined = ((robotx >> 2) + ((roboty >> 2) << 4));
                if (robotCombined != s1 && robotCombined != s2 && robotCombined != s3 && robotCombined != s4) {
                    if (s1 == 0) {
                        uploadSoldier(rc, robotx, roboty, 0);
                    } else if (s2 == 0) {
                        uploadSoldier(rc, robotx, roboty, 1);
                    } else if (s3 == 0) {
                        uploadSoldier(rc, robotx, roboty, 2);
                    } else {
                        uploadSoldier(rc, robotx, roboty, 3);
                    }
                }
            }
        }
    }

    static void generalScout(RobotController rc)  throws  GameActionException{
        if (((rc.getRoundNum() ^ rc.getID()) & 0b11) != 0) {
            return;
        }
        RobotInfo[] nearby = rc.senseNearbyRobots(300, rc.getTeam().opponent());
        int arc4 = rc.readSharedArray(63);
        if (0 == arc4) {
            scoutArchon(rc, nearby, arc4);
        }
        int s24 = rc.readSharedArray(5);
        if (0 == (s24 >> 8)) {
            scoutSoldier(rc, nearby, s24);
        }
    }


}
