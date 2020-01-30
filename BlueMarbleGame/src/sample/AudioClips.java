package sample;

import javafx.scene.media.AudioClip;

import java.nio.file.Paths;

/**
 * Class containing static final AudioClips for this game
 */
public class AudioClips {
    static final AudioClip[] buttonAudioClips = {
            new AudioClip(Paths.get("sounds/Push1.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Push2.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Push3.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Select1.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Select2.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Select3.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/Turn.wav").toUri().toString()),
    };
    static final AudioClip startup = new AudioClip(Paths.get("sounds/Windows 98 startup.wav").toUri().toString());
    static final AudioClip[] dice = {
            new AudioClip(Paths.get("sounds/dice rolling.wav").toUri().toString()),
            new AudioClip(Paths.get("sounds/dice rolled.wav").toUri().toString()),
    };
    static final AudioClip bankrupt =
            new AudioClip(Paths.get("sounds/18. Catastrophe (GAME OVER).mp3").toUri().toString());
    static final AudioClip gameOver =
            new AudioClip(Paths.get("sounds/JJD - Future.mp3").toUri().toString());
    static final AudioClip payday = new AudioClip(Paths.get("sounds/SEP_levelchange.wav").toUri().toString());
    static final AudioClip property = new AudioClip(Paths.get("sounds/SEI_DATA_OK.wav").toUri().toString());
    static final AudioClip build = new AudioClip(Paths.get("sounds/20_Build a Hotel.mp3").toUri().toString());
    static final AudioClip purchase = new AudioClip(Paths.get("sounds/SEI_vs_ok.wav").toUri().toString());
    static final AudioClip rent = new AudioClip(Paths.get("sounds/10_Pay Rent 1.mp3").toUri().toString());
    static final AudioClip welfare = new AudioClip(Paths.get("sounds/SEB_platinum.wav").toUri().toString());
    static final AudioClip welfareTax = new AudioClip(Paths.get("sounds/18_Pay Tax.mp3").toUri().toString());
    static final AudioClip goldenKey = new AudioClip(Paths.get("sounds/SEB_item.wav").toUri().toString());
    static final AudioClip desertedIsland = new AudioClip(Paths.get("sounds/Thunder 01.mp3").toUri().toString());
    static final AudioClip enterSpaceTravel =
            new AudioClip(Paths.get("sounds/SEP_time_increase.wav").toUri().toString());
    static final AudioClip spaceTravel = new AudioClip(Paths.get("sounds/SEP_stageskip.wav").toUri().toString());
}