package iveel.structures;

import battlecode.common.*;
import iveel.Structure;

public class Tower extends Structure{

    public Tower(RobotController rc) {
        super(rc);
        // TODO Auto-generated constructor stub
    }
    
    public void execute() throws GameActionException {
        rc.yield();
    }

}
