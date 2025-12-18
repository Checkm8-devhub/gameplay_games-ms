package com.checkm8.gameplay.games.ms.entities;

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

    @Column(nullable = false)
    private String fen;
    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Enumerated(EnumType.STRING)
    private GameWinner winner;
    public static enum GameWinner {
        WHITE,
        BLACK,
        DRAW,
    }

    public static Game createStartingGame(String whiteToken, String blackToken) {
        Game game = new Game();
        game.whiteToken = whiteToken;
        game.blackToken = blackToken;
        game.status = GameStatus.PLAYING;
        game.fen = STARTING_FEN;
        game.winner = null;
        
        return game;
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

    @Override
    public String toString() {
        return "Game [id=" + id + ", whiteToken=" + whiteToken + ", blackToken=" + blackToken + ", status=" + status
                + ", fen=" + fen + ", winner=" + winner + "]";
    }
}
