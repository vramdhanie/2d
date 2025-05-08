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
import com.niravramdhanie.twod.game.actions.ActionFactory;
import com.niravramdhanie.twod.game.core.GameStateManager;
import com.niravramdhanie.twod.game.entity.BallPlayer;
import com.niravramdhanie.twod.game.entity.Button;
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
        level.createLayout(layoutType);
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
            
            // Update level (includes buttons, etc.)
            level.update();
            
            // Update player position
            if (player != null) {
                player.update();
            }
            
            // Check for interaction with buttons
            checkButtonHighlights();
            
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
        
        // Level layout switching for testing
        if (k == KeyEvent.VK_1) setLevelLayout(1);
        if (k == KeyEvent.VK_2) setLevelLayout(2);
        if (k == KeyEvent.VK_3) setLevelLayout(3);
        if (k == KeyEvent.VK_0) setLevelLayout(0); // Random blocks (changed from R to 0)
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