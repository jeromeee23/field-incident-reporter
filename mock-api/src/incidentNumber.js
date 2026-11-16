const { db } = require('./db');

function formatIncidentNumber(number) {
  return `INC-${String(number).padStart(4, '0')}`;
}

function peekNextIncidentNumber() {
  const last = db.get('meta.lastIncidentNumber').value() || 0;
  return last + 1;
}

function persistCreatedIncident(incident, nextNumber) {
  db.get('incidents').push(incident).value();
  db.set('meta.lastIncidentNumber', nextNumber).write();
}

module.exports = {
  formatIncidentNumber,
  peekNextIncidentNumber,
  persistCreatedIncident,
};
