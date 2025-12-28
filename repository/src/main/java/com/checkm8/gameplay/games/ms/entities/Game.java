package com.checkm8.gameplay.games.ms.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
@NamedQueries(value =
        {
                @NamedQuery(name = "Game.getAll", query = "SELECT g FROM Game g"),
        })
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String whiteToken;

    @Column(nullable = false)
    private String blackToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;
    public static enum GameStatus {
        PLAYING,
        FINISHED,
    }

    @Enumerated(EnumType.STRING)
    private GameWinner winner;
    public static enum GameWinner {
        WHITE,
        BLACK,
        DRAW,
    }

    @Column(nullable = false)
    private String fen;
    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private String ucis;

    public static Game createStartingGame(String whiteToken, String blackToken) {
        Game game = new Game();
        game.whiteToken = whiteToken;
        game.blackToken = blackToken;
        game.status = GameStatus.PLAYING;
        game.fen = STARTING_FEN;
        game.winner = null;
        
        return game;
    }

    public List<String> getUciAsList() {
        if (this.ucis == null) return List.of();

        List<String> uciList = new ArrayList<>();
        for (String uci : this.ucis.split(" ")) {
            uciList.add(uci);
        }

        return uciList;
    }
    public void addUciToUcis(String uci) {
        if (this.ucis == null) this.ucis = uci;
        else                   this.ucis += " " + uci;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }

    public String getWhiteToken() { return this.whiteToken; }
    public void setWhiteToken(String whiteToken) { this.whiteToken = whiteToken; }

    public String getBlackToken() { return this.blackToken; }
    public void setBlackToken(String blackToken) { this.blackToken = blackToken; }

    public GameStatus getStatus() { return this.status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public String getFen() { return this.fen; }
    public void setFen(String fen) { this.fen = fen; }

    public GameWinner getWinner() { return winner; }
    public void setWinner(GameWinner winner) { this.winner = winner; }

    public String getUcis() { return ucis; }
    public void setUcis(String ucis) { this.ucis = ucis; }

    @Override
    public String toString() {
        return "Game [id=" + id + ", whiteToken=" + whiteToken + ", blackToken=" + blackToken + ", status=" + status
                + ", winner=" + winner + ", fen=" + fen + ", ucis=" + ucis + ", getUciAsList()=" + getUciAsList() + "]";
    }

}
