package com.example.gestioncommercial.report;


import com.example.gestioncommercial.configuration.AppConfiguration;
import com.example.gestioncommercial.configuration.ConfigKey;
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

        Map<ConfigKey, String> configurations = AppConfiguration.getInstance().getAllConfigurations();

        //SIEGE SOCIEL: CENTRE RIAD NR 61 ANGLE AV LALLA YACOUT & MOSTAPHA EL MAANI, ETG 2 NR 69, CASABLANCA.
        //R. C. N° 396931 | Patente N° 32285441 | I.F.N°25056198 | ICE N° 002033875000082
        //Tél: 06.45.22.39.07 | Fix :08.08.61.19.46 | E-mail: ibsyssarl@gmail.com

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", invoice.getReference());
        parameters.put("issueDate", invoice.getIssueDate());

        parameters.put("name", invoice.getClient().getName());
        parameters.put("address", invoice.getClient().getAddress());
        parameters.put("client_ice", invoice.getClient().getCommonCompanyIdentifier());

        parameters.put("invoiceItemsDataSet", invoiceItemsDataSet);

        parameters.put("totalHT", invoice.getTotalExcludingTaxes());
        parameters.put("totalTaxes", invoice.getTotalTaxes());
        parameters.put("totalTTC", invoice.getTotalIncludingTaxes());

        parameters.put("companyName", configurations.get(ConfigKey.COMPANY_NAME));
        parameters.put("companyAddress", configurations.get(ConfigKey.BUSINESS_ADDRESS));
        parameters.put("rc",     "R.C. N° " + configurations.get(ConfigKey.COMMERCIAL_REGISTRATION_NUMBER));
        parameters.put("patent", "Patente N° " + configurations.get(ConfigKey.COMPANY_PATENT_NUMBER));
        parameters.put("if",     "I.F. N° " + configurations.get(ConfigKey.TAX_IDENTIFIER_NUMBER));
        parameters.put("ice",    "I.C.E. N° " + configurations.get(ConfigKey.COMMON_IDENTIFIER_NUMBER));
        parameters.put("tel",   "Téléphone : " + configurations.get(ConfigKey.COMPANY_PHONE_NUMBER));
        parameters.put("fax",   "Fax :  " + configurations.get(ConfigKey.COMPANY_FIXED_PHONE_NUMBER));
        parameters.put("email", "E-mail : " + configurations.get(ConfigKey.COMPANY_EMAIL_ADDRESS));

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
