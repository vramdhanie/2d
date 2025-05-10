package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.niravramdhanie.twod.game.utils.ResourceLoader;

/**
 * A box entity that can be picked up and carried by the player.
 * The box can be in an active or inactive state, where the active state
 * means it will follow the rewind system.
 */
public class Box extends Block {
    // Movement tracking
    private boolean isBeingCarried;
    private float relativeX; // Relative position to player when picked up
    private float relativeY;
    
    // Box properties
    private boolean isActive; // Whether the box follows the rewind system
    private boolean isMovable; // Whether the box can be picked up
    
    // Visual properties
    private Color boxColor;
    private Color activeBoxColor;
    private BufferedImage boxImage;
    private BufferedImage activeBoxImage;
    
    // Reference to the carrier (player that is carrying this box)
    private Entity carrier;
    
    /**
     * Creates a new box entity.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     * @param isActive Whether the box follows the rewind system
     * @param isMovable Whether the box can be picked up
     */
    public Box(float x, float y, int width, int height, boolean isActive, boolean isMovable) {
        super(x, y, width, height);
        this.isBeingCarried = false;
        this.isActive = isActive;
        this.isMovable = isMovable;
        this.carrier = null;
        
        // Default colors
        this.boxColor = new Color(139, 69, 19); // Brown
        this.activeBoxColor = new Color(205, 133, 63); // Peru (lighter brown)
        
        try {
            // Load box images (fallback to colors if images can't be loaded)
            boxImage = ResourceLoader.loadImage("/sprites/box.png");
            activeBoxImage = ResourceLoader.loadImage("/sprites/box_active.png");
        } catch (Exception e) {
            System.err.println("Error loading box images: " + e.getMessage());
            boxImage = null;
            activeBoxImage = null;
        }
    }
    
    @Override
    public void update() {
        // Box behavior is mainly handled in PlayState
    }
    
    /**
     * Override collision detection to disable collision when being carried or when colliding with the carrier
     */
    @Override
    public boolean checkCollision(Entity other) {
        // If being carried, don't register any collisions at all with the carrier
        if (isBeingCarried && (carrier == other || other == carrier)) {
            return false;
        }
        // Otherwise, use the standard collision detection
        return super.checkCollision(other);
    }
    
    @Override
    public void render(Graphics2D g) {
        // Use images if available, otherwise draw with colors
        if ((isActive && activeBoxImage != null) || (!isActive && boxImage != null)) {
            g.drawImage(isActive ? activeBoxImage : boxImage, 
                       (int)position.x, (int)position.y, width, height, null);
        } else {
            // Fallback to drawing with colors
            drawBox(g);
        }
    }
    
    /**
     * Draws the box with gradients and highlights for a 3D effect
     */
    private void drawBox(Graphics2D g) {
        // Save original paint
        java.awt.Paint originalPaint = g.getPaint();
        
        // Choose appropriate color based on state
        Color baseColor = isActive ? activeBoxColor : boxColor;
        Color topColor = baseColor.brighter();
        Color bottomColor = baseColor.darker();
        
        // Create gradient for 3D effect
        GradientPaint gradient = new GradientPaint(
            (int)position.x, (int)position.y, topColor,
            (int)position.x, (int)position.y + height, bottomColor
        );
        
        // Apply gradient and draw box
        g.setPaint(gradient);
        g.fillRect((int)position.x, (int)position.y, width, height);
        
        // Draw box edges (darker)
        g.setColor(bottomColor.darker());
        g.drawRect((int)position.x, (int)position.y, width, height);
        
        // Draw highlights
        int highlight = 4;
        int shadow = 4;
        
        // Top highlight
        g.setColor(new Color(255, 255, 255, 100));
        g.fillRect((int)position.x + 1, (int)position.y + 1, width - 2, highlight);
        
        // Left highlight
        g.fillRect((int)position.x + 1, (int)position.y + highlight + 1, 
                   highlight, height - highlight - shadow - 1);
        
        // Bottom shadow
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRect((int)position.x + 1, (int)position.y + height - shadow, 
                   width - 2, shadow - 1);
        
        // Right shadow
        g.fillRect((int)position.x + width - shadow, (int)position.y + highlight + 1, 
                   shadow - 1, height - highlight - shadow - 1);
        
        // Draw a small indicator if the box is active
        if (isActive) {
            int indicatorSize = 6;
            g.setColor(new Color(0, 255, 0, 180));
            g.fillOval((int)position.x + width - indicatorSize - 2, 
                       (int)position.y + 2, indicatorSize, indicatorSize);
        }
        
        // Restore original paint
        g.setPaint(originalPaint);
    }
    
