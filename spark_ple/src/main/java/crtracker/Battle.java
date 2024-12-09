package crtracker;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Array;
import java.time.Instant;
import java.util.ArrayList;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.codehaus.jackson.annotate.JsonProperty;
/*
{"date":"2024-09-13T07:27:05Z","game":"gdc","mode":"Rage_Ladder","round":0,"type":"riverRacePvP",
"winner":0,
"players":[{"utag":"#C82LPR8J","ctag":"#QQ9QGJYJ","trophies":9000,"ctrophies":1100,"exp":67,"league":6,
"bestleague":10,
"deck":"0a18323d4a4c616b",
"evo":"0a18",
"tower":"",
"strength":15.375,
"crown":2,
"elixir":1.56,
"touch":1,
"score":200},
{"utag":"#9UC2GUJVP","ctag":"#QQJCR9CP","trophies":7160,"ctrophies":1012,"exp":46,"league":1,"bestleague":4,"deck":"05070c14171f445e","evo":"","tower":"","strength":13,"crown":0,"elixir":6.14,"touch":1,"score":100}],"warclan":{"day":3,"hour_seg":3,"period":"112-1","training":[false,false]}}
*/


class Player implements Serializable{
	@JsonProperty("utag")
	public String utag;
	@JsonProperty("ctag")
	public String ctag;
	@JsonProperty("trophies")
	public int trophies;
	@JsonProperty("ctrophies")
	public int ctrophies;
	@JsonProperty("exp")
	public int exp;
	@JsonProperty("league")
	public int league;
	@JsonProperty("bestleague")
	public int bestleague;
	@JsonProperty("deck")
	public String deck;
	@JsonProperty("evo")
	public String evo;
	@JsonProperty("tower")
	public String tower;
	@JsonProperty("strength")
	public double strength;
	@JsonProperty("elixir")
	public double elixir;
	@JsonProperty("touch")
	public int touch;
	@JsonProperty("score")
	public int score;

    public boolean verifyData()
    {
        // Vérification des champs String : ils ne doivent pas être null ou vides
        if (utag == null || utag.isEmpty() ||
        ctag == null || ctag.isEmpty() ||
        deck == null || deck.isEmpty() ||
        deck.length() != 16 
        ) return false;


        // Si toutes les vérifications passent
        return true;
    }
}
class WarClan implements Serializable{
	@JsonProperty("day")
	public int day=0;
	@JsonProperty("hourd_seg")
	public int hour_seg=0;
	@JsonProperty("period")
	public String period;
	@JsonProperty("training")
	ArrayList<Boolean> training;	
}

class Battle implements Serializable{
	@JsonProperty("date")
	public String date;
	@JsonProperty("game")
	public String game;
	@JsonProperty("mode")
	public String mode;
	@JsonProperty("round")
	public int round;
	@JsonProperty("type")
	public String type;
	@JsonProperty("winner")
	public int winner;
	@JsonProperty("players")
	ArrayList<Player> players;
	@JsonProperty("warclan")
	WarClan warclan;

    public boolean verifyData()
    {
    
        boolean b = true;
        if(players.size() == 2)
        {
            for(Player p : players)
            {
                if(!p.verifyData())
                {
                    b = false;
                }
            }
        }
        else
        {
            b = false;
        }
        if(
            date == null || date.isEmpty() ||
            game.isEmpty() || game == null ||
            mode.isEmpty() || mode == null ||
            type.isEmpty() || type == null
        ) b = false;
        

        return b;

       

      
    }

}


class GameKey implements WritableComparable<GameKey>, Cloneable {

    public String jsonLine;
    public long date;
    public String game;
    public String mode;
    public int round;
    public String type;
    public String player1;
    public String player2;

    public GameKey() {
        this.date = 0;
        this.game = "";
        this.mode = "";
        this.round = 0;
        this.type = "";
        this.player1 = "";
        this.player2 = "";
    }

    public GameKey(Battle b) {
        this.date = Instant.parse(b.date).getEpochSecond();
        this.game = b.game;
        this.mode = b.mode;
        this.type = b.type;
        this.player1 = b.players.get(0).utag;
        this.player2 = b.players.get(1).utag;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(date);
        out.writeUTF(game);
        out.writeUTF(mode);
        out.writeInt(round);
        out.writeUTF(type);
        out.writeUTF(player1);
        out.writeUTF(player2);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.date = in.readLong();
        this.game = in.readUTF();
        this.mode = in.readUTF();
        this.round = in.readInt();
        this.type = in.readUTF();
        this.player1 = in.readUTF();
        this.player2 = in.readUTF();
    }

    @Override
    public String toString() {
        // Trier les joueurs dans l'ordre lexicographique pour garantir la cohérence
        String thisPlayer1 = this.player1.compareTo(this.player2) <= 0 ? this.player1 : this.player2;
        String thisPlayer2 = this.player1.compareTo(this.player2) > 0 ? this.player1 : this.player2;
        // Créer une clé composite sans la date
        return this.game + "|" + this.mode + "|" + this.round + "|" + this.type + "|" + thisPlayer1 + "|" + thisPlayer2;
    }


    @Override
    public int compareTo(GameKey o) {

        // Comparer d'abord les clés composites
        int keyComparison = this.toString().compareTo(o.toString());
        if (keyComparison != 0) {
            return keyComparison; // Si les clés sont différentes, retournez le résultat
        }
    
        // Si les clés composites sont identiques, comparer les dates avec une tolérance de 10 secondes
        if (Math.abs(this.date - o.date) <= 10) {
            return 0; // Regrouper les parties avec des dates proches si toutes les autres infos sont identiques
        }
    
        // Sinon, comparer les dates normalement
        return Long.compare(this.date, o.date);
    }
}