package com.example.salesmanagement.report;


import com.example.salesmanagement.configuration.AppConfiguration;
import com.example.salesmanagement.configuration.ConfigKey;
import com.example.salesmanagement.document.DeliveryNote;
import com.example.salesmanagement.document.Document;
import com.example.salesmanagement.document.PurchaseDocument;
import com.example.salesmanagement.document.SalesDocument;
import jakarta.persistence.Entity;
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

public class DocumentReportManager {
    private JasperPrint generateSalesDocumentReport(Document document) throws JRException {
        List<ReportItem> reportItems = new ArrayList<>();
        document.getItems().forEach(item -> {
            reportItems.add(new ReportItem(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPriceExcludingTaxes().setScale(2, RoundingMode.DOWN),
                    item.getTotalTaxes().multiply(BigDecimal.valueOf(10L)).toBigInteger().intValueExact(),
                    item.getTotalExcludingTaxes().setScale(2, RoundingMode.DOWN),
                    item.getTotalIncludingTaxes().setScale(2, RoundingMode.DOWN),
                    item.getTotalTaxes().setScale(2, RoundingMode.DOWN)));
        });

        JRBeanCollectionDataSource salesDocumentItemsDataSet = new JRBeanCollectionDataSource(reportItems);

        Map<ConfigKey, String> configurations = AppConfiguration.getInstance().getAllConfigurations();

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("document_type", switch (document.getClass().getSimpleName()) {
            case "PurchaseOrder" -> "Bon de commande";
            case "PurchaseDeliveryNote" -> "Bon de réception";
            case "Quotation" -> "Devis";
            case "DeliveryNote" -> "Bon de livraison";
            case "Invoice" -> "Facture";
            case "CreditInvoice" -> "Facture d'avoir";
            default -> throw new IllegalStateException("Unexpected value: " + document.getClass().getSimpleName());
        });

        parameters.put("id", document.getReference());
        parameters.put("issueDate", document.getIssueDate());

        parameters.put("client_name", (document instanceof SalesDocument salesDocument) ? salesDocument.getClient().getName() : ((PurchaseDocument) document).getSupplier().getName());
        parameters.put("client_address", (document instanceof SalesDocument salesDocument) ? salesDocument.getClient().getAddress() : ((PurchaseDocument) document).getSupplier().getAddress());
        parameters.put("client_ice", (document instanceof SalesDocument salesDocument) ? salesDocument.getClient().getCommonCompanyIdentifier() : ((PurchaseDocument) document).getSupplier().getCommonCompanyIdentifier());

        parameters.put("salesDocumentItemsDataSet", salesDocumentItemsDataSet);

        parameters.put("totalHT", document.getTotalExcludingTaxes());
        parameters.put("totalTaxes", document.getTotalTaxes());
        parameters.put("totalTTC", document.getTotalIncludingTaxes());

        parameters.put("print_sales_document_heading", Boolean.parseBoolean(configurations.get(ConfigKey.PRINT_SALES_DOCUMENT_HEADING)));
        parameters.put("companyName", configurations.get(ConfigKey.COMPANY_NAME));
        parameters.put("companyAddress", configurations.get(ConfigKey.BUSINESS_ADDRESS));
        parameters.put("rc", configurations.get(ConfigKey.COMMERCIAL_REGISTRATION_NUMBER));
        parameters.put("patent", configurations.get(ConfigKey.COMPANY_PATENT_NUMBER));
        parameters.put("if", configurations.get(ConfigKey.TAX_IDENTIFIER_NUMBER));
        parameters.put("ice", configurations.get(ConfigKey.COMMON_IDENTIFIER_NUMBER));
        parameters.put("phone_number", configurations.get(ConfigKey.COMPANY_PHONE_NUMBER));
        parameters.put("fixed_phone_number", configurations.get(ConfigKey.COMPANY_FIXED_PHONE_NUMBER));
        parameters.put("email", configurations.get(ConfigKey.COMPANY_EMAIL_ADDRESS));

        parameters.put("amountInWords", getMoneyIntoWords(document.getTotalIncludingTaxes().toString()));

        String filePath;

        if (document instanceof DeliveryNote) {
            filePath = "/templates/deliverNoteSalesDocumentReport_" + (
                    Boolean.parseBoolean(configurations.get(ConfigKey.PRINT_DELIVERY_NOTE_UNIT_PRICE)) ? "withPrice.jrxml" : "withoutPrice.jrxml");
        } else {
            filePath = "/templates/documentReport.jrxml";
        }

        JasperReport report = null;
        try (InputStream inputStream = DocumentReportManager.class.getResourceAsStream(filePath)) {
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

    public void displayDocumentReport(Document salesDocument) throws JRException {
        JasperViewer jasperViewer = new JasperViewer(generateSalesDocumentReport(salesDocument), false);
        jasperViewer.setVisible(true);
        jasperViewer.setTitle(salesDocument.getClass().getAnnotation(Entity.class).name() + (salesDocument.getReference() == null ? " - DRAFT" : " N° " + salesDocument.getReference()));
        jasperViewer.setFitPageZoomRatio();
        jasperViewer.toFront();
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
