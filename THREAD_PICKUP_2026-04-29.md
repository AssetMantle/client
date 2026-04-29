# Thread pickup — 2026-04-29

> **Not a doc for users — engineering pickup notes.** If you're picking up the
> explorer in a future session, read this first. Lives at repo root, gitignored
> via `.gitignore` if you want it out of `git status`.

## What shipped today (all squash-merged on `explorer/master`)

| PR    | Squash      | Theme              | One-liner                                                       |
|-------|-------------|--------------------|-----------------------------------------------------------------|
| #98   | `531221c4`  | indexer            | Skip `onGenesis()` when `blockchain.startHeight > 1`            |
| #99   | `d35b75f1`  | data source        | Token price source CoinGecko → CoinMarketCap public widget API  |
| #100  | `370d2748`  | indexer + charts   | Seed `Token` table on non-genesis boot, humanize USD axis       |
| #101  | `3c7c99b4`  | build              | `sbt-digest` enabled — fingerprinted assets, immutable caching  |
| #102  | `237dfc62`  | UI                 | Logo, fonts, palette, default-light, Game-of-Life background    |
| #103  | `4a3d295f`  | UI                 | Light-mode design lift — hierarchy, contrast, donut palette     |

## Live state (production)

- **URL**: https://explorer.assetmantle.one (200, real LE cert)
- **Image**: `ghcr.io/assetmantle/client:lightmode-lift-v4` (`sha256:6d9e0e36`)
- **Akash**: DSEQ `26587209`, provider `akash1x2g8wfa429fukudgkclaag00d00z4rn846j7wq`
- **Indexer**: cold-syncing from height 22M; ~28 blocks/min steady-state

## Pivotal architecture decisions

1. **`onGenesis()` is skipped at startHeight > 1** because parallel
   `GetAccount`/`GetBalance` fan-out trips upstream nginx 429s, throws
   `JsonParseException` on the HTML body, and pegs `BLOCK_SCHEDULER` in a
   tight retry loop. Skipping it costs nothing for non-genesis starts.
2. **Token table is seeded once at boot via `startFixes`** (PR #100) using
   live REST endpoints. This was the side-effect we missed in PR #98 — without
   it, `validatorDetails` 500s with `CRYPTO_TOKEN_NOT_FOUND` on every page load.
3. **Asset versioning is pure Play idiom**: `sbt-digest` + `pipelineStages`,
   no helpers, no env vars. The 24 templates already used `routes.Assets.versioned`.
4. **Light-mode design lift uses var-remap**, not selector-fork. Aliasing
   `--dark-1`, `--card-color`, etc. inside `body.lightMode { }` makes every
   existing rule in `class.css` resolve correctly without touching that file.
   `lightMode.css` is 367 lines; `class.css` (1400 lines) untouched.
5. **`theme.js` resolves CSS vars via `Proxy + getComputedStyle`** at draw
   time, so chart palettes follow whichever theme is active when the chart
   is created.

## Open follow-ups (none blocking)

- **Token Price y-axis auto-ranges to -1..+1.5** instead of around the data
  values. Chart.js' `beginAtZero` default with sub-cent values. Trivial fix:
  add `suggestedMin`/`suggestedMax` from the data array in `lineChart.js`.
- **Validator donut "Self" wedge is always 0** — the indexer's MsgDelegate
  parser doesn't seed pre-22M self-delegations. Cosmetic; only the (Self vs
  Others) breakdown is misleading.
- **Archive RPC**: still depending on `rpc.assetmantle.one` (Polkachu /
  PublicNode), which prune at ~21M. Historical blocks <22M return NOT_FOUND.
  To backfill we'd need our own archive node — out of scope.

## Branch protection

`explorer/master` requires 1 approving review + linear history.
`enforce_admins: false`, so `gh pr merge <N> --squash --admin` works for the
deepanshutr account. Bypass list also includes `avkr003`.

## Resume incantation

See `~/assetmantle-infra/akash/client/THREAD_PICKUP_2026-04-29.md` for the
full deploy recipe. Highlights:

```bash
# rebuild + push
cd ~/github.com/AssetMantle/client && git pull
DOCKER_BUILDKIT=1 docker build --secret id=git,src=/tmp/build-key \
  -t ghcr.io/assetmantle/client:<new-tag> .
docker push ghcr.io/assetmantle/client:<new-tag>
docker tag ghcr.io/assetmantle/client:<new-tag> ghcr.io/assetmantle/client:latest
docker push ghcr.io/assetmantle/client:latest

# infra deploy (in ~/assetmantle-infra)
cd ~/assetmantle-infra/akash/client
$EDITOR deploy.yaml
PLAY_SECRET=$(cat .play-secret)
sed "s|__PLAY_SECRET__|$PLAY_SECRET|" deploy.yaml > deploy.rendered.yaml
DSEQ=26587209
PROVIDER=akash1x2g8wfa429fukudgkclaag00d00z4rn846j7wq
provider-services tx deployment update deploy.rendered.yaml --dseq $DSEQ \
  --from deployer --keyring-backend test --node https://akash-rpc.polkachu.com:443 \
  --chain-id akashnet-2 --gas-prices 0.025uakt --gas auto --gas-adjustment 1.5 -y
provider-services send-manifest deploy.rendered.yaml --dseq $DSEQ --provider $PROVIDER \
  --from deployer --keyring-backend test --node https://akash-rpc.polkachu.com:443 \
  --home ~/.akash
```

If `tx update` errors "Invalid: deployment hash", resource shape unchanged;
just send-manifest. If send-manifest 422s "manifest version validation
failed", run tx update first.

## Files to know

- `app/services/Startup.scala` — block scheduler, genesis skip, token seed
- `app/queries/coinmarketcap/GetTicker.scala` — CMC integration
- `app/utilities/JSON.scala` — diagnostic logging on non-JSON upstream responses
- `public/stylesheets/css/lightMode.css` — entire light-mode design system
- `public/javascripts/gameOfLife.js` — Conway's life canvas (background + button states)
- `public/javascripts/theme.js` — chart palette resolved lazily via getComputedStyle
- `public/javascripts/chart/lineChart.js` — humanized USD axis formatting
- `project/plugins.sbt` + `build.sbt` — `sbt-digest` wiring

## Verified live 2026-04-29 ~13:40 IST

- Homepage renders cream-on-cream with BPdotsSquare uppercase section titles
- Recent Activity shows ink-on-cream titles + muted subtitles
- Donut charts use orange + warm grey (no black-on-cream)
- Token Price card shows MNTL ≈ $2.49e-5 (matches CMC API exactly)
- Game-of-Life background runs at opacity 0.045 with `mix-blend: multiply`
- All static assets fingerprinted with `Cache-Control: max-age=31536000, immutable`
