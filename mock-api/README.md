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

Photo URLs are built from the request host, so Android Emulator calls to `http://10.0.2.2:3000` receive emulator-reachable image URLs. Override with `PUBLIC_BASE_URL` if needed (for example a physical device).

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

Delete success is an exception and is not wrapped in `data`.

Incident delete:

```json
{
  "status": "SUCCESS",
  "message": "Incident successfully deleted."
}
```

Photo delete:

```json
{
  "status": "SUCCESS",
  "message": "Photo successfully deleted.",
  "photoId": "photo-uuid"
}
```

## Endpoints

- `GET /health` — `{ "status": "OK", "message": "Field Incident Reporter API is running." }`
- `GET /incidents` — list incidents, newest first. Optional `?status=` accepts a comma-separated list such as `OPEN,IN_PROGRESS`.
- `GET /incidents/:id` — incident details by UUID.
- `POST /incidents` — create an incident. Client supplies the UUID. The API assigns `INC-0001`, `INC-0002`, and so on.
- `PATCH /incidents/:id` — replace the editable fields (`title`, `description`, `category`, `otherCategoryDescription`, `location`, `photos`). Photos is the complete desired list.
- `PATCH /incidents/:id/status` — transition status. Body accepts only `{ "status": "IN_PROGRESS" }`. Allowed transitions: OPEN → IN_PROGRESS or CANCELLED; IN_PROGRESS → COMPLETED or CANCELLED. COMPLETED and CANCELLED are terminal.
- `DELETE /incidents/:id` — permanently delete an incident and any associated local photo files. Deleted incident numbers are never reused.
- `POST /uploads/photos` — multipart upload. Fields: `incidentId` (text UUID; the incident does not need to exist yet) and `photos` (1–5 image files). Allowed formats: JPEG, PNG, WebP. Maximum 5 MB per file. All files are validated before any file is saved. Returns `201` with `{ "data": { "photos": [{ "id", "url" }] } }`.
- `GET /uploads/:filename` — serve a previously uploaded photo. Only files in `uploads/` with a `{uuid}.{jpg|jpeg|png|webp}` name are served.
- `DELETE /uploads/photos/:photoId` — delete one uploaded photo file by UUID. Missing photos return `404` with `PHOTO_NOT_FOUND`. This does not change incident records.

Incident numbering is stored in `db.json` as `meta.lastIncidentNumber` and survives server restarts.

Uploaded files are stored in `uploads/` and are not written into `db.json`. That folder is gitignored. Abandoned files (uploaded but never attached, or left behind after a failed create) are not cleaned up automatically; delete leftover files in `uploads/` if needed.

## Photo upload example

```bash
curl -sS -X POST http://localhost:3000/uploads/photos \
  -F "incidentId=11111111-1111-4111-8111-111111111111" \
  -F "photos=@./ok.jpg"
```
