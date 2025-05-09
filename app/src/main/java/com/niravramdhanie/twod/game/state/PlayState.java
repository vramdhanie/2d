package com.niravramdhanie.twod.game.state;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.niravramdhanie.twod.game.actions.Action;
import com.niravramdhanie.twod.game.actions.DoorAction;
import com.niravramdhanie.twod.game.actions.DoorController;
import com.niravramdhanie.twod.game.actions.MultiButtonAction;
import com.niravramdhanie.twod.game.actions.TimedAction;
import com.niravramdhanie.twod.game.core.GameStateManager;
import com.niravramdhanie.twod.game.entity.BallPlayer;
import com.niravramdhanie.twod.game.entity.Block;
import com.niravramdhanie.twod.game.entity.Button;
import com.niravramdhanie.twod.game.entity.Door;
import com.niravramdhanie.twod.game.level.Level;
import com.niravramdhanie.twod.game.utils.RewindManager;
import com.niravramdhanie.twod.game.utils.TimerManager;

public class PlayState extends GameState {
    private BallPlayer player;
    private Level level;
    private int screenWidth;
    private int screenHeight;
    private Random random;
    private boolean initialized = false;
    
    // Grid cell size (can be easily changed)
    private static final int GRID_CELL_SIZE = 32;
    
    // Timer variables
    private long startTime;
    private int timerDuration = 60; // Duration in seconds
    private Font timerFont;
    
    // Timer manager
    private TimerManager timerManager;
    
    // Rewind feature
    private RewindManager rewindManager;
    private boolean rewindEnabled = true;
    
    // Door controls
    private Door door;
    private DoorController doorController;
    private String doorId = "main_door";
    
    // Button IDs
    private String buttonTopId = "button_top";
    private String buttonBottomId = "button_bottom";
    
    // List for timed actions that need updates
    private List<TimedAction> timedActions = new ArrayList<>();
    
    // Multi-button action for two buttons
    private MultiButtonAction multiButtonAction;
    
    // New variables for button highlighting
    private List<Button> nearButtons = new ArrayList<>();
    
    public PlayState(GameStateManager gsm, int screenWidth, int screenHeight) {
        super(gsm);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        this.timerFont = new Font("Arial", Font.BOLD, 24);
        System.out.println("PlayState created with dimensions: " + screenWidth + "x" + screenHeight);
    }
    
    // Overload constructor for backward compatibility if needed
    public PlayState(GameStateManager gsm) {
        super(gsm);
        this.screenWidth = gsm.getWidth();
        this.screenHeight = gsm.getHeight();
        this.random = new Random();
        this.timerFont = new Font("Arial", Font.BOLD, 24);
        System.out.println("PlayState created with dimensions from GSM: " + screenWidth + "x" + screenHeight);
    }
    
