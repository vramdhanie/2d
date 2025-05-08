package com.niravramdhanie.twod.game.state;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.niravramdhanie.twod.game.actions.Action;
import com.niravramdhanie.twod.game.actions.ActionFactory;
import com.niravramdhanie.twod.game.core.GameStateManager;
import com.niravramdhanie.twod.game.entity.BallPlayer;
import com.niravramdhanie.twod.game.entity.Button;
import com.niravramdhanie.twod.game.entity.Entity;
import com.niravramdhanie.twod.game.level.Level;

public class PlayState extends GameState {
    private BallPlayer player;
    private Level level;
    private int screenWidth;
    private int screenHeight;
    private Random random;
    private boolean initialized = false;
    
    // Grid cell size (can be easily changed)
    private static final int GRID_CELL_SIZE = 32;
    
    public PlayState(GameStateManager gsm, int screenWidth, int screenHeight) {
        super(gsm);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        System.out.println("PlayState created with dimensions: " + screenWidth + "x" + screenHeight);
    }
    
    // Overload constructor for backward compatibility if needed
    public PlayState(GameStateManager gsm) {
        super(gsm);
        this.screenWidth = gsm.getWidth();
        this.screenHeight = gsm.getHeight();
        this.random = new Random();
        System.out.println("PlayState created with dimensions from GSM: " + screenWidth + "x" + screenHeight);
    }
    
