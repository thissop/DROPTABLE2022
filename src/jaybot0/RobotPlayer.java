package jaybot0;

import battlecode.common.*;
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
                        rc.resign();
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
        static final Random rng = new Random(6147);

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
                else {
                        return 0;
                }
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
        // static PriorityQueue<Integer> pq_ = new PriorityQueue<Integer>();
        static int[] heap_ = new int[3600];
        static Pair<Integer, LinkedList<Direction>> pathfindTo(MapLocation target) throws GameActionException {
                int startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();

                MapLocation[] preceding = preceding_;
                int[] visited = visited_;
                int[] dists = dists_;
                // PriorityQueue<Integer> pq = pq_;
                int[] heap = heap_;
                int heapsize=1;

                path_whichtime_++;
                int path_whichtime = path_whichtime_;
                if (newpzero > Integer.MAX_VALUE/3*2) {
                // if (newpzero > 1000) {
                        for (int i=3599; i>=0; i--) dists[i] = 0;
                        newpzero=0;
                }
                int dz = newpzero;
                MapLocation start = rc.getLocation();
                int starti = mapLocationToInt(start);
                rc.setIndicatorLine(start, target, 0, 0, 0);

                dists[starti]=dz;
                // pq.clear();
                heap[0] = starti;
                // pq.add(starti);
                preceding[starti] = start;
                int endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                System.out.println("i" + (endbc-startbc));

                startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                int visiting;
                MapLocation visiting_loc = start;

                Direction[] loutdirs = outDirs;
                MapLocation newloc; int newloci;
                outer: while (!visiting_loc.equals(target)) {
                // outer: while (visiting_loc.equals(start)) {
                        do {
                                // System.out.println("heap" + heapsize);
                                // if (pq.size() == 0) break outer;//return null;
                                // visiting = pq.poll() & 0b11111111111111;
                                if (heapsize == 0) return null;
                                visiting = heap[0] & 0b1111_11111_11111;
                                visiting_loc = intToMapLocation(visiting);
                                heapsize--;
                                //siftdown
                                heap[0] = heap[heapsize];
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

                        // System.out.println("visiting" + visiting_loc + dists[visiting] + " dz" + dz);
                        System.out.println("cost" +( dists[visiting] - dz));

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
                                        heap[heapsize] = newloci + ((cost +
                                        Math.max(Math.abs(newloc.x-target.x)*10,
                                        Math.abs(newloc.y-target.y)*10)*3)
                                        <<15);
                                        int ci = heapsize;
                                        int par = (ci-1)/2;
                                        while (heap[par] > heap[ci]) {
                                                int t = heap[par];
                                                heap[par] = heap[ci];
                                                heap[ci] = t;
                                                ci = par;
                                                par = (ci-1)/2;
                                        }
                                        heapsize++;
                                }
                        }
                }
                endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                System.out.println("p" + (endbc-startbc));

                startbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                LinkedList<Direction> directions = new LinkedList<Direction>();
                MapLocation loc = target;
                do {
                        // System.out.println(preceding[mapLocationToInt(loc)] + "==> " + loc);
                        directions.addFirst(preceding[mapLocationToInt(loc)].directionTo(loc));
                        rc.setIndicatorLine(preceding[mapLocationToInt(loc)], loc, 0, 255, 0);
                        rc.setIndicatorDot(loc, 255, 0, 0);
                        loc = preceding[mapLocationToInt(loc)];
                } while (!loc.equals(start));
                // rc.setIndicatorLine(preceding[mapLocationToInt(loc)], loc, 0, 0, 0);

                endbc = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                System.out.println("e" + (endbc-startbc));
                return new Pair(dists[mapLocationToInt(target)], directions);
        }
