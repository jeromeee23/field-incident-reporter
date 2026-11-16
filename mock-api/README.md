# Mock API

Local Express REST API for Field Incident Reporter, with JSON Server as the JSON persistence foundation.

## Requirements

- Node.js 18 or later
- npm

## Setup

```bash
npm install
```

## Start

```bash
npm start
```

For development with auto-reload:

```bash
npm run dev
```

The server listens on `http://localhost:3000`. Override the port with `PORT`.

## Endpoints

- `GET /health` — `{ "status": "OK", "message": "Field Incident Reporter API is running." }`