    @Override
    public void init() {
        System.out.println("PlayState.init() called");
        
        // Create the level with a grid
        level = new Level(screenWidth, screenHeight, GRID_CELL_SIZE);
        
        // Create a level layout with random blocks
        level.addRandomBlocks(10);
        
        // Add some buttons to the level
        addTestButtons();
        
        try {
            // Create player in the lower middle of the screen
            int gridCellSize = level.getGrid().getCellSize();
            int playerSize = gridCellSize; // Make player the same size as grid cells
            
            // Position player at the bottom middle of the grid
            int gridX = level.getGrid().getHorizontalCells() / 2;
            int gridY = level.getGrid().getVerticalCells() - 3;
            int playerX = level.getGrid().gridToScreenX(gridX);
            int playerY = level.getGrid().gridToScreenY(gridY);
            
            System.out.println("Creating player at: " + playerX + "," + playerY);
            player = new BallPlayer(playerX, playerY, playerSize, playerSize, screenWidth, screenHeight);
            
            // Pass the blocks to the player for collision detection
            player.setBlocks(level.getBlocks());
            
            initialized = true;
            System.out.println("PlayState initialization complete");
        } catch (Exception e) {
            System.err.println("Error initializing player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Changes the current level layout
     * @param layoutType The type of layout to create
     */
    public void setLevelLayout(int layoutType) {
        level.createLayout(layoutType);
        player.setBlocks(level.getBlocks());
    }
    
    @Override
    public void update() {
        // Update the level entities
        level.update();
        
        // Update the player
        player.update();
    }
    
    @Override
    public void render(Graphics2D g) {
        // Clear screen with background color
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        // Draw level (blocks and other entities)
        level.render(g);
        
        // Draw grid lines for debugging (optional)
        drawGridLines(g);
        
        // Draw player
        if (player != null) {
            player.render(g);
        } else {
            // Fallback if player is null
            g.setColor(Color.RED);
            g.fillOval(screenWidth / 2 - 16, screenHeight - 16 - 50, 32, 32);
            
            // Debug message
            g.setColor(Color.WHITE);
            g.drawString("Player object is null!", 10, 20);
            
            // If not initialized, try to initialize
            if (!initialized) {
                System.out.println("Player was null, attempting to initialize again");
                init();
            }
        }
        
        // Draw grid info
        drawGridInfo(g);
    }
    
    /**
     * Draws debug grid lines
     */
    private void drawGridLines(Graphics2D g) {
        if (level == null || level.getGrid() == null) return;
        
        g.setColor(new Color(50, 50, 50, 100)); // Semi-transparent gray
        
        int cellSize = level.getGrid().getCellSize();
        int horizontalCells = level.getGrid().getHorizontalCells();
        int verticalCells = level.getGrid().getVerticalCells();
        
        // Draw vertical lines
        for (int x = 0; x <= horizontalCells; x++) {
            int screenX = level.getGrid().gridToScreenX(x);
            g.drawLine(screenX, 0, screenX, screenHeight);
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= verticalCells; y++) {
            int screenY = level.getGrid().gridToScreenY(y);
            g.drawLine(0, screenY, screenWidth, screenY);
        }
    }
    
    /**
     * Draws grid information for debugging
     */
    private void drawGridInfo(Graphics2D g) {
        if (level == null || level.getGrid() == null) return;
        
        g.setColor(Color.WHITE);
        g.drawString("Grid: " + level.getGrid().getHorizontalCells() + "x" + 
                     level.getGrid().getVerticalCells() + " cells", 10, 20);
        g.drawString("Cell size: " + level.getGrid().getCellSize() + "px", 10, 40);
        g.drawString("Blocks: " + level.getBlocks().size(), 10, 60);
        
        // Show player grid position
        if (player != null) {
            int playerGridX = level.getGrid().screenToGridX((int)player.getX());
            int playerGridY = level.getGrid().screenToGridY((int)player.getY());
            g.drawString("Player grid pos: " + playerGridX + "," + playerGridY, 10, 80);
            
            // Show nearby buttons info
            List<Button> nearButtons = getButtonsNearPlayer();
            g.drawString("Nearby buttons: " + nearButtons.size(), 10, 100);
            if (nearButtons.size() > 0) {
                g.drawString("Press 'E' to activate", 10, 120);
            }
        }
    }
    
    /**
     * Adds test buttons to the level
     */
    private void addTestButtons() {
        int cellSize = level.getGrid().getCellSize();
        
        // Create one button for each action type at different positions
        List<ActionFactory.ActionType> actionTypes = List.of(
            ActionFactory.ActionType.MESSAGE,
            ActionFactory.ActionType.TIMED,
            ActionFactory.ActionType.TOGGLE,
            ActionFactory.ActionType.DOOR,
            ActionFactory.ActionType.MULTI,
            ActionFactory.ActionType.CYCLING_MESSAGE
        );
        
        int horizontalCells = level.getGrid().getHorizontalCells();
        int verticalCells = level.getGrid().getVerticalCells();
        
        // Calculate positions based on the number of action types
        List<int[]> positions = new ArrayList<>();
        
        // Place buttons in the four corners and center
        positions.add(new int[]{3, 3});  // Top-left
        positions.add(new int[]{horizontalCells - 4, 3});  // Top-right
        positions.add(new int[]{3, verticalCells - 4});  // Bottom-left
        positions.add(new int[]{horizontalCells - 4, verticalCells - 4});  // Bottom-right
        positions.add(new int[]{horizontalCells / 2, verticalCells / 2});  // Center
        
        // Add extra position if needed
        if (actionTypes.size() > positions.size()) {
            positions.add(new int[]{horizontalCells / 3, verticalCells / 3});
        }
        
        // Create buttons for each action type
        for (int i = 0; i < actionTypes.size() && i < positions.size(); i++) {
            Button button = new Button(0, 0, cellSize, cellSize);
            Action action = ActionFactory.createAction(actionTypes.get(i), button);
            button.setAction(action);
            
            int[] pos = positions.get(i);
            level.addEntity(button, pos[0], pos[1]);
            
            System.out.println("Added button with action: " + actionTypes.get(i) + " at " + pos[0] + "," + pos[1]);
        }
        
        // Additionally, add a random button somewhere
        int attempts = 0;
        boolean placed = false;
        
        while (!placed && attempts < 10) {
            // Try to find a random unoccupied position
            int[] pos = level.findSuitableButtonPosition();
            
            if (pos != null) {
                Button randomButton = new Button(0, 0, cellSize, cellSize);
                Action randomAction = ActionFactory.createRandomAction(randomButton);
                randomButton.setAction(randomAction);
                
                level.addEntity(randomButton, pos[0], pos[1]);
                System.out.println("Added random button at " + pos[0] + "," + pos[1]);
                placed = true;
            }
            
            attempts++;
        }
        
        System.out.println("Added test buttons to the level");
    }
    
    /**
     * Gets all buttons that are within one grid cell distance of the player
     * 
     * @return A list of buttons that are near the player
     */
    private List<Button> getButtonsNearPlayer() {
        List<Button> nearButtons = new ArrayList<>();
        
        if (player == null || level == null) return nearButtons;
        
        // Get player's grid position
        int playerGridX = level.getGrid().screenToGridX((int)player.getX());
        int playerGridY = level.getGrid().screenToGridY((int)player.getY());
        
        // Check all adjacent cells (including diagonals)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                // Skip the player's own cell
                if (dx == 0 && dy == 0) continue;
                
                int checkX = playerGridX + dx;
                int checkY = playerGridY + dy;
                
                // Get entity at this position
                Entity entity = level.getEntityAt(checkX, checkY);
                
                // If it's a button, add it to the list
                if (entity instanceof Button) {
                    nearButtons.add((Button) entity);
                }
            }
        }
        
        return nearButtons;
    }
    
    /**
     * Activates buttons that are near the player
     * 
     * @return True if any button was activated, false otherwise
     */
    private boolean activateNearbyButtons() {
        List<Button> nearButtons = getButtonsNearPlayer();
        boolean anyActivated = false;
        
        for (Button button : nearButtons) {
            if (button.activate()) {
                anyActivated = true;
                System.out.println("Button activated!");
            }
        }
        
        return anyActivated;
    }
    
    @Override
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_LEFT) player.setLeft(true);
        if (k == KeyEvent.VK_RIGHT) player.setRight(true);
        if (k == KeyEvent.VK_UP) player.setUp(true);
        if (k == KeyEvent.VK_DOWN) player.setDown(true);
        
