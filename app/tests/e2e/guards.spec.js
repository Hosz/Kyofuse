const { test, expect } = require('../support');
const { executeSQL, userPending } = require('../support/data');

test.describe('Guardas de Segurança de Rota', () => {
    test.describe.configure({ mode: 'serial' });

    test.beforeAll(async () => {
        await executeSQL(`
            INSERT INTO users (id, first_name, last_name, email, email_index, username, password_hash, role, status, email_verified, created_at, updated_at) 
            SELECT gen_random_uuid(), 'Usuario', 'Pendente', email, 'pendente_index_hash', '${userPending.username}', password_hash, 'USER', 'ACTIVE', true, NOW(), NOW()
            FROM users WHERE username = 'testeee'
            ON CONFLICT (username) DO NOTHING;

            INSERT INTO gamer_profiles (id, user_id, nickname, setup_status, created_at, updated_at) 
            SELECT gen_random_uuid(), id, '${userPending.username}', 'PENDING', NOW(), NOW() 
            FROM users WHERE username = '${userPending.username}' 
            ON CONFLICT DO NOTHING;
        `);
    });

    test('Não deve permitir usuário não autenticado acessar a página inicial (/home)', async ({ page }) => {
        await page.goto('/home');
        await expect(page).toHaveURL(/http:\/\/localhost:4200\/?$/);
    });

    test('Não deve permitir usuário não autenticado acessar o onboarding (/setup)', async ({ page }) => {
        await page.goto('/setup');
        await expect(page).toHaveURL(/http:\/\/localhost:4200\/?$/);
    });

    test('Deve redirecionar usuário com setup pendente para a tela de setup ao tentar acessar /home', async ({ page }) => {
        await executeSQL(`
            UPDATE gamer_profiles SET setup_status = 'PENDING' WHERE user_id IN (SELECT id FROM users WHERE username = '${userPending.username}');
        `);

        await page.login.visit();
        await page.login.submit(userPending.username, userPending.password);
        await expect(page).toHaveURL(/.*setup/);

        await page.goto('/home');
        await expect(page).toHaveURL(/.*setup/);
    });

    test('Deve concluir o setup de perfil inicial e liberar o acesso à home', async ({ page }) => {
        await executeSQL(`
            UPDATE gamer_profiles SET setup_status = 'PENDING' WHERE user_id IN (SELECT id FROM users WHERE username = '${userPending.username}');
        `);

        await page.login.visit();
        await page.login.submit(userPending.username, userPending.password);
        await expect(page).toHaveURL(/.*setup/);

        await page.profileSetup.complete('GamerSetupOk');
        await expect(page).toHaveURL(/.*home/);
    });
});
