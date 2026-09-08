import { expect } from "@playwright/test";

export class Login {

    constructor(page) {
        this.page = page;
    }

    async visit() {
        await this.page.goto('/');
    }

    async submit(username, password) {
        await this.page.getByRole('textbox', { name: /login/i }).fill(username);
        await this.page.getByLabel('Senha de acesso').fill(password);
        await this.page.getByRole('button', { name: 'Entrar' }).click();
    }


    async alertMessage(messages) {
        await expect.poll(async () => {
            for (const message of messages) {
                if (await this.page.getByText(message, { exact: true }).isVisible()) {
                    return true;
                }
            }

            return false;
        }).toBe(true);
    }

}