/*
        static int path_whichtime = 1;
        static MapLocation[] preceding = new MapLocation[3600];
        static int[] visited = new int[3600];
        static int[] dists = new int[3600];
        static int nextmaxcost = 0;
        static PriorityQueue<Integer> pq = new PriorityQueue<Integer>();

        static Pair<Integer, Direction> pathfindTo(MapLocation target) throws GameActionException {
                int startt = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                path_whichtime++;
                // if (nextmaxcost > Integer.MAX_VALUE*2/3) {
                        for (int i=3599; i>=0; i--) dists[i] = 0;
                        nextmaxcost=0;
                // }
                int startt2 = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                int maxcost = nextmaxcost;
                MapLocation start = rc.getLocation();
                rc.setIndicatorLine(start, target, 0, 0, 0);

                pq.clear();
                pq.add(mapLocationToInt(start));
                preceding[mapLocationToInt(start)] = start;
                int doneinit = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();

                int visiting;
                MapLocation visiting_loc = start;
                dists[mapLocationToInt(start)] = maxcost;

                outer: while (!visiting_loc.equals(target)) {

                        if (pq.size() == 0) return null;
                        do {
                                if (pq.size() == 0) break outer;
                                visiting = pq.poll() & 0b11111111111111;
                                visiting_loc = intToMapLocation(visiting);
                        } while (visited[visiting] == path_whichtime);
                        rc.setIndicatorDot(visiting_loc, 255, 0, 0);
                        rc.setIndicatorLine(visiting_loc, preceding[visiting], 255, 255, 0);

                        visited[visiting] = path_whichtime;
                        if (rc.canSenseLocation(visiting_loc) && rc.senseRobotAtLocation(visiting_loc) != null
                                && !visiting_loc.equals(rc.getLocation())) {
                                continue;
                        }

                        for (Direction dir : outDirs) {
                                MapLocation newloc = visiting_loc.add(dir);
                                int newloci = mapLocationToInt(newloc);
                                if (newloc.x>=0 && newloc.x<mapWidth &&
                                        newloc.y>=0 && newloc.y<mapHeight/* &&
                                        visited[newloci] != path_whichtime* /) {

                                        // System.out.println("new loc " + newloc.toString());
                                        int cost = dists[visiting] + getRubble(newloc) + 10;
                                        // (int)Math.floor(1.0 + getRubble(newloc)/10.0);

                                        if (dists[newloci] <= maxcost || cost < dists[newloci]) {
                                                dists[newloci] = cost;
                                                nextmaxcost = Math.max(nextmaxcost, dists[newloci]);
                                                preceding[newloci] = visiting_loc;
                                                // System.out.println(visiting_loc.toString() + "' goes to '" + newloc.toString());
                                                pq.add(mapLocationToInt(newloc) + ((cost +
                                                Math.min(newloc.x-target.x, newloc.y-target.y)*1000)<<15));
                                        }
                                }
                        }

                        // int eta = 4;
                        // // System.out.println("visiting" + visiting_loc.toString() + " target" + target.toString());
                        // int visiting_dist = Math.min(visiting_loc.x-target.x, visiting_loc.y-target.y);
                        // int visiting_cost = visiting_dist*10/moveCool*eta;
                        //
                        // for (Direction dir : outDirs) {
                        //         MapLocation newloc = visiting_loc.add(dir);
                        //         int newloci = mapLocationToInt(newloc);
                        //         if (newloc.x >= 0 && newloc.x < mapWidth &&
                        //                 newloc.y >= 0 && newloc.y < mapWidth &&
                        //                 visited[newloci] != path_whichtime) {
                        //                 // System.out.println("new loc " + newloc.toString());
                        //                 int cost = dists[visiting] -
                        //                 visiting_cost +
                        //                 (int)Math.floor((1.0 + getRubble(newloc)/10.0)*10.0)/moveCool +
                        //                 Math.min(newloc.x-target.x, newloc.y-target.y)*10/moveCool*eta;
                        //
                        //                 if (dists[newloci] <= maxcost || cost < dists[newloci]) {
                        //                         dists[newloci] = cost;
                        //                         nextmaxcost = Math.max(nextmaxcost, dists[newloci]);
                        //                         preceding[newloci] = visiting_loc;
                        //                         // System.out.println(visiting_loc.toString() + "' goes to '" + newloc.toString());
                        //                         pq.add(mapLocationToInt(newloc) + (cost<<15));
                        //                 }
                        //         }
                        // }
                        // if (pq.size() == 0) return null;
                }
                int donepathing = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();

                MapLocation loc = target;
                MapLocation nextloc = preceding[mapLocationToInt(loc)];
                if (loc.equals(start)) return new Pair(0, Direction.CENTER);

                while (!(nextloc.equals(start))) {
                        // System.out.println(nextloc.toString() + "--==>" + loc.toString());
                        rc.setIndicatorLine(nextloc, loc, 0, 0, 255);
                        loc = nextloc;
                        nextloc = preceding[mapLocationToInt(loc)];
                }

                rc.setIndicatorLine(nextloc, loc, 0, 255, 0);
                int now = Clock.getBytecodeNum()+bytecodeLim*rc.getRoundNum();
                System.out.println("i" + (doneinit-startt) +
                " p" + (donepathing-doneinit) + " b" + (now-donepathing));

                return new Pair(dists[mapLocationToInt(target)], start.directionTo(loc));
        }
*/
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

                while (true) {
                        turnCount += 1;
                        roundNum = rc.getRoundNum();
                        try {
                                switch (rc.getType()) {
                                        case ARCHON:     runArchon();  break;
                                        case MINER:      runMiner();   break;
                                        case SOLDIER:    runSoldier(); break;
                                        case LABORATORY:
                                        case WATCHTOWER:
                                        case BUILDER:
                                        case SAGE:       break;
                                }
                        } catch (Exception e) {
                                System.out.println(rc.getType() + " Exception");
                                e.printStackTrace();
                                rc.resign();
                        }
                        Clock.yield();
                }
        }

        static void runArchon() throws GameActionException {
                for (Direction dir : outDirs) {
                        if (rc.canBuildRobot(RobotType.MINER, dir)
                         && rc.getRoundNum() <2
                        ) {
                                rc.buildRobot(RobotType.MINER, dir);
                        }
                }
                Pair<Integer, LinkedList<Direction>> result = pathfindTo(new MapLocation(0, 0));

                // if (rc.getRoundNum() > 299) rc.resign();
        }

        static int totalmines = 0;
        static void runMiner() throws GameActionException {

                Pair<Integer, LinkedList<Direction>> result = pathfindTo(new MapLocation(0, 0));
                if (result == null) {
                        System.out.println("no pathing result");
                        rc.resign();
                }
                System.out.println("result: " + String.valueOf(result.first) + result.second.get(0).toString());
                Direction dir = result.second.get(0);
                if (dir != null && rc.canMove(dir)) rc.move(dir);

                return;

                /*
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
                                totalmines++;
                        }
                        while (rc.canMineLead(loc) && rc.senseLead(loc) > 1) {
                                // rc.setIndicatorDot(loc, 192, 192, 192);
                                rc.mineLead(loc);
                                didmine=1;
                                totalmines++;
                        }
                        if (didmine==0) {
                                rc.setIndicatorString("didn't mine");
                                // rc.setIndicatorDot(loc, 255, 255, 255);
                                break;
                        }
                }

                do_disintegrate: {
                        if (turnCount > 30 && rc.senseNearbyRobots(sensorRadSq, ally).length > 5 &&
                        rc.senseLead(rc.getLocation()) == 0 &&
                        rc.senseGold(rc.getLocation()) == 0) {
                                for (RobotInfo archon : archons) {
                                        if (rc.getLocation().distanceSquaredTo(archon.getLocation()) <= 8)
                                        break do_disintegrate;
                                }
                                rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
                                rc.disintegrate();
                        }
                }

                if (rc.senseLead(minelocs[0]) == 0 && rc.senseGold(minelocs[0]) == 0) {
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
                                RobotInfo[] nearestRobots = getSortedRobotInRange(rc.getLocation(),
                                                                sensorRadSq, ally);


                                Direction dir = nearestRobots.length > 3 ?
                                        nearestRobots[0].getLocation().directionTo(rc.getLocation())
                                        : Direction.values()[rng.nextInt(8)];
                                if (rc.canMove(dir)) {
                                        rc.move(dir);
                                }
                                rc.setIndicatorString("Nowhere to mine, moving away.");
                        }
                        else {
                                Direction dir = rc.getLocation().directionTo(bestloc);
                                rc.setIndicatorLine(rc.getLocation(), bestloc, 0, 0, 0);

                                if (rc.canMove(dir)) {
                                        rc.move(dir);
                                }
                                else {
                                        // dir = Direction.values()[rng.nextInt(8)];
                                        dir = dir.opposite();
                                        if (rc.canMove(dir)) {
                                                rc.move(dir);
                                        }
                                }
                        }
                }
                if (rc.senseLead(rc.getLocation()) != 0 || rc.senseGold(rc.getLocation()) != 0)
                        rc.setIndicatorDot(rc.getLocation(), 0, 0, 255);*/
        }

        static void runSoldier() throws GameActionException {
        //         int radius = rc.getType().actionRadiusSquared;
        //         Team opponent = rc.getTeam().opponent();
        //         RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        //
        //         if (enemies.length > 0) {
        //                 MapLocation toAttack = enemies[0].location;
        //                 if (rc.canAttack(toAttack)) {
        //                         rc.attack(toAttack);
        //                 }
        //         }
        //
        //         Direction dir = directions[rng.nextInt(directions.length)];
        //         if (rc.canMove(dir)) {
        //                 rc.move(dir);
        //                 System.out.println("I moved!");
        //         }
        }
}
