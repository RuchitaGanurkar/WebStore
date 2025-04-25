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
    
    it('Get Currencies by ID', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.currencyIds.forEach((currID) => {
                getRequestID('http://localhost:8080/api/currencies/' + currID, 200, currID, 'currencies');
            });
        });
    });
    
    it('Get Categories by ID', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.categoryIds.forEach((catID) => {
                getRequestID('http://localhost:8080/api/categories/' + catID, 200, catID, 'categories');
            });
        });
    });
    
    it('Get Product Price Detail by ID', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.productPriceIds.forEach((pPID) => {
                getRequestID('http://localhost:8080/api/product-price/' + pPID, 200, pPID, 'product-price');
            });
        });
    });
    /*
    it('Get Catalogues by Name', () => {
        cy.fixture('ids.json').then((ids) => {
            ids.catNames.forEach((catName) => {
                getRequestID('http://localhost:8080/api/catalogues/search/name?name=' + catName, 200, catName, 'cat-byName');
            });
        });
    });
    
    it('Get Catalogues by Description', () => {
        getRequest('http://localhost:8080/api/catalogues/search/description?description=for', 200);
    }); 
*/
});