    /**
     * Picks up the box, storing its relative position to the player.
     * 
     * @param playerX The player's X position
     * @param playerY The player's Y position
     * @param carrier The entity carrying this box
     * @return True if the box was picked up, false if it can't be picked up
     */
    public boolean pickUp(float playerX, float playerY, Entity carrier) {
        if (!isMovable || isBeingCarried) {
            return false;
        }
        
        // Store relative position to the player (center to center)
        float playerCenterX = playerX + carrier.getWidth() / 2;
        float playerCenterY = playerY + carrier.getHeight() / 2;
        float boxCenterX = position.x + width / 2;
        float boxCenterY = position.y + height / 2;
        
        // Calculate offset from player center to box center
        // This preserves the exact relative position at pickup moment
        relativeX = boxCenterX - playerCenterX;
        relativeY = boxCenterY - playerCenterY;
        
        // Log the pickup for debugging
        System.out.println("Box picked up with relative position: " + relativeX + ", " + relativeY);
        System.out.println("Player center: " + playerCenterX + ", " + playerCenterY);
        System.out.println("Box center: " + boxCenterX + ", " + boxCenterY);
        
        isBeingCarried = true;
        this.carrier = carrier;
        
        return true;
    }
    
    /**
     * Picks up the box without a specific carrier reference.
     * 
     * @param playerX The player's X position
     * @param playerY The player's Y position
     * @return True if the box was picked up, false if it can't be picked up
     */
    public boolean pickUp(float playerX, float playerY) {
        return pickUp(playerX, playerY, null);
    }
    
    /**
     * Updates the box position when being carried by the player.
     * 
     * @param playerX The player's X position
     * @param playerY The player's Y position
     */
    public void updateCarriedPosition(float playerX, float playerY) {
        if (!isBeingCarried) return;
        
        if (carrier != null) {
            // Calculate position using the current player position and the relative offset
            float playerCenterX = playerX + carrier.getWidth() / 2;
            float playerCenterY = playerY + carrier.getHeight() / 2;
            
            // Get player velocity if available (for smoother movement)
            float playerVelX = 0;
            float playerVelY = 0;
            if (carrier instanceof BallPlayer) {
                playerVelX = ((BallPlayer)carrier).getVelocity().x;
                playerVelY = ((BallPlayer)carrier).getVelocity().y;
            }
            
            // Position the box with its center at the correct offset from player center
            // Maintain the original relative position but adjust for any movement direction
            float boxCenterX = playerCenterX + relativeX;
            float boxCenterY = playerCenterY + relativeY;
            
            // Convert back to top-left corner position
            position.x = boxCenterX - width / 2;
            position.y = boxCenterY - height / 2;
        } else {
            // Fallback if carrier is null
            position.x = playerX + relativeX;
            position.y = playerY + relativeY;
        }
    }
    
    /**
     * Drops the box at its current position.
     */
    public void drop() {
        isBeingCarried = false;
        carrier = null;
        
        // Ensure the box is positioned on the grid when dropped
        // This helps prevent the box from being placed at weird positions
        if (carrier instanceof BallPlayer) {
            // Snap to grid if needed - uncomment if you want this behavior
            /*
            int gridCellSize = 32; // Should match GRID_CELL_SIZE in PlayState
            float gridX = Math.round(position.x / gridCellSize) * gridCellSize;
            float gridY = Math.round(position.y / gridCellSize) * gridCellSize;
            position.x = gridX;
            position.y = gridY;
            */
        }
        
        System.out.println("Box dropped at position: " + position.x + ", " + position.y);
    }
    
    /**
     * Checks if the box is being carried.
     * 
     * @return True if being carried, false otherwise
     */
    public boolean isBeingCarried() {
        return isBeingCarried;
    }
    
    /**
     * Gets the entity that is carrying this box.
     * 
     * @return The entity carrying this box, or null if not being carried
     */
    public Entity getCarrier() {
        return carrier;
    }
    
    /**
     * Checks if the box is in an active state (follows rewind).
     * 
     * @return True if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Sets whether the box is in an active state.
     * 
     * @param isActive Whether the box should follow the rewind system
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Checks if the box is movable.
     * 
     * @return True if the box can be picked up, false otherwise
     */
    public boolean isMovable() {
        return isMovable;
    }
    
    /**
     * Sets whether the box is movable.
     * 
     * @param isMovable Whether the box can be picked up
     */
    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }
    
    /**
     * Overrides position setter to handle the carried state
     */
    @Override
    public void setX(float x) {
        if (!isBeingCarried) {
            super.setX(x);
        }
    }
    
    /**
     * Overrides position setter to handle the carried state
     */
    @Override
    public void setY(float y) {
        if (!isBeingCarried) {
            super.setY(y);
        }
    }
    
    /**
     * Gets the relative X position to the carrier when picked up.
     * 
     * @return The relative X position
     */
    public float getRelativeX() {
        return relativeX;
    }
    
    /**
     * Gets the relative Y position to the carrier when picked up.
     * 
     * @return The relative Y position
     */
    public float getRelativeY() {
        return relativeY;
    }
    
    /**
     * Sets the relative X position to the carrier.
     * 
     * @param relativeX The relative X position
     */
    public void setRelativeX(float relativeX) {
        this.relativeX = relativeX;
    }
    
    /**
     * Sets the relative Y position to the carrier.
     * 
     * @param relativeY The relative Y position
     */
    public void setRelativeY(float relativeY) {
        this.relativeY = relativeY;
    }
} 