const { test, expect } = require('../support');
const { executeSQL, userLogin, feedData } = require('../support/data');

test.describe('Feed de Publicações', () => {

    test.beforeEach(async ({ page }) => {
        await executeSQL(`
            UPDATE users SET email_verified = true, status = 'ACTIVE' WHERE username = '${userLogin.username}';
            UPDATE gamer_profiles SET setup_status = 'COMPLETED', nickname = '${userLogin.username}' WHERE user_id IN (SELECT id FROM users WHERE username = '${userLogin.username}');
        `);

        await page.login.visit();
        await page.login.submit(userLogin.username, userLogin.password);
        await expect(page).toHaveURL(/.*home/);
    });

    test('Deve criar uma nova publicação no feed com sucesso', async ({ page }) => {
        await page.feed.publish(feedData.postContent);

        await expect(page.feed.toastSuccess).toBeVisible();
        await page.reload();
        await expect(page.feed.postByText(feedData.postContent)).toBeVisible();
    });

    test('Não deve permitir publicar quando o campo estiver vazio', async ({ page }) => {
        await expect(page.feed.buttonPublish).toBeDisabled();
    });
});
