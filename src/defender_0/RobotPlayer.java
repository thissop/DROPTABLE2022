package defender_0;

import battlecode.common.*;
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

    /** Array containing all the possible movement directions. */
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
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
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: runWatchTower(rc); break;
                    case BUILDER:    runBuilder(rc); break;
                    case SAGE:       break;
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
        int manufactured_miners = rc.readSharedArray(6) & 0b11111111;
        int manufactured_soldiers = rc.readSharedArray(6) & 0b1111111100000000;
        int manufactured_builders = rc.readSharedArray(7) & 0b111111;

        if (manufactured_miners<10) {
            Direction dir = directions[rng.nextInt(directions.length)];
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
                rc.writeSharedArray(6, rc.readSharedArray(6) + 1);
            }
        }

        else if (manufactured_soldiers<5) {
            Direction dir = directions[rng.nextInt(directions.length)];
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
            }
        }

        else if (manufactured_builders<3) {
                Direction [] builder_options = {Direction.NORTH, Direction.SOUTH};
                Direction dir = builder_options[rng.nextInt(builder_options.length)];
                rc.setIndicatorString("Trying to build a builder");
                if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                    rc.buildRobot(RobotType.BUILDER, dir);
                    rc.writeSharedArray(7, rc.readSharedArray(7) + 1);
                }
            }

        else {
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rng.nextBoolean()) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a miner");
                if (rc.canBuildRobot(RobotType.MINER, dir)) {
                    rc.buildRobot(RobotType.MINER, dir);
                    System.out.println("Built a miner!");
                    rc.writeSharedArray(6, rc.readSharedArray(6) + 1);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                    System.out.println("Built a soldier!");
                    rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
                }
            }
        }

        }


    /*
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
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }

    /*add code
    * to not just upgrade prototypes but to repair damaged nearby buildings */
    static void runBuilder(RobotController rc) throws GameActionException {
        int manufactured_towers = rc.readSharedArray(8) & 0b1111;

        MapLocation hq_loc = rc.getLocation();
        int closest_hq_dist = 25;

        int test_dist = 0;
        for (RobotInfo nearby_robot : rc.senseNearbyRobots()) {
            if (nearby_robot.getTeam().isPlayer() && nearby_robot.getType().equals(RobotType.ARCHON)) {
                test_dist = rc.getLocation().distanceSquaredTo(nearby_robot.getLocation());
                if (test_dist<closest_hq_dist) {
                    closest_hq_dist = test_dist;
                    hq_loc = nearby_robot.getLocation();
                }
            }
        }

        Direction [] tower_dirs = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

        boolean built = false;

        if (manufactured_towers<8) {
            if (!hq_loc.equals(rc.getLocation())) {
                if (rc.getLocation().directionTo(hq_loc).equals(Direction.NORTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, test_dir)) {
                            rc.buildRobot(RobotType.WATCHTOWER, test_dir);
                            System.out.println("Built a watchtower!");
                            rc.writeSharedArray(8, rc.readSharedArray(8) + 1);
                        }
                    }
                }
                else if (rc.getLocation().directionTo(hq_loc).equals(Direction.SOUTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, test_dir)) {
                            rc.buildRobot(RobotType.WATCHTOWER, test_dir);
                            System.out.println("Built a watchtower!");
                            rc.writeSharedArray(8, rc.readSharedArray(8) + 1);
                        }
                    }
                }
            }
        }

        boolean repaired = true;
        if (!built) {
            for (RobotInfo nearby_robot : rc.senseNearbyRobots()) {
                if (nearby_robot.getTeam().isPlayer() && nearby_robot.getMode().equals(RobotMode.PROTOTYPE)) {
                    if (rc.canRepair(nearby_robot.getLocation())) {
                        rc.repair(nearby_robot.getLocation());
                        System.out.println("Repaired a prototype robot!");
                        repaired = true;
                        break;
                    }
                }
            }
        }
    }

    /*IMPROVE ATTACK ALGO!*/
    /*
    * Perhaps by choosing based on a combination of enemy type, enemy closeness to our hq, enemy health.
    * currently attacks closest and lowest fighter, then closest fighter, then lowest fighter, then lowest non fighter
    * maybe we make three arrays: attack priority (based on type), ordinal distance (order in distance from robot), and health order
    * we can add all three of those values for each enemy robot, and attack the robot with the minimum or something
    * for now it just attacks first robot it can
    * */

    static void runWatchTower(RobotController rc) throws GameActionException {
        RobotInfo [] nearby_enemies = rc.senseNearbyRobots(20, rc.getTeam().opponent());
        for (RobotInfo nearby_enemy : nearby_enemies) {
                if (rc.canAttack(nearby_enemy.getLocation())) {
                    rc.attack(nearby_enemy.getLocation());
                    System.out.println("Attacked a nearby robot!");
                    break;
                }
        }
    }

    /*COMS*/

    /*WRITERS*/

    /*
    rc.buildRobot(RobotType.BUILDER, dir);
    rc.writeSharedArray(7, rc.readSharedArray(7) + 1);

    rc.buildRobot(RobotType.SAGE, dir);
    rc.writeSharedArray(7, rc.readSharedArray(7) + 0b1000000);

    rc.buildRobot(RobotType.WATCHTOWER, dir);
    rc.writeSharedArray(8, rc.readSharedArray(8) + 1);

    rc.buildRobot(RobotType.LABORATORY, dir);
    rc.writeSharedArray(8, rc.readSharedArray(8) + 0b10000);

    static int writeMiner(RobotPlayer rc) throws  GameActionException {
        rc.writeSharedArray(6, rc.readSharedArray(6) + 1);
    }

    static int writeSoldier(RobotPlayer rc) throws  GameActionException {
        rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
    }

    */


    /*READERS*/

    /*
    static int getMiners(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(6) & 0b11111111;
    }

    static int getSoldiers(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(6) & 0b1111111100000000;
    }

    static int getBuilders(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(7) & 0b111111;
    }

    static int getSages(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(76) & 0b111111000000;
    }

    static int getWatchtowers(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(8) & 0b1111;
    }

    static int getLaboratories(RobotPlayer rc) throws GameActionException {
        return rc.readSharedArray(8) & 0b11110000;
    }
    */

}

