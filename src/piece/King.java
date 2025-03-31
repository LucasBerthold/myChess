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
public class King extends Piece {

    public King(int col, int row, int color) {
        super(col, row, color);

        type = Type.KING;

        if (color == GamePanel.WHITE) {
            image = getImage("/Sprites/w-king");
        } else {
            image = getImage("/Sprites/b-king");

        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow)) {
            if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1 || Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) {

                if (isValidSquare(targetCol, targetRow)) {
                    return true;

                }
            }

            //CASTLING
            if (!moved) {
                //RIGHT CASTLING
                if (targetCol == preCol + 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece.col == preCol + 3 && piece.row == preRow && !piece.moved) {
                            GamePanel.castlingP = piece;
                            return true;
                        }
                    }
                }

                //LEFT CASTLING
                if (targetCol == preCol - 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
                    Piece p[] = new Piece[2];
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece.col == preCol - 3 && piece.row == targetRow) {
                            p[0] = piece;
                        }
                        if (piece.col == preCol - 4 && piece.row == targetRow) {
                            p[1] = piece;
                        }
                        if (p[0] == null && p[1] != null && !p[1].moved) {
                            GamePanel.castlingP = p[1];
                            return true;
                        }

                    }
                }
            }
        }

        return false;

    }

}
