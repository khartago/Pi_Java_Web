import { Controller } from '@hotwired/stimulus';
import { marked } from 'marked';

marked.use({ breaks: true, gfm: true });

export default class extends Controller {
    static targets = ['messages', 'input', 'send', 'status', 'languageButton'];
    static values = {
        endpoint: String,
        language: String,
    };

    connect() {
        this.messages = [];
        this.setLanguage(this.languageValue || 'auto');
    }

    focusInput() {
        if (this.hasInputTarget) {
            this.inputTarget.focus();
        }
    }

    onKeydown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.send(event);
        }
    }

    selectLanguage(event) {
        const language = event.currentTarget?.dataset?.language || 'auto';
        this.setLanguage(language);
    }

    async send(event) {
        event.preventDefault();

        const content = this.inputTarget.value.trim();
        if (!content) {
            return;
        }

        this.addMessage('user', content);
        this.inputTarget.value = '';
        this.setLoading(true);
        this.setStatus('Assistant is thinking...');

        try {
            const response = await fetch(this.endpointValue, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                },
                body: JSON.stringify({
                    language: this.languageValue || 'auto',
                    messages: this.buildPayload(),
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                const message = data?.error || 'Request failed.';
                throw new Error(message);
            }

            this.addMessage('assistant', data.reply, data.intent, data.summary);
            this.setStatus('');
        } catch (error) {
            const rawMessage = error instanceof Error && error.message ? error.message : 'Request failed.';
            const normalized = rawMessage.toLowerCase();
            const isRateLimit = normalized.includes('rate limit') || normalized.includes('quota') || normalized.includes('429');
            const displayMessage = isRateLimit
                ? 'Rate limit or no credits. Check your OpenAI usage/billing and try again.'
                : rawMessage;

            this.addMessage('assistant', displayMessage);
            this.setStatus('');
        } finally {
            this.setLoading(false);
        }
    }

    addMessage(role, content, intent = null, summary = null) {
        this.messages.push({ role, content });

        const message = document.createElement('div');
        message.classList.add('assistant-message');
        message.classList.add(role === 'user' ? 'is-user' : 'is-ai');

        const meta = document.createElement('span');
        meta.classList.add('assistant-message__meta');
        meta.textContent = role === 'user' ? 'You' : 'Assistant';

        // Show a "source: DB" badge when the reply used live backend data
        if (role === 'assistant' && intent && intent !== 'unknown') {
            const badge = document.createElement('span');
            badge.classList.add('assistant-source-badge');
            badge.title = summary || intent;
            badge.textContent = '⚡ stock DB';
            meta.append(' ', badge);
        }

        const body = document.createElement('div');
        body.classList.add('assistant-message__text');
        if (role === 'assistant') {
            try {
                const html = marked.parse(content);
                body.innerHTML = typeof html === 'string' ? html : content;
            } catch {
                body.textContent = content;
            }
        } else {
            body.textContent = content;
        }

        message.append(meta, body);
        this.messagesTarget.append(message);
        this.messagesTarget.scrollTop = this.messagesTarget.scrollHeight;
    }

    buildPayload() {
        return this.messages.slice(-8).map(({ role, content }) => ({ role, content }));
    }

    setLanguage(language) {
        this.languageValue = language;
        this.languageButtonTargets.forEach((button) => {
            button.classList.toggle('is-active', button.dataset.language === language);
        });
    }

    setLoading(isLoading) {
        this.sendTarget.disabled = isLoading;
        this.inputTarget.disabled = isLoading;
        this.element.classList.toggle('is-loading', isLoading);
    }

    setStatus(message) {
        this.statusTarget.textContent = message;
    }
}
