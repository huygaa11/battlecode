package kairat;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/*BaseBot represents Unit and Structure.
 * General:
 * 
 * Starts with 500 ore, and each team automatically receives 5 ore per turn before any mining income
 * 
 * USE OF CHANNELS:
 * Can only use radio channels from 0 to 65535. 
 * Each robot has its own unique channelNum. 
 * 
 * ///////////   5digits used : DON'T USE 5digit channels!///////////
 *   
 *   
 * == Structures (except HQ) 
 *   AA BB C
 *   
 *   AA: type of unit.
 *   BB: spawned order number. 1st or 2nd. 
 *   C: up this stucture's management. 
 *   
 *   10 - Helipad
 *   11 - tower
 *   12 - SupplyDepot
 *   13 - TechnologyInstitute
 *   14 - Barracks
 *   15 - HandwashStation
 *   16 - TrainingField
 *   17 - TankFactory
 *   18 - AerospaceLab
 *   19 - MinerFactory
 *   
 * == Units:   
 *   A BBB C 
 *   
 *   A: type of unit
 *   BBB: spawned order number. 1st or 2nd 
 *   C: up to this unit's management.
 *   
 *   2 - Beaver
 *   3 - Soldier
 *   4 - Miner
 *   5 - Tank 
 *   6 - Basher // no more than 550
 *   
 * ////////   4 digits used: DON'T USE 4digit channels begin with 1,2, 7-9.///////////
 *   Rest of robots (few number) must be 4 digits.
 *   
 * == Drone, Launcher, Computer, Commander
 *   A BB C - must be 4digits.
 *   
 *   6 - Drone
 *   7 - Launcher
 *   8 - Computer
 *   9 - Commander
 *    
 *   For example: 1st drone's channel is 1 01_
 *   
 * == HQ:  //all important global info
 *   A BBB 
 *   A:  Always 1 (making it different from structures).
 *   BBB:  up this stucture's management. 
 *   
 * == Army:
 *  A BB C
 *  A: always 2
 *  BB: spawned order number (1st army, 2nd army)
 *  C: up to this army's management.
 *   
 *  
 */
public abstract class BaseBot {

    // Channels for keeping track of total number of each robots //
    public static int Channel_Helipad = 10000;
    public static int Channel_Tower = 11000;
    public static int Channel_SupplyDepot = 12000;
    public static int Channel_TechnologyInstitute = 13000;
    public static int Channel_Barracks = 14000;
    public static int Channel_HandwashStation = 15000;
    public static int Channel_TrainingField = 16000;
    public static int Channel_TankFactory = 17000;
    public static int Channel_AerospaceLab = 18000;
    public static int Channel_MinerFactory = 19000;

    public static int Channel_Beaver = 20000;
    public static int Channel_Soldier = 30000;
    public static int Channel_Miner = 40000;
    public static int Channel_Tank = 50000;
    public static int Channel_Basher = 60000; // no more than 550 bashiers

    public static int Channel_Drone = 6000;
    public static int Channel_Launcher = 7000;
    public static int Channel_Computer = 8000;
    public static int Channel_Commander = 9000;
    public static int Channel_Army = 2000;

    // ////////Specific channels///////////////
    public static int Channel_ArmyMode = 1001;

    // ////////////////////////////////////

    protected RobotController rc;
    protected MapLocation myHQ, theirHQ;
    protected Team myTeam, theirTeam;
    protected Random rand;

    // for channeling
    protected int channelID; // this channel would be used for this robot's
                             // info; unique for each robot.
    protected int channelStartWith; // should be Channel_Beaver or ...

    public BaseBot(RobotController rc) {
        this.rc = rc;
        this.myHQ = rc.senseHQLocation();
        this.theirHQ = rc.senseEnemyHQLocation();
        this.myTeam = rc.getTeam();
        this.theirTeam = this.myTeam.opponent();
        this.rand = new Random(rc.getID());
    }

    /**
     * Initialize channelNum AA BBB
     * 
     * Increment total number of this robot type.
     * 
     * @throws GameActionException
     */
    public void initChannelNum() throws GameActionException {
        int spawnedOrder = rc.readBroadcast(channelStartWith) + 1;
        rc.broadcast(channelStartWith, spawnedOrder);
        channelID = channelStartWith + spawnedOrder * 10;
    }

    /**
     * Create a new channel for an army. Number of army must be limited to 99.
     * 
     * @return
     * @throws GameActionException
     */
    public int newArmyGetChannelID() throws GameActionException {
        int spawnedOrder = rc.readBroadcast(Channel_Army) + 1;
        rc.broadcast(Channel_Army, spawnedOrder);
        return Channel_Army + spawnedOrder * 10;
    }

    /**
     * Find a list of directions toward destination.
     * 
     * @param dest
     * @return
     */
    public Direction[] getDirectionsToward(MapLocation dest) {
        Direction toDest = rc.getLocation().directionTo(dest);
        Direction[] dirs = { toDest, toDest.rotateLeft(), toDest.rotateRight(),
                toDest.rotateLeft().rotateLeft(),
                toDest.rotateRight().rotateRight() };

        return dirs;
    }

    public RobotInfo[] getAllies() {
        RobotInfo[] allies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);
        return allies;
    }

    public RobotInfo[] getEnemiesInAttackingRange() {
        RobotInfo[] enemies = rc.senseNearbyRobots(
                RobotType.SOLDIER.attackRadiusSquared, theirTeam);
        return enemies;
    }

    public void attackLeastHealthEnemy(RobotInfo[] enemies)
            throws GameActionException {
        if (enemies.length == 0) {
            return;
        }

        double minEnergon = Double.MAX_VALUE;
        MapLocation toAttack = null;
        for (RobotInfo info : enemies) {
            if (info.health < minEnergon) {
                toAttack = info.location;
                minEnergon = info.health;
            }
        }

        rc.attackLocation(toAttack);
    }

    protected void attackEnemyZero() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getLocation(),
                rc.getType().attackRadiusSquared, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {// there are enemies nearby
            // try to shoot at them
            // specifically, try to shoot at enemy specified by nearbyEnemies[0]
            if (rc.isWeaponReady()
                    && rc.canAttackLocation(nearbyEnemies[0].location)) {
                rc.attackLocation(nearbyEnemies[0].location);
            }
        }
    }

    public void beginningOfTurn() {
        if (rc.senseEnemyHQLocation() != null) {
            theirHQ = rc.senseEnemyHQLocation();
        }
    }

    public void endOfTurn() throws GameActionException {
        transferSupplies();
    }

    public void go() throws GameActionException {
        beginningOfTurn();
        execute();
        endOfTurn();
    }

    public void execute() throws GameActionException {

    }

    public void transferSupplies() throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
        double lowestSupply = rc.getSupplyLevel();
        double transferAmount = 0;
        MapLocation suppliesToThisLocation = null;
        for (RobotInfo ri : nearbyAllies) {
            if (ri.supplyLevel < lowestSupply) {
                lowestSupply = ri.supplyLevel;
                transferAmount = (rc.getSupplyLevel() - ri.supplyLevel) / 2;
                suppliesToThisLocation = ri.location;
            }
        }
        if (suppliesToThisLocation != null) {
            rc.transferSupplies((int) transferAmount, suppliesToThisLocation);
        }
    }

    protected Direction getRandomDirection() {
        // System.out.println("heereeeee" +
        // Direction.values()[(int)(rand.nextDouble()*8)]);
        return Direction.values()[(int) (rand.nextDouble() * 8)];
    }

}
