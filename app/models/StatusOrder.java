package models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alvaro on 29/03/2015.
 */
public class StatusOrder {


    private Utils.StatusCompra status;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date createdDate = new Date();


    public StatusOrder(){
    }

    public Utils.StatusCompra getStatus() {
        return status;
    }

    public void setStatus(Utils.StatusCompra status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public String toString(){
        return this.status.name() + "-" + this.createdDate.toString();
    }
}
