# ADR-002: Board representation

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

The game engine requires a consistent representation of the game board to support movement, distance calculation, neighbourhood queries and line traversal.

These operations should remain independent from the board content itself in order to maximise reuse and keep gameplay logic readable.

## Decision

The board geometry is represented using **axial coordinates**.

`Position` and `Direction` encapsulate all geometric operations, including coordinate manipulation, neighbour computation, distance calculation and line traversal.

`Board` is responsible for storing the current projection of the board and its cells.

`BoardQuery` is limited to querying the contents of the board. It builds upon the geometric primitives provided by `Position` and `Direction` without implementing geometric algorithms itself.

Construction logic is owned by the domain objects, making a dedicated `BoardFactory` unnecessary.

## Consequences

- A single coordinate system is used consistently throughout the engine.
- Geometric algorithms are centralized and reusable across the game logic.
- Board queries remain focused on gameplay semantics rather than geometric calculations.
- The board representation is independent from any rendering or user interface concerns.
- Future gameplay rules can rely on the same geometric primitives without duplicating logic.