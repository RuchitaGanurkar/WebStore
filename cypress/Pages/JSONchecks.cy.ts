export function productsChecks(Object : any) {
        expect(Object).to.have.property('productId');
        expect(Object).to.have.property('productName');
        expect(Object).to.have.property('productDescription');
        expect(Object).to.have.property('category');
        expect(Object).to.have.property('createdAt');
        expect(Object).to.have.property('createdBy');
        expect(Object).to.have.property('updatedAt');
        expect(Object).to.have.property('updatedBy');
        expect(Object).to.have.property('prices');

        // Validating nested properties in 'category'
        expect(Object.category).to.have.property('categoryName');
        expect(Object.category).to.have.property('categoryDescription');
}

export function catalogueChecks(Object: any) {
    expect(Object).to.have.property('catalogueId');
    expect(Object).to.have.property('catalogueName');
    expect(Object).to.have.property('catalogueDescription');
    expect(Object).to.have.property('createdAt');
    expect(Object).to.have.property('createdBy');
    expect(Object).to.have.property('updatedAt');
    expect(Object).to.have.property('updatedBy');
    expect(Object).to.have.property('categories');
}

export function currencyChecks(Object: any) {
    expect(Object).to.have.property('currencyId');
    expect(Object).to.have.property('currencyCode');
    expect(Object).to.have.property('currencyName');
    expect(Object).to.have.property('currencySymbol');
    expect(Object).to.have.property('createdAt');
    expect(Object).to.have.property('createdBy');
    expect(Object).to.have.property('updatedAt');
    expect(Object).to.have.property('updatedBy');
}

export function categoryChecks(Object: any) {
    expect(Object).to.have.property('categoryId');
    expect(Object).to.have.property('categoryName');
    expect(Object).to.have.property('categoryDescription');
    expect(Object).to.have.property('createdAt');
    expect(Object).to.have.property('createdBy');
    expect(Object).to.have.property('updatedAt');
    expect(Object).to.have.property('updatedBy');
    expect(Object).to.have.property('products');
}

export function catalogueCategoryChecks(Object: any) {
    expect(Object).to.have.property('catalogueId');
    expect(Object).to.have.property('catalogueName');
    expect(Object).to.have.property('categoryId');
    expect(Object).to.have.property('categoryName');
    expect(Object).to.have.property('createdAt');
    expect(Object).to.have.property('createdBy');
    expect(Object).to.have.property('updatedAt');
    expect(Object).to.have.property('updatedBy');
}