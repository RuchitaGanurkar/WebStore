describe('We will make a connection with Postgres', () => {
    it('First connection', () => {
        cy.task("connectDB").then((res) => {
            cy.log('Database connection result: ', res);
        });
    })
})