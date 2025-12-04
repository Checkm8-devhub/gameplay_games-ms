package com.checkm8.gameplay.games.ms.api.v1.resources;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.checkm8.gameplay.games.ms.api.v1.dtos.ActionRequest;
import com.checkm8.gameplay.games.ms.beans.GamesBean;
import com.checkm8.gameplay.games.ms.entities.Game;
import com.checkm8.gameplay.games.ms.exceptions.GameNotFoundException;
import com.checkm8.gameplay.games.ms.exceptions.GameNotActiveException;
import com.checkm8.gameplay.games.ms.exceptions.IllegalMoveException;
import com.checkm8.gameplay.games.ms.exceptions.InvalidGameTokenException;
import com.checkm8.gameplay.games.ms.exceptions.InvalidUCIException;
import com.checkm8.gameplay.games.ms.exceptions.NotYourTurnException;

@ApplicationScoped
@Path("games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GamesResource {

    @Context
    private UriInfo uriInfo;

    @Inject
    private GamesBean gamesBean;

    // ****************************************
    //  GET
    // ****************************************
    @GET
    public Response getAll() {

        List<Game> games = gamesBean.getAll();
        return Response.ok(games).build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Integer id) {

        Game game = gamesBean.get(id);
        if (game == null) return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(game).build();
    }
    
    // ****************************************
    //  POST
    // ****************************************
    @POST
    public Response create() {

        Game game = gamesBean.create();

        return Response
            .status(Response.Status.CREATED)
            .entity(new Object() {
                public Integer id = game.getId();
                public String whiteToken = game.getWhiteToken();
                public String blackToken = game.getBlackToken();
            }).build();
    }

    // Expects game_token and actions in body
    @POST
    @Path("{id}/actions")
    public Response handleAction(@PathParam("id") Integer id, ActionRequest req) {

        if (req == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body required").build();
        if (req.gameToken == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid game token").build();

        try {
            
            if (req.resign) {
                gamesBean.handleResignation(id, req.gameToken);
                return Response.ok("resigned").build();
            }

            if (req.moveUCI != null) {
                gamesBean.handleMove(id, req.gameToken, req.moveUCI);
                return Response.ok("moved").build();
            }

            return Response.status(Response.Status.BAD_REQUEST).entity("Bad request body").build();

        } catch (GameNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GameNotActiveException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (InvalidGameTokenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (NotYourTurnException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (InvalidUCIException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalMoveException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ****************************************
    //  PUT
    // ****************************************
    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Integer id, Game game) {

        Game updatedGame = gamesBean.update(id, game);
        if (updatedGame == null) return Response.status(Response.Status.NOT_FOUND).build();
        else                     return Response.ok(updatedGame).build();
    }

    // ****************************************
    //  DELETE
    // ****************************************
    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Integer id) {

        boolean deleted = gamesBean.delete(id);

        if (deleted) return Response.noContent().build();
        else         return Response.status(Response.Status.NOT_FOUND).build();
    }
}
