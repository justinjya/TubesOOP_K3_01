package src.entities.interactables;

import java.awt.image.BufferedImage;
import src.assets.ImageLoader;
import src.entities.sim.Sim;
import src.main.Consts;
import src.main.GameTime;

public class Shower extends Interactables {
    // Attributes
    private int price = 0; // TO BE DETERMINED
    private int duration = 10;

    // Images of the shower
    private BufferedImage[] images;
    private BufferedImage[] icons;

    // CONSTRUCTOR
    public Shower(int x, int y) {
        super (
            "Shower",
            "take a shower",
            0,
            x,
            y,
            1,
            2
        );

        setPrice(price);
        setDuration(duration);

        // Load the image of the shower
        images = ImageLoader.loadShower();
    }

    @Override
    public BufferedImage getIcon() {
        return icons[getImageIndex()];
    }

    @Override
    public BufferedImage getImage() {
        return images[getImageIndex()];
    }

    @Override
    public void interact (Sim sim){
        Thread showering = new Thread() {
            @Override
            public void run() {
                try {
                    changeOccupiedState();
                    sim.setStatus("Taking a shower");
                    // count the time
                    GameTime.startDecrementTimeRemaining(Consts.ONE_SECOND * getDuration());
                    Thread.sleep(Consts.THREAD_ONE_SECOND * getDuration());
                    changeOccupiedState();
                    sim.resetStatus();
                    sim.setHealth(sim.getHealth() + 10); // increase sim's health
                    sim.setMood(sim.getMood() + 15); // increase sim's mood
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        showering.start();
    }
    
}