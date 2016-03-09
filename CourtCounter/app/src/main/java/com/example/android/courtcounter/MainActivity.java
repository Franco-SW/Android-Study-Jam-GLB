package com.example.android.courtcounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int scoreTeamA = 0;
    int scoreTeamB = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void add3ForTeamA(View v){
        addToTeamAScore(3);
    }

    public void add2ForTeamA(View v){
        addToTeamAScore(2);
    }

    public void add1ForTeamA(View v){
        addToTeamAScore(1);
    }

    public void addToTeamAScore(int points){
        scoreTeamA += points;
        displayForTeamA(scoreTeamA);
    }

    public void displayForTeamA(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_a_score);
        scoreView.setText(String.valueOf(score));
    }


    public void add3ForTeamB(View v){
        addToTeamBScore(3);
    }

    public void add2ForTeamB(View v){
        addToTeamBScore(2);
    }

    public void add1ForTeamB(View v){
        addToTeamBScore(1);
    }

    public void addToTeamBScore(int points){
        scoreTeamB += points;
        displayForTeamB(scoreTeamB);
    }
    public void displayForTeamB(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_b_score);
        scoreView.setText(String.valueOf(score));
    }

    public void resetScores(View v){
        scoreTeamA = 0;
        scoreTeamB = 0;
        displayForTeamA(scoreTeamA);
        displayForTeamB(scoreTeamB);
    }
}
