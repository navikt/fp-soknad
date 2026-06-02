# fp-soknad

Citizen-facing backend for application and vedlegg submission for foreldrepenger, svangerskapspenger and engangsstonad.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic              | Details                                                                                               |
|--------------------|-------------------------------------------------------------------------------------------------------|
| Role               | Accepts soknader and vedlegg, validates, journals, and forwards them into the processing chain        |
| Consumers          | Repo `foreldrepengesoknad`, apps `foreldrepengesoknad`, `engangsstonad` and `svangerskapspengesoknad` |
| Tech stack | Standard fp Java backend  using `fp-prosesstask`                                                      |
| Main integrations  | `fp-sak`, `fptilbake`, Joark/DokArkiv, PDL,                                            |
| Data               | GCS-backed encrypted draft storage; PostgreSQL temporary storage while processing until journaled.    |

Specials: 
- Citizen-facing using TokenX. Authorization pr endpoint using TilgangskontrollTjeneste. 
- Handles attachment scanning (clamav) and image-to-PDF conversion;
- Sak routing: Tries to assign submitted docs to sak (finalize journaling, send to `fp-sak`). Unsuccessful: temp. journaling is picked up by `fp-mottak` through Kafka.

## Entry points

- MellomlagringRest: enpoints to store applications-draft and attachements
- SøknadRest: endpoint for each of FP/ES/SVP, FP-changed, and additional documents.
- SøknadRest/staus: processing status used for frontend polling and routing.

## Verification

- For integration impact, verify via `navikt/fp-autotest`.
- Most relevant suite: `verdikjede`.
