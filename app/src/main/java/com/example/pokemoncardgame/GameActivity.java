package com.example.pokemoncardgame;

import android.os.Bundle;
import android.util.Log;
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

    // Interface for handling card callbacks
    public interface CardsCallback {
        void onCardsReceived(List<EveryCards> cards);
        void onCardsError(String errorMessage);
    }

    // Interface for handling detailed card callbacks
    public interface DetailedCardsCallback {
        void onCardsReceived(List<Card> cards);
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

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Fetch all cards
        getAllCards(new CardsCallback() {
            @Override
            public void onCardsReceived(List<EveryCards> receivedCards) {
                // Assign received cards to the global cards list
                cards = receivedCards;

                // Fetch ten random cards for two players
                getTenRandomCards();

                // Fetch detailed information for each card
                getCard(new DetailedCardsCallback() {
                    @Override
                    public void onCardsReceived(List<Card> receivedCards) {
                        // Now that you have detailedCards, you can pass it to layoutCards
                        layoutCards(receivedCards);
                        battleCards();
                    }

                    @Override
                    public void onCardsError(String errorMessage) {
                        // Handle error, show a message, etc.
                        Log.e("Cards", errorMessage);
                    }
                });
            }

            @Override
            public void onCardsError(String errorMessage) {
                // Handle error, show a message, etc.
                Log.e("Cards", errorMessage);
            }
        });
    }

    // Initialize GUI components (if any)
    private void initGui() {
        // Add initialization code for GUI components if needed
    }

    // Fetch all cards from the API
    private void getAllCards(CardsCallback callback) {
        String url = "https://api.tcgdex.net/v2/en/cards";

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            // Parse the JSON response using Gson
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

    // Display the card images in the layout
    private void layoutCards(List<Card> detailedCards) {
        System.out.println("\n\nStarting layoutCards method! \n\n");
        // LinearLayOut Setup
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        );
        int numberOfCardsToDisplay = 10;

        if (detailedCards == null)
        {
            System.err.println("\ndetailedCards is null. Returning!\n");
            return;
        }
        else {
            System.out.println("\ndetailedCards is populated!\n");
            for (int i = 0; i < numberOfCardsToDisplay; i++) {
                System.out.println("Starting loop!");
                //ImageView Setup
                ImageView imageView = new ImageView(this);

                // Setting image resource
                System.out.println("Getting detailcards" + detailedCards.get(i));
                System.out.println("Getting detailcards index: " + i);
                Card card = detailedCards.get(i);

                System.out.println("Layout Card: " + card.name);
                System.out.println("Layout Card URL: " + card.image);
                System.out.println("Picasso loading!");

                // Loading the image using Picasso library and appending "/low.jpg" to the URL
                Picasso.get().load(card.getImageUrl() + "/low.jpg")
                        .resize(500, 800)
                        .into(imageView);
                System.out.println("Picasso loaded!");

                // setting image position
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                );

                //adding view to layout
                linearLayout.addView(imageView);
            }
        }

        // make visible to program
        setContentView(linearLayout);
        System.out.println("\ndetailedCards is finished!\n");
    }

    // Fetch detailed information for each card
    private void getCard(DetailedCardsCallback callback) {
        System.out.println("getCard method. ");
        List<EveryCards> allPlayerCards = new ArrayList<>(playerOneCards);
        allPlayerCards.addAll(playerTwoCards);

        // Initialize detailedCards list
        detailedCards = new ArrayList<>();

        int[] cardsProcessed = {0};  // Using an array to make it effectively final

        for (EveryCards card : allPlayerCards) {
            String cardId = card.id;

            String url = "https://api.tcgdex.net/v2/en/cards/" + cardId;

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                // Parse the detailed card information using Gson
                Card detailedCard = new Gson().fromJson(response, Card.class);

                System.out.println("detailedCard category: " + detailedCard.category);
                System.out.println("Adding card: " + detailedCard);
                detailedCards.add(detailedCard);


                System.out.println("Card: " + detailedCard.name);
                System.out.println("Card URL: " + detailedCard.getImageUrl());

                cardsProcessed[0]++;

                if (cardsProcessed[0] == allPlayerCards.size()) {
                    // Call the callback when all detailed cards are fetched
                    callback.onCardsReceived(detailedCards);
                }

            }, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            );

            requestQueue.add(request);
        }
    }

    // Select random cards for two players
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

    // TODO: Implement the logic to get stats of 1 card for each player in the battleCards method

    public void battleCards() {
        System.out.println("\n\nStarting battleCards()\n\n");

        if (playerOneCards.size() <= 1 || playerTwoCards.size() <= 1) {
            // Ensure both players have at least one card
            return;
        }

        // Assume we are battling the first card of each player for simplicity
        Card playerOneCard = detailedCards.get(0);  // Change this based on your logic
        Card playerTwoCard = detailedCards.get(0);  // Change this based on your logic

        int playerOneAttack = 0;
        int playerTwoAttack = 0;

        int newAttackDamageValue = 0;

        int playerOneHP = playerOneCard.hp;
        int playerTwoHP = playerOneCard.hp;;

        // Simulate the battle
        while (playerOneHP > 0 && playerTwoHP > 0) {
            int i = 0;
            for (Attack attack : playerOneCard.attacks) {
                if (attack.damage == null)
                {
                    System.out.println("Damage is null, skipping card. ");
                    playerOneCard = detailedCards.get(i++);  // Change this based on your logic
                }
                else if (playerTwoHP > 0)  {
                    String cleanedDamageValue = attack.damage.trim()
                            .replaceAll("[-+]", "");
                    newAttackDamageValue = Integer.parseInt(cleanedDamageValue);
                    playerOneAttack = newAttackDamageValue;
                    System.out.println(playerOneCard.name + " Assigning attackValue " + playerOneAttack);
                    playerTwoHP -= playerOneAttack;
                    System.out.println("playerTwoHP after attack" + playerTwoHP);
                }
            }

            for (Attack attack : playerTwoCard.attacks) {
                if (attack.damage == null)
                {
                    System.out.println("Damage is null, skipping card. ");
                    playerTwoCard = detailedCards.get(i++);  // Change this based on your logic
                }
                else if (playerOneHP > 0) {
                    String cleanedDamageValue = attack.damage.trim()
                            .replaceAll("[-+]", "");
                    newAttackDamageValue = Integer.parseInt(cleanedDamageValue);
                    playerTwoAttack = newAttackDamageValue;
                    System.out.println(playerTwoCard.name + " Assigning attackValue " + playerTwoAttack);
                    playerOneHP -= playerTwoAttack;
                    System.out.println("playerOneHP" + playerOneHP);
                }
            }
        }

        // Determine the winner
        String winner;
        if (playerOneHP <= 0 && playerTwoHP <= 0) {
            winner = "It's a draw!";
        }
        else if (playerOneHP <= 0) {
            winner = "Player Two wins!";
            System.out.println(winner);
        }
        else {
            winner = "Player One wins!";
            System.out.println(winner);
        }

        // Display the result
        Toast.makeText(this, winner, Toast.LENGTH_LONG).show();
    }
}
