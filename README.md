# AI workshop — document handling

Upload a document, and an agent reads it: what kind of document it is, the facts worth pulling out
of it, and whether the file is legible enough to work with.

The domain language lives in [CONTEXT.md](./CONTEXT.md).

## Running it

Two terminals. Maven does not build the frontend and never will — Vite proxies `/api` to Spring Boot,
so the browser sees a single origin and there is no CORS configuration anywhere.

```bash
# terminal 1 — backend on :8080
export AZURE_OPENAI_API_KEY=...        # or use the Vertex provider, see below
./mvnw spring-boot:run

# terminal 2 — frontend on :5173
cd frontend && npm install && npm run dev
```

Then open http://localhost:5173.

## Choosing a provider

`aiworkshop.model.provider` picks which `ChatModel` bean is built. Override per run:

```bash
AI_PROVIDER=vertex ./mvnw spring-boot:run    # Gemini on Vertex AI
AI_PROVIDER=foundry ./mvnw spring-boot:run   # a deployment on Azure AI Foundry
```

Vertex authenticates with Application Default Credentials, not an API key:

```bash
gcloud auth application-default login
gcloud auth application-default set-quota-project "$GOOGLE_CLOUD_PROJECT"
```

Foundry needs `AZURE_OPENAI_API_KEY` exported. Both providers accept PDFs and images as inline data,
so uploads are sent to the model as-is — nothing extracts text first.

## Where the agent is

`DocumentAnalyzer` is the whole intake agent: an interface, a system message, and a return type.
LangChain4j generates the implementation, so the return type *is* the output schema — add a component
to `DocumentAnalysis` and the agent starts filling it in.

| | |
|---|---|
| `document/DocumentAnalyzer.java` | the agent |
| `document/DocumentAnalysis.java` | what it returns, and therefore what it is asked for |
| `document/DocumentIntake.java` | turns an upload into the file content the model reads |
| `document/DocumentStore.java` | in-memory; everything is lost on restart |
| `frontend/src/App.tsx` | the one screen |
