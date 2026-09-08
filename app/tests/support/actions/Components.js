export class Components {

    constructor(page) {
        this.page = page;
        this.buttonLogout = page.getByRole('button', { name: 'Sair' });
    }

    async logout() {
        await this.buttonLogout.click();
    }
}
