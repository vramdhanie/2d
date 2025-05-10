package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.niravramdhanie.twod.game.actions.DoorAction;
import com.niravramdhanie.twod.game.utils.ResourceLoader;

/**
 * A door entity that can be opened or closed.
 * When closed, it blocks player movement.
 * When open, it allows the player to pass through.
 * Extends Block to work with collision detection.
 */
public class Door extends Block implements DoorAction.DoorStateChangeListener {
    private String id;
    private boolean isOpen;
    private boolean isPermanentlyOpen;
    private BufferedImage doorImage;
    private BufferedImage doorOpenImage;
    private Color doorColor = new Color(139, 69, 19); // Brown door color
    private Color frameColor = new Color(101, 67, 33); // Darker frame color
    private Color openIndicatorColor = new Color(0, 255, 0, 128); // Semi-transparent green
    private Color permanentIndicatorColor = new Color(255, 215, 0, 160); // Semi-transparent gold
    private long openTime; // Time when the door was opened
    
    /**
     * Creates a new door entity.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     * @param id The unique ID of the door
     */
    public Door(float x, float y, int width, int height, String id) {
        super(x, y, width, height);
        this.id = id;
        this.isOpen = false;
        this.isPermanentlyOpen = false;
        this.openTime = 0;
        
        try {
            // Load door images
            doorImage = ResourceLoader.loadImage("/sprites/door_closed.png");
            doorOpenImage = ResourceLoader.loadImage("/sprites/door_open.png");
        } catch (Exception e) {
            System.err.println("Error loading door images: " + e.getMessage());
            doorImage = null;
            doorOpenImage = null;
        }
    }
    
    @Override
    public void update() {
        // Update door state
        if (isOpen && openTime == 0) {
            openTime = System.currentTimeMillis();
        } else if (!isOpen) {
            openTime = 0;
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (doorImage != null && doorOpenImage != null) {
            // Render using images
            if (isOpen) {
                g.drawImage(doorOpenImage, (int)position.x, (int)position.y, width, height, null);
                // Draw open indicator
                if (isPermanentlyOpen) {
                    drawPermanentOpenIndicator(g);
                } else {
                    drawOpenIndicator(g);
                }
            } else {
                g.drawImage(doorImage, (int)position.x, (int)position.y, width, height, null);
            }
        } else {
            // Fallback rendering with rectangles
            if (isOpen) {
                // Draw frame but no door
                drawDoorFrame(g);
                // Draw open indicator
                if (isPermanentlyOpen) {
                    drawPermanentOpenIndicator(g);
                } else {
                    drawOpenIndicator(g);
                }
            } else {
                // Draw both frame and door
                drawDoorFrame(g);
                drawDoorBody(g);
            }
        }
    }
    
    /**
     * Draws an indicator that the door is permanently open.
     * 
     * @param g The graphics context
     */
    private void drawPermanentOpenIndicator(Graphics2D g) {
        // Store original color
        Color originalColor = g.getColor();
        
        // Create a pulsating effect with gold color
        float alpha = 0.7f;
        if (openTime > 0) {
            long elapsed = System.currentTimeMillis() - openTime;
            alpha = 0.4f + (float)Math.abs(Math.sin(elapsed * 0.002f)) * 0.3f;
        }
        
        // Draw a glowing golden outline around the door frame
        g.setColor(new Color(
            permanentIndicatorColor.getRed(), 
            permanentIndicatorColor.getGreen(), 
            permanentIndicatorColor.getBlue(), 
            (int)(alpha * 255)
        ));
        
        // Draw glow over the door frame
        int glowSize = 6;  // Larger glow for permanent
        g.fillRoundRect(
            (int)position.x - glowSize, 
            (int)position.y - glowSize, 
            width + glowSize * 2, 
            height + glowSize * 2,
            10, 10
        );
        
        // Add "PERMANENTLY OPEN" text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String text = "PERMANENTLY OPEN";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Draw background for text to improve readability
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(
            (int)position.x + (width - textWidth) / 2 - 2, 
            (int)position.y + height + 2, 
            textWidth + 4, 
            textHeight
        );
        
        // Draw text
        g.setColor(Color.YELLOW);
        g.drawString(text, 
            (int)position.x + (width - textWidth) / 2, 
            (int)position.y + height + textHeight);
        
        // Restore original color
        g.setColor(originalColor);
    }
    
    /**
     * Draws an indicator that the door is open.
     * 
     * @param g The graphics context
     */
    private void drawOpenIndicator(Graphics2D g) {
        // Store original color
        Color originalColor = g.getColor();
        
        // Create a pulsating effect
        float alpha = 0.6f;
        if (openTime > 0) {
            long elapsed = System.currentTimeMillis() - openTime;
            alpha = 0.3f + (float)Math.abs(Math.sin(elapsed * 0.003f)) * 0.3f;
        }
        
        // Draw a glowing outline around the door frame
        g.setColor(new Color(
            openIndicatorColor.getRed(), 
            openIndicatorColor.getGreen(), 
            openIndicatorColor.getBlue(), 
            (int)(alpha * 255)
        ));
        
        // Draw glow over the door frame
        int glowSize = 4;
        g.fillRoundRect(
            (int)position.x - glowSize, 
            (int)position.y - glowSize, 
            width + glowSize * 2, 
            height + glowSize * 2,
            8, 8
        );
        
        // Add "OPEN" text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String text = "OPEN";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g.drawString(text, 
            (int)position.x + (width - textWidth) / 2, 
            (int)position.y + height + textHeight);
        
        // Restore original color
        g.setColor(originalColor);
    }
    
