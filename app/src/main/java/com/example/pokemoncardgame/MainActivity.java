package com.example.pokemoncardgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button startGameButton;
    List<EveryCards> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGui();

        startGameButton.setOnClickListener(this);
    }

    private void initGui() {
        startGameButton = findViewById(R.id.btn_start_game);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), GameActivity.class);

        startActivity(intent);
    }
}