describe('Product Catalogue API Testing', () => {

    it('Get all products', () => {
        cy.request({
            method: 'GET',
            url: 'http://localhost:8080/api/products',
        }).then((response) => {
            expect(response.status).to.eq(200);
            //expect(response.body).to.have.property('products');
            //expect(response.body.products).to.be.an('array');
        });
    });

});