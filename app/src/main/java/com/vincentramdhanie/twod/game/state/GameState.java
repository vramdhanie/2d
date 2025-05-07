package com.vincentramdhanie.twod.game.state;

import com.vincentramdhanie.twod.game.core.GameStateManager;

import java.awt.Graphics2D;

public abstract class GameState {
    protected GameStateManager gsm;
    
    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }
    
    public abstract void init();
    public abstract void update();
    public abstract void render(Graphics2D g);
    public abstract void keyPressed(int k);
    public abstract void keyReleased(int k);
    public abstract void mousePressed(int x, int y);
    public abstract void mouseReleased(int x, int y);
    public abstract void mouseMoved(int x, int y);
}