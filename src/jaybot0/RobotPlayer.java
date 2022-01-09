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

        /*
        ██████   ██████  ████████
        ██   ██ ██    ██    ██
        ██████  ██    ██    ██
        ██   ██ ██    ██    ██
        ██████   ██████     ██
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
                rng = new Random(rc.getLocation().x^rc.getLocation().y^rc.getRoundNum());

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
                         // && rc.getRoundNum() < 5
                        ) {
                                rc.buildRobot(RobotType.MINER, dir);
                        }
                }
                // if (rc.getRoundNum()>5)
                //         pathfindTo(new MapLocation(0, 0), false, -1);

                // if (rc.getRoundNum() > 299) rc.resign();
        }

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
