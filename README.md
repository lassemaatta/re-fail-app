# re-fail-app

## Development

Compile and watch the development build:

```bash
npx shadow-cljs watch frontend
```

### Cider

Alternatively, you can begin with `cider-jack-in-cljs`.

## Build report

Generate release build report under `public/report.html`.

```bash
npx shadow-cljs run shadow.cljs.build-report frontend public/report.html
```
