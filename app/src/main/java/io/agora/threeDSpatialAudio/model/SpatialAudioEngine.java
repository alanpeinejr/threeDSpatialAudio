package io.agora.threeDSpatialAudio.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.agora.rtc.RtcEngine;
import io.agora.threeDSpatialAudio.model.pojos.Position;

public class SpatialAudioEngine {
    private final static Logger log = LoggerFactory.getLogger(SpatialAudioEngine.class);

    private final static int DEFAULT_RANGE = 100;

    private int mRange;
    private RtcEngine mRtcEngine;

    public SpatialAudioEngine(RtcEngine rtcEngine){
        this(rtcEngine, DEFAULT_RANGE);
    }

    public SpatialAudioEngine(RtcEngine rtcEngine, int range){
        this.mRange = range;
        this.mRtcEngine = rtcEngine;

    }

    public void updatePosition(int uid, Position position){
        log.debug("Update user {} to position {}", uid, position);

    }




    public int getRange() {
        return mRange;
    }

    public void setRange(int mRange) {
        this.mRange = mRange;
    }
}
