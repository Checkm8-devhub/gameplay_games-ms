package com.checkm8.gameplay.games.ms.api.v1.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.security.RolesAllowed;
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

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

@ApplicationScoped
@Path("games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GamesResource {

    @Context
    private UriInfo uriInfo;

    @Inject
    private GamesBean gamesBean;

    // for timer
    @Inject
    Vertx vertx;

    // ****************************************
    //  GET
    // ****************************************
    @GET
    @RolesAllowed("admin")
    public Response getAll() {

        List<Game> games = gamesBean.getAll();
        return Response.ok(games).build();
    }

    @GET
    @Path("{id}")
    // @RolesAllowed({"user", "admin"})
    public Response get(@PathParam("id") Integer id) {

        Game game = gamesBean.get(id);
        if (game == null) return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(game).build();
    }

    // some explanation
    // - Uni is a lazy async container for one result. It runs when subscribed.
    //  - .createFrom().item(...) completes with item immediatelly
    //  - .createFrom().completionStage(...) completes when the given CompletionStage (for example CompletableFuture), completes.

    private static final Long EVENT_TIMEOUT_MS = 30000L;
    @GET
    @Path("{id}/events")
    @RolesAllowed({"user", "admin"})
    @Blocking
    public Uni<Response> getEvent(@PathParam("id") Integer id) {

        // since == current uciList size of requestee
        Integer since;
        try {
            since = Integer.parseInt(uriInfo.getQueryParameters().get("since").get(0));
        } catch (Exception e) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing/invalid 'since' query parameter").build());
        }

        Game game = gamesBean.get(id);
        if (game == null) return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());

        // register as a waiter
        CompletableFuture<List<String>> f = gamesBean.registerWaiter(id);

        // if already behind, respond immediatelly. No need to long pool
        List<String> list = game.getUciAsList();
        if (since < 0) since = 0;
        if (since > list.size()) since = list.size();
        if (list.size() > since) {
            gamesBean.removeWaiter(id, f);
            return Uni.createFrom().item(Response.ok(list.subList(since, list.size())).build());
        }

        // long pool: wait up to 30s for a new event
        // ---
        // cleanUp after timeout:
        // - if the future not completed yet => complete the future with empty list and remove the future
        long timerId = vertx.setTimer(EVENT_TIMEOUT_MS, tId -> {
            if (f.complete(List.of())) {
                gamesBean.removeWaiter(id, f);
            }
        });
        // set callback on completion. Just cancel the timer because we don't need the cleanup
        f.whenComplete((newUci, err) -> {
            gamesBean.removeWaiter(id, f);
            vertx.cancelTimer(timerId);
        });

        // on completion return newUci or empty
        return Uni.createFrom().completionStage(f)
            .onItem().transform(newUci -> {
                if (newUci == null || newUci.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                return Response.ok(newUci).build();
            });
        // ---
    }
    
    // ****************************************
    //  POST
    // ****************************************
    @POST
    @RolesAllowed("matchmaking")
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
    @RolesAllowed({"user", "admin"})
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
    @RolesAllowed("admin")
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
    @RolesAllowed("admin")
    public Response delete(@PathParam("id") Integer id) {

        boolean deleted = gamesBean.delete(id);

        if (deleted) return Response.noContent().build();
        else         return Response.status(Response.Status.NOT_FOUND).build();
    }
}
