package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.niravramdhanie.twod.game.actions.Action;
import com.niravramdhanie.twod.game.actions.MessageAction;
import com.niravramdhanie.twod.game.actions.TimedAction;
import com.niravramdhanie.twod.game.actions.ToggleAction;
import com.niravramdhanie.twod.game.utils.ResourceLoader;

/**
 * A button entity that can be placed on the grid and activated by the player.
 * The button is activated when the player is one grid space away and presses 'E'.
 */
public class Button extends Entity {
    private BufferedImage buttonImage;
    private BufferedImage buttonActiveImage;
    private Color color;
    private Color activeColor;
    private boolean activated;
    private Action action;
    
    /**
     * Creates a new button entity with the specified action.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     * @param action The action to execute when the button is activated
     */
    public Button(float x, float y, int width, int height, Action action) {
        super(x, y, width, height);
        this.action = action;
        this.activated = false;
        
        // Default colors
        this.color = new Color(200, 50, 50); // Red when inactive
        this.activeColor = new Color(50, 200, 50); // Green when active
        
        try {
            // Load button images
            buttonImage = ResourceLoader.loadImage("/sprites/button.png");
            buttonActiveImage = ResourceLoader.loadImage("/sprites/button_active.png");
        } catch (Exception e) {
            System.err.println("Error loading button images: " + e.getMessage());
            buttonImage = null;
            buttonActiveImage = null;
        }
        
        // If this is a timed action, set the target button
        if (action instanceof TimedAction) {
            ((TimedAction) action).setTargetButton(this);
        }
    }
    
    /**
     * Creates a new button entity with no action.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     */
    public Button(float x, float y, int width, int height) {
        this(x, y, width, height, null);
    }
    
