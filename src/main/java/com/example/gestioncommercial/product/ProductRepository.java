package com.example.gestioncommercial.product;

import com.example.gestioncommercial.DataAccessObject;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ProductRepository {
    private final DataAccessObject dao;

    public ProductRepository() {
        this.dao = new DataAccessObject();
    }

    public ObservableList<Product> findAll() {
        String productsQuery = "select * from product";
        return dao.getProducts(productsQuery);
    }

    public void save(Product product) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        String insertQuery = "insert into product(name, description, selling_price_excluding_tax, purchase_price_excluding_tax, quantity, tax_rate) values('%s', '%s', %s, %s, %d, %s)"
                .formatted(
                        product.getName(),
                        product.getDescription(),
                        df.format(product.getSellingPriceExcludingTax()),
                        df.format(product.getPurchasePriceExcludingTax()),
                        product.getQuantity(),
                        df.format(product.getTaxRate())
                );

        dao.saveData(insertQuery);
    }

    public void update(Product product) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        String updateQuery = "update product set name = '%s', description ='%s', purchase_price_excluding_tax = %s, selling_price_excluding_tax = %s, quantity =%s, tax_rate = %s where id = %d"
                .formatted(
                        product.getName(),
                        product.getDescription(),
                        df.format(product.getPurchasePriceExcludingTax()),
                        df.format(product.getSellingPriceExcludingTax()),
                        product.getQuantity(),
                        df.format(product.getTaxRate()),
                        product.getId()
                );

        dao.saveData(updateQuery);
    }

    public void deleteById(int id) throws SQLException {
        String query = "delete from product where id = " + id;
        dao.saveData(query);
    }
}
