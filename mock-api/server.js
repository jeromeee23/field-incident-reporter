const path = require('path');
const express = require('express');
const jsonServer = require('json-server');

const app = express();
const router = jsonServer.router(path.join(__dirname, 'db.json'));

app.use(express.json());

app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    message: 'Field Incident Reporter API is running.',
  });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Field Incident Reporter API listening on port ${PORT}`);
});
