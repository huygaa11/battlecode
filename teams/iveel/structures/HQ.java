package iveel.structures;

import battlecode.common.*;
import iveel.Structure;

public class HQ extends Structure {

    public HQ(RobotController rc) {
        super(rc);
    }
    
    public void execute() throws GameActionException {
        int numBeavers = rc.readBroadcast(2);

        if (rc.isCoreReady() && rc.getTeamOre() > 100 && numBeavers < 10) {
            Direction newDir = getSpawnDirection(RobotType.BEAVER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.BEAVER);
                rc.broadcast(2, numBeavers + 1);
            }
        }
        MapLocation rallyPoint;
        if (Clock.getRoundNum() < 600) {
            rallyPoint = new MapLocation( (this.myHQ.x + this.theirHQ.x) / 2,
                                          (this.myHQ.y + this.theirHQ.y) / 2);
        }
        else {
            rallyPoint = this.theirHQ;
        }
        rc.broadcast(0, rallyPoint.x);
        rc.broadcast(1, rallyPoint.y);

        rc.yield();
    }

}
