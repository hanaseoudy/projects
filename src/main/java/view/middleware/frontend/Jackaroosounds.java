package view.middleware.frontend;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Jackaroosounds {

    static final List<Clip> clip = new ArrayList<>();

    public Jackaroosounds(String s1, String s2) {
        try {
            URL soundURL1 = getClass().getResource("/" + s1);
            URL soundURL2 = getClass().getResource("/" + s2);
            if (soundURL1 == null || soundURL2 == null) {
                System.out.println("Sound file not found!");
                return;
            }

            AudioInputStream audioStream1 = AudioSystem.getAudioInputStream(soundURL1);
            AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(soundURL2);
            Clip clip1 = AudioSystem.getClip();
            Clip clip2 = AudioSystem.getClip();
            clip1.open(audioStream1);
            clip2.open(audioStream2);
            clip.add(clip2);
            clip.add(clip1);


        } catch (Exception ignored) {
        }
    }

    public static void play() {
        if (JackarooController.Sound == 0)
            clip.getFirst().start();
    }

    public static void stop() {
        clip.getFirst().stop();
    }

}