    @Override
    public void update() {
        // For toggle actions, update button appearance based on toggle state
        if (action instanceof ToggleAction) {
            ToggleAction toggleAction = (ToggleAction) action;
            if (toggleAction.isToggled()) {
                this.color = new Color(70, 70, 200); // Blue when toggled on
                this.activeColor = new Color(70, 200, 70); // Green when active and toggled on
            } else {
                this.color = new Color(200, 50, 50); // Red when toggled off
                this.activeColor = new Color(50, 200, 50); // Green when active and toggled off
            }
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        try {
            // Determine colors based on action type
            Color currentColor = activated ? activeColor : color;
            
            // For toggle actions, use different colors
            if (action instanceof ToggleAction) {
                ToggleAction toggleAction = (ToggleAction) action;
                if (toggleAction.isToggled()) {
                    currentColor = new Color(70, 200, 70); // Green when toggled on
                }
            }
            
            // For message actions, use a different color
            if (action instanceof MessageAction) {
                MessageAction messageAction = (MessageAction) action;
                if (messageAction.isCycling()) {
                    // Purple for cycling messages
                    currentColor = activated ? new Color(180, 100, 200) : new Color(120, 60, 140);
                } else {
                    // Yellow for regular messages
                    currentColor = activated ? new Color(220, 220, 50) : new Color(180, 180, 40);
                }
            }
            
            if (activated && buttonActiveImage != null) {
                g.drawImage(buttonActiveImage, (int)position.x, (int)position.y, width, height, null);
            } else if (!activated && buttonImage != null) {
                g.drawImage(buttonImage, (int)position.x, (int)position.y, width, height, null);
            } else {
                // Fallback if images aren't loaded
                g.setColor(currentColor);
                g.fillRect((int)position.x, (int)position.y, width, height);
                
                // Draw a button-like appearance
                g.setColor(activated ? color : Color.DARK_GRAY);
                g.fillRect((int)position.x + width/4, (int)position.y + height/4, width/2, height/2);
            }
            
            // Draw a small indicator if the button has an action
            if (action != null) {
                g.setColor(Color.WHITE);
                g.drawString("E", (int)position.x + 5, (int)position.y + 15);
                
                // For timed actions, draw a timer when activated
                if (action instanceof TimedAction && activated) {
                    drawTimerBar(g);
                }
                
                // For toggle actions, draw a toggle indicator
                if (action instanceof ToggleAction) {
                    drawToggleIndicator(g, ((ToggleAction) action).isToggled());
                }
                
                // For message actions, draw a message indicator
                if (action instanceof MessageAction) {
                    drawMessageIndicator(g, (MessageAction) action);
                }
            }
        } catch (Exception e) {
            // Ultimate fallback
            System.err.println("Error rendering button: " + e.getMessage());
            g.setColor(Color.MAGENTA);
            g.fillRect((int)position.x, (int)position.y, width, height);
        }
    }
    
    /**
     * Draws a timer bar for timed actions.
     * 
     * @param g The graphics context
     */
    private void drawTimerBar(Graphics2D g) {
        TimedAction timedAction = (TimedAction) action;
        long currentTime = System.currentTimeMillis();
        long activationTime = timedAction.getActivationTime();
        int durationMillis = timedAction.getDuration();
        
        if (activationTime == 0) {
            // Fallback if activation time is not available
            activationTime = currentTime - 100;
        }
        
        float progress = Math.min(1.0f, (float)(currentTime - activationTime) / durationMillis);
        
        // Draw timer bar underneath the button
        int barHeight = 5;
        int barWidth = (int)((1.0f - progress) * width);
        
        g.setColor(Color.GREEN);
        g.fillRect((int)position.x, (int)position.y + height + 2, barWidth, barHeight);
        
        g.setColor(Color.WHITE);
        g.drawRect((int)position.x, (int)position.y + height + 2, width, barHeight);
    }
    
    /**
     * Draws a toggle indicator for toggle actions.
     * 
     * @param g The graphics context
     * @param toggled Whether the action is toggled on
     */
    private void drawToggleIndicator(Graphics2D g, boolean toggled) {
        g.setColor(Color.WHITE);
        int circleSize = Math.min(width, height) / 3;
        int circleX = (int)position.x + width - circleSize - 5;
        int circleY = (int)position.y + 5;
        
        g.drawOval(circleX, circleY, circleSize, circleSize);
        
        if (toggled) {
            g.fillOval(circleX, circleY, circleSize, circleSize);
        }
    }
    
    /**
     * Draws a message indicator for message actions.
     * 
     * @param g The graphics context
     * @param messageAction The message action to draw the indicator for
     */
    private void drawMessageIndicator(Graphics2D g, MessageAction messageAction) {
        if (messageAction.isCycling()) {
            // Draw cycling indicator (showing current message number)
            g.setColor(Color.WHITE);
            g.drawString("" + (messageAction.getCurrentMessageIndex() + 1) + "/" + messageAction.getMessages().size(), 
                         (int)position.x + width - 20, (int)position.y + height - 5);
        } else {
            // Draw message icon
            g.setColor(Color.WHITE);
            int iconSize = Math.min(width, height) / 4;
            int iconX = (int)position.x + width - iconSize - 5;
            int iconY = (int)position.y + height - iconSize - 5;
            
            // Draw a simple message bubble icon
            g.drawRoundRect(iconX, iconY, iconSize, iconSize, 3, 3);
            g.drawLine(iconX + 2, iconY + 2, iconX + iconSize - 2, iconY + 2);
            g.drawLine(iconX + 2, iconY + iconSize / 2, iconX + iconSize - 2, iconY + iconSize / 2);
        }
    }
    
    /**
     * Activates the button and executes its action.
     * 
     * @return True if the button was activated, false if it was already activated
     */
    public boolean activate() {
        if (action == null) return false;
        
        // For toggle actions, always activate
        if (action instanceof ToggleAction) {
            action.execute();
            return true;
        }
        
        // For MessageAction with cycling, always activate
        if (action instanceof MessageAction && ((MessageAction) action).isCycling()) {
            activated = true;
            action.execute();
            return true;
        }
        
        // For other actions, only activate if not already activated
        if (!activated) {
            activated = true;
            action.execute();
            return true;
        }
        
        return false;
    }
    
    /**
     * Deactivates the button.
     */
    public void deactivate() {
        activated = false;
    }
    
    /**
     * Checks if the button is activated.
     * 
     * @return True if the button is activated, false otherwise
     */
    public boolean isActivated() {
        return activated;
    }
    
    /**
     * Sets the action for this button.
     * 
     * @param action The action to execute when the button is activated
     */
    public void setAction(Action action) {
        this.action = action;
        
        // If this is a timed action, set the target button
        if (action instanceof TimedAction) {
            ((TimedAction) action).setTargetButton(this);
        }
    }
    
    /**
     * Gets the action for this button.
     * 
     * @return The action
     */
    public Action getAction() {
        return action;
    }
    
    /**
     * Sets the color of the button when inactive.
     * 
     * @param color The color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Sets the color of the button when active.
     * 
     * @param activeColor The active color
     */
    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }
} 