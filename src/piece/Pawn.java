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
public class Pawn extends Piece {

    public Pawn(int col, int row, int color) {
        super(col, row, color);

        type = Type.PAWN;

        if (color == GamePanel.WHITE) {
            image = getImage("/Sprites/w-pawn");
        } else {
            image = getImage("/Sprites/b-pawn");

        }

    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {

            int moveValue;
            if (color == GamePanel.WHITE) {
                moveValue = -1;
            } else {
                moveValue = 1;

            }

            hittingP = getHittingP(targetCol, targetRow);

            //1 SQUARE MOVEMENT
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
                return true;
            }
            //2 SQUARE MOVEMENT
            if (targetCol == preCol && targetRow == preRow + (moveValue * 2) && hittingP == null && !moved && !pieceIsOnStraightLine(targetCol, targetRow)) {
                return true;
            }
            //DIAGONAL MOVEMENT TO CAPTURE
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null && hittingP.color != color) {
                return true;
            }
            //EN PASSANT
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;

    }
}
