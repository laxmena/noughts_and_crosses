package com.laxmena.noughtsandcrosses;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

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

    public static int getGameResult(ArrayList positions, ArrayList availablePositions) {
        int result = UNFINISHED;
        boolean gameOver;

        // Check Diagonals
        if(ROW == COLUMN) {
            // Check TopLeft to BottomRight Diagonal
            gameOver = true;
            int topLeft = (int) positions.get(0);
            for(int i=0; i<ROW && gameOver; i++) {
                if(topLeft != (int) positions.get(i)) {
                    gameOver = false;
                }
            }
            if(gameOver) return getWinnerForValue(topLeft);

            // Check TopRight to BottomLeft Diagonal
            gameOver = true;
            int topRight = (int) positions.get(ROW - 1);
            for(int i=0; i<ROW && gameOver; i++) {
                if(topRight != (int) positions.get(ROW - 1 - i)) {
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
        if(availablePositions.size() == 0) {
            return DRAW;
        }

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
        int rand = random.nextInt(availablePositions.size());
        return (int) availablePositions.get(rand);
    }


}
