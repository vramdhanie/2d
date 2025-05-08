package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import com.niravramdhanie.twod.game.graphics.Animation;
import com.niravramdhanie.twod.game.graphics.SpriteSheet;
import com.niravramdhanie.twod.game.utils.ResourceLoader;

public class BallPlayer extends Entity {
    // Movement flags
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    
    // Movement properties
    private float moveSpeed;
    private float maxSpeed;
    
    // Health properties
    private int health;
    private int maxHealth;
    
    // Animations
    private BufferedImage spriteImage; // Cache the original sprite image
    private SpriteSheet spriteSheet;
    private Animation idleAnim;
    private Animation leftAnim;
    private Animation rightAnim;
    private Animation upAnim;
    private Animation downAnim;
    private Animation currentAnim;
    private boolean spritesLoaded = false; // Flag to track if sprites are loaded
    
    // Game properties
    private int screenWidth;
    private int screenHeight;
    private List<Block> blocks;
    
    public BallPlayer(float x, float y, int width, int height, int screenWidth, int screenHeight) {
        super(x, y, width, height);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Set movement properties - instant movement with no friction
        moveSpeed = 0.84f;   // 1.2f * 0.7 = 0.84f
        maxSpeed = 2.8f;     // 4.0f * 0.7 = 2.8f
        
        // Set health properties
        this.maxHealth = 100;
        this.health = this.maxHealth;
        
        // Initialize flags
        left = false;
        right = false;
        up = false;
        down = false;
        
        // Load sprites and animations
        initAnimations();
    }
    
    private void initAnimations() {
        try {
            // Load sprite sheet
            spriteImage = ResourceLoader.loadImage("/sprites/ball.png");
            
            if (spriteImage != null) {
                System.out.println("Player sprite loaded successfully: " + spriteImage.getWidth() + "x" + spriteImage.getHeight());
                
                // Based on the file command output, the ball.png is a single 32x32 sprite
                // Not a sprite sheet as previously assumed
                
                // Create a single frame animation for all directions
                Animation singleFrameAnim = new Animation();
                singleFrameAnim.addFrame(spriteImage, 150);
                
                if (singleFrameAnim.hasFrames()) {
                    // Use the same animation for all directions since it's just one image
                    idleAnim = singleFrameAnim;
                    leftAnim = singleFrameAnim;
                    rightAnim = singleFrameAnim;
                    upAnim = singleFrameAnim;
                    downAnim = singleFrameAnim;
                    
                    // Set default animation
                    currentAnim = idleAnim;
                    spritesLoaded = true;
                    System.out.println("Player animation initialized successfully with single sprite");
                } else {
                    System.err.println("Failed to create animation from ball sprite");
                    createEmptyAnimations();
                }
            } else {
                System.err.println("Failed to load player sprite image");
                // Create empty animations
                createEmptyAnimations();
            }
        } catch (Exception e) {
            System.err.println("Error initializing animations: " + e.getMessage());
            e.printStackTrace();
            // Create empty animations as fallback
            createEmptyAnimations();
        }
    }
    
