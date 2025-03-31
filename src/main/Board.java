package main;

import java.awt.Color;
import java.awt.Graphics2D;

public class Board {

    public static final int MAX_COLUMNS = 8;
    public static final int MAX_ROWS = 8;

    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;
    public static final int BOARD_SIZE = SQUARE_SIZE * 8;
    public static final int BORDER_THICKNESS = 2;
    public static final int LINE_THICKNESS = BORDER_THICKNESS;

    public void draw(Graphics2D g2) {

        Color borderColor = new Color(130, 85, 50);
        g2.setColor(borderColor);
        g2.fillRoundRect(0, 0, BOARD_SIZE + BORDER_THICKNESS * 2, BOARD_SIZE + BORDER_THICKNESS * 2, 10, 10);
        int startX = BORDER_THICKNESS;
        int startY = BORDER_THICKNESS;

        Color lightSquare = new Color(240, 217, 181);
        Color darkSquare = new Color(181, 136, 99);
        Color lineColor = borderColor;

        int c = 0;
        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < MAX_COLUMNS; col++) {
                g2.setColor(c == 0 ? lightSquare : darkSquare);
                g2.fillRoundRect(startX + col * SQUARE_SIZE, startY + row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, 15, 15);
                c = 1 - c;
            }
            c = 1 - c;
        }

        g2.setColor(lineColor);
        for (int i = 0; i <= MAX_COLUMNS; i++) {
            int x = startX + i * SQUARE_SIZE;
            g2.fillRect(x - LINE_THICKNESS / 2, startY, LINE_THICKNESS, BOARD_SIZE);
        }
        for (int i = 0; i <= MAX_ROWS; i++) {
            int y = startY + i * SQUARE_SIZE;
            g2.fillRect(startX, y - LINE_THICKNESS / 2, BOARD_SIZE, LINE_THICKNESS);
        }

    }

}
