package com.stackoverflow.camel.labs.sql.model;

import java.math.BigDecimal;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ";", generateHeaderColumns = true, crlf = "UNIX")
public class Model2 {

    public Model2() {
    }

    @DataField(pos = 1, columnName = "A_Liererant")
    private String vendor;
    @DataField(pos = 2, columnName = "F_EAN")
    private String ean;
    @DataField(pos = 3, columnName = "G_Lief. Artikelnummer")
    private String itemId;
    @DataField(pos = 4, columnName = "H_Menge")
    private BigDecimal quantity;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

}
