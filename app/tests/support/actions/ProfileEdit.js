export class ProfileEdit {

    constructor(page) {
        this.page = page;
        this.inputNickname = page.getByLabel('Nickname');
        this.inputBio = page.getByLabel('Bio');
        this.buttonSave = page.getByRole('button', { name: 'Salvar alterações' });
        this.buttonCancel = page.getByRole('link', { name: 'Cancelar' });
    }

    async visit() {
        await this.page.goto('/perfil/editar');
    }

    async update(nickname, bio) {
        if (nickname !== undefined) {
            await this.inputNickname.fill(nickname);
        }
        if (bio !== undefined) {
            await this.inputBio.fill(bio);
        }
        await this.buttonSave.click();
    }
}