    /**
     * Draws the door frame.
     * 
     * @param g The graphics context
     */
    private void drawDoorFrame(Graphics2D g) {
        // Draw the door frame
        g.setColor(frameColor);
        
        // Left side
        g.fillRect((int)position.x, (int)position.y, width / 8, height);
        
        // Right side
        g.fillRect((int)position.x + width - width / 8, (int)position.y, width / 8, height);
        
        // Top
        g.fillRect((int)position.x, (int)position.y, width, height / 8);
        
        // Bottom
        g.fillRect((int)position.x, (int)position.y + height - height / 8, width, height / 8);
    }
    
    /**
     * Draws the door body.
     * 
     * @param g The graphics context
     */
    private void drawDoorBody(Graphics2D g) {
        // Door body fill
        int doorBodyX = (int)position.x + width / 8;
        int doorBodyY = (int)position.y + height / 8;
        int doorBodyWidth = width - width / 4;
        int doorBodyHeight = height - height / 4;
        
        // Create a gradient for 3D effect
        GradientPaint gradient = new GradientPaint(
            doorBodyX, doorBodyY, doorColor.brighter(),
            doorBodyX + doorBodyWidth, doorBodyY, doorColor.darker()
        );
        
        // Save original paint
        java.awt.Paint originalPaint = g.getPaint();
        
        // Apply gradient and draw door
        g.setPaint(gradient);
        g.fillRect(doorBodyX, doorBodyY, doorBodyWidth, doorBodyHeight);
        
        // Add a door handle
        g.setColor(Color.BLACK);
        g.fillOval(doorBodyX + doorBodyWidth - doorBodyWidth / 6, 
                  doorBodyY + doorBodyHeight / 2, 
                  doorBodyWidth / 10, 
                  doorBodyWidth / 10);
        
        // Restore original paint
        g.setPaint(originalPaint);
    }
    
    /**
     * Opens the door.
     */
    public void open() {
        isOpen = true;
    }
    
    /**
     * Opens the door permanently, so it can't be closed again.
     */
    public void openPermanently() {
        isOpen = true;
        isPermanentlyOpen = true;
        System.out.println("Door " + id + " has been permanently opened!");
    }
    
    /**
     * Checks if the door is permanently open.
     * 
     * @return True if permanently open, false otherwise
     */
    public boolean isPermanentlyOpen() {
        return isPermanentlyOpen;
    }
    
    /**
     * Closes the door if it's not permanently open.
     */
    public void close() {
        if (!isPermanentlyOpen) {
            isOpen = false;
        }
    }
    
    /**
     * Checks if the door is open.
     * 
     * @return True if open, false if closed
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Gets the door ID.
     * 
     * @return The door ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the door ID.
     * 
     * @param id The door ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Toggles the door state unless it's permanently open.
     * 
     * @return The new door state
     */
    public boolean toggle() {
        if (!isPermanentlyOpen) {
            isOpen = !isOpen;
        }
        return isOpen;
    }
    
    @Override
    public void onDoorStateChanged(String doorId, boolean isOpen) {
        if (this.id.equals(doorId)) {
            if (isOpen) {
                open();
            } else if (!isPermanentlyOpen) {
                close();
            }
        }
    }
} 