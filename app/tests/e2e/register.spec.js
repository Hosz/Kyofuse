const { test, expect } = require('../support');
const { executeSQL, userRegister, invalidUsers } = require('../support/data');
const { getConfirmationLink } = require('../support/mailpit');

test.describe('Cadastro com Sucesso', () => {
    test.beforeAll(async () => {
        await executeSQL(`
            DELETE FROM gamer_profiles WHERE user_id IN (SELECT id FROM users WHERE username = '${userRegister.username}');
            DELETE FROM users WHERE username = '${userRegister.username}';
        `);
    });

    test('Deve registrar novo usuario e ativar conta via e-mail', async ({ page }) => {
        await page.register.visit();
        await page.register.submit(userRegister);
        await expect(page.register.headingConfirmEmail).toBeVisible();

        const link = await getConfirmationLink(userRegister.email);
        await page.goto(link);
        await expect(page.register.headingAccountActivated).toBeVisible();
        await expect(page).toHaveURL(/.*\/setup/);
    });
});

test.describe('Cenários Negativos de Cadastro', () => {
    test.beforeEach(async ({ page }) => {
        await page.register.visit();
        await page.register.open();
    });

    test('Não deve cadastrar quando submeter formulário em branco', async ({ page }) => {
        await page.register.submitForm();

        await expect(page.register.errorFirstName).toBeVisible();
        await expect(page.register.errorLastName).toBeVisible();
        await expect(page.register.errorEmail).toBeVisible();
        await expect(page.register.errorUsername).toBeVisible();
        await expect(page.register.errorPassword).toBeVisible();
        await expect(page.register.errorRobot).toBeVisible();
    });

    test('Não deve cadastrar com e-mail em formato inválido', async ({ page }) => {
        await page.register.fillForm(invalidUsers.invalidEmail);
        await page.register.submitForm();

        await expect(page.register.errorInvalidEmail).toBeVisible();
    });

    test('Não deve cadastrar quando as senhas forem divergentes', async ({ page }) => {
        await page.register.fillForm(invalidUsers.passwordMismatch);
        await page.register.submitForm();

        await expect(page.register.errorPasswordMismatch).toBeVisible();
    });

    test('Não deve cadastrar com senha menor que 8 caracteres', async ({ page }) => {
        await page.register.fillForm(invalidUsers.shortPassword);
        await page.register.submitForm();

        await expect(page.register.errorShortPassword).toBeVisible();
    });
});