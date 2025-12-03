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

    @Transactional
    public List<Game> getAllGames() {
        return em.createNamedQuery("Game.getAll", Game.class).getResultList();
    }
}