    @Override
    public void init() {
        System.out.println("PlayState.init() called");
        
        // Create the level with a grid
        level = new Level(screenWidth, screenHeight, GRID_CELL_SIZE);
        
        // Create level 1 layout with border blocks
        level.createLevel1();
        
        // Initialize door controller
        doorController = new DoorController();
        
        // Add the door and buttons
        setupLevel1();
        
        try {
            // Create player in the middle left of the screen
            int gridCellSize = level.getGrid().getCellSize();
            int playerSize = gridCellSize; // Make player the same size as grid cells
            
            // Position player at the middle left of the grid
            int gridX = 2; // A few cells from the left border
            int gridY = level.getGrid().getVerticalCells() / 2;
            int playerX = level.getGrid().gridToScreenX(gridX);
            int playerY = level.getGrid().gridToScreenY(gridY);
            
            System.out.println("Creating player at: " + playerX + "," + playerY);
            player = new BallPlayer(playerX, playerY, playerSize, playerSize, screenWidth, screenHeight);
            
            // Pass the blocks to the player for collision detection
            player.setBlocks(level.getBlocks());
            
            // Add door to collision blocks if it's closed
            updateDoorCollision();
            
            // Initialize the timer manager
            timerManager = new TimerManager(timerDuration);
            timerManager.start();
            
            // Initialize the rewind manager
            rewindManager = new RewindManager(player, level.getButtons(), timerManager);
            
            // Set the rewind manager for buttons
            Button.setRewindManager(rewindManager);
            
            initialized = true;
            System.out.println("PlayState initialization complete");
        } catch (Exception e) {
            System.err.println("Error initializing player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the level 1 layout with door and timed buttons
     */
    private void setupLevel1() {
        int cellSize = level.getGrid().getCellSize();
        int horizontalCells = level.getGrid().getHorizontalCells();
        int verticalCells = level.getGrid().getVerticalCells();
        
        // Create the door in the middle of the right side
        int doorGridX = horizontalCells - 2; // One cell from the right border
        int doorGridY = verticalCells / 2;
        
        float doorX = level.getGrid().gridToScreenX(doorGridX);
        float doorY = level.getGrid().gridToScreenY(doorGridY);
        door = new Door(doorX, doorY, cellSize, cellSize, doorId);
        level.addEntity(door, doorGridX, doorGridY);
        
        // Register door with controller
        doorController.registerDoor(door);
        
        // Create a door action that the multi-button action will control
        DoorAction doorAction = new DoorAction(doorId);
        doorAction.setDoorStateChangeListener(doorController);
        
        // Create a multi-button action that requires both buttons
        // Use permanent activation so door stays open once opened
        multiButtonAction = new MultiButtonAction(doorAction, true);
        multiButtonAction.addRequiredButton(buttonTopId);
        multiButtonAction.addRequiredButton(buttonBottomId);
        
        // Create the top button (1-second timer)
        int topButtonGridX = horizontalCells / 2;
        int topButtonGridY = 2; // Two cells from the top
        
        float topButtonX = level.getGrid().gridToScreenX(topButtonGridX);
        float topButtonY = level.getGrid().gridToScreenY(topButtonGridY);
        
        // Create a timed action for the top button
        TimedAction topTimedAction = createTimedButtonAction(buttonTopId, 1000); // 1 second
        
        Button topButton = new Button(topButtonX, topButtonY, cellSize, cellSize, topTimedAction);
        topButton.setColor(new Color(200, 80, 80)); // Red when inactive
        topButton.setActiveColor(new Color(255, 100, 100)); // Brighter red when active
        level.addEntity(topButton, topButtonGridX, topButtonGridY);
        
        // Register the timed action for updates
        timedActions.add(topTimedAction);
        
        // Create the bottom button (1-second timer)
        int bottomButtonGridX = horizontalCells / 2;
        int bottomButtonGridY = verticalCells - 3; // Two cells from the bottom
        
        float bottomButtonX = level.getGrid().gridToScreenX(bottomButtonGridX);
        float bottomButtonY = level.getGrid().gridToScreenY(bottomButtonGridY);
        
        // Create a timed action for the bottom button
        TimedAction bottomTimedAction = createTimedButtonAction(buttonBottomId, 1000); // 1 second
        
        Button bottomButton = new Button(bottomButtonX, bottomButtonY, cellSize, cellSize, bottomTimedAction);
        bottomButton.setColor(new Color(80, 80, 200)); // Blue when inactive
        bottomButton.setActiveColor(new Color(100, 100, 255)); // Brighter blue when active
        level.addEntity(bottomButton, bottomButtonGridX, bottomButtonGridY);
        
        // Register the timed action for updates
        timedActions.add(bottomTimedAction);
        
        // Start with the door closed
        door.close();
        
        System.out.println("Level 1 setup complete with door and two timed buttons");
    }
    
    /**
     * Creates a timed action that updates the multi-button action when a button is pressed
     */
    private TimedAction createTimedButtonAction(String buttonId, long duration) {
        // Create action that updates the multi-button action when activated
        Action activateAction = new UpdateMultiButtonAction(buttonId, true);
        
        // Create action that updates when deactivated
        Action deactivateAction = new UpdateMultiButtonAction(buttonId, false);
        
        // Create timed action with both activate and deactivate actions
        TimedAction timedAction = new TimedAction(activateAction, duration);
        timedAction.setDeactivateAction(deactivateAction);
        
        return timedAction;
    }
    
    /**
     * Inner class for actions that update the multi-button state
     */
    private class UpdateMultiButtonAction implements Action {
        private String buttonId;
        private boolean activated;
        
        public UpdateMultiButtonAction(String buttonId, boolean activated) {
            this.buttonId = buttonId;
            this.activated = activated;
        }
        
        @Override
        public void execute() {
            // Update the multi-button action with this button's state
            boolean changed = multiButtonAction.updateButtonState(buttonId, activated);
            
            // Always respect permanent activation - never close the door once it's been fully activated
            if (multiButtonAction.isPermanentlyActivated()) {
                // Ensure door stays open if permanently activated
                if (door != null && !door.isOpen()) {
                    door.open();
                    System.out.println("Door will remain open permanently!");
                }
                return;
            }
            
            // Only handle non-permanent state below this point
            
            // If a button is deactivated and we haven't achieved permanent activation yet,
            // make sure the door is closed
            if (!activated) {
                // Manually update the door state to ensure it closes
                if (door != null && door.isOpen()) {
                    door.close();
                    System.out.println("Door closed - both buttons must be pressed simultaneously!");
                }
            }
        }
        
        @Override
        public String getDescription() {
            return "Update " + buttonId + " to " + (activated ? "activated" : "deactivated");
        }
    }
    
    /**
     * Get the remaining time in seconds
     * @return Remaining time in seconds (0 if timer has expired)
     */
    private int getRemainingTime() {
        // Use the timer manager instead of calculating directly
        return timerManager.getTime();
    }
    
    /**
     * Changes the current level layout
     * @param layoutType The type of layout to create
     */
    public void setLevelLayout(int layoutType) {
        // Clear the level
        level.clearLevel();
        
        // Recreate level 1
        level.createLevel1();
        
        // Update player blocks
        player.setBlocks(level.getBlocks());
    }
    
    @Override
    public void update() {
        if (!initialized) return;
        
        try {
            // Update timer
            timerManager.update();
            
            // Update rewind manager
            if (rewindManager != null) {
                rewindManager.update();
            }
            
            // Update timed actions
            for (TimedAction timedAction : timedActions) {
                timedAction.update();
            }
            
            // Update level (includes buttons, etc.)
            level.update();
            
            // Update player position
            if (player != null) {
                player.update();
            }
            
            // Check for interaction with buttons
            checkButtonHighlights();
            
            // Check if door has been successfully opened for the first time
            if (multiButtonAction != null && multiButtonAction.isPermanentlyActivated() && door != null && !door.isOpen()) {
                // Force the door to open if it's not already open
                door.open();
                System.out.println("Door permanently opened! It will not close again.");
            }
            
            // Update door collision if necessary
            updateDoorCollision();
            
        } catch (Exception e) {
            System.err.println("Error in PlayState.update(): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!initialized) return;
        
        try {
            // Clear the screen
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, screenWidth, screenHeight);
            
            // Draw the grid (if needed for debugging)
            // level.getGrid().render(g);
            
            // Draw the level blocks
            level.render(g);
            
            // Draw player
            if (player != null) {
                player.render(g);
            }
            
            // Draw remaining time
            renderTimer(g);
            
            // Draw interaction indicator for buttons
            drawButtonHighlights(g);
            
            // Draw rewind status indicator
            drawRewindStatusIndicator(g);
            
        } catch (Exception e) {
            System.err.println("Error in PlayState.render(): " + e.getMessage());
            e.printStackTrace();
            
            // Display error information
            g.setColor(Color.RED);
            g.drawString("Render Error: " + e.getMessage(), 10, 20);
        }
    }
    
    /**
     * Renders the timer at the top center of the screen
     */
    private void renderTimer(Graphics2D g) {
        // Use the timer manager to render the timer
        if (timerManager != null) {
            timerManager.render(g, screenWidth);
        }
    }
    
    /**
     * Draws an indicator showing the current rewind state
     */
    private void drawRewindStatusIndicator(Graphics2D g) {
        if (rewindManager == null) return;
        
        // Save original color and font
        Color originalColor = g.getColor();
        Font originalFont = g.getFont();
        
        // Set font and position for rewind indicator
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int y = screenHeight - 40;
        
        // Draw different indicators based on rewind state
        switch (rewindManager.getCurrentState()) {
            case RECORDING:
                // Red recording indicator
                g.setColor(Color.RED);
                g.fillOval(10, y, 15, 15);
                g.drawString("Recording", 30, y + 12);
                break;
                
            case REWINDING:
                // Flashing blue rewind indicator
                if ((System.currentTimeMillis() / 250) % 2 == 0) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.CYAN);
                }
                
                int[] xPoints = {20, 5, 5};
                int[] yPoints = {y + 7, y, y + 15};
                g.fillPolygon(xPoints, yPoints, 3);
                
                g.drawString("Rewinding", 30, y + 12);
                break;
                
            case IDLE:
                // Gray idle indicator
                g.setColor(Color.GRAY);
                g.drawOval(10, y, 15, 15);
                g.drawString("Press 'R' to Record", 30, y + 12);
                break;
        }
        
        // Restore original color and font
        g.setColor(originalColor);
        g.setFont(originalFont);
    }
    
    /**
     * Checks for buttons near the player and highlights them
     */
    private void checkButtonHighlights() {
        if (player == null) return;
        
        // Get all buttons from the level
        List<Button> buttons = level.getButtons();
        
        // Clear the list of nearby buttons
        nearButtons.clear();
        
        // Check each button to see if it's near the player
        for (Button button : buttons) {
            // Calculate distance between player center and button center
            float playerCenterX = player.getX() + player.getWidth() / 2;
            float playerCenterY = player.getY() + player.getHeight() / 2;
            float buttonCenterX = button.getX() + button.getWidth() / 2;
            float buttonCenterY = button.getY() + button.getHeight() / 2;
            
            // Calculate the distance in grid cells (not pixels)
            float dx = Math.abs(playerCenterX - buttonCenterX) / GRID_CELL_SIZE;
            float dy = Math.abs(playerCenterY - buttonCenterY) / GRID_CELL_SIZE;
            
            // Button is within 1 cell of player (Manhattan distance)
            if (dx <= 1 && dy <= 1) {
                nearButtons.add(button);
            }
        }
    }
    
    /**
     * Draws highlights around buttons that are near the player
     */
    private void drawButtonHighlights(Graphics2D g) {
        if (nearButtons.isEmpty()) return;
        
        // Save original stroke
        java.awt.Stroke originalStroke = g.getStroke();
        
        // Set a thicker stroke for highlighting
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.WHITE);
        
        // Draw highlight around each nearby button's grid cell
        for (Button button : nearButtons) {
            // Calculate the grid position for the button
            int gridX = level.getGrid().screenToGridX((int)button.getX());
            int gridY = level.getGrid().screenToGridY((int)button.getY());
            
            // Convert grid position to screen coordinates
            int screenX = level.getGrid().gridToScreenX(gridX);
            int screenY = level.getGrid().gridToScreenY(gridY);
            
            // Draw the cell outline with a pulsating effect
            long currentTime = System.currentTimeMillis();
            float pulseIntensity = (float)Math.abs(Math.sin(currentTime * 0.003)) * 0.5f + 0.5f;
            
            // Create a partially transparent white based on pulse
            Color highlightColor = new Color(
                1.0f, 1.0f, 1.0f, 0.5f + pulseIntensity * 0.5f
            );
            g.setColor(highlightColor);
            
            // Draw the rectangle around the grid cell
            g.drawRect(screenX, screenY, level.getGrid().getCellSize(), level.getGrid().getCellSize());
        }
        
        // Restore original stroke
        g.setStroke(originalStroke);
    }
    
    /**
     * Activates buttons near the player
     */
    private void activateNearbyButtons() {
        // Process each nearby button
        for (Button button : nearButtons) {
            boolean activated = button.activate();
            
            if (activated) {
                System.out.println("Button activated at " + button.getX() + "," + button.getY());
            }
        }
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
            g.drawString("Nearby buttons: " + nearButtons.size(), 10, 100);
            if (!nearButtons.isEmpty()) {
                g.drawString("Press 'E' to activate", 10, 120);
            }
        }
    }
    
