# fp-soknad

## Withdrawal plan: expand rollout

The contract accepts optional `uttaksplan.perioder` (`List<UttakPeriodeDto>`)
alongside the existing `uttaksplan.uttaksperioder`. There is no separate
`fellesUttaksplan` field.

When `perioder` is absent or null, the existing flow is unchanged. When supplied,
it is authoritative: the applicant's periods are mapped to the existing FPSAK
format. An empty list does not fall back to the old list. Change applications
still require a non-empty legacy list to establish the change date.

The complete new list is preserved in application JSON, but this phase does not
store the other parent's proposed plan in fpoversikt or make it available to that
parent. Applications proceed directly to FPSAK preparation after journaling.

Rollout order:

1. Merge the contract changes and publish a contract release using the existing
   `Publish kontrakt` workflow.
2. Update `soknad-kontrakt.version` in the root POM to that exact published version
   in a separate app change, then deploy through the normal pipeline. Until this
   step, the app still uses its previous contract.
3. Roll out the fpoversikt integration separately. The complete implementation is
   retained on `felles-uttaksplan-mottak`; merge master into it after the expand
   release, retaining the published contract version rather than the old snapshot.

For local development before publishing the contract:

```sh
mvn -f kontrakter/pom.xml install
mvn -Dsoknad-kontrakt.version=2.3.5-SNAPSHOT test
```
