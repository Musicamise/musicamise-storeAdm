package models;

import bootstrap.S3Plugin;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.*;
import play.Logger;
import services.MongoService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * Created by Alvaro on 29/03/2015.
 */
@Document
public class Product {

    @Id
    private String id;

    @TextIndexed(weight = 2)
    private  String title;
    @TextIndexed
    private  String description;

    private  double price;
    private  double priceCompareWith;

    private String priceFormatted;
    private String priceOldFormatted;

    private List<Image> images;

    @TextIndexed
    private String slug;

    private boolean newProduct;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date createdDate = new Date();

    private double weight;

    private boolean onLineVisible;
    private boolean storeVisible;
    private boolean sendMail;

    // private boolean hasDiscount;
    // private double discount;
    // private Utils.DiscountType discountType;


    //Organization
    @TextIndexed
    private Set<String> types;

    @TextIndexed
    private Set<String> userTags;
    @TextIndexed
    private Set<String> collectionsSlugs;


    private Set<String> localStoresSlugs;


    @DBRef(lazy = true)
    private Set<Inventory> inventories;


    public Product(){
        this.userTags = new HashSet<>();
        this.types = new HashSet<>();
        this.localStoresSlugs = new HashSet<>();
        this.images = new ArrayList<>();
        this.collectionsSlugs = new HashSet<>();
        this.inventories = new HashSet<>();
    }

    // public boolean isHasDiscount() {
    //     return hasDiscount;
    // }

    // public void setHasDiscount(boolean hasDiscount) {
    //     this.hasDiscount = hasDiscount;
    // }
    
    // public double getDiscount() {
    //     return discount;
    // }
    // public void setDiscount(double discount) {
    //     this.discount = discount;
    // }

    // public Utils.DiscountType getDiscountType() {
    //     return discountType;
    // }
    // public void setDiscountType(Utils.DiscountType discountType) {
    //     this.discountType = discountType;
    // }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        if (!id.equals(product.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Set<Inventory> getInventories() {
        return inventories;
    }

    public void setInventories(Set<Inventory> inventories) {
        this.inventories = inventories;
    }

    public Set<String> getCollectionsSlugs() {
        return collectionsSlugs;
    }

    public void setCollectionsSlugs(Set<String> collectionsSlugs) {
        this.collectionsSlugs = collectionsSlugs;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isStoreVisible() {
        return storeVisible;
    }

    public void setStoreVisible(boolean storeVisible) {
        this.storeVisible = storeVisible;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(!title.equals(this.title))
            setSlug(title);
        this.title = title;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.priceFormatted = this.getFormatedPrice(this.price);
    }

    public String getPriceFormatted() {
        return priceFormatted;
    }

    public void setPriceFormatted(String priceFormatted) {
        this.priceFormatted = priceFormatted;
    }

    public String getPriceOldFormatted() {
        return priceOldFormatted;
    }

    public void setPriceOldFormatted(String priceOldFormatted) {
        this.priceOldFormatted = priceOldFormatted;
    }

    public double getPriceCompareWith() {
        return priceCompareWith;
    }

    public void setPriceCompareWith(double priceCompareWith) {
        this.priceCompareWith = priceCompareWith;
        this.priceOldFormatted = this.getFormatedPrice(this.priceCompareWith);

    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getSlug() {
        return slug;
    }


    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    public void setSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug =  slug.toLowerCase(Locale.ENGLISH);
        int count = MongoService.countProductWithSlug(slug);
        this.slug = slug+(count>0?(count+1):"");

    }

   

    public boolean isNewProduct() {
        return newProduct;
    }

    public void setNewProduct(boolean newProduct) {
        this.newProduct = newProduct;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isOnLineVisible() {
        return onLineVisible;
    }

    public void setOnLineVisible(boolean onLineVisible) {
        this.onLineVisible = onLineVisible;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(Set<String> userTags) {
        this.userTags = userTags;
    }

    public Set<String> getLocalStoresSlugs() {
        return localStoresSlugs;
    }

    public void setLocalStoresSlugs(Set<String> localStoresSlugs) {
        this.localStoresSlugs = localStoresSlugs;
    }
    public String getFormatedPrice(double price){
        Locale locale = new Locale ("pt", "BR");
        Locale.setDefault(locale);

        DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getCurrencyInstance(locale);

        return formatter.format(price);
    }

}
