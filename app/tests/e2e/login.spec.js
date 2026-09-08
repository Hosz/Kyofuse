const { test, expect } = require('../support');
const { executeSQL, userLogin } = require('../support/data');

test.beforeAll(async () => {
    // Garante via banco que o usuário está ATIVO, com e-mail verificado e perfil completo
    await executeSQL(`UPDATE users SET email_verified = true, status = 'ACTIVE' WHERE username = '${userLogin.username}'`);
    await executeSQL(`UPDATE gamer_profiles SET setup_status = 'COMPLETED' WHERE nickname = '${userLogin.username}'`);
});

test('deve logar com sucesso na plataforma', async ({ page }) => {
    await page.login.visit();
    await page.login.submit(userLogin.username, userLogin.password);

    // Valida que foi redirecionado com sucesso para a página home
    await expect(page).toHaveURL(/.*home/);
});

test('deve falhar ao logar com senha errada', async ({ page }) => {
    await page.login.visit();
    await page.login.submit(userLogin.username, 'senhaerrada');
    await page.login.alertMessage([
        'Falha ao autenticar. Verifique suas credenciais.',
        'Credenciais inválidas.',
        'Dados inválidos.'
    ]);
})

test('deve falhar ao logar com usuario invalido', async({page}) =>{
    await page.login.visit();
    await page.login.submit('usuarioinvalido', userLogin.password);

    await page.login.alertMessage([
        'Falha ao autenticar. Verifique suas credenciais.',
        'Credenciais inválidas.',
        'Dados inválidos.'
    ]);

})

test('deve falhar ao logar sem username e senha', async({page}) => {
    await page.login.visit();
    await page.login.submit('', '');

    await page.login.alertMessage([
        'Informe seu login.',
        'Informe sua senha de acesso.'
    ]);

})