    private void createEmptyAnimations() {
        spritesLoaded = false;
        // Create basic animations with null frames as a fallback
        idleAnim = new Animation();
        leftAnim = new Animation();
        rightAnim = new Animation();
        upAnim = new Animation();
        downAnim = new Animation();
        currentAnim = idleAnim;
    }
    
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }
    
    @Override
    public void update() {
        // Track previous animation for transition checks
        Animation previousAnim = currentAnim;
        
        // Reset velocity immediately when no keys are pressed for instant stopping
        if (!left && !right) velocity.x = 0;
        if (!up && !down) velocity.y = 0;
        
        // Calculate input direction vector
        float dirX = 0, dirY = 0;
        if (left) dirX -= 1;
        if (right) dirX += 1;
        if (up) dirY -= 1;
        if (down) dirY += 1;
        
        // Normalize diagonal movement
        if (dirX != 0 && dirY != 0) {
            // Calculate the normalized direction
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= length;
            dirY /= length;
        }
        
        // Apply movement only if keys are pressed (no acceleration buildup)
        if (dirX != 0) {
            velocity.x = dirX * maxSpeed;
        }
        
        if (dirY != 0) {
            velocity.y = dirY * maxSpeed;
        }
        
        // Set appropriate animation based on movement direction
        if (velocity.x < 0 && Math.abs(velocity.x) > Math.abs(velocity.y)) {
            if (spritesLoaded) currentAnim = leftAnim;
        } else if (velocity.x > 0 && Math.abs(velocity.x) > Math.abs(velocity.y)) {
            if (spritesLoaded) currentAnim = rightAnim;
        } else if (velocity.y < 0 && Math.abs(velocity.y) > Math.abs(velocity.x)) {
            if (spritesLoaded) currentAnim = upAnim;
        } else if (velocity.y > 0 && Math.abs(velocity.y) > Math.abs(velocity.x)) {
            if (spritesLoaded) currentAnim = downAnim;
        } else if (velocity.x == 0 && velocity.y == 0) {
            if (spritesLoaded) currentAnim = idleAnim;
        }
        
        // If animation changed, reset the animation
        if (previousAnim != currentAnim && currentAnim != null) {
            currentAnim.reset();
        }
        
        // Calculate new position
        float newX = position.x + velocity.x;
        float newY = position.y + velocity.y;
        
        // Check screen boundaries
        if (newX < 0) newX = 0;
        if (newX > screenWidth - width) newX = screenWidth - width;
        if (newY < 0) newY = 0;
        if (newY > screenHeight - height) newY = screenHeight - height;
        
        // Check block collisions for X movement
        boolean collisionX = false;
        position.x = newX;
        for (Block block : blocks) {
            if (checkCollision(block)) {
                collisionX = true;
                // Resolve X collision
                if (velocity.x > 0) { // Moving right
                    position.x = block.getX() - width;
                } else if (velocity.x < 0) { // Moving left
                    position.x = block.getX() + block.getWidth();
                }
                velocity.x = 0;
                break;
            }
        }
        
        // Check block collisions for Y movement
        boolean collisionY = false;
        position.y = newY;
        for (Block block : blocks) {
            if (checkCollision(block)) {
                collisionY = true;
                // Resolve Y collision
                if (velocity.y > 0) { // Moving down
                    position.y = block.getY() - height;
                } else if (velocity.y < 0) { // Moving up
                    position.y = block.getY() + block.getHeight();
                }
                velocity.y = 0;
                break;
            }
        }
        
        // If no collision occurred, move normally
        if (!collisionX) position.x = newX;
        if (!collisionY) position.y = newY;
        
        // Update current animation
        if (spritesLoaded && currentAnim != null) {
            currentAnim.update();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        try {
            // Check if we have a valid sprite animation
            if (spritesLoaded && currentAnim != null) {
                BufferedImage currentFrame = currentAnim.getCurrentFrame();
                if (currentFrame != null) {
                    g.drawImage(currentFrame, (int)position.x, (int)position.y, width, height, null);
                    return;
                } else {
                    System.err.println("Current animation frame is null: " + 
                        (currentAnim == idleAnim ? "idle" : 
                         currentAnim == leftAnim ? "left" : 
                         currentAnim == rightAnim ? "right" : 
                         currentAnim == upAnim ? "up" : 
                         currentAnim == downAnim ? "down" : "unknown"));
                }
            } else {
                System.err.println("Cannot render sprite: spritesLoaded=" + spritesLoaded + 
                                  ", currentAnim=" + (currentAnim == null ? "null" : "not null"));
            }
            
            // Fallback if sprites aren't loaded or animation has no frames
            g.setColor(Color.RED);
            g.fillOval((int)position.x, (int)position.y, width, height);
        } catch (Exception e) {
            System.err.println("Error rendering player: " + e.getMessage());
            e.printStackTrace();
            
            // Emergency fallback
            g.setColor(Color.RED);
            g.fillOval((int)position.x, (int)position.y, width, height);
        }
    }
    
    // Health getters and setters
    public int getHealth() { return health; }
    public void setHealth(int health) { 
        this.health = Math.max(0, Math.min(maxHealth, health)); 
    }
    
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { 
        this.maxHealth = maxHealth;
        // Adjust current health if it exceeds the new max
        if (health > maxHealth) health = maxHealth;
    }
    
    public void damage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    // Input handlers
    public void setLeft(boolean left) { this.left = left; }
    public void setRight(boolean right) { this.right = right; }
    public void setUp(boolean up) { this.up = up; }
    public void setDown(boolean down) { this.down = down; }
}