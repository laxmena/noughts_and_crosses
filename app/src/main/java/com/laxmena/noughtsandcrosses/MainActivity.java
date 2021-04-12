package com.laxmena.noughtsandcrosses;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import static com.laxmena.noughtsandcrosses.ConstantsUtil.CROSS_WINS_TOAST_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.DRAW_TOAST_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.EMPTY_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.GAME_RESULT;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_VAL;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.NOUGHT_WINS_TOAST_MESSAGE;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.ROW;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.THREAD_SLEEP_TIME;
import static com.laxmena.noughtsandcrosses.ConstantsUtil.UNFINISHED;

/**
 * Noughts and Crosses - Bot vs Bot Tic-Tac-Toe app.
 *
 * - Two Bots plays the game of TicTacToe against each other.
 * - Bot1 or "Player Cross" randomly plays in the board.
 * - Bot 2 or "Player Nought" uses Greedy Strategy. If there is an immediate threat or an
 * immediate opportunity to win the game, This bot plays that position.
 * - This app supports 'Dark Mode'.
 * - App is designed in a way that it can be easily extended to Playing grids
 * of any size (eg. 4*4, 5*5, etc). Minimal changes to the UI and GameUtil reference to the cells \
 * will be sufficient.
 *
 * @author Lakshmanan Meiyappan
 * @version 1.0
 * @since 04/11/2021
 */

public class MainActivity extends AppCompatActivity {

    private ArrayList boardPositions, availablePositions;
    private int gameResult;
    private Thread playerCross, playerNought;
    private Handler playerCrossHandler, playerNoughtHandler;
    private LinearLayout mainLayout;
    private TextView resultTextView;
    GameUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);
        resultTextView = findViewById(R.id.result_text);
        util = new GameUtil();

        if(savedInstanceState != null) {
            boardPositions = savedInstanceState.getIntegerArrayList(BOARD_POSITIONS);
            availablePositions = savedInstanceState.getIntegerArrayList(AVAILABLE_POSITIONS);
            gameResult = savedInstanceState.getInt(GAME_RESULT);
        } else {
            // Initialize Game and Update the View
            initializeGame();
        }
        publishWinner();
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
        publishWinner();
    }

    // Method invoked when Start button in the UI is clicked
    public void startGame(View view) {
        cleanUpThreads();
        initializeGame();
        startPlayerThreads();
        updateView();
    }

    // Create and Start the Player Threads
    private void startPlayerThreads() {
        playerCross = new PlayerCross();
        playerNought = new PlayerNought();
        playerCross.start();
        playerNought.start();
    }

    public void cleanUpThreads() {
        // If there are un-executed messages from the previous game, clear the message queue.
        mHandler.removeCallbacksAndMessages(null);
        if(playerCross != null && playerCross.isAlive()) {
            playerCrossHandler.removeCallbacksAndMessages(null);
            playerCrossHandler.getLooper().quit();
        }
        if(playerNought != null && playerNought.isAlive()) {
            playerNoughtHandler.removeCallbacksAndMessages(null);
            playerNoughtHandler.getLooper().quit();
        }
    }

    // Method sets Cross at the given index
    public void setCrossAtIndex(int index) {
        boardPositions.set(index, CROSS_VAL);
        availablePositions.remove(Integer.valueOf(index));
        updateCellView(index, CROSS_VAL);
    }

    // Method sets Nought (O) at the given index
    public void setNoughtAtIndex(int index) {
        boardPositions.set(index, NOUGHT_VAL);
        availablePositions.remove(Integer.valueOf(index));
        updateCellView(index, NOUGHT_VAL);
    }

    // Updates single cell view
    private void updateCellView(int index, int value) {
        TextView cell = findViewById(BOARD_UI_POSITIONS[index]);
        util.updateCellView(cell, value);
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
        gameResult = util.getGameResult(boardPositions, availablePositions);
        return gameResult;
    }

    // Helper method to get the next random value
    private int getNextPositionRandom() {
        return util.getNextPositionRandom(availablePositions);
    }

    // Helper method to get the next playable location using Greedy Strategy
    private int getNextPositionGreedy() {
        return util.getNextPositionGreedy(boardPositions, availablePositions);
    }

    // Publish the result of the Game in the UI
    private void publishWinner() {
        String toastMessage = EMPTY_MESSAGE;
        String message = EMPTY_MESSAGE;

        if(gameResult == CROSS_WINS) {
            toastMessage = CROSS_WINS_TOAST_MESSAGE;
            message = CROSS_WINS_MESSAGE;
        }
        else if(gameResult == NOUGHT_WINS){
            toastMessage = NOUGHT_WINS_TOAST_MESSAGE;
            message = NOUGHT_WINS_MESSAGE;
        }
        else if(gameResult == DRAW) {
            toastMessage = DRAW_TOAST_MESSAGE;
            message = DRAW_MESSAGE;
        }

        if(gameResult != UNFINISHED) {
            Snackbar snackbar = Snackbar.make(mainLayout, toastMessage, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        resultTextView.setText(message);
    }

    // Used to show delay between moves in the UI
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

        // Compute the board position to play next and notify Main thread
        private void makeMove() {
            Message msg = mHandler.obtainMessage(CROSS_VAL);
            msg.arg1 = getNextPositionRandom();
            mHandler.sendMessage(msg);
        }

        @Override
        public void run() {
            if(gameResult != UNFINISHED) return;
            Looper.prepare();
            makeMove(); // Make the first move
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
        // Compute the board position to play next and notify Main thread
        private void makeMove() {
            Message msg = mHandler.obtainMessage(NOUGHT_VAL);
            msg.arg1 = getNextPositionGreedy();
            mHandler.sendMessage(msg);
        }

        @Override
        public void run() {
            Looper.prepare();
            // Handle messages receiving from the UI Thread
            playerNoughtHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if(gameResult == UNFINISHED)  makeMove();
                }
            };
            Looper.loop();
        }
    }

}