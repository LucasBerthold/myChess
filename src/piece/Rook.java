/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package piece;

import main.GamePanel;
import main.Type;

/**
 *
 * @author lucas
 */
public class Rook extends Piece {

    public Rook(int col, int row, int color) {
        super(col, row, color);

        type = Type.ROOK;

        if (color == GamePanel.WHITE) {
            image = getImage("/Sprites/w-rook");
        } else {
            image = getImage("/Sprites/b-rook");

        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && !pieceIsOnStraightLine(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;

    }
}
