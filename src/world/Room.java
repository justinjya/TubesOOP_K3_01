package src.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import src.assets.ImageLoader;
import src.main.Consts;
import src.main.KeyHandler;
import src.entities.handlers.*;
import src.entities.sim.Sim;
import src.entities.interactables.*;
import src.entities.interactables.Stove;

public class Room {
    // Attributes
    private String name;
    private ArrayList<Interactables> listOfObjects;
    private ArrayList<Sim> listOfSims;
    
    // For adding, editing, and removing objects
    private House houseInsideOf;
    private boolean isEditingRoom;
    private CollisionHandler collisionHandler;
    private Interactables moveableObject = null;
    private Interactables selectedObject = null;

    // Position inside the game window
    private int centerX = Consts.WIDTH / 2 - 3 * Consts.SCALED_TILE;
    private int centerY = Consts.HEIGHT / 2 - 3 * Consts.SCALED_TILE;

    // Image for the room background
    private BufferedImage image;

    // CONSTRUCTOR
    public Room(String name) {
        // Attributes
        this.name = name;
        this.listOfObjects = new ArrayList<>(); 
        this.listOfSims = new ArrayList<>();
        this.isEditingRoom = false;

        // Load the image of the room
        this.image = ImageLoader.loadWood();

        // only for debug
        this.listOfObjects.add(new Stove(0, 0, 0));
    }

    // GETTERS
    public String getName() {
        return name;
    }

    public ArrayList<Interactables> getListOfObjects() {
        return listOfObjects;
    }

    public boolean isEditingRoom() {
        return isEditingRoom;
    }

    public ArrayList<Sim> getListOfSims() {
        return listOfSims;
    }

    public House getHouseInsideOf() {
        return houseInsideOf;
    }
    
    // SETTERS
    public void addSim(Sim sim) {
        listOfSims.add(sim);
    }

    public void removeSim(Sim sim) {
        listOfSims.remove(sim);
    }

    public void changeisEditingRoomState() {
        this.isEditingRoom = !this.isEditingRoom;
    }

    public void setHouseInsideOf(House house) {
        this.houseInsideOf = house;
    }

    public void addObject(Interactables object) {
        changeisEditingRoomState();
        moveableObject = object;
        collisionHandler = new CollisionHandler(moveableObject, this);
    }

    public void editObject(Interactables object) {
        moveableObject = object;
        listOfObjects.remove(object);
        collisionHandler = new CollisionHandler(moveableObject, this);
    }
    
    public void selectObject() {
        changeisEditingRoomState();
        try {
            for (Interactables object : listOfObjects) {
                if (object instanceof Door) continue;
    
                selectedObject = object;
                break;
            }
        }
        catch (ConcurrentModificationException e) {}
    }

    // OTHERS
    public void update() {
        // Editing an existing object
        updateSelectedObject();

        // Adding a new object
        updateUnaddedObject();
    }

    public void draw(Graphics2D g) {
        // Draw the room floor
        drawFloor(g);
        
        // Draw objects inside of the room
        drawObjects(g);

        // Draw sims inside of the room
        drawSims(g);

        // Draw the selector for an object
        drawObjectSelector(g);
        
        // Draw selected object
        drawSelectedObject(g);
    }
            
    private Interactables findNearestObject(String direction) {
        Interactables minObject = null;
        int minDistance = Integer.MAX_VALUE;
        int distance = Integer.MAX_VALUE;
        int dx = 0;
        int dy = 0;

        for (Interactables object : listOfObjects) {
            if (object == selectedObject || object instanceof Door) {
                continue;
            }
            
            dx = object.getX() - selectedObject.getX();
            dy = object.getY() - selectedObject.getY();
            distance = (int) Math.sqrt((dx * dx) + (dy * dy));
            
            switch (direction) {
                case "up":
                    if (dy < 0 && distance < minDistance)  {
                        minDistance = distance;
                        minObject = object;
                    }
                    break;
                case "left":
                    if (dx < 0 && distance < minDistance) {
                        minDistance = distance;
                        minObject = object;
                    }
                    break;
                case "down":
                    if (dy > 0 && distance < minDistance) {
                        minDistance = distance;
                        minObject = object;
                    }
                    break;
                case "right":
                    if (dx > 0 && distance < minDistance) {
                        minDistance = distance;
                        minObject = object;
                    }
                    break;
                default:
                    break;
            }
        }

        if (minObject == null) {
            return selectedObject;
        }
        else {
            return minObject;
        }
    }

