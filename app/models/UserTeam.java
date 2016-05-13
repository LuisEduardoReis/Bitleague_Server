package models;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class UserTeam {

    ArrayList<String> players;
    boolean hasTeam;
    ArrayList<String> benchers;
    ArrayList<String> defences;
    ArrayList<String> midfielders;
    ArrayList<String> strickers;
    String goalkeeper;



    public UserTeam() {
        hasTeam = false;

        players = new ArrayList<>();
    //    lineup = new ArrayList<>();
        benchers = new ArrayList<>();
        defences = new ArrayList<>();
        midfielders = new ArrayList<>();
        strickers = new ArrayList<>();

    }

    public String checkValidity() {
        if (strickers.size() + defences.size() + midfielders.size() +1 != 11 ) return "Lineup must have 11 players!";


        if (!goalkeeper.equals("")) return "Lineup must have exactly 1 goalkeeper";
        if (defences.size() < 3) return "Lineup can't have less than 3 defences";
        if (defences.size() > 5) return "Lineup can't have more than 5 defences";
        if (midfielders.size() < 2) return "Lineup can't have less than 2 midfielders";
        if (midfielders.size()> 5) return "Lineup can't have more than 5 midfielders";
        if (strickers.size() < 1) return "Lineup must have at least 1 striker";
        if (strickers.size() > 4) return "Lineup can't have more than 4 strikers";

        return "ok";
    }


    public void removePlayer(String player_id) {
        players.remove(player_id);
        if(benchers.contains(player_id))
            benchers.remove(player_id);

        if(defences.contains(player_id))
            defences.remove(player_id);

        if(midfielders.contains(player_id))
            midfielders.remove(player_id);

        if(strickers.contains(player_id))
            strickers.remove(player_id);

        if(goalkeeper.equals(player_id))
            goalkeeper ="";
    }

    public void addPlayer(String player_id) {
        players.add(player_id);
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public ArrayList<String> getBenchers() {
        return benchers;
    }

    public void setBenchers(ArrayList<String> benchers) {
        this.benchers = benchers;
    }

    public ArrayList<String> getDefences() {
        return defences;
    }

    public void setDefences(ArrayList<String> defences) {
        this.defences = defences;
    }

    public ArrayList<String> getMidfielders() {
        return midfielders;
    }

    public void setMidfielders(ArrayList<String> midfielders) {
        this.midfielders = midfielders;
    }

    public ArrayList<String> getStrickers() {
        return strickers;
    }

    public void setStrickers(ArrayList<String> strickers) {
        this.strickers = strickers;
    }
}
