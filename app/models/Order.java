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
@Document
public class Order {

    @Id
    private  String id;

    private List<Inventory> products ;

    private String idCompraPageseguro;

    private String email;

    private User user;

    private Address shippingAddress;

    private double total;
    private String totalFormatted;

    private GiftCard giftCard;

    private DiscountCode discountCode;

    private List<StatusOrder> status;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date createdDate = new Date();

    private double totalValueItems;
    private String totalValueItemsFormatted;

    private double totalShipping;
    private String totalShippingFormatted;

    private double totalDiscount;
    private String totalDiscountFormatted;

    private double giftCardValue;
    private String giftCardValueFormatted;

    public Order(){
        this.products = new ArrayList<>();
        this.status = new ArrayList<>();
    }


    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }


    public String getTotalFormatted() {
        return totalFormatted;
    }

    public void setTotalFormatted(String totalFormatted) {
        this.totalFormatted = totalFormatted;
    }


    public double getGiftCardValue() {
        return giftCardValue;
    }

    public void setGiftCardValue(double giftCardValue) {
        this.giftCardValue = giftCardValue;
    }  


    public String getGiftCardValueFormatted() {
        return totalValueItemsFormatted;
    }

    public void setGiftCardValueFormatted(String totalValueItemsFormatted) {
        this.totalValueItemsFormatted = totalValueItemsFormatted;
    }

    public double getTotalValueItems() {
        return totalValueItems;
    }

    public void setTotalValueItems(double totalValueItems) {
        this.totalValueItems = totalValueItems;
    }  


    public String getTotalValueItemsFormatted() {
        return totalValueItemsFormatted;
    }

    public void setTotalValueItemsFormatted(String totalValueItemsFormatted) {
        this.totalValueItemsFormatted = totalValueItemsFormatted;
    }

    public double getTotalShipping() {
        return totalShipping;
    }

    public void setTotalShipping(double totalShipping) {
        this.totalShipping = totalShipping;
    }  


    public String getTotalShippingFormatted() {
        return totalShippingFormatted;
    }

    public void setTotalShippingFormatted(String totalShippingFormatted) {
        this.totalShippingFormatted = totalShippingFormatted;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }  


    public String getTotalDiscountFormatted() {
        return totalDiscountFormatted;
    }

    public void setTotalDiscountFormatted(String totalDiscountFormatted) {
        this.totalDiscountFormatted = totalDiscountFormatted;
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Inventory> getProducts() {
        return products;
    }

    public void setProducts(List<Inventory> products) {
        this.products = products;
    }

    public String getIdCompraPageseguro() {
        return idCompraPageseguro;
    }

    public void setIdCompra(String idCompraPageseguro) {
        this.idCompraPageseguro = idCompraPageseguro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public DiscountCode getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(DiscountCode discountCode) {
        this.discountCode = discountCode;
    }

    public List<StatusOrder> getStatus() {
        return status;
    }

    public void setStatus(List<StatusOrder> status) {
        this.status = status;
    }

   
}
