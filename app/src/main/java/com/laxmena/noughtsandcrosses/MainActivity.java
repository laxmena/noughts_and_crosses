package com.laxmena.noughtsandcrosses;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static com.laxmena.noughtsandcrosses.ConstantsUtil.BOARD_UI_POSITIONS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.COLUMN;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.ROW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.UNFINISHED;

public class MainActivity extends AppCompatActivity {

    public ArrayList boardPositions, availablePositions;
    private int gameResult;
    private Thread playerCross, playerNought;
    private Handler playerCrossHandler, playerNoughtHandler;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mainLayout = findViewById(R.id.main_layout);
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
        gameResult = UNFINISHED;

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
        playerCross = new PlayerCross();
        playerNought = new Thread(new PlayerNought());
        playerCross.start();
        playerNought.start();
    }

    private void cleanUp() {
        playerCrossHandler.getLooper().quit();
        playerNoughtHandler.getLooper().quit();
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
        GameUtil.updateCellView(cell, value);
    }

    // Updates entire board's view
    private void updateView() {
        for(int i=0; i<ROW*COLUMN; i++) {
            int value = (int) boardPositions.get(i);
            updateCellView(i, value);
        }
    }

    // getGameResult helper method
    private int getGameResult() {
        gameResult = GameUtil.getGameResult(boardPositions, availablePositions);
        return gameResult;
    }

    private int getNextPositionRandom() {
        return GameUtil.getNextPositionRandom(availablePositions);
    }

    private void publishWinner() {
        String message = DRAW_MESSAGE;
        if(gameResult == CROSS_WINS) message = CROSS_WINS_MESSAGE;
        else if(gameResult == NOUGHT_WINS) message = NOUGHT_WINS_MESSAGE;
        Snackbar snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // Thread Logic
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            int what = msg.what;
            int index = msg.arg1;

            switch (what) {
                case CROSS_VAL:
                    setCrossAtIndex(index);
                    if (getGameResult() == UNFINISHED) {
                        playerNoughtHandler.sendMessage(playerNoughtHandler.obtainMessage());
                    }
                    break;
                case NOUGHT_VAL:
                    setNoughtAtIndex(index);
                    if (getGameResult() == UNFINISHED) {
                        playerCrossHandler.sendMessage(playerCrossHandler.obtainMessage());
                    }
                    break;
            }

            if(gameResult != UNFINISHED) publishWinner();
        }
    };

    public class PlayerCross extends Thread {

        private void makeMove() {
            Message msg = mHandler.obtainMessage(CROSS_VAL);
            msg.arg1 = getNextPositionRandom();
            mHandler.sendMessage(msg);
        }

        @Override
        public void run() {
            if(gameResult != UNFINISHED) return;
            Looper.prepare();
            makeMove();
            playerCrossHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if(gameResult == UNFINISHED)
                        makeMove();
                }
            };
            Looper.loop();
        }
    }

    public class PlayerNought extends Thread {

        private void makeMove() {
            Message msg = mHandler.obtainMessage(NOUGHT_VAL);
            msg.arg1 = getNextPositionRandom();
            mHandler.sendMessage(msg);
        }

        @Override
        public void run() {
            Looper.prepare();
            playerNoughtHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if(gameResult == UNFINISHED)
                        makeMove();
                }
            };
            Looper.loop();
        }
    }

}