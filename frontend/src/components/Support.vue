<template>
  <div class="support">
    <div class="content-wrapper">
      <!-- Documentation Section -->
      <section class="support-section">
        <h2>Dokumentation</h2>
        <div class="section-content">
          <p>Zugriff auf die Dokumentation und Hilferessourcen:</p>
          <a href="#" class="documentation-link" target="_blank">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            Zur Dokumentation
          </a>
        </div>
      </section>

      <!-- Contact Form Section -->
      <section class="support-section">
        <h2>Kontaktformular</h2>
        <div class="section-content">
          <p>Kontaktieren Sie unser Support-Team:</p>
          <form class="contact-form">
            <div class="form-group">
              <label for="name">Name</label>
              <input type="text" id="name" placeholder="Ihr Name" />
            </div>
            <div class="form-group">
              <label for="email">Email</label>
              <input type="email" id="email" placeholder="Ihre E-Mail-Adresse" />
            </div>
            <div class="form-group">
              <label for="subject">Betreff</label>
              <input type="text" id="subject" placeholder="Betreff" />
            </div>
            <div class="form-group">
              <label for="message">Nachricht</label>
              <textarea id="message" rows="6" placeholder="Ihre Nachricht"></textarea>
            </div>
            <button type="submit" class="submit-button" disabled>Nachricht senden (Funktionalität folgt)</button>
          </form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
// Component for Support page
// Displays documentation links and contact form placeholder
</script>

<style scoped>
.support {
  padding: 2rem 0;
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.support-section {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 12px;
  padding: 2rem;
}

.support-section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #f8fafc;
  border-bottom: 2px solid #f97316;
  padding-bottom: 0.75rem;
}

.section-content {
  color: #cbd5e1;
  font-size: 1rem;
  line-height: 1.6;
}

.documentation-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: #374151;
  color: #f97316;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  border: 1px solid #f97316;
}

.documentation-link:hover {
  background: #f97316;
  color: #1f2937;
  transform: translateY(-2px);
}

.contact-form {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #f8fafc;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  background: #374151;
  border: 1px solid #4b5563;
  border-radius: 8px;
  color: #f8fafc;
  font-family: inherit;
  transition: border-color 0.2s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.submit-button {
  padding: 0.75rem 1.5rem;
  background: #f97316;
  color: #1f2937;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-button:not(:disabled):hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(249, 115, 22, 0.2);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .support {
    padding: 1.5rem 1rem;
  }

  .content-wrapper {
    gap: 1.5rem;
  }

  .support-section {
    padding: 1.5rem;
  }

  .support-section h2 {
    font-size: 1.25rem;
  }

  .contact-form {
    gap: 1rem;
  }
}
</style>
