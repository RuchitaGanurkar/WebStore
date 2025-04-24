import { getRequest, getRequestID } from '../Pages/unitTests.cy';

describe('Product Catalogue API Tests - GET Requests', () => {

    it('Get all Products', () => {
        getRequest('http://localhost:8080/api/products', 200);
    });

    it('Get all Catalogues', () => {
        getRequest('http://localhost:8080/api/catalogues', 200);
    });

    it('Get all Currencies', () => {
        getRequest('http://localhost:8080/api/currencies', 200);
    });

    it('Get all Categories', () => {
        getRequest('http://localhost:8080/api/categories', 200);
    });

    it('Get all Catalogue-Category Pairs', () => {
        getRequest('http://localhost:8080/api/catalogue-categories', 200);
    });

    it('Get Products by ID', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.productIds.forEach((prodID) => {
                getRequestID('http://localhost:8080/api/products/' + prodID, 200, prodID, 'products');
            });
        });
    });

    it('Get Catalogues by ID', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.catalogueIds.forEach((catID) => {
                getRequestID('http://localhost:8080/api/catalogues/' + catID, 200, catID, 'catalogues');
            });
        });
    });
    /*
    it('Get Currencies by ID', () => {
        getRequest('http://localhost:8080/api/currencies/1', 200);
    });

    it('Get Categories by ID', () => {
        getRequest('http://localhost:8080/api/categories/2', 200);
    });

    it('Get Catalogues by Name', () => {
        getRequest('http://localhost:8080/api/catalogues/search/name?name=Winter', 200);
    });

    it('Get Catalogues by Description', () => {
        getRequest('http://localhost:8080/api/catalogues/search/description?description=for', 200);
    });

    it('Get Product Price Detail by ID', () => {
        getRequest('http://localhost:8080/api/currencies/1', 200);
    }); 
*/
});