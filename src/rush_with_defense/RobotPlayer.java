package rush_with_defense;

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
                //rc.resign(); // REMOVE!!!

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

        // do it every ten in case of map flip or something messes it up
        if (turnCount==0||turnCount%10==0) {

            int archon_information [] = downloadArchon(rc, 1);
            int arch_x = archon_information[0];
            int arch_y = archon_information[1];

            // write own location if
            if (arch_x+arch_y==0) {
                MapLocation current_loc = rc.getLocation();
                int current_x = current_loc.x;
                int current_y = current_loc.y;
                uploadArchon(rc, current_x, current_y, turnCount, 0, 1, rc.getID());

            }

        }

        int manufactured_miners = rc.readSharedArray(6) & 0b11111111;
        int manufactured_soldiers = rc.readSharedArray(6) & 0b1111111100000000;

        System.out.println("MANUFACTURED SOLDIERS: "+manufactured_soldiers);
        System.out.println("MANUFACTURED MINERS: "+manufactured_miners);


        if (manufactured_miners<5) {
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

        else {

            boolean nearby_builder = true; // FFFFFFF
            for (RobotInfo nearby_robot : rc.senseNearbyRobots(34, rc.getTeam())) {
                RobotType nearby_type = nearby_robot.getType();
                if (nearby_type.equals(RobotType.BUILDER)||nearby_type.equals(RobotType.WATCHTOWER)) {
                    //nearby_builder = true;
                    break;
                }
            }

            if (!nearby_builder) {
                Direction [] builder_options = {Direction.NORTH, Direction.SOUTH};
                Direction dir = builder_options[rng.nextInt(builder_options.length)];
                if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                    rc.buildRobot(RobotType.BUILDER, dir);
                }
            } else {

                double soldier_miner_ratio = (double) manufactured_soldiers / manufactured_miners;
                System.out.println("SOLDIER:MINER RATIO-->"+soldier_miner_ratio);
                if (soldier_miner_ratio>=4) {
                    // try to build a miner
                    for (Direction miner_dir : directions) {
                        if (rc.canBuildRobot(RobotType.MINER, miner_dir)) {
                            rc.buildRobot(RobotType.MINER, miner_dir);
                            rc.writeSharedArray(6, rc.readSharedArray(6) + 1);
                            break;
                        }
                    }
                } else {
                    // try to build a soldier
                    for (Direction soldier_dir : directions) {
                        if (rc.canBuildRobot(RobotType.SOLDIER, soldier_dir)) {
                            rc.buildRobot(RobotType.SOLDIER, soldier_dir);
                            rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
                            break;
                        }
                    }
                }
            }
        }

            /*
            Direction dir = directions[rng.nextInt(directions.length)];
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                System.out.println("Built a soldier!");
                rc.writeSharedArray(6, rc.readSharedArray(6) + 0b100000000);
            }
            */
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

        int enemy_archon_information[] = downloadArchon(rc, 2); // just using archon num 2 for default
        int enemy_arch_x = enemy_archon_information[0];
        int enemy_arch_y = enemy_archon_information[1];

        int soldier_tag = rc.getID()%10;

        // replace current soldier_random_archon bc it's prohibitavly expensive

        // simultaneously check for archon

        for (RobotInfo nearby_bot : rc.senseNearbyRobots(20, rc.getTeam().opponent())) {

            if (nearby_bot.getType().equals(RobotType.ARCHON)) {
                // write attempt attack move random

                MapLocation nearby_enemy_archon_loc = nearby_bot.getLocation();
                Direction nearby_enemy_archon_dir = rc.getLocation().directionTo(nearby_enemy_archon_loc);

                if (enemy_arch_x+enemy_arch_y==0) {

                    uploadArchon(rc, nearby_enemy_archon_loc.x, nearby_enemy_archon_loc.y, turnCount, 0, 2, nearby_bot.getID());

                }

                if (rc.canAttack(nearby_enemy_archon_loc)) {
                    rc.attack(nearby_enemy_archon_loc);
                }

                else if (rc.canMove(nearby_enemy_archon_dir)) {
                    rc.move(nearby_enemy_archon_dir);
                } else {soldier_random(rc);}

                break;
            }


        }

        // begin attack if enemy loc is known! --> fix if archon is killed
        if (enemy_arch_y + enemy_arch_x > 0) {
            MapLocation enemy_archon_loc = new MapLocation(enemy_arch_x, enemy_arch_y);
            Direction dir_to_enemy_archon = rc.getLocation().directionTo(enemy_archon_loc);
            if (rc.canSenseLocation(enemy_archon_loc)) {
                if (rc.canAttack(enemy_archon_loc)) {
                    rc.attack(enemy_archon_loc);
                    System.out.println("ATTACKED ENEMY ARCHON!");
                }

                else if (rc.canMove(dir_to_enemy_archon)) {
                    rc.move(dir_to_enemy_archon);
                    System.out.println("MOVED TO ENEMY ARCHON!");
                }

                else {soldier_random(rc);}

            } else if (rc.canMove(dir_to_enemy_archon)) {
                rc.move(dir_to_enemy_archon);
                System.out.println("MOVED TO ENEMY ARCHON!");
            }

            else {soldier_random(rc);}

        }

        else {

            int friendly_archon_information[] = downloadArchon(rc, 1);
            int friendly_arch_x = friendly_archon_information[0];
            int friendly_arch_y = friendly_archon_information[1];
            MapLocation friendly_archon_loc = new MapLocation(friendly_arch_x, friendly_arch_y);

            System.out.println("Friendly Archon Loc: " + friendly_archon_loc);
            if (friendly_arch_x + friendly_arch_y > 0) {

                if (0 <= soldier_tag && soldier_tag <= 2) {
                    // go horizontal
                    MapLocation horizontal_guess = guess_location(rc, friendly_archon_loc, "horizontal");
                    System.out.println("Horizontal Guess Location: " + horizontal_guess);
                    Direction horizontal_try_dir = rc.getLocation().directionTo(horizontal_guess);

                    if (rc.canSenseLocation(horizontal_guess)) {
                        RobotInfo test_info = rc.senseRobotAtLocation(horizontal_guess);
                        if (!test_info.equals(null)) {
                            if (test_info.getType().equals(RobotType.ARCHON) && !test_info.getTeam().isPlayer()) {
                                // upload to array!
                                uploadArchon(rc, horizontal_guess.x, horizontal_guess.y, turnCount, 0, 2, test_info.getID());
                                if (rc.canAttack(horizontal_guess)) {
                                    rc.attack(horizontal_guess);
                                    System.out.println("ATTACKED ARCHON!");
                                }
                                else if (rc.canMove(horizontal_try_dir)) {
                                    rc.move(horizontal_try_dir);
                                } else {soldier_random(rc);}
                            } else {
                                soldier_random(rc);
                            }
                            } else {
                            soldier_random(rc);
                        }
                    }
                    else if (rc.canMove(horizontal_try_dir)) {
                        rc.move(horizontal_try_dir);
                    } else {soldier_random(rc);}

                }

                else if (soldier_tag > 2 && soldier_tag < 7) {
                    // go to rotational
                    MapLocation rotational_guess = guess_location(rc, friendly_archon_loc, "rotational");
                    System.out.println("Rotational Guess Location: " + rotational_guess);
                    Direction rotational_try_dir = rc.getLocation().directionTo(rotational_guess);

                    if (rc.canSenseLocation(rotational_guess)) {
                        RobotInfo test_info = rc.senseRobotAtLocation(rotational_guess);
                        if (!test_info.equals(null)) {
                            if (test_info.getType().equals(RobotType.ARCHON) && !test_info.getTeam().isPlayer()) {
                                // upload to array!
                                uploadArchon(rc, rotational_guess.x, rotational_guess.y, turnCount, 0, 2, test_info.getID());
                                if (rc.canAttack(rotational_guess)) {
                                    rc.attack(rotational_guess);
                                    System.out.println("ATTACKED ARCHON!");
                                }
                                else if (rc.canMove(rotational_try_dir)) {
                                    rc.move(rotational_try_dir);
                                } else {soldier_random(rc);}
                            } else {
                                soldier_random(rc);
                            }
                        } else {
                            soldier_random(rc);
                        }
                    }
                    else if (rc.canMove(rotational_try_dir)) {
                        rc.move(rotational_try_dir);
                    } else {soldier_random(rc);}

                }

                else {
                    // go to vertical
                    MapLocation vertical_guess = guess_location(rc, friendly_archon_loc, "vertical");
                    System.out.println("vertical Guess Location: " + vertical_guess);
                    Direction vertical_try_dir = rc.getLocation().directionTo(vertical_guess);

                    if (rc.canSenseLocation(vertical_guess)) {
                        RobotInfo test_info = rc.senseRobotAtLocation(vertical_guess);
                        if (test_info != null) {
                            if (test_info.getType().equals(RobotType.ARCHON) && !test_info.getTeam().isPlayer()) {
                                // upload to array!
                                uploadArchon(rc, vertical_guess.x, vertical_guess.y, turnCount, 0, 2, test_info.getID());
                                if (rc.canAttack(vertical_guess)) {
                                    rc.attack(vertical_guess);
                                    System.out.println("ATTACKED ARCHON!");
                                }
                                else if (rc.canMove(vertical_try_dir)) {
                                    rc.move(vertical_try_dir);
                                } else {soldier_random(rc);}
                            } else {
                                soldier_random(rc);
                            }
                        } else {
                            soldier_random(rc);
                        }
                    }
                    else if (rc.canMove(vertical_try_dir)) {
                        rc.move(vertical_try_dir);
                    } else {soldier_random(rc);}
                }


            } else {soldier_random(rc);}
        }

        /* Default routine  */
        /*
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
         */
    }

    /*add code
     * to not just upgrade prototypes but to repair damaged nearby buildings */
    static void runBuilder(RobotController rc) throws GameActionException {
        //int manufactured_towers = rc.readSharedArray(8) & 0b1111;

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

        boolean nearby_tower = false;
        boolean nearby_lab = false;

        RobotInfo [] nearby_friendlies = rc.senseNearbyRobots(20, rc.getTeam());

        for (RobotInfo nearby_fiendly : nearby_friendlies) {
            RobotType friendly_type = nearby_fiendly.getType();
            if (friendly_type.equals(RobotType.WATCHTOWER)) {
                nearby_tower = true;
            }

            else if (friendly_type.equals(RobotType.LABORATORY)) {
                nearby_lab =  true;
            }
        }

        if (!nearby_tower) {
            if (!hq_loc.equals(rc.getLocation())) {
                if (rc.getLocation().directionTo(hq_loc).equals(Direction.NORTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, test_dir)) {
                            rc.buildRobot(RobotType.WATCHTOWER, test_dir);
                            System.out.println("Built a watchtower!");
                        }
                    }
                }
                else if (rc.getLocation().directionTo(hq_loc).equals(Direction.SOUTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, test_dir)) {
                            rc.buildRobot(RobotType.WATCHTOWER, test_dir);
                            System.out.println("Built a watchtower!");
                        }
                    }
                }
            }
        }

        if (nearby_tower) {
            for (RobotInfo nearby_robot : rc.senseNearbyRobots()) {
                if (nearby_robot.getTeam().isPlayer() && nearby_robot.getMode().equals(RobotMode.PROTOTYPE)) {
                    if (rc.canRepair(nearby_robot.getLocation())) {
                        rc.repair(nearby_robot.getLocation());
                        System.out.println("Repaired a prototype robot!");
                        break;
                    }
                }
            }
        }

        /*
        if (turnCount>=1500&&!nearby_lab) {
            if (!hq_loc.equals(rc.getLocation())) {
                if (rc.getLocation().directionTo(hq_loc).equals(Direction.NORTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.LABORATORY, test_dir)) {
                            rc.buildRobot(RobotType.LABORATORY, test_dir);
                            System.out.println("Built a lab!");
                        }
                    }
                }
                else if (rc.getLocation().directionTo(hq_loc).equals(Direction.SOUTH)) {
                    for (Direction test_dir : tower_dirs) {
                        if (rc.canBuildRobot(RobotType.LABORATORY, test_dir)) {
                            rc.buildRobot(RobotType.LABORATORY, test_dir);
                            System.out.println("Built a watchtower!");
                        }
                    }
                }
            }
        }
         */
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
        int lowest_offensive_health = 9999;
        MapLocation lowest_offensive_location = new MapLocation(0,0);


        boolean found_offensive_to_attack = false;
        // fix distance !!!
        for (RobotInfo nearby_enemy : rc.senseNearbyRobots(13, rc.getTeam().opponent())) {
            RobotType enemy_type = nearby_enemy.getType();

            if (enemy_type.equals(RobotType.SOLDIER)||enemy_type.equals(RobotType.SAGE)||enemy_type.equals(RobotType.WATCHTOWER)) {
                if (nearby_enemy.getHealth()<lowest_offensive_health) {
                    lowest_offensive_health = nearby_enemy.getHealth();
                    lowest_offensive_location = nearby_enemy.getLocation();
                    found_offensive_to_attack = true;
                }
            }
        }
        if (found_offensive_to_attack) {
            if (rc.canAttack(lowest_offensive_location)) {
                rc.attack(lowest_offensive_location);
            }
        } else {
            for (RobotInfo nearby_enemy : rc.senseNearbyRobots(13, rc.getTeam().opponent())) {
                MapLocation nearby_loc = nearby_enemy.getLocation();
                if (rc.canAttack(nearby_loc)) {
                    rc.attack(nearby_loc);
                    break;
                }
            }
        }
    }

    /* MISC. FUNCS */

    static MapLocation guess_location(RobotController rc, MapLocation reference_location, String guess_type) {

        int max_x = rc.getMapWidth()-1;
        int max_y = rc.getMapHeight()-1;

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

        else { // horizontal
            MapLocation guess = new MapLocation(ref_x, max_y-ref_y);
            return guess;
        }
    }

    static void soldier_random(RobotController rc) throws GameActionException {

        int lowest_offensive_health = 9999;
        MapLocation lowest_offensive_location = new MapLocation(0,0);


        boolean found_offensive_to_attack = false;
        // fix distance !!!
        RobotInfo [] nearby_enemies = rc.senseNearbyRobots(13, rc.getTeam().opponent());
        for (RobotInfo nearby_enemy : nearby_enemies) {
            RobotType enemy_type = nearby_enemy.getType();

            if (enemy_type.equals(RobotType.SOLDIER)||enemy_type.equals(RobotType.SAGE)||enemy_type.equals(RobotType.WATCHTOWER)) {
                if (nearby_enemy.getHealth()<lowest_offensive_health) {
                    lowest_offensive_health = nearby_enemy.getHealth();
                    lowest_offensive_location = nearby_enemy.getLocation();
                    found_offensive_to_attack = true;
                }
            }
        }


        if (found_offensive_to_attack) {
            if (rc.canAttack(lowest_offensive_location)) {
                rc.attack(lowest_offensive_location);
            }
        } else {

            boolean found_neutral_to_attack = false;

            for (RobotInfo nearby_enemy : nearby_enemies) {
                MapLocation nearby_loc = nearby_enemy.getLocation();
                if (rc.canAttack(nearby_loc)) {
                    rc.attack(nearby_loc);
                    found_neutral_to_attack = true;
                    break;
                }
            }
             if (!found_neutral_to_attack) {
                Direction random_dir = directions[rng.nextInt(directions.length)];
                if (rc.canMove(random_dir)) {
                    rc.move(random_dir);
                    System.out.println("I moved randomly yuh.");
                }
            }
        }
    }

    /*
    static void repear_friendly_nearby(RobotController rc) throws GameActionException {

    }
     */


    /*COMS*/

    /*WRITERS*/
    // idk if we want to use 13 bytes on storing an id, probably good for now
    static void uploadArchon(RobotController rc, int x, int y, int time, int mode, int arcNum, int id) throws  GameActionException{
        rc.writeSharedArray(0 + arcNum, (x >> 2) + ((y >> 2) << 4) + ((time>>7) << 8) + (mode << 12));
        rc.writeSharedArray(60 + arcNum, id);
    }

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

    /**
     * arcNum (0-3) which arc you want
     * return int[] thats {actual x, actual y, actual time, mode}
     */

    static int[] downloadArchon(RobotController rc, int arcNum)  throws  GameActionException{
        int val = rc.readSharedArray(arcNum);

        return new int[]{(val & 0b1111) << 2, (val & 0b11110000) >> 2, (val & 0b111100000000) >> 1, (val >> 12) & 0b1};
    }

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

