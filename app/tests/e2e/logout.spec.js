const { test, expect } = require('../support');
const { executeSQL, userLogin } = require('../support/data');

test.describe('Encerramento de Sessão (Logout)', () => {

    test.beforeAll(async () => {
        await executeSQL(`UPDATE users SET email_verified = true, status = 'ACTIVE' WHERE username = '${userLogin.username}'`);
        await executeSQL(`UPDATE gamer_profiles SET setup_status = 'COMPLETED' WHERE nickname = '${userLogin.username}'`);
    });

    test('Deve realizar logout com sucesso e impedir acesso a rotas protegidas', async ({ page }) => {
        await page.login.visit();
        await page.login.submit(userLogin.username, userLogin.password);
        await expect(page).toHaveURL(/.*home/);

        await page.components.logout();

        await expect(page).toHaveURL(/http:\/\/localhost:4200\/?$/);

        await page.goto('/home');
        await expect(page).toHaveURL(/http:\/\/localhost:4200\/?$/);
    });
});