    private void updateSelectedObject() {
        if (isEditingRoom && moveableObject == null) {
            // Find the nearest object based on the WASD keys
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_W)) {
                selectedObject = findNearestObject("up");
            }
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_A)) {
                selectedObject = findNearestObject("left");
            }
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_S)) {
                selectedObject = findNearestObject("down");
            }
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_D)) {
                selectedObject = findNearestObject("right");
            }

            if (KeyHandler.isKeyPressed(KeyHandler.KEY_ENTER)) {
                editObject(selectedObject);
            }
        }
    }

    // TO - DO !!! : Integrate with inventory
    private void updateUnaddedObject() {
        if (isEditingRoom && moveableObject != null) {
            boolean inCollision = false;
            boolean isCollidingWithSim = false;
            boolean isWallOccupied = false;

            inCollision = collisionHandler.isCollision(moveableObject.getX(), moveableObject.getY());
            isCollidingWithSim = collisionHandler.isCollidingWithSim(moveableObject.getX(), moveableObject.getY(), listOfSims);
            
            moveableObject.move(collisionHandler);
            moveableObject.updateBounds();

            // To rotate the door
            if (moveableObject instanceof Door) {
                Door door = (Door) moveableObject;
                isWallOccupied = collisionHandler.isWallOccupied(door);

                if (KeyHandler.isKeyPressed(KeyHandler.KEY_R)) {
                    door.rotate(door.getX(), door.getY());
                }
            }

            // Add the object if enter is pressed and object is not in collision with another object
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_ENTER) && (!inCollision && !isWallOccupied && !isCollidingWithSim)) {
                listOfObjects.add(moveableObject);
                moveableObject = null;
                changeisEditingRoomState();
            }

            // Cancel adding or moving an object if escape is pressed and add object into sim inventory
            if (KeyHandler.isKeyPressed(KeyHandler.KEY_ESCAPE)) {
                Sim sim = houseInsideOf.getOwner();
                
                if (!(moveableObject instanceof Door)) {
                    moveableObject.setX(0);
                    moveableObject.setY(3);
                    sim.getInventory().addItem(moveableObject);
                }
                moveableObject = null;
                changeisEditingRoomState();
            }
        }
    }

    private void drawFloor(Graphics2D g) {
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int tileX = centerX + (x * Consts.SCALED_TILE);
                int tileY = centerY + (y * Consts.SCALED_TILE) - Consts.OFFSET_Y;
                g.drawImage(image, tileX, tileY, Consts.SCALED_TILE, Consts.SCALED_TILE, null);
            }
        }
    }

    private void drawObjects(Graphics2D g) {
        try {
            for (Interactables object : listOfObjects) {
                object.draw(g, object);
            }
        }
        catch (ConcurrentModificationException e) {}
    }

    private void drawSims(Graphics2D g) {
        try {
            for (Sim sim: listOfSims) {
                sim.draw(g);
            }
        }
        catch (ConcurrentModificationException e) {}
    }

    // TO - DO !!! : Find a better way to show selecting an object
    // TO - DO !!! : Fix edit room when room is empty
    private void drawObjectSelector(Graphics2D g) {
        try {
            if (isEditingRoom && moveableObject == null) {
                g.setColor(new Color(255, 0, 0, 64)); // Transparent red color
                g.fillRect(selectedObject.getX(), selectedObject.getY(), selectedObject.getWidth(), selectedObject.getHeight());
            }
        }
        catch (NullPointerException e) {}
    }

    private void drawSelectedObject(Graphics2D g) {
        try {
            if (isEditingRoom && moveableObject != null) {
                moveableObject.draw(g, moveableObject);
            }
        }
        catch (NullPointerException e) {}
    }

    // ONLY FOR DEBUGGING
    public void drawCollisionBox(Graphics2D g) {
        for (Interactables object : listOfObjects) {
            object.drawCollisionBox(g);
        }
    }
}
