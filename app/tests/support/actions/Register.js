export class Register {

    constructor(page) {
        this.page = page;
        this.headingConfirmEmail = page.getByRole('heading', { name: 'Confirme seu E-mail' });
        this.headingAccountActivated = page.getByRole('heading', { name: 'Conta Ativada!' });

        this.buttonOpenRegister = page.getByRole('button', { name: /Registrar/i });
        this.buttonSubmit = page.locator('button[type="submit"]');

        this.inputFirstName = page.getByPlaceholder('João');
        this.inputLastName = page.getByPlaceholder('Silva');
        this.inputEmail = page.getByPlaceholder('nome@agencia.com');
        this.inputUsername = page.getByPlaceholder('Rival_01');
        this.inputPassword = page.getByPlaceholder('Mínimo 8 caracteres');
        this.inputConfirmPassword = page.getByPlaceholder('Repita sua senha');
        this.checkboxRobot = page.getByRole('checkbox', { name: 'Não sou um robô' });

        this.errorFirstName = page.getByText('Informe seu nome.');
        this.errorLastName = page.getByText('Informe seu sobrenome.');
        this.errorEmail = page.getByText('Informe seu e-mail de contato.');
        this.errorInvalidEmail = page.getByText('Informe um e-mail válido.');
        this.errorUsername = page.getByText('Escolha um username.');
        this.errorPassword = page.getByText('Crie uma senha de acesso.');
        this.errorShortPassword = page.getByText('Mínimo de 8 caracteres.');
        this.errorPasswordMismatch = page.getByText('As senhas não coincidem.');
        this.errorRobot = page.getByText('Confirme que você não é um robô.');
    }

    async visit() {
        await this.page.goto('/');
    }

    async open() {
        await this.buttonOpenRegister.click();
    }

    async fillForm(userData) {
        if (userData.name !== undefined) await this.inputFirstName.fill(userData.name);
        if (userData.lastName !== undefined) await this.inputLastName.fill(userData.lastName);
        if (userData.email !== undefined) await this.inputEmail.fill(userData.email);
        if (userData.username !== undefined) await this.inputUsername.fill(userData.username);
        if (userData.password !== undefined) await this.inputPassword.fill(userData.password);
        if (userData.confirmPassword !== undefined) {
            await this.inputConfirmPassword.fill(userData.confirmPassword);
        } else if (userData.password !== undefined) {
            await this.inputConfirmPassword.fill(userData.password);
        }
    }

    async submitForm() {
        await this.buttonSubmit.click();
    }

    async submit(userData) {
        await this.open();
        await this.fillForm(userData);

        await this.checkboxRobot.click();
        await this.page.waitForTimeout(3000);
        await this.submitForm();
    }
}