    /**
     * Updates collision with the door based on its open/closed state
     */
    private void updateDoorCollision() {
        if (door == null || player == null) return;
        
        // Get current blocks from the level
        List<Block> blocks = new ArrayList<>(level.getBlocks());
        
        // Remove the door from blocks if it's in there
        blocks.removeIf(block -> block.equals(door));
        
        // Add door only if it's closed
        if (!door.isOpen()) {
            blocks.add(door);
        }
        
        // Update player's collision blocks
        player.setBlocks(blocks);
    }
    
    @Override
    public void keyPressed(int k) {
        // Handle player movement
        if (player != null) {
            if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) {
                player.setLeft(true);
            }
            if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) {
                player.setRight(true);
            }
            if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) {
                player.setUp(true);
            }
            if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) {
                player.setDown(true);
            }
        }
        
        // Handle interaction with 'E' key
        if (k == KeyEvent.VK_E) {
            activateNearbyButtons();
        }
        
        // Handle pause with Escape key
        if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.PAUSE_STATE);
        }
        
        // Handle rewind feature with 'R' key
        if (k == KeyEvent.VK_R && rewindEnabled && rewindManager != null) {
            rewindManager.toggleRewind();
        }
        
        // For now, all level layout keys just reset to level 1
        if (k == KeyEvent.VK_1 || k == KeyEvent.VK_2 || k == KeyEvent.VK_3 || k == KeyEvent.VK_0) {
            setLevelLayout(1);
        }
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