package com.example.gestioncommercial.report;


import com.example.gestioncommercial.invoice.Invoice;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import pl.allegro.finance.tradukisto.MoneyConverters;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceReportManager {
    private JasperPrint generateInvoice(Invoice invoice) throws JRException {
        List<InvoiceReportItem> invoiceReportItems = new ArrayList<>();
        invoice.getInvoiceItems().forEach(item -> {
            InvoiceReportItem invoiceReportItem =
                    new InvoiceReportItem(
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getUnitPriceExcludingTaxes().setScale(2, RoundingMode.DOWN),
                            item.getTotalTaxes().multiply(BigDecimal.valueOf(10L)).toBigInteger().intValueExact(),
                            item.getTotalExcludingTaxes().setScale(2, RoundingMode.DOWN),
                            item.getTotalIncludingTaxes().setScale(2, RoundingMode.DOWN),
                            item.getTotalTaxes().setScale(2, RoundingMode.DOWN));

            invoiceReportItems.add(invoiceReportItem);
        });

        JRBeanCollectionDataSource invoiceItemsDataSet = new JRBeanCollectionDataSource(invoiceReportItems);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", invoice.getId());
        parameters.put("issueDate", invoice.getIssueDate());

        parameters.put("name", invoice.getClient().getName());
        parameters.put("address", invoice.getClient().getAddress());
        parameters.put("ICE", invoice.getClient().getCommonCompanyIdentifier());

        parameters.put("invoiceItemsDataSet", invoiceItemsDataSet);

        parameters.put("totalHT", invoice.getTotalExcludingTaxes());
        parameters.put("totalTaxes", invoice.getTotalTaxes());
        parameters.put("totalTTC", invoice.getTotalIncludingTaxes());

        parameters.put("amountInWords", getMoneyIntoWords(invoice.getTotalIncludingTaxes().toString()));

        String filePath = "/templates/invoiceReport.jrxml";

        JasperReport report = null;
        try (InputStream inputStream = InvoiceReportManager.class.getResourceAsStream(filePath)) {
            if (inputStream != null) {
                // Process the input stream
                report = JasperCompileManager.compileReport(inputStream);
            } else {
                System.out.println("Resource not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
    }

    public void displayInvoiceReport(Invoice invoice) throws JRException {
        JasperViewer jasperViewer = new JasperViewer(generateInvoice(invoice), false);
        jasperViewer.setVisible(true);
        jasperViewer.setTitle("Facture N° " + invoice.getId());
        jasperViewer.setFitPageZoomRatio();
    }

    private String getMoneyIntoWords(String input) {
        MoneyConverters converter = MoneyConverters.FRENCH_BANKING_MONEY_VALUE;

        String[] amounts = input.split("\\.");
        BigDecimal dirhams = new BigDecimal(amounts[0]);
        BigDecimal centimes = new BigDecimal(amounts[1]);
        String output;

        if (dirhams.compareTo(BigDecimal.ONE) == 1) {
            output = converter.asWords(dirhams).split("€")[0] + " DIRHAMS";
        } else {
            output = converter.asWords(dirhams).split("€")[0] + " DIRHAM";
        }

        if (centimes.compareTo(BigDecimal.ZERO) == 1) {
            output = output.concat(", ET " + converter.asWords(centimes).split("€")[0] + " CENTIMES");
        }

        return output.toUpperCase();
    }

}
