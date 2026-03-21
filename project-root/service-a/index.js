const express = require('express');

const app = express();
const PORT = 3001;

app.use(express.json());

app.get('/hello', (req, res) => {
  res.json({
    service: 'A',
    message: 'Hello from service A'
  });
});

app.post('/data', (req, res) => {
  const body = req.body;
  res.status(201).json({
    service: 'A',
    message: 'Data created successfully',
    data: body
  });
});

app.put('/data/:id', (req, res) => {
  const { id } = req.params;
  const body = req.body;
  res.json({
    service: 'A',
    message: `Data with ID ${id} updated successfully`,
    data: body
  });
});

app.delete('/data/:id', (req, res) => {
  const { id } = req.params;
  res.json({
    service: 'A',
    message: `Data with ID ${id} deleted successfully`
  });
});

app.listen(PORT, () => {
  console.log(`Service A is running on http://localhost:${PORT}`);
});
