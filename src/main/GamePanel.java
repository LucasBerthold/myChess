/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

/**
 *
 * @author lucas
 */
public class GamePanel extends JPanel implements Runnable {

    public static final int BOARD_WIDTH = Board.MAX_COLUMNS * Board.SQUARE_SIZE + Board.BORDER_THICKNESS * 2;
    public static final int BOARD_HEIGHT = Board.MAX_ROWS * Board.SQUARE_SIZE + Board.BORDER_THICKNESS * 2;

    public static final int LEFT_BORDER = 10;
    public static final int RIGHT_BORDER = 10;
    public static final int TOP_BORDER = 105;
    public static final int BOTTOM_BORDER = 10;

    public static final int WIDTH = BOARD_WIDTH + LEFT_BORDER + RIGHT_BORDER;
    public static final int HEIGHT = BOARD_HEIGHT + TOP_BORDER + BOTTOM_BORDER;

    public static Rectangle restartButton;
    public static Rectangle exitButton;

    final int FPS = 60;

    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    //TILES COLORS
    Color yellow = new Color(255, 255, 0, 100); //MOVE
    Color white = new Color(255, 255, 255, 100); //MOVE
    Color red = new Color(255, 0, 0, 100); //CHECK

    //COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static int currentColor = WHITE;

    //PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;

    //LAST MOVES
    ArrayList<Piece> moves = new ArrayList<>();
    Piece lastMove;

