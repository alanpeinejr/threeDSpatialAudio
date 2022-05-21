package io.agora.threeDSpatialAudio.model.pojos;

import androidx.annotation.NonNull;

public enum HeadArticulation {
    forward(0, "Forward"),
    left(1, "Left"),
    back(2, "Back"),
    right(3, "Right");
    public final String displayName;
    public final int index;
    HeadArticulation(int index, String displayName) {
        this.displayName = displayName;
        this.index =  index;
    }

    public static HeadArticulation byIndex(int index){
        for(HeadArticulation headArticulation : HeadArticulation.values()){
            if(headArticulation.index == index) {
                return headArticulation;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return this.displayName;
    }
}
