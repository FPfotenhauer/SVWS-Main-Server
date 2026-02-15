# Architecture Documentation

This folder contains all architectural documentation for the SVWS-Main-Server project.

## Architecture Decision Records (ADRs)

ADRs document major architectural decisions and their rationale.

| Document | Title | Status |
|----------|-------|--------|
| [ADR-001](./ADR-001.md) | Multi-Tenant Architecture | Accepted |
| [ADR-002](./ADR-002.md) | Technology Stack Selection | Accepted |
| [ADR-003](./ADR-003.md) | API Design | Accepted |
| [ADR-004](./ADR-004.md) | Port & Adapter Pattern | Accepted |
| [ADR-005](./ADR-005.md) | Authentication Strategy | Accepted |
| [ADR-006](./ADR-006.md) | Password Security | Accepted |
| [ADR-007](./ADR-007.md) | Database Migration | Accepted |

## System Documentation

- [System Overview (Arc42 Format)](./Arch42-Doc.md) - Comprehensive system description following the Arc42 template

## Reading Guide

**New to the project?**
1. Start with [Arch42-Doc.md](./Arch42-Doc.md) for a system overview
2. Read [ADR-002](./ADR-002.md) to understand technology choices

**Need specific information?**
- Authentication: [ADR-005](./ADR-005.md), [ADR-006](./ADR-006.md)
- API Design: [ADR-003](./ADR-003.md)
- Architecture Pattern: [ADR-004](./ADR-004.md)
- Multi-Tenancy: [ADR-001](./ADR-001.md)
- Database: [ADR-007](./ADR-007.md)

## ADR Format

Each ADR follows this structure:
- **Context**: Background and problem statement
- **Decision**: What was decided and why
- **Consequences**: Implications of the decision (positive and negative)
- **Status**: Accepted, Rejected, Superseded, etc.

## Contributing

When making significant architectural decisions, document them as a new ADR:
1. Create `ADR-XXX.md` (increment the number)
2. Follow the format above
3. Submit as part of code review
4. Update this README with a link
