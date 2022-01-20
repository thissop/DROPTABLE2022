package soldierjaybot;

import battlecode.common.*;
// import com.sun.glass.ui.Robot;

import java.awt.*;
import java.util.*;

class MiningComparator implements Comparator<MapLocation> {
        public int compare(MapLocation l1, MapLocation l2) {
                RobotController rc = RobotPlayer.rc;
                try {
                        if (l1 == null) return 1;
                        if (l2 == null) return -1;
                        if (!rc.onTheMap(l1)) return 1;
                        if (!rc.onTheMap(l2)) return -1;
                        if (rc.canMineGold(l1) || rc.canMineGold(l2)) {
                                return -Integer.compare(rc.senseGold(l1), rc.senseGold(l2));
                        }
                        else if (rc.canMineLead(l1) || rc.canMineLead(l2)) {
                                return -Integer.compare(rc.senseLead(l1), rc.senseLead(l2));
                        }
                        return 0;
                }
                catch (GameActionException e) {
                        // rc.resign();
                        return 0;
                }
        }
}

class Pair<K, V> {
        K first; V second;
        Pair(K l1, V l2) {
                this.first = l1;
                this.second = l2;
        }
}

public strictfp class RobotPlayer {
        static int turnCount = 0;
        static int roundNum = 0;
        static Random rng;

        static RobotController rc;
        static ArrayList<RobotInfo> archons = new ArrayList<RobotInfo>();

        static Direction dirN = Direction.NORTH;
        static Direction dirNE = Direction.NORTHEAST;
        static Direction dirE = Direction.EAST;
        static Direction dirSE = Direction.SOUTHEAST;
        static Direction dirS = Direction.SOUTH;
        static Direction dirSW = Direction.SOUTHWEST;
        static Direction dirW = Direction.WEST;
        static Direction dirNW = Direction.NORTHWEST;
        static Direction dirO = Direction.CENTER;

        static Direction[] cardinalDirs = {dirN, dirE, dirS, dirW};
        static Direction[] outDirs = {dirN, dirNE, dirE, dirSE, dirS, dirSW, dirW, dirNW};
        static Direction[] alldirs = Direction.values();

        static int actionRadSq;
        static int sensorRadSq;

        static int actionCool;
        static int moveCool;

        static int bytecodeLim;

        static int mapWidth, mapHeight;

        static Team ally;
        static Team enemy;


        /*
        ██    ██ ████████ ██ ██      ███████
        ██    ██    ██    ██ ██      ██
        ██    ██    ██    ██ ██      ███████
        ██    ██    ██    ██ ██           ██
         ██████     ██    ██ ███████ ███████
        */



        static MapLocation[] getSortedLocationInRange(MapLocation location, int radSq) throws GameActionException {
                MapLocation[] result = rc.getAllLocationsWithinRadiusSquared(location, radSq);
                Arrays.sort(result, Comparator.comparingInt(loc->location.distanceSquaredTo(loc)));
                return result;
        }

        static RobotInfo[] getSortedRobotInRange(MapLocation location, int radSq, Team team) throws GameActionException {
                RobotInfo[] result = rc.senseNearbyRobots(location, radSq, team);
                Arrays.sort(result, Comparator.comparingInt(info->info.getLocation().distanceSquaredTo(location)));
                return result;
        }

        static int[] rubble = new int[3600];
        static int getRubble(MapLocation location) throws GameActionException {
                if (rubble[mapLocationToInt(location)] != 0) return rubble[mapLocationToInt(location)]-1;
                if (rc.canSenseLocation(location)) {
                        int rubblenum = rc.senseRubble(location);
                        rubble[mapLocationToInt(location)] = rubblenum+1;
                        return rubblenum;
                }
                return 0;
        }

        static boolean hasRubbleData(MapLocation location) {
                return rubble[mapLocationToInt(location)] != 0;
        }

        static int mapLocationToInt(MapLocation location) {
                return location.x + location.y * mapWidth;
        }

        static MapLocation intToMapLocation(int i) {
                return new MapLocation(i%mapWidth, (i/mapWidth)%mapHeight);
        }


        static int path_whichtime_ = 1;
        static MapLocation[] preceding_ = new MapLocation[3600];
        static int[] visited_ = new int[3600];
        static int[] dists_ = new int[3600];
        static int newpzero = 0;
        static int curdzero = 0;
        static int[] heapind_ = new int[3600];
        static int[] heap_ = new int[3600];
        static int heapsize_ = 0;
        // don't use pausing yet unless you absolutely know what you're doing
        static Pair<Integer, LinkedList<Direction>> pathfindTo(MapLocation target,
        boolean savestate, int maxbytecode) throws GameActionException {
                int startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                // System.out.println("maxbytecode " + maxbytecode + " savestate " + savestate);

                MapLocation[] preceding = preceding_;
                int[] visited = visited_;
                int[] dists = dists_;
                int[] heapind = heapind_;
                int[] heap = heap_;
                int path_whichtime = path_whichtime_;
                int heapsize = heapsize_;
                MapLocation start = rc.getLocation();
                int starti = mapLocationToInt(start);
                rc.setIndicatorLine(start, target, 0, 0, 0);

                Direction[] loutdirs = outDirs;

                int dz = curdzero;

                if (!savestate) {
                        heapsize=1;

                        path_whichtime_++;
                        path_whichtime = path_whichtime_;
                        if (newpzero > Integer.MAX_VALUE/3*2) {
                                for (int i=3599; i>=0; i--) dists[i] = 0;
                                newpzero=0;
                        }
                        dz = newpzero;
                        curdzero = dz;

                        dists[starti]=dz;
                        heap[0] = starti;
                        preceding[starti] = start;
                }
                else {
                        for (int i=7; i>=0; i--) {
                                MapLocation newloc = start.add(loutdirs[i]);
                                if (newloc.x < 0 || newloc.x >= mapWidth
                                || newloc.y < 0 || newloc.y >= mapHeight) continue;

                                int newloci = mapLocationToInt(newloc);
                                int cost = dists[starti] + getRubble(start) + 10;
                                dists[newloci] = cost;
                                preceding[newloci] = start;
                                newpzero = Math.max(newpzero, dists[newloci]);
                        }
                }
                int endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                // System.out.println("i" + (endbc-startbc));

                startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                int visiting;
                MapLocation visiting_loc = start;

                MapLocation newloc; int newloci;
                outer: while (!visiting_loc.equals(target)) {
                        endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                        if (endbc - startbc >= maxbytecode && maxbytecode > 0) {
                                heapsize_=heapsize;
                                // System.out.println("OUT OF BYTECODE, CONTINUE LATER" + (endbc - startbc) +
                                // "/" + maxbytecode);
                                return new Pair(null, null);
                        }

                        do {
                                // System.out.println("heapsize"+ heapsize);
                                if (heapsize == 0) return null;
                                visiting = heap[0] & 0b1111_11111_11111;
                                visiting_loc = intToMapLocation(visiting);
                                heapsize--;
                                //siftdown
                                heapind[heap[heapsize] & 0b1111_11111_11111] = 0;
                                heap[0] = heap[heapsize];
                                heap[heapsize] = 0;
                                int ci=0;
                                int lc = 2*ci+1;
                                int rc = 2*ci+2;
                                while (
                                (lc < heapsize && heap[ci] > heap[lc])
                                || (rc < heapsize && heap[ci] > heap[rc])) {
                                        int smallest = ci;
                                        if (lc < heapsize && heap[smallest] > heap[lc]) {
                                                smallest = lc;
                                        }
                                        if (rc < heapsize && heap[smallest] > heap[rc]) {
                                                smallest = rc;
                                        }

                                        heapind[heap[smallest] & 0b1111_11111_11111] = ci;
                                        heapind[heap[ci] & 0b1111_11111_11111] = smallest;
                                        int t = heap[smallest];
                                        heap[smallest] = heap[ci];
                                        heap[ci] = t;

                                        ci = smallest;
                                        lc = 2*ci+1;
                                        rc = 2*ci+2;
                                }
                        } while (visited[visiting] == path_whichtime);
                        visited[visiting] = path_whichtime;

                        if (rc.canSenseLocation(visiting_loc) &&
                        rc.senseRobotAtLocation(visiting_loc) != null
                                && !visiting_loc.equals(rc.getLocation())) {
                                continue;
                        }

                        rc.setIndicatorDot(visiting_loc, 0, 0, 255);
                        rc.setIndicatorLine(start, target, 0, 0, 0);

                        // System.out.println("visiting" + visiting_loc);
                        // System.out.println("cost" +( dists[visiting] - dz));

                        // if (rc.getRoundNum()>600) rc.resign();

                        for (int i=7; i>=0; i--) {
                                newloc = visiting_loc.add(loutdirs[i]);
                                if (newloc.x < 0 || newloc.x >= mapWidth
                                || newloc.y < 0 || newloc.y >= mapHeight) continue;
                                // System.out.println("newloc" + newloc);
                                newloci = mapLocationToInt(newloc);
                                int cost = dists[visiting] + getRubble(visiting_loc) + 10;
                                if (dists[newloci] <= dz || cost < dists[newloci]) {
                                        dists[newloci] = cost;
                                        preceding[newloci] = visiting_loc;
                                        newpzero = Math.max(newpzero, dists[newloci]);
                                        // System.out.println("newloci" + newloci);
                                        int didadd=0;
                                        int ci; int par;
                                        int eta=3;
                                        if (heap[heapind[newloci]] == newloci) {
                                                ci = heapind[newloci];
                                                heap[ci] = newloci + ((cost +
                                                Math.max(Math.abs(newloc.x-target.x)*10,
                                                Math.abs(newloc.y-target.y)*10)*eta)
                                                <<15);
                                                par = (ci-1)/2;
                                        }
                                        else {
                                                ci = heapsize;
                                                heap[ci] = newloci + ((cost +
                                                Math.max(Math.abs(newloc.x-target.x)*10,
                                                Math.abs(newloc.y-target.y)*10)*eta)
                                                <<15);
                                                par = (ci-1)/2;
                                                didadd=1;
                                        }
                                        while (heap[par] > heap[ci]) {
                                                heapind[heap[par] & 0b1111_11111_11111] = ci;
                                                heapind[heap[ci] & 0b1111_11111_11111] = par;
                                                int t = heap[par];
                                                heap[par] = heap[ci];
                                                heap[ci] = t;
                                                ci = par;
                                                par = (ci-1)/2;
                                        }
                                        if (didadd==1)heapsize++;
                                }
                        }
                        if (!hasRubbleData(visiting_loc) ||
                        visiting_loc.distanceSquaredTo(start) > 40 && rng.nextInt(100)<40) {
                                newloc = target;
                                if (newloc.x < 0 || newloc.x >= mapWidth
                                || newloc.y < 0 || newloc.y >= mapHeight) continue;
                                // System.out.println("newloc" + newloc);
                                newloci = mapLocationToInt(newloc);
                                int cost = dists[visiting] + getRubble(visiting_loc) +
                                Math.max(Math.abs(newloc.x-target.x)*13,
                                Math.abs(newloc.y-target.y)*13);

                                if (dists[newloci] <= dz || cost < dists[newloci]) {
                                        dists[newloci] = cost;
                                        preceding[newloci] = visiting_loc;
                                        newpzero = Math.max(newpzero, dists[newloci]);
                                        // System.out.println("newloci" + newloci);
                                        int didadd=0;
                                        int ci; int par;
                                        if (heap[heapind[newloci]] == newloci) {
                                                ci = heapind[newloci];
                                                heap[ci] = newloci + ((cost)<<15);
                                                par = (ci-1)/2;
                                        }
                                        else {
                                                ci = heapsize;
                                                heap[ci] = newloci + ((cost)<<15);
                                                par = (ci-1)/2;
                                                didadd=1;
                                        }
                                        while (heap[par] > heap[ci]) {
                                                heapind[heap[par] & 0b1111_11111_11111] = ci;
                                                heapind[heap[ci] & 0b1111_11111_11111] = par;
                                                int t = heap[par];
                                                heap[par] = heap[ci];
                                                heap[ci] = t;
                                                ci = par;
                                                par = (ci-1)/2;
                                        }
                                        if (didadd==1)heapsize++;
                                }
                        }
                }
                heapsize_=heapsize;

                endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                // System.out.println("p" + (endbc-startbc));

                startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                LinkedList<Direction> directions = new LinkedList<Direction>();
                MapLocation loc = target;
                do {
                        // System.out.println(preceding[mapLocationToInt(loc)] + "==> " + loc);
                        directions.addFirst(preceding[mapLocationToInt(loc)].directionTo(loc));
                        rc.setIndicatorLine(preceding[mapLocationToInt(loc)], loc, 0, 255, 0);
                        // rc.setIndicatorDot(loc, 255, 0, 0);
                        loc = preceding[mapLocationToInt(loc)];
                        if (loc == preceding[mapLocationToInt(loc)] ||
                        loc == preceding[mapLocationToInt(preceding[mapLocationToInt(loc)])] ||
                        loc == preceding[mapLocationToInt(preceding[mapLocationToInt(preceding[mapLocationToInt(loc)])])]) {
                                break;
                        }
                } while (!loc.equals(start));
                rc.setIndicatorLine(preceding[mapLocationToInt(loc)], loc, 0, 0, 0);

                endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                // System.out.println("e" + (endbc-startbc));
                return new Pair(dists[mapLocationToInt(target)], directions);
        }

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    // static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    // static final Random rng = new Random(6147);

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
            RobotPlayer.rc = rc;
            actionRadSq = rc.getType().actionRadiusSquared;
            sensorRadSq = rc.getType().visionRadiusSquared;

            actionCool = rc.getType().actionCooldown;
            moveCool = rc.getType().movementCooldown;

            bytecodeLim = rc.getType().bytecodeLimit;

            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(sensorRadSq, ally);
            for (RobotInfo nearby : nearbyRobots) {
                    if (nearby.getType() == RobotType.ARCHON)
                    archons.add(nearby);
            }

            ally = rc.getTeam();
            enemy = ally.opponent();

            mapWidth = rc.getMapWidth();
            mapHeight = rc.getMapHeight();
            rng = new Random(rc.getLocation().x^rc.getLocation().y^rc.getRoundNum());


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
                        runMiner();
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
                // rc.resign();

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

             static int x=0;
             static int y=0;
             static boolean continuePathing = false;
             static int moveattempts=0;
             static LinkedList<Direction> curpath = null;
             static void runMiner() throws GameActionException {
                     // if (rc.canSenseLocation(new MapLocation(x, y)) &&
                     // rc.senseRobotAtLocation(new MapLocation(x,y)) != null || x==0 && y==0) {
                     //         x = rng.nextInt(29);
                     //         y = rng.nextInt(29);
                     // }
                     //
                     // Pair<Integer, LinkedList<Direction>> result = pathfindTo(new MapLocation(x, y), continuePathing, Clock.getBytecodesLeft()*7/10);
                     // if (result == null) {
                     //         System.out.println("no pathing result");
                     //         rc.resign();
                     // }
                     // if (result.first == null) {
                     //         continuePathing = true;
                     //         if (curpath == null || curpath.size() == 0 || false) return;
                     //         Direction dir = curpath.peek();
                     //         System.out.println("move: " + curpath.peek());
                     //         if (rc.canMove(dir)) {
                     //                 rc.move(dir);
                     //                 curpath.remove();
                     //         }
                     //         if (rc.getLocation().x == x && rc.getLocation().y == y) {
                     //                 x = rng.nextInt(29);
                     //                 y = rng.nextInt(29);
                     //         }
                     //         // System.out.println("resuming pathing");
                     //         return;
                     // }
                     // curpath = result.second;
                     // continuePathing = false;
                     // System.out.println("move: " + curpath.peek());
                     // Direction dir = curpath.peek();
                     // if (rc.canMove(dir)) {
                     //         rc.move(dir);
                     //         curpath.remove();
                     // }
                     // if (rc.getLocation().x == x && rc.getLocation().y == y) {
                     //         x = rng.nextInt(29);
                     //         y = rng.nextInt(29);
                     // }
                     //
                     // return;/*


                     int bckp = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                     MapLocation[] minelocs = new MapLocation[9];
                     int i=0;
                     for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), actionRadSq)) {
                             minelocs[i++] = loc;
                     }
                     Arrays.sort(minelocs, new MiningComparator());

                     i=0;
                     while (rc.isActionReady()) {
                             MapLocation loc = minelocs[i++];
                             if (loc == null) break;
                             int didmine=0;
                             while (rc.canMineGold(loc)) {
                                     // rc.setIndicatorDot(loc, 255, 215, 0);
                                     rc.mineGold(loc);
                                     didmine=1;
                             }
                             while (rc.canMineLead(loc) && rc.senseLead(loc) > 1) {
                                     // rc.setIndicatorDot(loc, 192, 192, 192);
                                     rc.mineLead(loc);
                                     didmine=1;
                             }
                             if (didmine==0) {
                                     rc.setIndicatorString("didn't mine");
                                     // rc.setIndicatorDot(loc, 255, 255, 255);
                                     break;
                             }
                     }

                     boolean cramped = false;
                     for (i = 0; i < archons.size(); i++) {
                             if (rc.getLocation().distanceSquaredTo(archons.get(i).getLocation()) < 10) {
                                     cramped = true;
                                     MapLocation bestloc = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
                                     outer:{
                                             if (curpath==null||curpath.size()==0){
                                                     Pair<Integer, LinkedList<Direction>> result
                                                             = pathfindTo(bestloc, continuePathing, Clock.getBytecodesLeft()*7/10);
                                                     if (result == null) {
                                                             continuePathing = false;
                                                             break outer;
                                                     }
                                                     else if (result.first == null) {
                                                             continuePathing = true;
                                                             break outer;
                                                     }
                                                     curpath=result.second;
                                             }
                                             continuePathing = false;
                                             Direction dir = curpath.peek();
                                             // rc.setIndicatorLine(rc.getLocation(), bestloc, 0, 0, 0);

                                             if (rc.canMove(dir)) {
                                                     rc.move(dir);
                                                     curpath.remove();
                                             }
                                             // else {
                                             //         // dir = Direction.values()[rng.nextInt(8)];
                                             //         dir = dir.opposite();
                                             //         if (rc.canMove(dir)) {
                                             //                 rc.move(dir);
                                             //         }
                                             // }
                                     }
                                     // Direction dir = archons.get(i).getLocation().directionTo(rc.getLocation());
                                     // if (rc.canMove(dir)) rc.move(dir);
                             }
                     }
                     // if (rc.senseNearbyRobots(3, ally).length > 5) {
                     //         Direction dir = rc.senseNearbyRobots(3, ally)[0].getLocation()
                     //                         .directionTo(rc.getLocation());
                     //         if (rc.canMove(dir)) {
                     //                 rc.move(dir);
                     //         }
                     // }
                     if ((rc.senseLead(minelocs[0]) <=1 && rc.senseGold(minelocs[0]) <=1 && !cramped) ||
                             rc.senseNearbyRobots(3, ally).length > 5) {
                             rc.setIndicatorString("Nothing to mine.");

                             MapLocation bestloc = null;
                             int amt = 0;
                             int isgold = 0;
                             for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), sensorRadSq)) {
                                     if (rc.senseRobotAtLocation(loc) != null) continue;
                                     if (rc.senseGold(loc) > amt * isgold) {
                                             amt = amt;
                                             isgold = 1;
                                             bestloc = loc;
                                     }
                                     else if (rc.senseLead(loc) > amt && isgold == 0) {
                                             amt = amt;
                                             bestloc = loc;
                                     }
                             }
                             if (bestloc == null) {
                                     // RobotInfo[] nearestRobots = getSortedRobotInRange(rc.getLocation(),
                                     //                                 sensorRadSq, ally);
                                     //
                                     //
                                     // Direction dir = nearestRobots.length > 3 ?
                                     //         nearestRobots[0].getLocation().directionTo(rc.getLocation())
                                     //         : Direction.values()[rng.nextInt(8)];
                                     // if (rc.canMove(dir)) {
                                     //         rc.move(dir);
                                     // }
                                     bestloc = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
                                     outer:{
                                             if (curpath==null||curpath.size()==0){
                                                     Pair<Integer, LinkedList<Direction>> result
                                                             = pathfindTo(bestloc, continuePathing, Clock.getBytecodesLeft()*7/10);
                                                     if (result == null) {
                                                             continuePathing = false;
                                                             break outer;
                                                     }
                                                     else if (result.first == null) {
                                                             continuePathing = true;
                                                             break outer;
                                                     }
                                                     curpath=result.second;
                                                     rc.setIndicatorString("Pathing to wander");
                                             }
                                             continuePathing = false;
                                             Direction dir = curpath.peek();
                                             // rc.setIndicatorLine(rc.getLocation(), bestloc, 0, 0, 0);

                                             if (rc.canMove(dir)) {
                                                     rc.move(dir);
                                                     curpath.remove();
                                             }
                                             else {
                                                     moveattempts++;
                                                     if (moveattempts >= 4) {
                                                             curpath=null;
                                                     }
                                             }
                                             rc.setIndicatorString("going to wander"+dir);
                                             // else {
                                             //         // dir = Direction.values()[rng.nextInt(8)];
                                             //         dir = dir.opposite();
                                             //         if (rc.canMove(dir)) {
                                             //                 rc.move(dir);
                                             //         }
                                             // }
                                     }

                                     // rc.setIndicatorString("Nowhere to mine, wandering.");
                             }
                             else {
                                     outer:{
                                             if (curpath==null||curpath.size()==0){
                                                     Pair<Integer, LinkedList<Direction>> result
                                                             = pathfindTo(bestloc, continuePathing, Clock.getBytecodesLeft()*7/10);
                                                     if (result == null) {
                                                             continuePathing = false;
                                                             break outer;
                                                     }
                                                     else if (result.first == null) {
                                                             continuePathing = true;
                                                             break outer;
                                                     }
                                                     curpath=result.second;
                                                     rc.setIndicatorString("Pathing to mineloc");
                                             }
                                             continuePathing = false;
                                             Direction dir = curpath.peek();
                                             // rc.setIndicatorLine(rc.getLocation(), bestloc, 0, 0, 0);

                                             if (rc.canMove(dir)) {
                                                     rc.move(dir);
                                                     curpath.remove();
                                             }
                                             else {
                                                     moveattempts++;
                                                     if (moveattempts >= 4) {
                                                             curpath=null;
                                                     }
                                             }
                                             rc.setIndicatorString("Going to mineloc");
                                             // else {
                                             //         // dir = Direction.values()[rng.nextInt(8)];
                                             //         dir = dir.opposite();
                                             //         if (rc.canMove(dir)) {
                                             //                 rc.move(dir);
                                             //         }
                                             // }
                                             // rc.setIndicatorString("Somewhere to mine.");
                                     }
                             }
                     }
                     if (rc.senseLead(rc.getLocation()) != 0 || rc.senseGold(rc.getLocation()) != 0)
                             rc.setIndicatorDot(rc.getLocation(), 0, 0, 255);//*/

                     System.out.println("used: "+(Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum()-bckp));
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

    static int getMiners() throws GameActionException {
            return rc.readSharedArray(6) & 0b11111111;
    }

    static int getSoldiers() throws GameActionException {
        return rc.readSharedArray(6) & 0b1111111100000000;
    }

    static int getBuilders() throws GameActionException {
        return rc.readSharedArray(7) & 0b111111;
    }

    static int getSages() throws GameActionException {
        return rc.readSharedArray(76) & 0b111111000000;
    }

    static int getWatchtowers() throws GameActionException {
        return rc.readSharedArray(8) & 0b1111;
    }

    static int getLaboratories() throws GameActionException {
        return rc.readSharedArray(8) & 0b11110000;
    }
}
