# ADR-004: Action resolution workflow

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

Many gameplay mechanics cannot be resolved by directly creating a game action.

A single player decision may require evaluating game rules, requesting additional information from the player, or generating multiple actions depending on the current game state.

The engine therefore requires a dedicated workflow capable of progressively resolving gameplay intentions while remaining independent from the user interface.

## Decision

Gameplay intentions are resolved through dedicated resolver classes.

Each resolver encapsulates the decision logic required to resolve a specific type of gameplay action. During the resolution process, a resolver may require additional player input before it can produce one or more game actions.

Rather than interacting directly with the user interface, resolvers expose the required interactions through `InteractionRequest` objects and consume the corresponding `InteractionResult` objects once they are provided by the game flow.

Resolvers rely on queries to evaluate the current game state and delegate game action construction to dedicated builders when appropriate.

Resolvers determine **what** information is required and **what** actions should be produced, but they neither communicate with the user interface nor modify the game state.

## Consequences

- Gameplay decision logic is isolated from user interface concerns.
- Interaction management remains independent from gameplay resolution.
- Complex gameplay mechanics can be implemented as progressive resolution workflows.
- Action construction remains reusable and independent from gameplay decision logic.
- State mutations remain the exclusive responsibility of game action handlers.
- The same resolution workflow can support different front-ends without modification.