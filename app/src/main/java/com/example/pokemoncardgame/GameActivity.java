package com.example.pokemoncardgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    List<Card> playerOneDetailedCards = new ArrayList<>();;
    List<Card> playerTwoDetailedCards  = new ArrayList<>();;
    ImageView playerOneImageView;
    ImageView playerTwoImageView;
    Button btn_next_battle;
    Button btn_return_to_main_menu;
    TextView tv_playerOne_score_counter;
    TextView tv_playerTwo_score_counter;
    int playerOneCardNumber;
    int playerTwoCardNumber;
    int[] cardsProcessed = {0};

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
                        System.out.println("Battle counters: " + playerOneCardNumber + ", " + playerTwoCardNumber);
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

    // Check and call the callback when all detailed cards are fetched
    private void checkAndCallback(int[] cardsProcessed, DetailedCardsCallback callback) {
        System.out.println("cardsProcessed: "+cardsProcessed[0]);
        int totalCards = 20;
        if (cardsProcessed[0] == 20) {
            callback.onCardsReceived(playerOneDetailedCards, playerTwoDetailedCards);
            System.out.println("Fetched all " + totalCards + " cards!");
        } else {
            // If not all cards are fetched, continue fetching the remaining cards
            System.out.println("Fetching remaining cards...");
            getCard(callback);
        }
    }

    // Fetch detailed information for each card
    private void getCard(DetailedCardsCallback callback) {
        System.out.println("getCard method. ");
        List<EveryCards> everyPlayerOneCards = new ArrayList<>(playerOneCards);
        List<EveryCards> everyPlayerTwoCards = new ArrayList<>(playerTwoCards);

        // Initialize detailedCards list
        playerOneDetailedCards = new ArrayList<>();
        playerTwoDetailedCards = new ArrayList<>();

        int totalCards = everyPlayerOneCards.size() + everyPlayerTwoCards.size();  // Total number of cards
        System.out.println("everyPlayerOneCards: " + everyPlayerOneCards.size());

        for (EveryCards card : everyPlayerOneCards) {
            String cardId = card.id;

            String url = "https://api.tcgdex.net/v2/en/cards/" + cardId;

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                // Parse the detailed card information using Gson
                Card detailedCard = new Gson().fromJson(response, Card.class);

                System.out.println("P1 detailedCard category: " + detailedCard.category);

                // Check if the category is "Pokemon"
                if ("Pokemon".equals(detailedCard.category) && cardsProcessed[0] <= 20) {
                    System.out.println("Adding card to P1 list: " + detailedCard);
                    playerOneDetailedCards.add(detailedCard);
                    System.out.println("P1 Card: " + detailedCard.name);
                    System.out.println("P1 Card URL: " + detailedCard.image);

                    cardsProcessed[0]++;
                    System.out.println("P1 cardsProcessed: "+cardsProcessed[0]);
                }
                else {
                    System.out.println("Not adding to P1 list. Pokemon card category: "
                            + detailedCard.category);
                }

                // Check and call the callback
                checkAndCallback(cardsProcessed, callback);

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

                System.out.println("P2 detailedCard category: " + detailedCard.category);

                // Check if the category is "Pokemon"
                if ("Pokemon".equals(detailedCard.category ) && cardsProcessed[0] <= 20) {
                    System.out.println("Adding card to P2 list : " + detailedCard);
                    playerTwoDetailedCards.add(detailedCard);
                    System.out.println("P2 Card: " + detailedCard.name);
                    System.out.println("P2 Card URL: " + detailedCard.image);

                    cardsProcessed[0]++;
                    System.out.println("P2 cardsProcessed: "+cardsProcessed[0]);
                }
                else {
                    System.out.println("Not adding to list P2. Pokemon card category: "
                            + detailedCard.category);
                }

                // Check and call the callback
                checkAndCallback(cardsProcessed, callback);

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

        if (playerOneDetailedCards.size() <= 0 || playerTwoDetailedCards.size() <= 0) {
            // Ensure both players have at least one card
            System.out.println("Not enough cards to battle.");
            return;
        }

        if (playerOneCardNumber >= playerOneDetailedCards.size() || playerTwoCardNumber >= playerTwoDetailedCards.size()) {
            // No more cards to battle
            System.out.println("No more cards to battle. The game has ended.");
            return;
        }

        // Assume we are battling the first card of each player for simplicity
        System.out.println("\nplayerOneCardNumber index: " + playerOneCardNumber +"\n");
        System.out.println("\nplayerTwoCardNumber index: " + playerTwoCardNumber +"\n");
        Card playerOneCard = playerOneDetailedCards.get(playerOneCardNumber);  // Change this based on your logic
        Card playerTwoCard = playerTwoDetailedCards.get(playerTwoCardNumber);  // Change this based on your logic

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
            if (playerOneCard.attacks == null) {
                System.out.println("No attacks, skipping card. ");
                playerOneCard = playerOneDetailedCards.get(playerOneCardNumber++);
                Picasso.get().load(playerOneCard.image + "/low.jpg")
                        .resize(200, 400)
                        .into(playerOneImageView);
                System.out.println("After skipping card:");
                System.out.println("PlayerCard counters:" + playerOneCardNumber + ", " + playerTwoCardNumber);
            } else {
                for (Attack attack : playerOneCard.attacks) {
                    if (attack.damage == null) {
                        System.out.println("Damage is null, skipping card. ");
                        playerOneCard = playerOneDetailedCards.get(playerOneCardNumber++);
                        Picasso.get().load(playerOneCard.image + "/low.jpg")
                                .resize(200, 400)
                                .into(playerOneImageView);
                        System.out.println("After skipping card:");
                        System.out.println("PlayerCard counters:" + playerOneCardNumber + ", " + playerTwoCardNumber);
                    } else if (playerTwoHP > 0) {
                        String cleanedDamageValue = attack.damage.trim().replaceAll("[-+×x]", "");
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
            if (playerTwoCard.attacks == null) {
                System.out.println("No attacks, skipping card. ");
                playerTwoCard = playerOneDetailedCards.get(playerTwoCardNumber++);
                Picasso.get().load(playerTwoCard.image + "/low.jpg")
                        .resize(200, 400)
                        .into(playerOneImageView);
                System.out.println("After skipping card:");
                System.out.println("PlayerCard counters:" + playerOneCardNumber + ", " + playerTwoCardNumber);
            } else {
                for (Attack attack : playerTwoCard.attacks) {
                    if (attack.damage == null) {
                        System.out.println("Damage is null, skipping card. ");
                        playerTwoCard = playerTwoDetailedCards.get(playerTwoCardNumber++);  // Change this based on your logic
                        Picasso.get().load(playerTwoCard.image + "/low.jpg")
                                .resize(200, 400)
                                .into(playerTwoImageView);
                        playerTwoCardNumber++;
                        System.out.println("After skipping card:");
                        System.out.println("PlayerCard counters:" + playerOneCardNumber + ", " + playerTwoCardNumber);
                    } else if (playerOneHP > 0) {
                        String cleanedDamageValue = attack.damage.trim().replaceAll("[-+×x]", "");
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
        } else if (playerOneHP >= 1 && playerTwoHP <= 0) {
            winner = "Player one wins!";
            System.out.println(winner);
        } else if (playerTwoHP >= 1 && playerOneHP <= 0) {
            winner = "Player Two wins!";
            System.out.println(winner);
        } else {
            // Handle the case where the loop exited but none of the above conditions were met
            winner = "Unexpected result!";
            System.out.println(winner);
        }

        System.out.println("Before incrementing counters:");
        System.out.println("PlayerCard counters: " + playerOneCardNumber + ", " + playerTwoCardNumber);

        // Increment the counters
        playerOneCardNumber++;
        playerTwoCardNumber++;

        System.out.println("After incrementing counters:");
        System.out.println("PlayerCard counters:" + playerOneCardNumber + ", " + playerTwoCardNumber);
        System.out.println("Winner: " + winner);
    }
}
