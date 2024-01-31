package com.example.pokemoncardgame;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public interface CardsCallback {
        void onCardsReceived(List<EveryCards> cards);
        void onCardsError(String errorMessage);
    }

    public static RequestQueue requestQueue;
    List<EveryCards> cards;
    List<EveryCards> playerOneCards;
    List<EveryCards> playerTwoCards;
    List<Card> detailedCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initGui();

        requestQueue = Volley.newRequestQueue(this);

        // Cards should now be filled.
        getAllCards(new CardsCallback() {
            @Override
            public void onCardsReceived(List<EveryCards> receivedCards) {
                // Assign Cards to players
                cards = receivedCards;
                System.out.println("Testing getTenRandomCards()");
                getTenRandomCards();
                layoutCards();
                System.out.println("Testing getCard()");
                getCard();
            }

            @Override
            public void onCardsError(String errorMessage) {
                // Handle error, show a message, etc.
                Log.e("Cards", errorMessage);
            }
        });
    }

    private void initGui() {

    }

    private void getAllCards(CardsCallback callback) {
        String url = "https://api.tcgdex.net/v2/en/cards";

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            cards = new Gson().fromJson(response, new TypeToken<List<EveryCards>>(){}.getType());

            if (cards != null) {
                Log.d("Cards", "Received cards: " + cards.size());
                callback.onCardsReceived(cards);
            } else {
                Log.e("Cards", "Failed to parse cards data.");
                callback.onCardsError("Failed to parse cards data.");
            }

        }, error -> {
            Log.e("Cards", "Error fetching cards data: " + error.toString());
            callback.onCardsError("Error fetching cards data: " + error.toString());
        });
        requestQueue.add(request);
    }

    private void layoutCards() {
        // LinearLayOut Setup
        LinearLayout linearLayout= new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.
                LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        );
        //ImageView Setup
        ImageView imageView = new ImageView(this);

        // Setting image resource
        // Url with card stuff.
        Picasso.get().load(detailedCards)
                .resize(50,50)
                .into(imageView);

        //setting image position
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.
                LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        //adding view to layout
        linearLayout.addView(imageView);

        //make visible to program
        setContentView(linearLayout);
    }

    private void getCard() {
        List<EveryCards> allPlayerCards = new ArrayList<>(playerOneCards);
        allPlayerCards.addAll(playerTwoCards);

        // Initialize detailedCards list
        detailedCards = new ArrayList<>();

        for (EveryCards  card : allPlayerCards)
        {
            String cardId = card.id;

            String url = "https://api.tcgdex.net/v2/en/cards/" + cardId;

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                Card detailedCard  = new Gson().fromJson(response, Card.class);

                detailedCards.add(detailedCard);

                System.out.println("Card: "+ detailedCard.name);

            }, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            );

            requestQueue.add(request);
        }
    }

    public void getTenRandomCards() {
        System.out.println("Testing getTenRandomCards()");

        // Initialize playerOneCards and playerTwoCards
        playerOneCards = new ArrayList<>();
        playerTwoCards = new ArrayList<>();

        Random rand = new Random();

        Log.d("GameActivity", "Selecting twenty elements randomly from the cards list:");

        // Size of cards
        int numberOfRandomCards = 20;

        System.out.println("Selecting twenty elements randomly from the cards list : ");
        System.out.println("List of cards: " + cards.size());
        // Loop 20 times
        for (int i = 0; i < numberOfRandomCards; i++) {
            // Generate a random index
            int randomIndex = rand.nextInt(cards.size());

            // If number is even or uneven add according to if it is.
            if ( i % 2 == 0)
            {
                playerOneCards.add(cards.get(randomIndex));

                // Console write
                System.out.println("Player One Card: " + playerOneCards.get(playerOneCards.size() - 1));
            }
            else
            {
                playerTwoCards.add(cards.get(randomIndex));
                // Console write
                System.out.println("Player Two Card: " + playerTwoCards.get(playerTwoCards.size() - 1));
            }
        }
    }
    public void battleCards()
    {
        //TODO Get stats of 1 card for each player.
    }
}