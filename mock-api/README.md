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

## Response envelopes

Success:

```json
{ "data": {} }
```

Error:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message."
  }
}
```

Delete success is an exception and is not wrapped in `data`:

```json
{
  "status": "SUCCESS",
  "message": "Incident successfully deleted."
}
```

## Endpoints

- `GET /health` — `{ "status": "OK", "message": "Field Incident Reporter API is running." }`
- `GET /incidents` — list incidents, newest first. Optional `?status=` accepts a comma-separated list such as `OPEN,IN_PROGRESS`.
- `GET /incidents/:id` — incident details by UUID.
- `POST /incidents` — create an incident. Client supplies the UUID. The API assigns `INC-0001`, `INC-0002`, and so on.
- `PATCH /incidents/:id` — replace the editable fields (`title`, `description`, `category`, `otherCategoryDescription`, `location`, `photos`). Photos is the complete desired list.
- `DELETE /incidents/:id` — permanently delete an incident. Deleted incident numbers are never reused.

Incident numbering is stored in `db.json` as `meta.lastIncidentNumber` and survives server restarts.
