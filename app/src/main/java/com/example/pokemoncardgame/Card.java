package com.example.pokemoncardgame;

import java.util.ArrayList;

public class Card {
    public String category;
    public String id;
    public String illustrator;
    public String image;
    public String localId;
    public String name;
    public String rarity;
    public Set set;
    public Variants variants;
    public ArrayList<Integer> dexId;
    public int hp;
    public ArrayList<String> types;
    public String evolveFrom;
    public String stage;
    public ArrayList<Ability> abilities;
    public ArrayList<Attack> attacks;
    public ArrayList<Weakness> weaknesses;
    public Legal legal;
}
class Ability{
    public String type;
    public String name;
    public String effect;
}

class Attack{
    public ArrayList<String> cost;
    public String name;
    public String effect;
    public String damage;
}

class CardCount{
    public int official;
    public int total;
}

class Legal{
    public boolean standard;
    public boolean expanded;
}
class Set{
    public CardCount cardCount;
    public String id;
    public String logo;
    public String name;
    public String symbol;
}

class Variants{
    public boolean firstEdition;
    public boolean holo;
    public boolean normal;
    public boolean reverse;
    public boolean wPromo;
}

class Weakness{
    public String type;
    public String value;
}