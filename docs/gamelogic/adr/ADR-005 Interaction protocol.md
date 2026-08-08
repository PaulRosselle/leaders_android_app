# ADR-005: Interaction protocol

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

Some gameplay mechanics require information that cannot be determined by the game engine alone, such as player decisions or external confirmations.

The game engine must remain independent from any presentation technology while supporting different front-ends, including graphical interfaces, AI players and automated tests.

The communication mechanism must therefore describe *what* information is required without prescribing *how* it is obtained.

## Decision

The game engine communicates with external applications through a generic interaction protocol.

When additional information is required, the engine produces an `InteractionRequest` describing the expected interaction. Once the application has collected the requested information, it returns an `InteractionResult` to the engine.
After processing an `InteractionResult`, the resolver may produce an `InteractionFeedback` describing a gameplay event that should be presented by the external application. The application acknowledges the feedback before the resolution workflow continues.

Resolvers declare which interactions and feedbacks are required but never communicate directly with the application. The orchestration layer is responsible for forwarding interaction requests, collecting the corresponding results, forwarding feedbacks and waiting for their acknowledgements before resuming the resolution workflow.

The interaction protocol is independent from any user interface implementation and describes gameplay interactions and gameplay presentation semantics exclusively through domain concepts.

## Consequences

- The game engine remains independent from any presentation technology.
- Human players, AI players and automated tests can all interact with the engine through the same protocol.
- Gameplay logic remains independent from interaction management.
- Gameplay feedback can be emitted without depending on a rendering framework.
- The orchestration layer becomes responsible for coordinating interaction exchanges and feedback acknowledgements.
- New interaction mechanisms can be introduced without modifying gameplay resolution.