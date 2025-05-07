package com.vincentramdhanie.twod.game.state;

import com.vincentramdhanie.twod.game.core.GameStateManager;
import com.vincentramdhanie.twod.game.entity.BallPlayer;
import com.vincentramdhanie.twod.game.entity.Block;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayState extends GameState {
    private BallPlayer player;
    private List<Block> blocks;
    private int screenWidth;
    private int screenHeight;
    private Random random;
    private boolean initialized = false;
    
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
        
        // Create blocks
        blocks = new ArrayList<>();
        createBlocks(10); // Create 10 random blocks
        
        try {
            // Create player in the lower middle of the screen
            int playerSize = 32;
            int playerX = screenWidth / 2 - playerSize / 2;
            int playerY = screenHeight - playerSize - 50; // 50 pixels from bottom
            System.out.println("Creating player at: " + playerX + "," + playerY);
            player = new BallPlayer(playerX, playerY, playerSize, playerSize, screenWidth, screenHeight);
            player.setBlocks(blocks);
            
            initialized = true;
            System.out.println("PlayState initialization complete");
        } catch (Exception e) {
            System.err.println("Error initializing player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createBlocks(int numBlocks) {
        int blockWidth = 64;
        int blockHeight = 64;
        
        for (int i = 0; i < numBlocks; i++) {
            // Generate random positions, but don't spawn in the bottom center
            // where the player will start
            int x, y;
            boolean validPosition;
            
            do {
                validPosition = true;
                x = random.nextInt(screenWidth - blockWidth);
                y = random.nextInt(screenHeight - blockHeight);
                
                // Avoid spawning in the bottom center (player spawn area)
                int playerSpawnX = screenWidth / 2 - 50; // 50 is half player spawn width
                int playerSpawnY = screenHeight - 150; // 150 is approximate player spawn height
                int playerSpawnWidth = 100; // Width of the spawn area
                int playerSpawnHeight = 150; // Height of the spawn area
                
                // Check if block overlaps with player spawn area
                if (x < playerSpawnX + playerSpawnWidth &&
                    x + blockWidth > playerSpawnX &&
                    y < playerSpawnY + playerSpawnHeight &&
                    y + blockHeight > playerSpawnY) {
                    validPosition = false;
                }
                
                // Check if block overlaps with other blocks
                for (Block block : blocks) {
                    if (x < block.getX() + block.getWidth() + 10 &&
                        x + blockWidth + 10 > block.getX() &&
                        y < block.getY() + block.getHeight() + 10 &&
                        y + blockHeight + 10 > block.getY()) {
                        validPosition = false;
                        break;
                    }
                }
            } while (!validPosition);
            
            blocks.add(new Block(x, y, blockWidth, blockHeight));
        }
    }
    
    @Override
    public void update() {
        player.update();
        
        // Update blocks (if they had dynamic behavior)
        for (Block block : blocks) {
            block.update();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        // Clear screen with background color
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        // Draw blocks
        if (blocks != null) {
            for (Block block : blocks) {
                block.render(g);
            }
        }
        
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