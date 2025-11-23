package com.checkm8.gameplay.games.ms.api.v1.resources;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.checkm8.gameplay.games.ms.beans.GamesBean;
import com.checkm8.gameplay.games.ms.entities.Game;

@ApplicationScoped
@Path("games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GamesResource {

    @Context
    private UriInfo uriInfo;

    @Inject
    private GamesBean gamesBean;

    @GET
    public Response Test() {

        return Response
            .ok("Hello World")
            .build();
    }

    @GET
    @Path("/all")
    public Response getAllGames() {

        List<Game> games = gamesBean.getAllGames();
        return Response
            .ok(games)
            .build();
    }
}
