package models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alvaro on 29/03/2015.
 */
@Document(collection = "order")
public class Order {

    @Id
    private  String id;

    private List<Inventory> products ;

    private String idCompraPageseguro;

    private String email;

    private UserInner user;

    @Field("shippingAddress")
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

    public void setIdCompraPageseguro(String idCompraPageseguro) {
        this.idCompraPageseguro = idCompraPageseguro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserInner getUser() {
        return user;
    }

    public void setUser(UserInner user) {
        this.user = user;
    }
    public void setUser(User user) {
        this.user = new UserInner();
        this.user.email = user.getEmail();
        this.user.id = user.getId();
        this.user.address = user.getAddress();
        this.user.displayName = user.getDisplayName();
        this.user.fullName = user.getFullName();
        this.user.firstName = user.getFirstName();
        this.user.lastName = user.getLastName();

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
    @Document
    public class UserInner {

        private String id;
        private String email;

        private String password;
        private String salt;

        private String firstName;
        private String lastName;
        private String fullName;
        private String displayName;

        // private GenderUser gender;
        private String gender;

        private boolean marketingEmail;
        private boolean manager;
        private boolean active;

        private String notes;

        private List<String> tags;

        private List<Address> address;

        //password or active
        private String token;

        private String fbId;

        //products slugs
        private List<String> wishList;

        private List<UserInteration> userInterations;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date createdDate = new Date();

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date updatedDate = new Date();

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date resetPasswordExpires = new Date();


        public UserInner(){
            this.address = new ArrayList<>();
            this.tags = new ArrayList<>();
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }


        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }


        public Date getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Date createdDate) {
            this.createdDate = createdDate;
        }


        public Date getResetPasswordExpires() {
            return resetPasswordExpires;
        }

        public void setResetPasswordExpires(Date resetPasswordExpires) {
            this.resetPasswordExpires = resetPasswordExpires;
        }



        public Date getUpdatedDate() {
            return updatedDate;
        }

        public void setUpdatedDate(Date updatedDate) {
            this.updatedDate = updatedDate;
        }


        public String getFirstCity(){
            if(getAddress()!=null&&getAddress().size()>0){
                return getAddress().get(0).getCity();
            }else return "";
        }
        public String getFirstAddress(){
            if(getAddress()!=null&&getAddress().size()>0){
                return getAddress().get(0).getAddress();
            }else return "";
        }

        public String getFullName(){
            return this.fullName;
        }

        public void setFullName(String fullName){
            this.fullName = fullName;
        }

        public void setPassWord(String pass) {
            String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());

            this.password = hashed;

        }

        public String getPassword() {
            return password;
        }

        public String getId(){
            return id;
        }
        public void setId(String id){
            this.id= id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public boolean isMarketingEmail() {
            return marketingEmail;
        }

        public void setMarketingEmail(boolean marketingEmail) {
            this.marketingEmail = marketingEmail;
        }

        public boolean isManager() {
            return manager;
        }

        public void setManager(boolean manager) {
            this.manager = manager;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<Address> getAddress() {
            return address;
        }

        public void setAddress(List<Address> address) {
            this.address = address;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }


    }
}



