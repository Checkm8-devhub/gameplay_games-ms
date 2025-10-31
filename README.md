# GAMEPLAY / GAMES Microservice

Handles request (Create, Join, ...) at ```/games```

## Active game logic (long-polling)
1. Client: ```GET /games/{id}/events?since={last_seen_event}```
2. Server: Wait up to 30s for a new event
    - If a new event arrives -> respond with updated events.
    - If no event after 30s  -> respond empty
3. Client: Get response and repeat 1.
4. The client can make an event (move, resign, ...) on ```/games/{game_id}/commands```
