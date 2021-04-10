package com.laxmena.noughtsandcrosses;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static com.laxmena.noughtsandcrosses.ConstantsUtil.AVAILABLE_POSITIONS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.BOARD_POSITIONS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.BOARD_UI_POSITIONS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.COLUMN;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.GAME_RESULT;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.ROW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.THREAD_SLEEP_TIME;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.UNFINISHED;

public class MainActivity extends AppCompatActivity {

    private ArrayList boardPositions, availablePositions;
    private int gameResult;
    private Thread playerCross, playerNought;
    private Handler playerCrossHandler, playerNoughtHandler;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);

        if(savedInstanceState != null) {
            boardPositions = savedInstanceState.getIntegerArrayList(BOARD_POSITIONS);
            availablePositions = savedInstanceState.getIntegerArrayList(AVAILABLE_POSITIONS);
            gameResult = savedInstanceState.getInt(GAME_RESULT);
        } else {
            // Initialize Game and Update the View
            initializeGame();
        }
        updateView();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putIntegerArrayList(BOARD_POSITIONS, boardPositions);
        outState.putIntegerArrayList(AVAILABLE_POSITIONS, availablePositions);
        outState.putInt(GAME_RESULT, gameResult);
        super.onSaveInstanceState(outState);
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

    // Helper method to find the game result
    private int getGameResult() {
        gameResult = GameUtil.getGameResult(boardPositions, availablePositions);
        return gameResult;
    }

    // Helper method to get the next random value
    private int getNextPositionRandom() {
        return GameUtil.getNextPositionRandom(availablePositions);
    }

    // Publish the result of the Game in the UI
    private void publishWinner() {
        updateView();
        String message = DRAW_MESSAGE;
        if(gameResult == CROSS_WINS) message = CROSS_WINS_MESSAGE;
        else if(gameResult == NOUGHT_WINS) message = NOUGHT_WINS_MESSAGE;
        Snackbar snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void sleepThread(Thread thread) {
        try {
            thread.sleep(THREAD_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Main Handler which mediates between two threads
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            int what = msg.what;
            int index = msg.arg1;

            if(what == CROSS_VAL) {
                setCrossAtIndex(index);
                // If the game is unfinished, notify Nought player to make the next move
                if (getGameResult() == UNFINISHED) {
                    sleepThread(playerCross);
                    playerNoughtHandler.sendMessage(playerNoughtHandler.obtainMessage());
                }
            } else if(what == NOUGHT_VAL) {
                setNoughtAtIndex(index);
                // If the game is unfinished, notify Cross Player to play next move
                if (getGameResult() == UNFINISHED) {
                    sleepThread(playerNought);
                    playerCrossHandler.sendMessage(playerCrossHandler.obtainMessage());
                }
            }

            // If the game is finished, publish the winner in UI
            if(gameResult != UNFINISHED) publishWinner();
        }
    };

    /**
     * PlayerCross Thread implements the logic for Cross Player's moves.
     * Message Handler is defined, so that it can perform action when UI Thread sends a message.
     *
     * This is the first player, so it makes the first move in the game.
     */
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
            // Make the first move in the game
            makeMove();

            // Handle messages from the UI Thread
            playerCrossHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if(gameResult == UNFINISHED)
                        makeMove();
                }
            };
            Looper.loop();
        }
    }

    /**
     * PlayerNought Thread Class defines the logic for Player2 or Nought Player.
     * Similar to PlayerCross, this method also defines a handler to handle messages.
     *
     * Since this is the second player, moves are made only after first player has played.
     */
    public class PlayerNought extends Thread {
        GameUtil util = new GameUtil();
        private void makeMove() {
            Message msg = mHandler.obtainMessage(NOUGHT_VAL);
            msg.arg1 = util.getNextPositionMinimax(boardPositions, availablePositions, NOUGHT_VAL);
//            msg.arg1 = getNextPositionRandom();
            mHandler.sendMessage(msg);
        }

        @Override
        public void run() {
            Looper.prepare();
            // Handlemessages from the UI Thread
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