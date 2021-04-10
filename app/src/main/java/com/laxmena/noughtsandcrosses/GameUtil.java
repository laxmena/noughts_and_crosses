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

    Random random = new Random();

    // Logic to check if the match is over and if there are any winners.
    // This logic is scalable, and can be used for any grid size.
    public int getGameResult(ArrayList positions, ArrayList availablePositions) {
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

    // Pass gameResult to this method to get the winner
    private int getWinnerForValue(int gameResult) {
        switch (gameResult) {
            case CROSS_VAL: return CROSS_WINS;
            case NOUGHT_VAL: return NOUGHT_WINS;
            default: return UNFINISHED;
        }
    }

    // This strategy returns random position, where the player can play next.
    public int getNextPositionRandom(ArrayList availablePositions) {
        synchronized (random) {
            int rand = random.nextInt(availablePositions.size());
            return (int) availablePositions.get(rand);
        }
    }

    // Updates the view of the given cell with the respective value
    public void updateCellView(TextView cell, int value) {
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

    // Uses Greedy Strategy to find the next move to be played.
    // If there is any immediate threat or a chance for immediate victory, we capitalize on that.
    public int getNextPositionGreedy(ArrayList positions, ArrayList availablePositions) {
        ArrayList<Integer> newAvailablePositions;
        int crossResult, noughtResult;

        for(int i=0; i<availablePositions.size(); i++) {
            int moveIndex = (int)availablePositions.get(i);
            newAvailablePositions = (ArrayList<Integer>) availablePositions.clone();
            newAvailablePositions.remove(Integer.valueOf(moveIndex));

            // logic for cross wins
            positions.set(moveIndex, CROSS_VAL);
            crossResult = getGameResult(positions, newAvailablePositions);
            // logic for nought wins
            positions.set(moveIndex, NOUGHT_VAL);
            noughtResult = getGameResult(positions, newAvailablePositions);

            // Reset the value
            positions.set(moveIndex, EMPTY_VAL);

            // If either of crossResult or noughtResult is not UNFINISHED, We play there.
            // Playing in this cell will either ensure victory
            // or prevent Opponent to secure victory by occupy this threatening cell.
            if(crossResult != UNFINISHED || noughtResult != UNFINISHED) {
                return moveIndex;
            }
        }

        // If there is no immediate threat or victory opportunity, we play a random position.
        return getNextPositionRandom(availablePositions);
    }


}
