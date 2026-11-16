const express = require('express');
const { db } = require('./db');
const {
  DEFAULT_STATUS,
  ERROR_CODES,
  TERMINAL_STATUSES,
} = require('./constants');
const { sendDeleteSuccess, sendError, sendSuccess } = require('./http');
const {
  formatIncidentNumber,
  peekNextIncidentNumber,
  persistCreatedIncident,
} = require('./incidentNumber');
const {
  parseStatusFilter,
  validateCreateBody,
  validateUpdateBody,
} = require('./incidentValidation');

const router = express.Router();

function findIncidentById(id) {
  return db.get('incidents').find({ id }).value();
}

function listIncidents() {
  return db.get('incidents').value() || [];
}

router.get('/', (req, res) => {
  const parsed = parseStatusFilter(req.query.status);
  if (parsed.error) {
    return sendError(
      res,
      parsed.error.status,
      parsed.error.code,
      parsed.error.message,
    );
  }

  let incidents = listIncidents().slice();
  if (parsed.statuses) {
    incidents = incidents.filter((incident) => parsed.statuses.includes(incident.status));
  }

  incidents.sort((left, right) => {
    if (left.createdAt < right.createdAt) {
      return 1;
    }
    if (left.createdAt > right.createdAt) {
      return -1;
    }
    return 0;
  });

  return sendSuccess(res, incidents);
});

router.get('/:id', (req, res) => {
  const incident = findIncidentById(req.params.id);
  if (!incident) {
    return sendError(
      res,
      404,
      ERROR_CODES.INCIDENT_NOT_FOUND,
      'Incident not found.',
    );
  }

  return sendSuccess(res, incident);
});

router.post('/', (req, res) => {
  const result = validateCreateBody(req.body);
  if (result.error) {
    return sendError(
      res,
      result.error.status,
      result.error.code,
      result.error.message,
    );
  }

  if (findIncidentById(result.value.id)) {
    return sendError(
      res,
      409,
      ERROR_CODES.INCIDENT_CONFLICT,
      'An incident with this ID already exists.',
    );
  }

  const now = new Date().toISOString();
  const nextNumber = peekNextIncidentNumber();
  const incident = {
    id: result.value.id,
    incidentNumber: formatIncidentNumber(nextNumber),
    title: result.value.title,
    description: result.value.description,
    category: result.value.category,
    otherCategoryDescription: result.value.otherCategoryDescription,
    location: result.value.location,
    photos: result.value.photos,
    status: DEFAULT_STATUS,
    createdAt: now,
    updatedAt: now,
    completedAt: null,
    cancelledAt: null,
  };

  persistCreatedIncident(incident, nextNumber);
  return sendSuccess(res, incident, 201);
});

router.patch('/:id', (req, res) => {
  const incident = findIncidentById(req.params.id);
  if (!incident) {
    return sendError(
      res,
      404,
      ERROR_CODES.INCIDENT_NOT_FOUND,
      'Incident not found.',
    );
  }

  if (TERMINAL_STATUSES.includes(incident.status)) {
    return sendError(
      res,
      409,
      ERROR_CODES.INCIDENT_CONFLICT,
      'Completed or cancelled incidents cannot be edited.',
    );
  }

  const result = validateUpdateBody(req.body);
  if (result.error) {
    return sendError(
      res,
      result.error.status,
      result.error.code,
      result.error.message,
    );
  }

  const updated = db
    .get('incidents')
    .find({ id: req.params.id })
    .assign({
      title: result.value.title,
      description: result.value.description,
      category: result.value.category,
      otherCategoryDescription: result.value.otherCategoryDescription,
      location: result.value.location,
      photos: result.value.photos,
      updatedAt: new Date().toISOString(),
    })
    .write();

  return sendSuccess(res, updated);
});

router.delete('/:id', (req, res) => {
  const incident = findIncidentById(req.params.id);
  if (!incident) {
    return sendError(
      res,
      404,
      ERROR_CODES.INCIDENT_NOT_FOUND,
      'Incident not found.',
    );
  }

  db.get('incidents').remove({ id: req.params.id }).write();
  return sendDeleteSuccess(res);
});

module.exports = router;
