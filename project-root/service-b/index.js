const express = require('express');

const app = express();
const PORT = 3002;

app.use(express.json());

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

app.post('/send-to-a', async (req, res) => {
  try {
    const response = await fetch('http://localhost:3001/data', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const dataFromA = await response.json();
    res.json({
      service: 'B',
      dataFromA: dataFromA
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to post to Service A', details: error.message });
  }
});

app.put('/update-a/:id', async (req, res) => {
  const { id } = req.params;
  try {
    const response = await fetch(`http://localhost:3001/data/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req.body)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const dataFromA = await response.json();
    res.json({
      service: 'B',
      dataFromA: dataFromA
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to put to Service A', details: error.message });
  }
});

app.delete('/delete-a/:id', async (req, res) => {
  const { id } = req.params;
  try {
    const response = await fetch(`http://localhost:3001/data/${id}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const dataFromA = await response.json();
    res.json({
      service: 'B',
      dataFromA: dataFromA
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete from Service A', details: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`Service B is running on http://localhost:${PORT}`);
});
