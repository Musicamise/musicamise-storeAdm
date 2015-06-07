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
import java.text.*;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by Alvaro on 29/03/2015.
 */
@Document
public class DiscountCode {

    @Id
    private String code;

    private int timesLeft;
    private int timesUsed;

    private boolean noTimesLimits;

    private Utils.DiscountType type;

    private double valueOf;

    private Utils.DiscountValidation ordersValidation;

    private Utils.DiscountPaymentAdjust whereApply;

    private boolean noDateLimits;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date startDate = new Date();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date endDate = new Date();

    private boolean active;

    private double overValueOf;
    private List<String> products ;
    private List<String> collectionsId ;

    public DiscountCode(){

    }

    public String getDateRangeWithTime(){
        if(this.startDate!=null&&this.endDate!=null) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH);
            String startDate = format.format(this.startDate);
            String endDate = format.format(this.endDate);
            return startDate+" - "+endDate;
        }
        return "";
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

    public double getOverValueOf() {
        return overValueOf;
    }

    public void setOverValueOf(double overValueOf) {
        this.overValueOf = overValueOf;
    }

    public List<String> getCollectionsId() {
        return collectionsId;
    }

    public void setCollectionsId(List<String> collectionsId) {
        this.collectionsId = collectionsId;
    }

    public Utils.DiscountType getType() {
        return type;
    }

    public void setType(Utils.DiscountType type) {
        this.type = type;
    }

    public Utils.DiscountValidation getOrdersValidation() {
        return ordersValidation;
    }

    public void setOrdersValidation(Utils.DiscountValidation ordersValidation) {
        this.ordersValidation = ordersValidation;
    }

    public Utils.DiscountPaymentAdjust getWhereApply() {
        return whereApply;
    }

    public void setWhereApply(Utils.DiscountPaymentAdjust whereApply) {
        this.whereApply = whereApply;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTimesLeft() {
        return timesLeft;
    }

    public void setTimesLeft(int timesLeft) {
        this.timesLeft = timesLeft;
    }

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public boolean isNoTimesLimits() {
        return noTimesLimits;
    }

    public void setNoTimesLimits(boolean noTimesLimits) {
        this.noTimesLimits = noTimesLimits;
    }


    public double getValueOf() {
        return valueOf;
    }

    public void setValueOf(double valueOf) {
        this.valueOf = valueOf;
    }



    public boolean isNoDateLimits() {
        return noDateLimits;
    }

    public void setNoDateLimits(boolean noDateLimits) {
        this.noDateLimits = noDateLimits;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}