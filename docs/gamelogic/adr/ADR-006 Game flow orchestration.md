# ADR-006: Game flow orchestration

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

Executing a game involves coordinating multiple independent responsibilities, including gameplay resolution, interaction exchanges, game state mutation and automatic game progression.

No individual component should be responsible for both implementing gameplay logic and coordinating the overall execution flow.

The engine therefore requires a dedicated orchestration layer responsible for sequencing these responsibilities while keeping them loosely coupled.

## Decision

Game flow is coordinated by a dedicated orchestration layer.

The orchestration layer drives the execution of the game by coordinating resolvers, interaction exchanges, feedback acknowledgements and game action execution without implementing gameplay rules itself.

It is responsible for determining when gameplay resolution should continue, when external interactions are required, when feedback must be acknowledged by the external application and when produced actions should be applied to the game state.

The orchestration layer communicates with external applications through `IGameFlowListener`, allowing the engine to forward interaction requests and feedbacks and to receive interaction results and feedback acknowledgements without depending on any specific presentation technology.

## Consequences

- Gameplay sequencing is centralized in a single component.
- Gameplay rules remain independent from execution flow.
- Interaction management remains independent from gameplay decision logic.
- Game state mutations remain independent from orchestration.
- The orchestration layer can evolve without affecting gameplay rules or state management.
- Feedback acknowledgement is handled by the orchestration layer and does not become part of gameplay resolution logic.