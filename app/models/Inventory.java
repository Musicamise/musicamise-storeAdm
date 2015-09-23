package models;

import bootstrap.S3Plugin;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import play.Logger;
import services.MongoService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by Alvaro on 29/03/2015.
 */
@Document
public class Inventory {

    @Id
    private String sku;

    @DBRef(lazy = true)
    private Product product;

    private String type;

    private int quantity;

    private String size;

    private String color;

    private String genderSlug;

    private boolean orderOutOfStock;

    private boolean sellInOutOfStock;

    private double priceWithQuantity;

    private String priceWithQuantityFormatted;

    public String toString(){
        return "sku "+this.sku+" quantity "+this.quantity+" size "+this.size+" color "+this.color+" type "+this.type+" genderSlug "
                +this.genderSlug+" priceWithQuantityFormatted "+this.priceWithQuantityFormatted
                +" Price "+this.product.getPriceFormatted()
                +" title "+this.product.getTitle()+" description "+this.product.getDescription();
    }
    public String getName(){
        return ""+this.sku +"-"+this.product.getSlug()+"-"+this.size+ "-"+this.type+"-"
                +this.genderSlug;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

     public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPriceWithQuantity() {
        return priceWithQuantity;
    }

    public void setPriceWithQuantity(double priceWithQuantity) {
        this.priceWithQuantity = priceWithQuantity;
        this.priceWithQuantityFormatted = this.formatValues(priceWithQuantity);
    }


    public String getPriceWithQuantityFormatted() {
        return priceWithQuantityFormatted;
    }

    public void setPriceWithQuantityFormatted(String priceWithQuantityFormatted) {
        this.priceWithQuantityFormatted = priceWithQuantityFormatted;
    }

    public Inventory(){
        this.product = new Product();
    }

    public Inventory(Inventory inventory){
        this.sku = inventory.getSku();
        this.product = inventory.getProduct();
        this.quantity = inventory.getQuantity();
        this.size = inventory.getSize();
        this.genderSlug = inventory.getGenderSlug();
        this.orderOutOfStock = inventory.isOrderOutOfStock();
        this.sellInOutOfStock = inventory.isSellInOutOfStock();
    }

    public boolean isSellInOutOfStock() {
        return sellInOutOfStock;
    }

    public void setSellInOutOfStock(boolean sellInOutOfStock) {
        this.sellInOutOfStock = sellInOutOfStock;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public String getGenderSlug() {
        return genderSlug;
    }

    public void setGenderSlug(String genderSlug) {
        this.genderSlug = genderSlug;
    }

    public boolean isOrderOutOfStock() {
        return orderOutOfStock;
    }

    public void setOrderOutOfStock(boolean orderOutOfStock) {
        this.orderOutOfStock = orderOutOfStock;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int hashCode()
    {
        return sku.hashCode();
    }

    public boolean equals(Object obj) 
    {
        return (((Inventory)this).getSku().equals(((Inventory)obj).getSku()));
    }

    public String formatValues(double price){
        Locale locale = new Locale ("pt", "BR");
        Locale.setDefault(locale);

        DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getCurrencyInstance(locale);

        return formatter.format(price);
    }
}
