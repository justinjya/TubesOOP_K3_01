package src.entities.interactables;

import java.awt.image.BufferedImage;
import src.assets.ImageLoader;
import src.entities.sim.Sim;
import src.main.Consts;
import src.main.GameTime;

public class Aquarium extends Interactables {
    private int duration = Consts.ONE_SECOND * 5;

    // Images of the aquarium
    private BufferedImage[] images;

    // CONSTRUCTOR
    public Aquarium (int x, int y) {
        super (
            "Aquarium",
            "feed the fish",
            0,
            x,
            y,
            2,
            1
        );

        // Load the image of the shower
        images = ImageLoader.loadAquarium();
    }

    @Override
    public BufferedImage getIcon() {
        return images[0];
    }

    @Override
    public BufferedImage getImage() {
        return images[getImageIndex()];
    }

    @Override
    public void interact (Sim sim){
        Thread feedingfish = new Thread() {
            @Override
            public void run() {
                try {
                    changeOccupiedState();
                    sim.setStatus("Feeding the fish");
                    // count the time
                    GameTime.startDecrementTimeRemaining(Consts.ONE_SECOND * duration);
                    Thread.sleep(Consts.THREAD_ONE_SECOND * duration);
                    changeOccupiedState();
                    sim.resetStatus();
                    sim.setMood(sim.getMood() + 5); // increase sim's mood
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        feedingfish.start();
    }
}