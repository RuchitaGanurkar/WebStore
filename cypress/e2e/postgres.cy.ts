describe('We will make a connection with Postgres', () => {
    it('First connection', () => {
        cy.task("connectDB", "SELECT * FROM web_store.currency WHERE currency_id = 1;")
        .then((res : any) => {
            cy.log('Database connection result: ', res[0].currency_name);
        });
    })
})