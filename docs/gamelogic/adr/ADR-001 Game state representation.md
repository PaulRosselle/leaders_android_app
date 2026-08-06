# ADR-001: Game state representation

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

The game engine must support gameplay features that rely on navigating through the evolution of a game, including undo/redo, replay, AI simulation and deterministic execution.

Representing the current game state as the only source of truth would tightly couple gameplay execution with these features and make state restoration unnecessarily complex.

The architecture therefore separates the representation of the current game state from the history of actions that produced it.

## Decision

The game state is represented by two complementary concepts:

- **GameHistory** is the authoritative record of everything that happened during a game.
- **Game** is a mutable projection representing the current state of the game.

All state changes are represented by immutable implementations of `IGameAction`.

Actions are applied to the current projection exclusively through their associated `GameActionHandler`. Each handler is responsible for both applying and reverting its corresponding action.

Game history is organised as hierarchical segments (`GameHistory`, `Turn`, `TurnPhase`, ...), allowing navigation through meaningful gameplay boundaries while preserving a linear sequence of actions.

Queries never modify the projection. Their responsibility is limited to evaluating the current state in order to support gameplay decisions.

## Consequences

- The current game state remains lightweight and focused on gameplay execution.
- Game history becomes the single authoritative record of a game.
- Undo, replay and AI simulation all rely on the same execution mechanism.
- Every new game action requires a dedicated reversible handler.
- State mutations are explicit and centralized within handlers.
- Queries remain side-effect free and cannot alter the game state.
- Understanding the engine requires distinguishing between the game history and its current projection.