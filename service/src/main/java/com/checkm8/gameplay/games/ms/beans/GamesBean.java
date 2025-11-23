package com.checkm8.gameplay.games.ms.beans;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.checkm8.gameplay.games.ms.entities.Game;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class GamesBean {

    @PersistenceContext(unitName = "games-jta")
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
