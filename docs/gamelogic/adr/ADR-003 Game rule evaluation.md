# ADR-003: Game rule evaluation

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

Gameplay mechanics require evaluating a wide variety of rules, including movement, character abilities, recruitment, victory conditions and board state validation.

These rules must be reusable throughout the engine without introducing side effects or coupling gameplay evaluation with state mutation.

## Decision

Gameplay rules are evaluated through dedicated query classes.

Queries are responsible for reading the current game projection and deriving gameplay information from it. They never modify the game state or produce gameplay actions.

Each query focuses on a specific aspect of the game and exposes a reusable API that can be shared by different engine components.

Resolvers, handlers and other gameplay services rely on queries to evaluate the current game state before making decisions or applying actions.

## Consequences

- Gameplay rule evaluation remains independent from state mutation.
- Queries are reusable across multiple gameplay features.
- Rule implementations remain centralized, reducing duplication throughout the engine.
- Query execution is deterministic and side-effect free.
- New gameplay rules can be introduced without affecting the action execution pipeline.