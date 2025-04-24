import { catalogueChecks, currencyChecks, productsChecks, categoryChecks, catalogueCategoryChecks } from '../Pages/JSONchecks.cy';

export function getRequest(url: string, expectedStatus: number) {
    cy.request({
        method: 'GET',
        url: url
    })
    .then((response) => {
        expect(response.status).to.eq(expectedStatus);
        expect(response.body).to.be.an('array');

        response.body.forEach((Object: any) => {
            if(Object.hasOwnProperty('categoryName') && Object.hasOwnProperty('catalogueName')) {
                catalogueCategoryChecks(Object);
            }
            else if(Object.hasOwnProperty('productName')) {
                productsChecks(Object);
            }
            else if(Object.hasOwnProperty('catalogueName')) {
                catalogueChecks(Object);
            }
            else if(Object.hasOwnProperty('currencyName')) {
                currencyChecks(Object);
            }
            else if(Object.hasOwnProperty('categoryName')) {
                categoryChecks(Object);
            }
        });
    });
}

export function getRequestID(url: string, expectedStatus: number, ID: any, type: string) {
    cy.request({
        method: 'GET',
        url: `${url}`
    })
    .then((response) => {
        expect(response.status).to.eq(expectedStatus);
        expect(response.body).to.be.an('object');
        if(type == 'products')
            expect(response.body.productId).to.eq(ID);
        else if(type == 'catalogues')
            expect(response.body.catalogueId).to.eq(ID);
    });
}