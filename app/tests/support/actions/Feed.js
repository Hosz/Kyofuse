export class Feed {

    constructor(page) {
        this.page = page;
        this.inputComposer = page.locator('app-mention-input textarea');
        this.buttonPublish = page.getByRole('button', { name: 'Publicar' });
        this.postList = page.locator('app-feed-list');
        this.toastSuccess = page.getByText('Publicação realizada com sucesso!');
    }

    postByText(text) {
        return this.postList.locator('app-post-content').getByText(text).first();
    }

    async visit() {
        await this.page.goto('/home');
    }

    async publish(text) {
        await this.inputComposer.fill(text);
        await this.buttonPublish.click();
    }
}
