const { test: base, expect } = require("@playwright/test");
const { Login } = require('./actions/login');
const { Register } = require('./actions/Register');
const { Components } = require('./actions/Components');
const { Profile } = require('./actions/Profile');
const { ProfileEdit } = require('./actions/ProfileEdit');
const { Feed } = require('./actions/Feed');
const { ProfileSetup } = require('./actions/ProfileSetup');

const test = base.extend({

    page: async ({ page }, use) => {

        const context = page;
        context["login"] = new Login(page);
        context["register"] = new Register(page);
        context["components"] = new Components(page);
        context["profile"] = new Profile(page);
        context["profileEdit"] = new ProfileEdit(page);
        context["feed"] = new Feed(page);
        context["profileSetup"] = new ProfileSetup(page);

        await use(context);
    },

});

export { test, expect };
