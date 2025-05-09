package com.niravramdhanie.twod.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.niravramdhanie.twod.game.actions.Action;
import com.niravramdhanie.twod.game.actions.DoorAction;
import com.niravramdhanie.twod.game.actions.MessageAction;
import com.niravramdhanie.twod.game.actions.MultiAction;
import com.niravramdhanie.twod.game.actions.TimedAction;
import com.niravramdhanie.twod.game.actions.ToggleAction;
import com.niravramdhanie.twod.game.utils.ResourceLoader;
import com.niravramdhanie.twod.game.utils.RewindManager;

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
    
    // Fonts for button icons
    private static Font iconFont;
    
    // Reference to the rewind manager
    private static RewindManager rewindManager;
    
    // Visual effects for activation
    private long activationTime;
    private boolean pulseEffect;
    
    static {
        // Initialize the icon font
        iconFont = new Font("Arial", Font.BOLD, 14);
    }
    
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
        this.pulseEffect = true;
        this.activationTime = 0;
        
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
        // Update activation time for effects
        if (activated) {
            if (activationTime == 0) {
                activationTime = System.currentTimeMillis();
            }
        } else {
            activationTime = 0;
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        try {
            // Draw the base button
            drawButtonBase(g);
            
            // Draw action-specific icon
            drawButtonIcon(g);
            
            // Draw additional indicators based on action type
            if (action != null) {
                // For timed actions, draw a timer when activated
                if (action instanceof TimedAction && activated) {
                    drawTimerBar(g);
                }
            }
            
            // Draw activation glow if activated
            if (activated) {
                drawActivationIndicator(g);
            }
        } catch (Exception e) {
            // Ultimate fallback
            System.err.println("Error rendering button: " + e.getMessage());
            g.setColor(Color.MAGENTA);
            g.fillRect((int)position.x, (int)position.y, width, height);
        }
    }
    
    /**
     * Draws the base button shape with appropriate colors
     */
    private void drawButtonBase(Graphics2D g) {
        // Determine colors based on action type and state
        Color baseColor = determineButtonColor();
        Color topColor = baseColor.brighter();
        Color bottomColor = baseColor.darker();
        
        // Create gradient for 3D effect
        GradientPaint gradient = new GradientPaint(
            (int)position.x, (int)position.y, topColor,
            (int)position.x, (int)position.y + height, bottomColor
        );
        
        // Store original paint and set gradient
        java.awt.Paint originalPaint = g.getPaint();
        g.setPaint(gradient);
        
        // Draw button body with rounded corners
        g.fillRoundRect((int)position.x, (int)position.y, width, height, 5, 5);
        
        // Draw button border
        g.setColor(activated ? Color.WHITE : Color.DARK_GRAY);
        g.drawRoundRect((int)position.x, (int)position.y, width, height, 5, 5);
        
        // Draw button top (lighter) for 3D effect
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRoundRect((int)position.x + 2, (int)position.y + 2, width - 4, height / 3, 3, 3);
        
        // Reset original paint
        g.setPaint(originalPaint);
    }
    
    /**
     * Determines the appropriate color for the button based on its action type and state
     */
    private Color determineButtonColor() {
        if (action == null) {
            return activated ? activeColor : color;
        }
        
        // Colors for different action types
        if (action instanceof TimedAction) {
            return activated ? new Color(255, 140, 0) : new Color(200, 110, 0); // Orange for timed
        } else if (action instanceof ToggleAction) {
            ToggleAction toggleAction = (ToggleAction) action;
            if (toggleAction.isToggled()) {
                return new Color(0, 170, 220); // Blue when toggled on
            } else {
                return new Color(60, 60, 180); // Dark blue when toggled off
            }
        } else if (action instanceof MessageAction) {
            MessageAction messageAction = (MessageAction) action;
            if (messageAction.isCycling()) {
                return activated ? new Color(180, 100, 200) : new Color(120, 60, 140); // Purple for cycling
            } else {
                return activated ? new Color(220, 220, 50) : new Color(180, 180, 40); // Yellow for regular
            }
        } else if (action instanceof DoorAction) {
            return activated ? new Color(50, 200, 50) : new Color(40, 130, 40); // Green for door
        } else if (action instanceof MultiAction) {
            return activated ? new Color(200, 50, 200) : new Color(150, 30, 150); // Magenta for multi
        }
        
        return activated ? activeColor : color;
    }
    
    /**
     * Draws an appropriate icon based on the button's action type
     */
    private void drawButtonIcon(Graphics2D g) {
        if (action == null) {
            return;
        }
        
        // Save original font and color
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
        
        // Set icon font
        g.setFont(iconFont);
        
        // Get icon and color based on action type
        String icon = "";
        Color iconColor = Color.WHITE;
        
        if (action instanceof TimedAction) {
            icon = "⏱"; // Clock icon for timed actions
            iconColor = Color.WHITE;
        } else if (action instanceof ToggleAction) {
            icon = "⇆"; // Toggle icon
            iconColor = Color.WHITE;
            
            ToggleAction toggleAction = (ToggleAction) action;
            if (toggleAction.isToggled()) {
                icon = "ON";
            } else {
                icon = "OFF";
            }
            
        } else if (action instanceof MessageAction) {
            MessageAction messageAction = (MessageAction) action;
            if (messageAction.isCycling()) {
                icon = "♻"; // Recycling symbol for cycling messages
            } else {
                icon = "!"; // Exclamation for regular messages
            }
            iconColor = Color.WHITE;
        } else if (action instanceof DoorAction) {
            icon = "🚪"; // Door symbol
            iconColor = Color.WHITE;
        } else if (action instanceof MultiAction) {
            icon = "✦"; // Star for multi-actions
            iconColor = Color.WHITE;
        }
        
        // Center the icon in the button
        FontMetrics metrics = g.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(icon, g);
        int x = (int)(position.x + (width - bounds.getWidth()) / 2);
        int y = (int)(position.y + (height - bounds.getHeight()) / 2 + metrics.getAscent());
        
        // Draw icon with a subtle shadow for better visibility
        g.setColor(Color.BLACK);
        g.drawString(icon, x + 1, y + 1);
        g.setColor(iconColor);
        g.drawString(icon, x, y);
        
        // Draw interaction hint
        drawInteractionHint(g);
        
        // Restore original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    /**
     * Draws a small "E" hint to show the button can be activated
     */
    private void drawInteractionHint(Graphics2D g) {
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(new Color(255, 255, 255, 200));
        g.drawString("E", (int)position.x + width - 10, (int)position.y + 12);
    }
    
    /**
     * Draws a timer bar for timed actions
     */
    private void drawTimerBar(Graphics2D g) {
        if (!(action instanceof TimedAction)) return;
        
        TimedAction timedAction = (TimedAction) action;
        if (!timedAction.isActive()) return;
        
        // Store original color
        Color originalColor = g.getColor();
        
        // Get the timer fraction (0.0 to 1.0)
        float fraction = timedAction.getTimeRemainingFraction();
        
        // Draw the timer bar background
        g.setColor(Color.DARK_GRAY);
        g.fillRect((int)position.x, (int)position.y + height + 2, width, 4);
        
        // Draw the timer bar foreground
        if (fraction > 0.6f) {
            g.setColor(Color.GREEN);
        } else if (fraction > 0.3f) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        
        g.fillRect((int)position.x, (int)position.y + height + 2, (int)(width * fraction), 4);
        
        // Restore original color
        g.setColor(originalColor);
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
     * Draws an indicator for message actions.
     * 
     * @param g The graphics context
     * @param messageAction The message action
     */
    private void drawMessageIndicator(Graphics2D g, MessageAction messageAction) {
        int indicatorSize = Math.min(width, height) / 4;
        int indicatorX = (int)position.x + width - indicatorSize - 5;
        int indicatorY = (int)position.y + height - indicatorSize - 5;
        
        if (messageAction.isCycling()) {
            // Draw cycling indicator (circular arrow)
            g.setColor(Color.WHITE);
            g.drawOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
            
            // Draw arrow inside the circle
            int centerX = indicatorX + indicatorSize / 2;
            int centerY = indicatorY + indicatorSize / 2;
            int arrowSize = indicatorSize / 3;
            
            g.drawLine(centerX, centerY - arrowSize, centerX + arrowSize, centerY);
            g.drawLine(centerX, centerY - arrowSize, centerX - arrowSize, centerY);
        } else {
            // Draw speech bubble for regular messages
            g.setColor(Color.WHITE);
            g.fillRoundRect(indicatorX, indicatorY, indicatorSize, indicatorSize, 3, 3);
            g.setColor(Color.BLACK);
            g.drawString("!", indicatorX + indicatorSize/3, indicatorY + 2*indicatorSize/3);
        }
    }
    
    /**
     * Draws a visual indicator that the button is activated
     */
    private void drawActivationIndicator(Graphics2D g) {
        // Store original color
        Color originalColor = g.getColor();
        
        // Create a pulsating effect
        float alpha = 0.7f;
        if (pulseEffect) {
            long elapsed = System.currentTimeMillis() - activationTime;
            alpha = 0.3f + (float)Math.abs(Math.sin(elapsed * 0.005f)) * 0.4f;
        }
        
        // Draw a glowing outline
        g.setColor(new Color(
            activeColor.getRed() / 255f,
            activeColor.getGreen() / 255f,
            activeColor.getBlue() / 255f,
            alpha
        ));
        
        // Draw outer glow
        int glowSize = 4;
        g.fillRoundRect(
            (int)position.x - glowSize,
            (int)position.y - glowSize,
            width + glowSize * 2,
            height + glowSize * 2,
            10, 10
        );
        
        // Restore original color
        g.setColor(originalColor);
    }
    
    /**
     * Sets the rewind manager for all buttons.
     * 
     * @param rewindManager The rewind manager
     */
    public static void setRewindManager(RewindManager rewindManager) {
        Button.rewindManager = rewindManager;
    }
    
    /**
     * Activates the button and executes its action.
     * 
     * @return True if the button was activated, false if it was already activated
     */
    public boolean activate() {
        boolean wasActivated = activated;
        
        if (activated) {
            // If already activated, only perform action if it's a toggle or cycling action
            if (action instanceof ToggleAction || 
                (action instanceof MessageAction && ((MessageAction)action).isCycling())) {
                if (action != null) {
                    action.execute();
                }
                
                // Record the button activation if we're recording
                if (rewindManager != null) {
                    rewindManager.recordButtonActivation(this, true);
                }
                
                return true;
            }
            return false;
        }
        
        activated = true;
        if (action != null) {
            action.execute();
        }
        
        // Record the button activation if we're recording
        if (rewindManager != null && !wasActivated) {
            rewindManager.recordButtonActivation(this, true);
        }
        
        return true;
    }
    
    /**
     * Deactivates the button.
     */
    public void deactivate() {
        boolean wasActivated = activated;
        activated = false;
        
        // Record the button deactivation if we're recording
        if (rewindManager != null && wasActivated) {
            rewindManager.recordButtonActivation(this, false);
        }
    }
    
    /**
     * Checks if the button is activated.
     * 
     * @return True if activated, false otherwise
     */
    public boolean isActivated() {
        return activated;
    }
    
    /**
     * Sets the action to execute when the button is activated.
     * 
     * @param action The action
     */
    public void setAction(Action action) {
        this.action = action;
        
        // If this is a timed action, set the target button
        if (action instanceof TimedAction) {
            ((TimedAction) action).setTargetButton(this);
        }
    }
    
    /**
     * Gets the button's action.
     * 
     * @return The action
     */
    public Action getAction() {
        return action;
    }
    
    /**
     * Sets the button's inactive color.
     * 
     * @param color The color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Sets the button's active color.
     * 
     * @param activeColor The active color
     */
    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }
    
    /**
     * Force sets the toggle state without executing any actions.
     * Useful for rewind operations where we want to set the state
     * without triggering the actual toggle behavior.
     * 
     * @param toggled The toggle state to set
     * @return True if the action was a ToggleAction and state was set, false otherwise
     */
    public boolean forceSetToggleState(boolean toggled) {
        if (action instanceof ToggleAction) {
            ToggleAction toggleAction = (ToggleAction) action;
            toggleAction.setToggled(toggled);
            
            // If this toggle controls a door, update the door state directly
            if (toggleAction.getOnAction() instanceof DoorAction) {
                DoorAction doorAction = (DoorAction) toggleAction.getOnAction();
                doorAction.setDoorOpen(toggled);
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Sets whether the button is activated without executing any actions.
     * Useful for rewind operations.
     * 
     * @param activated Whether the button should be activated
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
        
        // Update activation time when the button is activated during rewind
        // to ensure the visual indicator plays properly
        if (activated) {
            this.activationTime = System.currentTimeMillis();
        } else {
            this.activationTime = 0;
        }
    }
    
    /**
     * Sets whether this button should use a pulsating effect when activated.
     * 
     * @param pulseEffect True to enable pulsing, false for static activation indicators
     */
    public void setPulseEffect(boolean pulseEffect) {
        this.pulseEffect = pulseEffect;
    }
} 