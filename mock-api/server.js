const express = require('express');
const { ERROR_CODES } = require('./src/constants');
const { sendError } = require('./src/http');
const incidentRoutes = require('./src/incidentRoutes');

const app = express();

app.use(express.json());

app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    message: 'Field Incident Reporter API is running.',
  });
});

app.use('/incidents', incidentRoutes);

app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return sendError(res, 400, ERROR_CODES.INVALID_REQUEST, 'Invalid JSON request body.');
  }

  console.error(err);
  return sendError(
    res,
    500,
    ERROR_CODES.INTERNAL_SERVER_ERROR,
    'An unexpected error occurred.',
  );
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Field Incident Reporter API listening on port ${PORT}`);
});
