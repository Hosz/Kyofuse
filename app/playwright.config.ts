import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './tests/e2e',       // Onde estão os seus testes
    fullyParallel: true,          // Roda testes em paralelo para ser rápido
    retries: 0,                   // Tentativas em caso de falha (ajuste se precisar)
    workers: undefined,
    reporter: 'html',             // Gera relatório visual em HTML

    use: {
        baseURL: 'http://localhost:4200', // Porta padrão do Angular
        trace: 'on-first-retry',          // Grava vídeo e passos se o teste falhar
        screenshot: 'only-on-failure',
    },

    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],

    webServer: {
        command: 'npm start',
        url: 'http://localhost:4200',
        reuseExistingServer: true,
        timeout: 120000,
    },
});
