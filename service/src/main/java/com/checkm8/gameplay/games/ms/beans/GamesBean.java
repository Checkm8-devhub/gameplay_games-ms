package com.checkm8.gameplay.games.ms.beans;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import com.checkm8.gameplay.games.ms.entities.Game;

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
}
