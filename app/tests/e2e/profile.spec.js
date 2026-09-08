const { test, expect } = require('../support');
const { executeSQL, userLogin, profileData } = require('../support/data');

test.describe('Perfil do Jogador', () => {
    test.describe.configure({ mode: 'serial' });

    test.beforeEach(async ({ page }) => {
        await executeSQL(`
            UPDATE users SET email_verified = true, status = 'ACTIVE' WHERE username = '${userLogin.username}';
            UPDATE gamer_profiles SET setup_status = 'COMPLETED', nickname = '${userLogin.username}', bio = 'Bio inicial' WHERE user_id IN (SELECT id FROM users WHERE username = '${userLogin.username}');
        `);

        await page.login.visit();
        await page.login.submit(userLogin.username, userLogin.password);
        await expect(page).toHaveURL(/.*home/);
    });

    test('Deve visualizar os dados do próprio perfil', async ({ page }) => {
        await page.profile.visit();

        await expect(page).toHaveURL(/.*perfil/);
        await expect(page.profile.nickname).toHaveText(userLogin.username);
        await expect(page.profile.buttonEditProfile).toBeVisible();
    });

    test('Deve editar as informações do perfil com sucesso', async ({ page }) => {
        await page.profile.visit();
        await page.profile.openEdit();

        await expect(page).toHaveURL(/.*perfil\/editar/);

        await page.profileEdit.update(profileData.nickname, profileData.bio);

        await expect(page).toHaveURL(/.*perfil/);
        await expect(page.profile.nickname).toHaveText(profileData.nickname);
        await expect(page.profile.bio).toHaveText(profileData.bio);
    });
});
