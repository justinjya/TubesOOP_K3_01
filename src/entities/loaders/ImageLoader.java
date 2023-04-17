package src.entities.loaders;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import src.main.Consts;

public class ImageLoader {
    private static BufferedImage scaleImage(BufferedImage image, int x, int y) {
        BufferedImage scaledImage = new BufferedImage(Consts.SCALED_TILE * x, Consts.SCALED_TILE * y, image.getType());
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(image, 0, 0, Consts.SCALED_TILE * x, Consts.SCALED_TILE * y, null);
        g.dispose();
        return scaledImage;
    }

    private static BufferedImage readImage(String folder, String fileName, int x, int y) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File("./src/assets/" + folder + "/" + fileName + ".png"));
            image = scaleImage(image, x, y);
            return image;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage[] loadSim() {
        BufferedImage[] images = new BufferedImage[12];
        images[0] = readImage("sim", "idle_down", 1, 1);
        images[1] = readImage("sim", "idle_up", 1, 1);
        images[2] = readImage("sim", "idle_left", 1, 1);
        images[3] = readImage("sim", "idle_right", 1, 1);
        images[4] = readImage("sim", "walk_down_1", 1, 1);
        images[5] = readImage("sim", "walk_down_2", 1, 1);
        images[6] = readImage("sim", "walk_up_1", 1, 1);
        images[7] = readImage("sim", "walk_up_2", 1, 1);
        images[8] = readImage("sim", "walk_left_1", 1, 1);
        images[9] = readImage("sim", "walk_left_2", 1, 1);
        images[10] = readImage("sim", "walk_right_1", 1, 1);
        images[11] = readImage("sim", "walk_right_2", 1, 1);
        return images;
    }

    public static BufferedImage[] loadBeds() {
        BufferedImage[] images = new BufferedImage[6];
        images[0] = readImage("beds", "bed_idle", 4, 1);
        images[1] = readImage("beds", "bed_occupied", 4, 1);
        return images;
    }
}
