# Trae Preflight

This folder is prepared for `wangxt-1073-1`.

Use `.env` for stable local ports and compose project identity:

- APP_PORT: 18373
- API_PORT: 19373
- WEB_PORT: 20373
- DB_PORT: 21373
- REDIS_PORT: 22373

Smoke entry:

```bash
bash scripts/smoke.sh
```

The preflight files are environment scaffolding only. The generated business
project can replace or extend them when needed.
