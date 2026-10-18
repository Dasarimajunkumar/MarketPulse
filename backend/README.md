# MarketPulse Backend Service

Production Financial News & Regulatory Intelligence Ingestion Engine.

## Architecture
- **Source Polling**: Polling SEBI, RBI, NSE, Moneycontrol, ET, and Mint feeds at configured intervals (5m / 10m).
- **XML/JSON Normalization**: Standardizing feed payloads, normalizing publication timestamps to UTC/IST.
- **Deduplication**: Deduplicating cross-publisher news by title fingerprinting and entity mapping.
- **Grounded RAG Chat Endpoint**: `/api/v1/chat` integrates with Google Gemini API to deliver evidence-backed AI research responses with source citation.

## Endpoints
- `GET /api/v1/news`
- `GET /api/v1/news/:eventId`
- `GET /api/v1/sources`
- `GET /api/v1/market/indices`
- `GET /api/v1/companies/:companyId/events`
- `GET /api/v1/alerts`
- `POST /api/v1/chat`

## How to Run
```bash
cd backend
npm install
npm start
```
