export class Profile {

    constructor(page) {
        this.page = page;
        this.header = page.locator('app-profile-header');
        this.nickname = this.header.getByRole('heading', { level: 1 });
        this.bio = this.header.locator('p.text-body-sm');
        this.buttonEditProfile = this.header.getByRole('link', { name: 'Editar Perfil' });
    }

    async visit() {
        await this.page.goto('/perfil');
        await this.page.waitForURL(/.*perfil\/.+/);
    }

    async openEdit() {
        await this.buttonEditProfile.click();
    }
}
