package com.niravramdhanie.twod.game.level;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.niravramdhanie.twod.game.entity.Block;
import com.niravramdhanie.twod.game.entity.Entity;
import com.niravramdhanie.twod.game.utils.GridSystem;

/**
 * Represents a game level with entities placed on a grid.
 */
public class Level {
    private GridSystem grid;
    private List<Entity> entities;
    private Random random;
    private int screenWidth;
    private int screenHeight;
    
    /**
     * Creates a new level with the specified dimensions.
     * 
     * @param screenWidth The width of the screen in pixels
     * @param screenHeight The height of the screen in pixels
     * @param gridCellSize The size of each grid cell in pixels
     */
    public Level(int screenWidth, int screenHeight, int gridCellSize) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.grid = new GridSystem(gridCellSize, gridCellSize, screenWidth, screenHeight);
        this.entities = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Adds a block at the specified grid position.
     * 
     * @param gridX The X position on the grid
     * @param gridY The Y position on the grid
     * @return True if the block was added successfully, false if the position was already occupied
     */
    public boolean addBlock(int gridX, int gridY) {
        int cellSize = grid.getCellSize();
        Block block = new Block(0, 0, cellSize, cellSize);
        
        if (grid.placeEntity(block, gridX, gridY)) {
            entities.add(block);
            return true;
        }
        
        return false;
    }
    
    /**
     * Adds any entity at the specified grid position.
     * 
     * @param entity The entity to add
     * @param gridX The X position on the grid
     * @param gridY The Y position on the grid
     * @return True if the entity was added successfully, false if the position was already occupied
     */
    public boolean addEntity(Entity entity, int gridX, int gridY) {
        int cellSize = grid.getCellSize();
        entity.setWidth(cellSize);
        entity.setHeight(cellSize);
        
        if (grid.placeEntity(entity, gridX, gridY)) {
            entities.add(entity);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes an entity at the specified grid position.
     * 
     * @param gridX The X position on the grid
     * @param gridY The Y position on the grid
     * @return True if an entity was removed, false otherwise
     */
    public boolean removeEntityAt(int gridX, int gridY) {
        Entity entity = grid.removeEntity(gridX, gridY);
        if (entity != null) {
            entities.remove(entity);
            return true;
        }
        return false;
    }
    
    /**
     * Gets the entity at the specified grid position.
     * 
     * @param gridX The X position on the grid
     * @param gridY The Y position on the grid
     * @return The entity at the specified position, or null if no entity is there
     */
    public Entity getEntityAt(int gridX, int gridY) {
        return grid.getEntity(gridX, gridY);
    }
    
    /**
     * Adds blocks at random positions on the grid.
     * 
     * @param count The number of blocks to add
     */
    public void addRandomBlocks(int count) {
        int horizontalCells = grid.getHorizontalCells();
        int verticalCells = grid.getVerticalCells();
        
        // Reserve space for player at bottom center
        int playerGridX = horizontalCells / 2;
        int playerGridY = verticalCells - 3;
        int reserveRadius = 2;
        
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            boolean placed = false;
            
            while (!placed && attempts < 100) {
                int gridX = random.nextInt(horizontalCells);
                int gridY = random.nextInt(verticalCells);
                
                // Avoid the player spawn area
                if (Math.abs(gridX - playerGridX) <= reserveRadius && 
                    Math.abs(gridY - playerGridY) <= reserveRadius) {
                    attempts++;
                    continue;
                }
                
                if (addBlock(gridX, gridY)) {
                    placed = true;
                } else {
                    attempts++;
                }
            }
        }
    }
    
    /**
     * Creates a border of blocks around the level.
     */
    public void addBorderBlocks() {
        int horizontalCells = grid.getHorizontalCells();
        int verticalCells = grid.getVerticalCells();
        
        // Add top and bottom borders
        for (int x = 0; x < horizontalCells; x++) {
            addBlock(x, 0);
            addBlock(x, verticalCells - 1);
        }
        
        // Add left and right borders (skip corners as they're already added)
        for (int y = 1; y < verticalCells - 1; y++) {
            addBlock(0, y);
            addBlock(horizontalCells - 1, y);
        }
    }
    
    /**
     * Updates all entities in the level.
     */
    public void update() {
        for (Entity entity : entities) {
            entity.update();
        }
    }
    
    /**
     * Renders all entities in the level.
     * 
     * @param g The Graphics2D object to render to
     */
    public void render(Graphics2D g) {
        for (Entity entity : entities) {
            entity.render(g);
        }
    }
    
    /**
     * Gets all entities in the level.
     * 
     * @return A list of all entities
     */
    public List<Entity> getEntities() {
        return entities;
    }
    
    /**
     * Gets all blocks in the level.
     * 
     * @return A list of all blocks
     */
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Block) {
                blocks.add((Block) entity);
            }
        }
        return blocks;
    }
    
    /**
     * Gets the grid system.
     * 
     * @return The grid system
     */
    public GridSystem getGrid() {
        return grid;
    }
    
    /**
     * Creates a predefined level layout.
     * 
     * @param layoutType The type of layout to create (1, 2, 3, etc.)
     */
    public void createLayout(int layoutType) {
        clearLevel();
        
        switch (layoutType) {
            case 1:
                // Border with some blocks in the middle
                addBorderBlocks();
                addBlock(5, 5);
                addBlock(5, 6);
                addBlock(6, 5);
                addBlock(10, 8);
                addBlock(11, 8);
                addBlock(12, 8);
                break;
                
            case 2:
                // Maze-like pattern
                addBorderBlocks();
                // Horizontal walls
                for (int x = 5; x < 10; x++) {
                    addBlock(x, 5);
                    addBlock(x + 5, 10);
                }
                // Vertical walls
                for (int y = 6; y < 10; y++) {
                    addBlock(5, y);
                    addBlock(15, y);
                }
                break;
                
            case 3:
                // Random scattered blocks
                addRandomBlocks(20);
                break;
                
            default:
                // Default: just add 10 random blocks
                addRandomBlocks(10);
                break;
        }
    }
    
    /**
     * Clears all entities from the level.
     */
    public void clearLevel() {
        entities.clear();
        grid.clearGrid();
    }
    
    /**
     * Resizes the level to fit a new screen size.
     * 
     * @param newScreenWidth The new screen width in pixels
     * @param newScreenHeight The new screen height in pixels
     */
    public void resize(int newScreenWidth, int newScreenHeight) {
        this.screenWidth = newScreenWidth;
        this.screenHeight = newScreenHeight;
        grid.resize(newScreenWidth, newScreenHeight);
    }
} 