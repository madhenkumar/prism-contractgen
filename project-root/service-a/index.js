const express = require('express');

const app = express();
const PORT = 3001;

app.get('/hello', (req, res) => {
  res.json({
    service: 'A',
    message: 'Hello from service A'
  });
});

app.listen(PORT, () => {
  console.log(`Service A is running on http://localhost:${PORT}`);
});
