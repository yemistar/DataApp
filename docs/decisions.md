# Architecture Decisions

This project is intentionally small. The goal is to keep the app useful as an offline field log while leaving clear extension points for sync, reporting, and future backend work.

## Offline First

Farm data capture should not depend on a stable network connection. The app writes records locally first so feed, mortality, egg, treatment, and environment logs can be captured in the field.

## Room As Source Of Truth

Room stores the durable data model. UI state is derived from database flows instead of separate in-memory screen state, which keeps the dashboard, capture forms, and vet view aligned with the same local data.

## Single App State

`AppRepository` combines Room flows into one `AppState`. `AppViewModel` exposes that state to Compose as a `StateFlow`, keeping UI rendering predictable and making the data boundary easy to inspect.

## Pending Queue

Each new log also creates a `PendingItem`. The queue acts as a local outbox, so a future sync layer can drain pending records without changing the capture UI.

## Import And Export

Import/export uses JSON so a farm operator can back up or move data without a server. The same boundary also gives the app a simple handoff point for future integrations.