        // Alternative WASD controls
        if (k == KeyEvent.VK_A) player.setLeft(true);
        if (k == KeyEvent.VK_D) player.setRight(true);
        if (k == KeyEvent.VK_W) player.setUp(true);
        if (k == KeyEvent.VK_S) player.setDown(true);
        
        // Button activation with E key
        if (k == KeyEvent.VK_E) {
            activateNearbyButtons();
        }
        
        // Level layout switching for testing
        if (k == KeyEvent.VK_1) setLevelLayout(1);
        if (k == KeyEvent.VK_2) setLevelLayout(2);
        if (k == KeyEvent.VK_3) setLevelLayout(3);
        if (k == KeyEvent.VK_R) setLevelLayout(0); // Random blocks
    }
    
    @Override
    public void keyReleased(int k) {
        if (k == KeyEvent.VK_LEFT) player.setLeft(false);
        if (k == KeyEvent.VK_RIGHT) player.setRight(false);
        if (k == KeyEvent.VK_UP) player.setUp(false);
        if (k == KeyEvent.VK_DOWN) player.setDown(false);
        
        // Alternative WASD controls
        if (k == KeyEvent.VK_A) player.setLeft(false);
        if (k == KeyEvent.VK_D) player.setRight(false);
        if (k == KeyEvent.VK_W) player.setUp(false);
        if (k == KeyEvent.VK_S) player.setDown(false);
    }
    
    @Override
    public void mousePressed(int x, int y) {
        // Not used in this example
    }
    
    @Override
    public void mouseReleased(int x, int y) {
        // Not used in this example
    }
    
    @Override
    public void mouseMoved(int x, int y) {
        // Not used in this example
    }
}