    //BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean stalemate;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(UIManager.getColor("Panel.background"));

        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void restart() {
        canMove = false;
        validSquare = false;
        promotion = false;
        gameover = false;
        stalemate = false;

        lastMove = null;
        activeP = null;
        checkingP = null;
        castlingP = null;

        moves.clear();
        pieces.clear();
        simPieces.clear();
        promoPieces.clear();

        currentColor = WHITE;

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void setPieces() {

        //WHITE TEAM
        pieces.add(new Pawn(0, 6, WHITE));
        pieces.add(new Pawn(1, 6, WHITE));
        pieces.add(new Pawn(2, 6, WHITE));
        pieces.add(new Pawn(3, 6, WHITE));
        pieces.add(new Pawn(4, 6, WHITE));
        pieces.add(new Pawn(5, 6, WHITE));
        pieces.add(new Pawn(6, 6, WHITE));
        pieces.add(new Pawn(7, 6, WHITE));
        pieces.add(new Rook(7, 7, WHITE));
        pieces.add(new Rook(0, 7, WHITE));

        pieces.add(new Knight(1, 7, WHITE));
        pieces.add(new Knight(6, 7, WHITE));

        pieces.add(new Bishop(2, 7, WHITE));
        pieces.add(new Bishop(5, 7, WHITE));
        pieces.add(new Queen(3, 7, WHITE));
        pieces.add(new King(4, 7, WHITE));

        //BLACK TEAM
        pieces.add(new Pawn(0, 1, BLACK));
        pieces.add(new Pawn(1, 1, BLACK));
        pieces.add(new Pawn(2, 1, BLACK));
        pieces.add(new Pawn(3, 1, BLACK));
        pieces.add(new Pawn(4, 1, BLACK));
        pieces.add(new Pawn(5, 1, BLACK));
        pieces.add(new Pawn(6, 1, BLACK));
        pieces.add(new Pawn(7, 1, BLACK));
        pieces.add(new Rook(0, 0, BLACK));
        pieces.add(new Rook(7, 0, BLACK));

        pieces.add(new Knight(1, 0, BLACK));
        pieces.add(new Knight(6, 0, BLACK));

        pieces.add(new Bishop(2, 0, BLACK));
        pieces.add(new Bishop(5, 0, BLACK));

        pieces.add(new Queen(3, 0, BLACK));
        pieces.add(new King(4, 0, BLACK));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();

        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();

                repaint();
                delta--;
            }

        }
    }

    private void update() {
        if (promotion) {
            promoting();
        } else if (!gameover && !stalemate) {
            boolean isOverPiece = false;

            for (Piece piece : simPieces) {
                if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    isOverPiece = true;
                }
            }

            if (!isOverPiece) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            if (mouse.pressed) {
                if (activeP == null) {
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;
                            break;
                        }
                    }
                } else {
                    simulate();
                }
            }

            if (!mouse.pressed && activeP != null) {
                if (validSquare) {
                    copyPieces(simPieces, pieces);
                    activeP.updatePosition();
                    moves.add(new Piece(activeP));
                    activeP.startCol = activeP.preCol;
                    activeP.startRow = activeP.preRow;

                    int i = 0;
                    for (Piece p : moves) {
                        i++;
                        System.out.println(i + " " + p.type + " - col/row:" + p.col + "/" + p.row + " - startCol/startRow:" + p.startCol + "/" + p.startRow);
                    }
                    System.out.println("-----------------------");

                    if (castlingP != null) {
                        castlingP.updatePosition();
                    }

                    if (isKingInCheck() && isCheckMate()) {
                        gameover = true;
                    } else if (isStaleMate() && !isKingInCheck()) {
                        stalemate = true;
                    } else {
                        if (canPromote()) {
                            promotion = true;
                        } else {
                            changePlayer();
                        }
                    }
                } else {
                    copyPieces(pieces, simPieces);
                    activeP.resetPosition();
                    activeP = null;
                }
            }
        }
    }

    private void simulate() {
        canMove = false;
        validSquare = false;

        copyPieces(pieces, simPieces);

        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;

        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        if (activeP.canMove(activeP.col, activeP.row)) {
            canMove = true;

            if (activeP.hittingP != null) {
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            if (!isIllegal(activeP) && !opponentCanCaptureKing()) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {

        Piece king = getKing(false);
        for (Piece piece : simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if (activeP.canMove(king.col, king.row)) {
            checkingP = activeP;

            return true;
        } else {
            checkingP = null;
        }
        return false;
    }

    private Piece getKing(boolean opponent) {
        Piece king = null;

        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }
        return king;

    }

    private boolean isCheckMate() {

        Piece king = getKing(true);

        if (kingCanMove(king)) {

            return false;
        } else {
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0) {
                if (checkingP.row < king.row) {
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row) {
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == 0) {
                if (checkingP.col < king.col) {
                    for (int col = checkingP.col; col < king.row; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > king.col) {
                    for (int col = checkingP.col; col > king.row; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {

                if (checkingP.row < king.row) {
                    if (checkingP.col < king.col) {
                        if (checkingP.col > king.col) {
                            for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return false;
                                    }
                                }
                            }
                        }
                        if (checkingP.col < king.col) {
                            for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return false;
                                    }
                                }
                            }
                        }

                    }
                    if (checkingP.row > king.row) {
                        if (checkingP.col > king.col) {
                            for (int col = checkingP.col, row = checkingP.row; col < king.col; col--, row--) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return false;
                                    }
                                }
                            }
                        }
                        if (checkingP.col < king.col) {
                            for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return true;
        }

    }

    private boolean kingCanMove(Piece king) {

        if (isValidMove(king, -1, -1)) {
            return true;
        }
        if (isValidMove(king, 0, -1)) {
            return true;
        }
        if (isValidMove(king, 1, -1)) {
            return true;
        }
        if (isValidMove(king, -1, 0)) {
            return true;
        }
        if (isValidMove(king, 1, 0)) {
            return true;
        }
        if (isValidMove(king, -1, 1)) {
            return true;
        }
        if (isValidMove(king, 0, 1)) {
            return true;
        }
        if (isValidMove(king, 1, 1)) {
            return true;
        }

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

        boolean isValidMove = false;

        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {

            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }
            if (!isIllegal(king)) {
                isValidMove = true;
            }
        }

        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStaleMate() {
        int count = 0;

        for (Piece piece : simPieces) {
            if (piece.color != currentColor) {
                count++;
            }
        }

        if (count == 1) {
            if (!kingCanMove(getKing(true))) {
                return true;
            }
        }
        return false;
    }

    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private void changePlayer() {

        if (currentColor == WHITE) {

            currentColor = BLACK;
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        };

        activeP = null;
    }

    private void promoting() {
        boolean isOverPromotionArea = false;

        for (Piece piece : promoPieces) {
            System.out.println("Mouse: " + mouse.x + ", " + mouse.y);
            System.out.println("Peça: " + piece.x + ", " + piece.y);
            System.out.println();

            if (mouse.x >= piece.x - 50 && mouse.x <= piece.x + 50
                    && mouse.y >= piece.y - 50 && mouse.y <= piece.y + 50) {

                isOverPromotionArea = true;

                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                if (mouse.pressed) {
                    switch (piece.type) {
                        case ROOK:
                            simPieces.add(new Rook(activeP.col, activeP.row, currentColor));
                            break;
                        case KNIGHT:
                            simPieces.add(new Knight(activeP.col, activeP.row, currentColor));
                            break;
                        case BISHOP:
                            simPieces.add(new Bishop(activeP.col, activeP.row, currentColor));
                            break;
                        case QUEEN:
                            simPieces.add(new Queen(activeP.col, activeP.row, currentColor));
                            break;
                        default:
                            break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }

        if (!isOverPromotionArea) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean canPromote() {

        if (activeP.type == Type.PAWN) {
            if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(9, 2, currentColor));
                promoPieces.add(new Knight(9, 3, currentColor));
                promoPieces.add(new Bishop(9, 4, currentColor));
                promoPieces.add(new Queen(9, 5, currentColor));
                return true;
            }
        }

        return false;
    }

    public void drawRoundedTopRect(Graphics2D g2, int x, int y, int width, int height, int arcWidth, int arcHeight, Color fillColor, Color borderColor) {
        Path2D path = new Path2D.Double();

        path.moveTo(x, y + height);

        path.lineTo(x + width, y + height);

        path.lineTo(x + width, y + arcHeight);

        path.quadTo(x + width, y, x + width - arcWidth, y);

        path.lineTo(x + arcWidth, y);

        path.quadTo(x, y, x, y + arcHeight);

        path.closePath();

        g2.setColor(fillColor);
        g2.fill(path);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(3));
        g2.draw(path);
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Monospaced", Font.BOLD, 35));

        int x, y;
        int xOffset = LEFT_BORDER;
        int yOffset = TOP_BORDER;

        g2.translate(xOffset, yOffset);

        board.draw(g2);

        if (!moves.isEmpty()) {
            Piece lastItem = moves.get(moves.size() - 1);
            g2.setColor(yellow);

            x = Board.BORDER_THICKNESS * 2 + lastItem.startCol * Board.SQUARE_SIZE;
            y = Board.BORDER_THICKNESS * 2 + lastItem.startRow * Board.SQUARE_SIZE;

            g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);
            x = Board.BORDER_THICKNESS * 2 + lastItem.col * Board.SQUARE_SIZE;
            y = Board.BORDER_THICKNESS * 2 + lastItem.row * Board.SQUARE_SIZE;

            g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);

            if (moves.size() >= 2) {
                if (lastMove == null) {
                    lastItem = moves.get(moves.size() - 2);

                    x = Board.BORDER_THICKNESS * 2 + lastItem.startCol * Board.SQUARE_SIZE;
                    y = Board.BORDER_THICKNESS * 2 + lastItem.startRow * Board.SQUARE_SIZE;

                    g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);
                    x = Board.BORDER_THICKNESS * 2 + lastItem.col * Board.SQUARE_SIZE;
                    y = Board.BORDER_THICKNESS * 2 + lastItem.row * Board.SQUARE_SIZE;

                    g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);
                }
            }

        }
        if (lastMove == null || lastMove.type != Type.KING) {
            if (opponentCanCaptureKing()) {
                Piece king = getKing(false);
                if (activeP != king) {

                    g2.setColor(red);

                    x = Board.BORDER_THICKNESS * 2 + king.col * Board.SQUARE_SIZE;
                    y = Board.BORDER_THICKNESS * 2 + king.row * Board.SQUARE_SIZE;

                    g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);

                }
            }
        }
        for (Piece p : simPieces) {
            p.draw(g2);
        }

        if (activeP != null) {
            lastMove = activeP;
            if (canMove && !opponentCanCaptureKing()) {

                g2.setColor(white);

                x = Board.BORDER_THICKNESS * 2 + activeP.col * Board.SQUARE_SIZE;
                y = Board.BORDER_THICKNESS * 2 + activeP.row * Board.SQUARE_SIZE;

                g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);

                for (Piece p : simPieces) {
                    p.draw(g2);
                }

            }
            activeP.draw(g2);
        }
        if (lastMove != null) {
            if (lastMove.color != currentColor) {
                lastMove = null;

            } else {
                if (!(lastMove.type == Type.KING && opponentCanCaptureKing())) {
                    g2.setColor(white);
                } else {
                    g2.setColor(red);
                }

                x = Board.BORDER_THICKNESS * 2 + lastMove.startCol * Board.SQUARE_SIZE;
                y = Board.BORDER_THICKNESS * 2 + lastMove.startRow * Board.SQUARE_SIZE;

                g2.fillRect(x, y, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2, Board.SQUARE_SIZE - Board.BORDER_THICKNESS * 2);
            }
        }
        for (Piece p : simPieces) {
            p.draw(g2);
        }
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (gameover) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            g2.setFont(new Font("Monospaced", Font.BOLD, 40));

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0 + Board.BORDER_THICKNESS, 0 + Board.BORDER_THICKNESS, BOARD_WIDTH - Board.BORDER_THICKNESS * 2, BOARD_HEIGHT - Board.BORDER_THICKNESS * 2);

            String s = "";

            FontMetrics fm;
            int stringWidth;
            int stringHeight;
            int textX;
            int textY;
            int height = 200, width = 540, xPos = BOARD_WIDTH / 2 - width / 2, yPos = BOARD_HEIGHT / 2 - height / 2, round = 15;
            if (currentColor == WHITE) {
                g2.setColor(new Color(240, 217, 181));
                g2.fillRoundRect(xPos, yPos, width, height, round, round);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(new Color(130, 85, 50));
                g2.drawRoundRect(xPos, yPos, width, height, round, round);

                g2.setColor(new Color(181, 136, 99));

                s = "CONGRATULATIONS";
                fm = g2.getFontMetrics();
                stringWidth = fm.stringWidth(s);
                stringHeight = fm.getHeight();
                textX = (BOARD_WIDTH - stringWidth) / 2;
                textY = (BOARD_HEIGHT) / 2 + stringHeight * 2 + 40;
                g2.drawString(s, textX, textY - stringHeight - 100);

                s = "WHITE WINS";
                fm = g2.getFontMetrics();
                stringWidth = fm.stringWidth(s);
                stringHeight = fm.getHeight();
                textX = (BOARD_WIDTH - stringWidth) / 2;
                textY = (BOARD_HEIGHT) / 2 + stringHeight;
                g2.drawString(s, textX, textY - 100);

                int buttonWidth = 200;
                int buttonHeight = 50;
                int buttonY = (BOARD_WIDTH + stringHeight) / 2;
                int restartButtonX = xPos + 50;
                int exitButtonX = xPos + width - buttonWidth - 50;

                g2.setColor(new Color(0, 217, 181, 50));
                g2.fillRoundRect(restartButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(0, 85, 50));
                g2.drawRoundRect(restartButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(0, 136, 99, 200));

                g2.drawString("Restart", restartButtonX + 17, buttonY + 36);

                g2.setColor(new Color(240, 0, 181, 50));
                g2.fillRoundRect(exitButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(130, 0, 50));
                g2.drawRoundRect(exitButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(181, 0, 99, 140));

                g2.drawString("Exit", exitButtonX + 55, buttonY + 36);

                restartButton = new Rectangle(restartButtonX, buttonY, buttonWidth, buttonHeight);
                exitButton = new Rectangle(exitButtonX, buttonY, buttonWidth, buttonHeight);

            } else {
                g2.setColor(new Color(240, 217, 181));
                g2.fillRoundRect(xPos, yPos, width, height, round, round);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(new Color(130, 85, 50));
                g2.drawRoundRect(xPos, yPos, width, height, round, round);

                g2.setColor(new Color(181, 136, 99));

                s = "CONGRATULATIONS";
                fm = g2.getFontMetrics();
                stringWidth = fm.stringWidth(s);
                stringHeight = fm.getHeight();
                textX = (BOARD_WIDTH - stringWidth) / 2;
                textY = (BOARD_HEIGHT) / 2 + stringHeight * 2 + 40;
                g2.drawString(s, textX, textY - stringHeight - 100);

                s = "BLACK WINS";
                fm = g2.getFontMetrics();
                stringWidth = fm.stringWidth(s);
                stringHeight = fm.getHeight();
                textX = (BOARD_WIDTH - stringWidth) / 2;
                textY = (BOARD_HEIGHT) / 2 + stringHeight;
                g2.drawString(s, textX, textY - 100);

                int buttonWidth = 200;
                int buttonHeight = 50;
                int buttonY = (BOARD_WIDTH + stringHeight) / 2;
                int restartButtonX = xPos + 50;
                int exitButtonX = xPos + width - buttonWidth - 50;

                g2.setColor(new Color(0, 217, 181, 50));
                g2.fillRoundRect(restartButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(0, 85, 50));
                g2.drawRoundRect(restartButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(0, 136, 99, 200));

                g2.drawString("Restart", restartButtonX + 17, buttonY + 36);

                g2.setColor(new Color(240, 0, 181, 50));
                g2.fillRoundRect(exitButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(130, 0, 50));
                g2.drawRoundRect(exitButtonX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g2.setColor(new Color(181, 0, 99, 140));

                g2.drawString("Exit", exitButtonX + 55, buttonY + 36);

                restartButton = new Rectangle(restartButtonX, buttonY, buttonWidth, buttonHeight);
                exitButton = new Rectangle(exitButtonX, buttonY, buttonWidth, buttonHeight);

            }

            if (mouse.pressed) {
                if (restartButton.contains(mouse.x, mouse.y)) {
                    restart();
                }
                if (exitButton.contains(mouse.x, mouse.y)) {
                    System.exit(0);
                }
            }

        } else if (stalemate) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0 + Board.BORDER_THICKNESS, 0 + Board.BORDER_THICKNESS, BOARD_WIDTH - Board.BORDER_THICKNESS * 2, BOARD_HEIGHT - Board.BORDER_THICKNESS * 2);
            g2.drawString("STALEMATE", 75 + LEFT_BORDER, TOP_BORDER + 100);
        } else {
            if (promotion) {
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(0 + Board.BORDER_THICKNESS, 0 + Board.BORDER_THICKNESS, BOARD_WIDTH - Board.BORDER_THICKNESS * 2, BOARD_HEIGHT - Board.BORDER_THICKNESS * 2);
                lastMove = null;
                int height = 150, width = 540, xPos = BOARD_WIDTH / 2 - width / 2, yPos = BOARD_HEIGHT / 2 - height / 2, round = 15;
                g2.setColor(new Color(240, 217, 181));
                g2.fillRoundRect(xPos, yPos, width, height, round, round);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(new Color(130, 85, 50));
                g2.drawRoundRect(xPos, yPos, width, height, round, round);

                int height2 = 50, width2 = 240, round2 = 10;
                int xPos2 = BOARD_WIDTH / 2 - width2 / 2;
                int yPos2 = -height2;

                drawRoundedTopRect(g2, xPos2, yPos2, width2, height2, round2, round2, new Color(240, 217, 181), new Color(130, 85, 50));
                g2.setColor(new Color(181, 136, 99));
                g2.drawString("Promote to", 298, -12);

                int counter = 0;
                for (Piece piece : promoPieces) {
                    int size = height;
                    counter++;
                    g2.drawImage(piece.image, xPos * counter, yPos, size, size, null);
                    piece.x = (xPos * counter) + size / 2;
                    piece.y = yPos + size / 2;
                }
            } else {
                if (currentColor == WHITE) {
                    int height2 = 50, width2 = 270, round = 10;
                    int xPos = BOARD_WIDTH / 2 - width2 / 2;
                    int yPos = -height2;

                    drawRoundedTopRect(g2, xPos, yPos, width2, height2, round, round, new Color(240, 217, 181), new Color(130, 85, 50));
                    g2.setColor(new Color(181, 136, 99));
                    g2.drawString("White's turn", 277, -12);
                } else {
                    int height2 = 50, width2 = 270, round = 10;
                    int xPos = BOARD_WIDTH / 2 - width2 / 2;
                    int yPos = -height2;

                    drawRoundedTopRect(g2, xPos, yPos, width2, height2, round, round, new Color(240, 217, 181), new Color(130, 85, 50));
                    g2.setColor(new Color(181, 136, 99));
                    g2.drawString("Black's turn", 278, -12);
                }

            }

        }

    }

}
