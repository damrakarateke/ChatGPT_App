import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import rateLimit from "express-rate-limit";

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json({ limit: "1mb" }));

app.use("/chat", rateLimit({ windowMs: 60 * 1000, max: 30 }));

// Optional Auth
app.use((req, res, next) => {
  const expected = process.env.APP_AUTH_TOKEN;
  if (expected && req.header("X-App-Auth") !== expected) {
    return res.status(401).json({ error: "Unauthorized" });
  }
  next();
});

app.post("/chat", async (req, res) => {
  try {
    const { message } = req.body;
    if (!message) return res.status(400).json({ error: "Missing message" });

    const resp = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: "gpt-4o-mini",
        messages: [
          { role: "system", content: "You are a helpful assistant." },
          { role: "user", content: message }
        ],
        temperature: 0.2
      }),
    });

    const data = await resp.json();
    const reply = data?.choices?.[0]?.message?.content?.trim() || "";
    res.json({ reply });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Server error" });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Proxy läuft auf http://localhost:${port}`);
});
