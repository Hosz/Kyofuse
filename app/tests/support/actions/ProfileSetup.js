export class ProfileSetup {

    constructor(page) {
        this.page = page;
        this.inputNickname = page.locator('#setup-nickname');
        this.buttonSave = page.getByRole('button', { name: /Salvar e Continuar/i });
        this.buttonSkip = page.getByRole('button', { name: /Pular/i });
    }

    async visit() {
        await this.page.goto('/setup');
    }

    async complete(nickname) {
        if (nickname) {
            await this.inputNickname.fill(nickname);
        }
        await this.buttonSave.click();
    }

    async skip() {
        await this.buttonSkip.click();
    }
}
