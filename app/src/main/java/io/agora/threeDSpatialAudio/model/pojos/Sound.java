package io.agora.threeDSpatialAudio.model.pojos;

import androidx.annotation.NonNull;

public enum Sound {
        music(0, "Music","with_you_in_my_arms_ssj011001.mp3"),
        sound1(1, "Sound 1", "sound01.mp3"), // "DogsBarkingCUandDistInfuriated"
        sound2(2, "Sound 2","sound02.mp3"), // "ManWhistlingLikeAS"
        sound3(3, "Sound 3", "sound03.mp3"),        // "CrixCicadasLoopNig"
        song01Vocal(4, "Vocals","song01_vocal.mp3"),
        sound01Instrumental(5,"Instrumental","song01_instrumental.mp3");
        public final String filePath;
        public final String displayName;
        public final int index;

        Sound(int index, String displayName, String filePath) {
            this.filePath = filePath;
            this.displayName = displayName;
            this.index =  index;
        }

        public static Sound byIndex(int index){
            for(Sound sound : Sound.values()){
                if(sound.index == index) {
                    return sound;
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