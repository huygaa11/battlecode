package kairat.structures;

import kairat.Structure;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class TechnologyInstitute extends Structure {

    public TechnologyInstitute(RobotController rc) throws GameActionException {
        super(rc);

        // Initialize channelID and increment total number of this RobotType
        channelStartWith = Channel_TechnologyInstitute;
        initChannelNum();
    }

}
