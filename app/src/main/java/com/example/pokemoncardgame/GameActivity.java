package com.example.pokemoncardgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    // Interface for handling card callbacks
    public interface CardsCallback {
        void onCardsReceived(List<EveryCards> cards);
        void onCardsError(String errorMessage);
    }

    // Interface for handling detailed card callbacks
    public interface DetailedCardsCallback {
        void onCardsReceived(List<Card> playerOneCards, List<Card> playerTwoCards);
        void onCardsError(String errorMessage);
    }

    public static RequestQueue requestQueue;
    List<EveryCards> cards;
    List<EveryCards> playerOneCards;
    List<EveryCards> playerTwoCards;
    List<Card> playerOneDetailedCards;
    List<Card> playerTwoDetailedCards;
    ImageView playerOneImageView;
    ImageView playerTwoImageView;
    Button btn_next_battle;
    Button btn_return_to_main_menu;
    TextView tv_playerOne_score_counter;
    TextView tv_playerTwo_score_counter;
    int playerOneCardNumber;
    int playerTwoCardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initGui();



        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        btn_next_battle.setOnClickListener(this);

        btn_return_to_main_menu.setOnClickListener(this::goToMainMenu);

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
                    public void onCardsReceived(List<Card> playerOneCards, List<Card> playerTwoCards) {
                        // Now that you have detailedCards for both players, you can use them
                        // in your logic, such as displaying images and battling cards.
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

    private void goToMainMenu(View gameActivity) {
        Intent mainIntent = new Intent(gameActivity.getContext(),MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public void onClick(View view) {
        battleCards();
    }

    // Initialize GUI components (if any)
    private void initGui() {
        // Add initialization code for GUI components if needed
        playerOneImageView = findViewById(R.id.iv_playerOneCard);
        playerTwoImageView = findViewById(R.id.iv_playerTwoCard);
        btn_next_battle = findViewById(R.id.btn_next_battle);
        btn_return_to_main_menu = findViewById(R.id.btn_return_home);
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

    public void getCardImage()
    {

    }

/*    // Display the card images in the layout
    private void layoutCards(int playerOneId, int playerTwoId) {
        if (detailedCards == null)
        {
            System.err.println("\ndetailedCards is null. Returning!\n");
            return;
        }
        else {
            Card playerOneCard = detailedCards.get(playerOneId);
            // Loading the image using Picasso library and appending "/low.jpg" to the URL
            Picasso.get().load(playerOneCard.getImageUrl() + "/low.jpg")
                    .resize(200, 400)
                    .into(playerOneImageView);

            Card playerTwoCard = detailedCards.get(playerTwoId);
            // Loading the image using Picasso library and appending "/low.jpg" to the URL
            Picasso.get().load(playerTwoCard.getImageUrl() + "/low.jpg")
                    .resize(200, 400)
                    .into(playerTwoImageView);

            System.out.println("Picasso loaded!");
            System.out.println("\ndetailedCards is populated!\n");

        }
    }*/

    // Fetch detailed information for each card
    private void getCard(DetailedCardsCallback callback) {
        System.out.println("getCard method. ");
        List<EveryCards> everyPlayerOneCards = new ArrayList<>(playerOneCards);
        List<EveryCards> everyPlayerTwoCards = new ArrayList<>(playerTwoCards);

        // Initialize detailedCards list
        playerOneDetailedCards = new ArrayList<>();
        playerTwoDetailedCards = new ArrayList<>();

        int[] cardsProcessed = {0};  // Using an array to make it effectively final

        for (EveryCards card : everyPlayerOneCards) {
            String cardId = card.id;

            String url = "https://api.tcgdex.net/v2/en/cards/" + cardId;

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                // Parse the detailed card information using Gson
                Card detailedCard = new Gson().fromJson(response, Card.class);

                System.out.println("detailedCard category: " + detailedCard.category);
                System.out.println("Adding card: " + detailedCard);

                playerOneDetailedCards.add(detailedCard);

                System.out.println("Card: " + detailedCard.name);
                System.out.println("Card URL: " + detailedCard.image);

                cardsProcessed[0]++;
                System.out.println(cardsProcessed[0]);

                // Check the condition when all detailed cards are fetched
                if (cardsProcessed[0] == everyPlayerOneCards.size() + everyPlayerTwoCards.size()) {
                    System.out.println("Callbacking");
                    // Call the callback when all detailed cards are fetched
                    callback.onCardsReceived(playerOneDetailedCards, playerTwoDetailedCards);
                }

            }, error -> {
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
            });

            requestQueue.add(request);
        }

        for (EveryCards card : everyPlayerTwoCards) {
            String cardId = card.id;

            String url = "https://api.tcgdex.net/v2/en/cards/" + cardId;

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                // Parse the detailed card information using Gson
                Card detailedCard = new Gson().fromJson(response, Card.class);

                System.out.println("detailedCard category: " + detailedCard.category);
                System.out.println("Adding card: " + detailedCard);

                playerTwoDetailedCards.add(detailedCard);

                System.out.println("Card: " + detailedCard.name);
                System.out.println("Card URL: " + detailedCard.image);

                cardsProcessed[0]++;
                System.out.println(cardsProcessed[0]);

                // Check the condition when all detailed cards are fetched
                if (cardsProcessed[0] == everyPlayerOneCards.size() + everyPlayerTwoCards.size()) {
                    System.out.println("Callbacking");
                    // Call the callback when all detailed cards are fetched
                    callback.onCardsReceived(playerOneDetailedCards, playerTwoDetailedCards);
                }

            }, error -> {
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
            });

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
        Card playerOneCard = playerOneDetailedCards.get(playerOneCardNumber);  // Change this based on your logic
        Card playerTwoCard = playerTwoDetailedCards.get(playerOneCardNumber);  // Change this based on your logic

        Picasso.get().load(playerOneCard.image + "/low.jpg")
                .resize(200, 400)
                .into(playerOneImageView);

        Picasso.get().load(playerTwoCard.image + "/low.jpg")
                .resize(200, 400)
                .into(playerTwoImageView);

        int playerOneAttack = 0;
        int playerTwoAttack = 0;

        int newAttackDamageValue = 0;

        int playerOneHP = playerOneCard.hp;
        int playerTwoHP = playerTwoCard.hp;

        do {
            System.out.println("playerOneCard: " + playerOneCard);
            System.out.println("playerOneCard.attacks: " + playerOneCard.attacks);
            if (playerOneCard.attacks == null)
            {
                System.out.println("No attacks, skipping card. ");
                playerOneCardNumber++;
                playerOneCard = playerOneDetailedCards.get(playerOneCardNumber);
                Picasso.get().load(playerOneCard.image + "/low.jpg")
                        .resize(200, 400)
                        .into(playerOneImageView);
            }
            else {
                for (Attack attack : playerOneCard.attacks) {
                    if (attack.damage == null) {
                        System.out.println("Damage is null, skipping card. ");
                        playerOneCardNumber++;
                        playerOneCard = playerOneDetailedCards.get(playerOneCardNumber);
                        Picasso.get().load(playerOneCard.image + "/low.jpg")
                                .resize(200, 400)
                                .into(playerOneImageView);
                    } else if (playerTwoHP > 0) {
                        String cleanedDamageValue = attack.damage.trim().replaceAll("[-+x×]", "");
                        newAttackDamageValue = Integer.parseInt(cleanedDamageValue);
                        playerOneAttack = newAttackDamageValue;
                        System.out.println(playerOneCard.name + " Assigning attackValue " + playerOneAttack);
                        playerTwoHP -= playerOneAttack;
                        System.out.println("playerTwoHP after attack: " + playerTwoHP);
                    }
                }
            }


            System.out.println("playerTwoCard: " + playerTwoCard);
            System.out.println("playerTwoCard.attacks: " + playerTwoCard.attacks);
            if (playerTwoCard.attacks == null)
            {
                System.out.println("No attacks, skipping card. ");
                playerTwoCardNumber++;
                playerTwoCard = playerOneDetailedCards.get(playerTwoCardNumber);
                Picasso.get().load(playerTwoCard.image + "/low.jpg")
                        .resize(200, 400)
                        .into(playerOneImageView);

            }
            else
            {
                for (Attack attack : playerTwoCard.attacks) {
                    if (attack.damage == null) {
                        System.out.println("Damage is null, skipping card. ");
                        playerTwoCardNumber++;
                        playerTwoCard = playerTwoDetailedCards.get(playerTwoCardNumber);  // Change this based on your logic
                        Picasso.get().load(playerTwoCard.image + "/low.jpg")
                                .resize(200, 400)
                                .into(playerTwoImageView);
                    } else if (playerOneHP > 0) {
                        String cleanedDamageValue = attack.damage.trim().replaceAll("[-+x×]", "");
                        newAttackDamageValue = Integer.parseInt(cleanedDamageValue);
                        playerTwoAttack = newAttackDamageValue;
                        System.out.println(playerTwoCard.name + " Assigning attackValue: " + playerTwoAttack);
                        playerOneHP -= playerTwoAttack;
                        System.out.println("playerOneHP" + playerOneHP);
                    }
                }
            }

        } while (playerOneHP > 0 && playerTwoHP > 0);

        // Determine the winner
        String winner;
        if (playerOneHP <= 0 && playerTwoHP <= 0) {
            winner = "It's a draw!";
            System.out.println(winner);
            playerOneCardNumber++;
            playerTwoCardNumber++;
        } else if (playerOneHP >= 1 && playerTwoHP < 1) {
            winner = "Player one wins!";
            System.out.println(winner);
            playerOneCardNumber++;
            playerTwoCardNumber++;
        } else if (playerTwoHP >= 1 && playerTwoHP < 1) {
            winner = "Player Two wins!";
            System.out.println(winner);
            playerOneCardNumber++;
            playerTwoCardNumber++;
        }
    }
}
