<template>
  <section class="page">
    <div class="card">
      <h2>Meldedaten und Schulbewerbung.de</h2>
      <p>
        In diesem Bereich finden Sie den Einstieg zu Meldedaten und können direkt zur Plattform
        Schulbewerbung.de wechseln.
      </p>
      <a
        class="action"
        href="https://www.schulbewerbung.de"
        target="_blank"
        rel="noopener noreferrer"
      >
        Schulbewerbung.de öffnen
      </a>
    </div>
  </section>
</template>

<style scoped>
.page {
  padding: 2rem 0;
}

.card {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 700px;
}

.card h2 {
  margin: 0 0 0.75rem 0;
  color: #f8fafc;
}

.card p {
  margin: 0 0 1rem 0;
  color: #94a3b8;
  line-height: 1.5;
}

.action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.65rem 1rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  color: #111827;
  background: #f97316;
}

.action:hover {
  background: #fb923c;
}
</style>
