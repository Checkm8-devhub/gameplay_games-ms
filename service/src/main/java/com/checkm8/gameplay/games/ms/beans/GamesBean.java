package com.checkm8.gameplay.games.ms.beans;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import com.checkm8.gameplay.games.ms.entities.Game;
import com.checkm8.gameplay.games.ms.entities.Game.GameStatus;
import com.checkm8.gameplay.games.ms.exceptions.GameNotActiveException;
import com.checkm8.gameplay.games.ms.exceptions.GameNotFoundException;
import com.checkm8.gameplay.games.ms.exceptions.IllegalMoveException;
import com.checkm8.gameplay.games.ms.exceptions.InvalidGameTokenException;
import com.checkm8.gameplay.games.ms.exceptions.InvalidUCIException;
import com.checkm8.gameplay.games.ms.exceptions.NotYourTurnException;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class GamesBean {

    @PersistenceContext
    private EntityManager em;
    private Logger log = Logger.getLogger(GamesBean.class.getName());

    @PostConstruct
    private void init() {
        log.info("Bean initialized " + GamesBean.class.getSimpleName());
    }
    @PreDestroy
    private void destroy() {
        log.info("Bean destroyed " + GamesBean.class.getSimpleName());
    }

    public List<Game> getAll() {
        return em.createNamedQuery("Game.getAll", Game.class).getResultList();
    }

    public Game get(Integer id) {
        return em.find(Game.class, id);
    }

    @Transactional
    public Game create() {

        Game game = Game.createStartingGame("w", "b");
        em.persist(game);
        return game;
    }

    // allow updating for status and fen. Changing id / tokens is not possible
    @Transactional
    public Game update(Integer id, Game game) {

        if (game == null || id == null) throw new IllegalArgumentException();

        Game oldGame = this.get(id);
        if (oldGame == null) return null;

        if (game.getStatus() != null) oldGame.setStatus(game.getStatus());
        if (game.getFen() != null)    oldGame.setFen(game.getFen());
        return oldGame;
    }

    @Transactional
    public boolean delete(Integer id) {

        Game game = this.get(id);
        if (game != null) {
            em.remove(game);
            return true;
        }
        return false;
    }

    // returns true if white
    public boolean validateGameToken(Game game, String gameToken) {
        boolean isWhite = game.getWhiteToken().equals(gameToken);
        boolean isBlack = game.getBlackToken().equals(gameToken);
        if (!isWhite && !isBlack)
            throw new InvalidGameTokenException("Invalid game token");

        return isWhite ? true : false;
    }

    @Transactional
    public void handleResignation(Integer id, String gameToken) {

        Game game = this.get(id);
        if (game == null)
            throw new GameNotFoundException("Game not found");
        if (game.getStatus() != GameStatus.PLAYING)
            throw new GameNotActiveException("Game not active");

        boolean isWhite = validateGameToken(game, gameToken);

        if (isWhite) game.setWinner("b");
        else         game.setWinner("w");

        game.setStatus(GameStatus.FINISHED);
    }

    @Transactional
    public void handleMove(Integer id, String gameToken, String modeUCI) {

        Game game = this.get(id);
        if (game == null)
            throw new GameNotFoundException("Game not found");
        if (game.getStatus() != GameStatus.PLAYING)
            throw new GameNotActiveException("Game not active");
        
        // validate gameToken
        boolean isWhite = validateGameToken(game, gameToken);
        boolean isBlack = !isWhite;

        // validate side
        Board board = new Board();
        board.loadFromFen(game.getFen());
        Side side = board.getSideToMove();
        if (!isWhite && side == Side.WHITE) throw new NotYourTurnException("It is white to move");
        if (!isBlack && side == Side.BLACK) throw new NotYourTurnException("It is black to move");

        // parse UCI
        if (modeUCI == null || modeUCI.length() != 4 && modeUCI.length() != 5)
            throw new InvalidUCIException("Invalid UCI");
        String from = modeUCI.substring(0, 2).toUpperCase();
        String to = modeUCI.substring(2, 4).toUpperCase();
        String promotion = (modeUCI.length() == 5) ? modeUCI.substring(4, 5) : null;

        // create move
        Move move;
        if (promotion != null) {
            Piece promotionPiece = Piece.fromFenSymbol(promotion);
            move = new Move(Square.fromValue(from), Square.fromValue(to), promotionPiece);
        }
        else move = new Move(Square.fromValue(from), Square.fromValue(to));

        // validate move
        if (!board.legalMoves().contains(move))
            throw new IllegalMoveException("Illegal move");

        // play move
        board.doMove(move);
        
        // update FEN
        game.setFen(board.getFen());
    }
}
