/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static main.GamePanel.LEFT_BORDER;
import static main.GamePanel.TOP_BORDER;

/**
 *
 * @author lucas
 */
public class Mouse extends MouseAdapter {

    public int x, y;
    public boolean pressed;

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX() - LEFT_BORDER;
        y = e.getY() - TOP_BORDER;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX() - LEFT_BORDER;
        y = e.getY() - TOP_BORDER;

    }
}
