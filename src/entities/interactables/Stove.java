package src.entities.interactables;

import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;

import src.assets.ImageLoader;
import src.main.UserInterface;
import src.main.KeyHandler;
import src.main.GameTime;
import src.items.Item;
import src.entities.sim.Sim;
import src.items.foods.BakedFood;
import src.entities.sim.Inventory;
import src.main.menus.RecipeBookMenu;
import src.entities.handlers.InteractionHandler;

public class Stove extends Interactables{
    // Types of stove
    private static String[] names = {
        "Gas Stove",
        "Electric Stove"
    };
    private static int[] width = {
        2,
        1
    };
    private static int[] height = {
        1,
        1
    };
    private static int[] prices = {
        100,
        200
    };

    // Attributes
    BakedFood foodToCook = null;
    
    // Images of stove
    private BufferedImage[] icons;
    private BufferedImage[] images;

    // CONSTRUCTOR
    public Stove(int imageIndex) {
        super (
            names[imageIndex],
            "cook",
            imageIndex,
            2,
            2,
            width[imageIndex],
            height[imageIndex]
        );

        if (imageIndex == 0) {
            setPlayAreaX(1);
        }
        setPrice(prices[imageIndex]);

        // Load the icons and images of the stoves
        this.icons = ImageLoader.loadStovesIcons(); 
        this.images = ImageLoader.loadStoves();
    }

    public Stove(int x, int y, int imageIndex) {
        super (
            names[imageIndex],
            "cook",
            imageIndex,
            x,
            y,
            width[imageIndex],
            height[imageIndex]
        );

        setPrice(prices[imageIndex]);

        // Load the icons and images of the stoves
        this.icons = ImageLoader.loadStovesIcons(); 
        this.images = ImageLoader.loadStoves();
    }

    public void setFoodToCook(BakedFood food) {
        this.foodToCook = food;
    }

    // IMPLEMENTATION OF ABSTRACT METHODS
    @Override
    public void changeOccupiedState() {
        if (!isOccupied()) {
            setImageIndex(getImageIndex() + 2);
        }
        else {
            setImageIndex(getImageIndex() - 2);
        }

        this.occupied = !this.occupied;
    }

    @Override
    public BufferedImage getIcon() {
        return icons[getImageIndex() % 2];
    }

    @Override
    public BufferedImage getImage() {
        return images[getImageIndex()];
    }

    @Override
    public void interact(Sim sim) {
        Thread cooking = new Thread() {
            @Override
            public void run() {
                UserInterface.viewRecipes();
                foodToCook = null;

                while (true) {
                    if (KeyHandler.isKeyPressed(KeyHandler.KEY_ENTER)) {
                        if (!RecipeBookMenu.isAllIngredientAvailable()) {
                            RecipeBookMenu.ableToCook();
                            return;
                        }

                        Sim currentSim = UserInterface.getCurrentSim();
                        InteractionHandler currentSimInteract = currentSim.getInteractionHandler();

                        Stove stove = (Stove) currentSimInteract.getInteractableObject();
                        BakedFood foodToCook = RecipeBookMenu.listOfBakedFoods.get(RecipeBookMenu.slotSelected);
        
                        stove.setFoodToCook(foodToCook);
                        UserInterface.viewRecipes();
                        break;
                    }
                    if (KeyHandler.isKeyPressed(KeyHandler.KEY_ESCAPE)) {
                        UserInterface.viewRecipes();
                        break;
                    }
                }

                if (foodToCook == null) return;
                
                double cookDuration = foodToCook.getHungerPoint() * 1.5;
                changeOccupiedState();
                images[getImageIndex()] = ImageLoader.changeSimColor(images[getImageIndex()], sim);
                
                sim.setStatus("Cooking");
                Thread t = GameTime.startDecrementTimeRemaining((int) cookDuration);

                while (t.isAlive()) continue;
                
                changeOccupiedState();
                sim.resetStatus();
                sim.setMood(sim.getMood() + 10);

                Inventory simInventory = sim.getInventory();

                try {
                    simInventory.addItem(foodToCook);
                    for (String ingredient : foodToCook.getIngredients()) {
                        for (Item rawFood : simInventory.getMapOfItems().keySet()) {
                            if (!ingredient.equals(rawFood.getName())) continue;

                            simInventory.removeItem(rawFood);
                        }
                    }
                }
                catch (ConcurrentModificationException cme) {}

                // reset the images
                images = ImageLoader.loadStoves();
            }
        };
        cooking.start();
    } 
}
