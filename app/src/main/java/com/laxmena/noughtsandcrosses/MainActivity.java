package com.laxmena.noughtsandcrosses;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.View;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import static com.laxmena.noughtsandcrosses.ConstantsUtil.BOARD_UI_POSITIONS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.COLUMN;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_COLOR;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_STRING;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DEFAULT_COLOR;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_STRING;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_COLOR;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_STRING;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.ROW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.UNFINISHED;
import static com.laxmena.noughtsandcrosses.GameUtil.getGameResult;
import static com.laxmena.noughtsandcrosses.GameUtil.getNextPositionRandom;

public class MainActivity extends AppCompatActivity {

    public ArrayList boardPositions, availablePositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Game and Update the View
        initializeGame();
        updateView();
    }

    /**
     * initializeGame() : Initializes Board Positions and Available Positions List
     *
     * boardPositions: ArrayList contains information about the value in that position.
     *                0 - denotes its empty
     *                1 - represents Cross (X)
     *                2 - represents Noughts (O)
     * availablePositions: contains the position's index where the players can occupy.
     *
     * Initially all boardPositions will be empty, and all positions will be available to play.
     */

    private void initializeGame() {
        boardPositions = new ArrayList();
        availablePositions = new ArrayList();

        for(int i = 0; i< ROW*COLUMN; i++) {
            boardPositions.add(EMPTY_VAL);
            availablePositions.add(i);
        }
    }

    // Method invoked when Start button in the UI is clicked
    public void startGame(View view) {
        initializeGame();
        playGame();
        updateView();
    }

    private void playGame() {
        int gameResult = UNFINISHED;
        int move = 0;
        while(gameResult == UNFINISHED) {
            if(move % 2 == 0) {
                setCrossAtIndex(getNextPositionRandom(availablePositions));
            } else {
                setNoughtAtIndex(getNextPositionRandom(availablePositions));
            }
            gameResult = getGameResult(boardPositions, availablePositions);
            move += 1;
        }
    }

    public void setCrossAtIndex(int index) {
        boardPositions.set(index, CROSS_VAL);
        availablePositions.remove(Integer.valueOf(index));
        updateCellView(index, CROSS_VAL);
    }

    public void setNoughtAtIndex(int index) {
        boardPositions.set(index, NOUGHT_VAL);
        availablePositions.remove(Integer.valueOf(index));
        updateCellView(index, NOUGHT_VAL);
    }

    // Updates single cell view
    private void updateCellView(int index, int value) {
        TextView cell = findViewById(BOARD_UI_POSITIONS[index]);
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

    // Updates entire board's view
    private void updateView() {
        for(int i=0; i<ROW*COLUMN; i++) {
            int value = (int) boardPositions.get(i);
            updateCellView(i, value);
        }
    }

}