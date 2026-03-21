const express = require('express');

const app = express();
const PORT = 3002;

app.get('/call-a', async (req, res) => {
  try {
    const response = await fetch('http://localhost:3001/hello');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const dataFromA = await response.json();
    res.json({
      service: 'B',
      dataFromA: dataFromA
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch from Service A', details: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`Service B is running on http://localhost:${PORT}`);
});
