package models;

import bootstrap.S3Plugin;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import play.Logger;
import services.MongoService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by Alvaro on 29/03/2015.
 */
@Document
public class GiftCard {

    @Id
    private String code;

    private String userIdFrom;
    private String userIdTo;


    private boolean used;
    private boolean active;

    private double price;

    public GiftCard(){

    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdDate = new Date();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date usedDate = new Date();

    public String getFormatedPrice(){
        Locale locale = new Locale ("pt", "BR");
        Locale.setDefault(locale);

        DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getCurrencyInstance(locale);

        return formatter.format(getPrice());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserIdFrom() {
        return userIdFrom;
    }

    public void setUserIdFrom(String userIdFrom) {
        this.userIdFrom = userIdFrom;
    }

    public String getUserIdTo() {
        return userIdTo;
    }

    public void setUserIdTo(String userIdTo) {
        this.userIdTo = userIdTo;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(Date usedDate) {
        this.usedDate = usedDate;
    }
}