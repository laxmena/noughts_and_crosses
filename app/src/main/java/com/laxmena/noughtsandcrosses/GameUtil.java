package com.laxmena.noughtsandcrosses;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import static com.laxmena.noughtsandcrosses.ConstantsUtil.*;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.COLUMN;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.ROW;

public class GameUtil {

    static Random random = new Random();
    int iter;

    public static int getGameResult(ArrayList positions, ArrayList availablePositions) {
        int result = UNFINISHED;
        boolean gameOver;

        // Check Diagonals
        if(ROW == COLUMN) {
            // Check TopLeft to BottomRight Diagonal
            gameOver = true;
            int topLeft = (int) positions.get(0);
            for(int i=0; i<ROW && gameOver; i++) {
                if(topLeft != (int) positions.get(i*ROW + i)) {
                    gameOver = false;
                }
            }
            if(gameOver) return getWinnerForValue(topLeft);

            // Check TopRight to BottomLeft Diagonal
            gameOver = true;
            int topRight = (int) positions.get(ROW-1);
            for(int i=0; i<ROW && gameOver; i++) {
                if(topRight != (int) positions.get((ROW-1)*(i+1)) ) {
                    gameOver = false;
                }
            }
            if(gameOver) return getWinnerForValue(topRight);
        }

        // Check Horizontal Rows
        for(int i=0; i<ROW; i++) {
            gameOver = true;
            int val = (int) positions.get(i*3);
            for(int j=0; j<COLUMN && gameOver; j++) {
                if(val != (int) positions.get( (i*3)+j) ) {
                    gameOver = false;
                }
            }
            if(gameOver) return getWinnerForValue(val);
        }

        // Check Vertical Columns
        for(int i=0; i<COLUMN; i++) {
            gameOver = true;
            int val = (int) positions.get(i);
            for(int j=0; j<COLUMN && gameOver; j++) {
                if(val != (int)positions.get( i+(j*3) )) {
                    gameOver = false;
                }
            }
            if(gameOver) return getWinnerForValue(val);
        }

        // Check for Draw
        if(availablePositions.size() == 0)
            return DRAW;

        return result;
    }

    private static int getWinnerForValue(int value) {
        switch (value) {
            case CROSS_VAL: return CROSS_WINS;
            case NOUGHT_VAL: return NOUGHT_WINS;
            default: return UNFINISHED;
        }
    }

    public static int getNextPositionRandom(ArrayList availablePositions) {
        synchronized (random) {
            int rand = random.nextInt(availablePositions.size());
            return (int) availablePositions.get(rand);
        }
    }

    public static void updateCellView(TextView cell, int value) {
        cell.setTypeface(Typeface.DEFAULT_BOLD);
        cell.setTextAppearance(android.R.style.TextAppearance_Material_Display3);
        cell.setGravity(Gravity.CENTER);

        if(value == CROSS_VAL) {
            cell.setTextColor(CROSS_COLOR);
            cell.setText(CROSS_STRING);
        }
        else if(value == NOUGHT_VAL) {
            cell.setTextColor(NOUGHT_COLOR);
            cell.setText(NOUGHT_STRING);
        }
        else if(value == EMPTY_VAL) {
            cell.setTextColor(DEFAULT_COLOR);
            cell.setText(EMPTY_STRING);
        }
    }

    public int miniMax(ArrayList positions, ArrayList availablePositions, int PLAYER_VAL) {
        iter += 1;

        int gameResult = getGameResult(positions, availablePositions);
        System.out.println("iter: " + iter + " | Game Result: " + gameResult);
        switch (gameResult) {
            case CROSS_WINS: return -10;
            case NOUGHT_WINS: return 10;
            case DRAW: return 0;
        }
        ArrayList moves = new ArrayList<Integer>();
        ArrayList scores = new ArrayList<Integer>();
        int score;

        for(int i=0; i<availablePositions.size(); i++) {
            int moveIndex = (int)availablePositions.get(i);
            moves.add(moveIndex);

            positions.set(moveIndex, PLAYER_VAL);
            availablePositions.remove(Integer.valueOf(moveIndex));
            switch (PLAYER_VAL) {
                case CROSS_VAL:
                    score = miniMax(positions, availablePositions, NOUGHT_VAL);
                    scores.add(score);
                    break;
                case NOUGHT_VAL:
                    score = miniMax(positions, availablePositions, CROSS_VAL);
                    scores.add(score);
                    break;
            }
            positions.set(moveIndex, EMPTY_VAL);
            availablePositions.add(moveIndex);
        }

        int bestMove = -1;
        int bestScore = -10000;
        System.out.println("Moves: " + moves.toString());
        System.out.println("Scores: " + scores.toString());
        for(int i=0; i<moves.size(); i++) {
            score = (int) scores.get(i);
            if(score > bestScore) {
                bestScore = score;
                bestMove = (int) moves.get(i);
            }
        }

        return bestMove;
    }

    public int getNextPositionMinimax(ArrayList positions,
                                             ArrayList availablePositions,
                                             int PLAYER_VAL) {
        int position = miniMax(positions, availablePositions, PLAYER_VAL);
        iter = 0;
        return position;
    }
}
