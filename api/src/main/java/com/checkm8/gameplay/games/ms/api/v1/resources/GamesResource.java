package com.checkm8.gameplay.games.ms.api.v1.resources;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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
