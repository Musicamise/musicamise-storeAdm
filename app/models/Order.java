package models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
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
    private  String friendlyId;
    
    @Field("products")
    private List<InventoryOrder> products ;

    private PagSeguroInfo pagSeguroInfo;
    private String notes;

    private String email;

    private UserInner user;
    private List<EmailSent> emailSents;

    @Field("shippingAddress")
    private Address shippingAddress;

    private double total;
    private String totalFormatted;

    private GiftCard giftCard;

    private DiscountCode discountCode;

    private List<StatusOrder> status;

    private String statusEntrega;
    private String statusCompra;

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
        this.emailSents = new ArrayList<>();
    }

    public void addStatus(StatusOrder status){
        this.status.add(status);
        this.statusCompra = status.getStatus().name();
    }

    public String toString(){
        String shippingAddress = "";
        if(this.shippingAddress!=null)
            shippingAddress = " shippingAddress " + this.shippingAddress.toString();
        String user = "";
        if(this.user!=null){
            user = this.user.toString();
        }else{
            user = this.email;
        }
        return "products: " + this.products.toString() +  shippingAddress
                + " user "+ user;

    }

    public StatusOrder getLastStatus(){
        if(this.status.size()>0){
            return this.status.get(this.status.size()-1);
        }else{
            return null;
        }
    }

    public boolean isAbleToUpdateInventory(StatusOrder status){
        this.status.remove(status);
        if(status.getStatus().equals(Utils.StatusCompra.PAGO)){
            if(this.status.size()>0){
                boolean canUpdate = true;
                for(StatusOrder statusOrder:this.status){
                    if(statusOrder.getStatus().equals(Utils.StatusCompra.PAGO)){
                        canUpdate = false;
                    }else if(statusOrder.getStatus().equals(Utils.StatusCompra.CANCELADO)||statusOrder.getStatus().equals(Utils.StatusCompra.DEVOLVIDA)){
                        canUpdate = true;
                    }
                }
                return canUpdate;
            }else{
                return true;
            }
        }else{
            if(this.status.size()>0){
                boolean canUpdate = false;
                for(StatusOrder statusOrder:this.status){
                    if(statusOrder.getStatus().equals(Utils.StatusCompra.PAGO)){
                        canUpdate = true;
                    }else if(statusOrder.getStatus().equals(Utils.StatusCompra.CANCELADO)||statusOrder.getStatus().equals(Utils.StatusCompra.DEVOLVIDA)){
                        canUpdate = false;
                    }
                }
                return canUpdate;
            }else{
                return false;
            }
        }

    }

    public String formatValues(double price){
        Locale locale = new Locale ("pt", "BR");
        Locale.setDefault(locale);

        DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getCurrencyInstance(locale);

        return formatter.format(price);
    }
    
    public String getFriendlyId() {
        return friendlyId;
    }

    public void setFriendlyId(String friendlyId) {
        this.friendlyId = friendlyId;
    }
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        this.giftCardValueFormatted = this.formatValues(giftCardValue);
    }  


    public String getGiftCardValueFormatted() {
        return giftCardValueFormatted;
    }

    public void setGiftCardValueFormatted(String giftCardValueFormatted) {
        this.giftCardValueFormatted = giftCardValueFormatted;
    }

    public double getTotalValueItems() {
        return totalValueItems;
    }

    public void setTotalValueItems(double totalValueItems) {
        this.totalValueItems = totalValueItems;
        this.totalValueItemsFormatted = this.formatValues(totalValueItems);
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
        this.totalShippingFormatted = this.formatValues(totalShipping);
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
        this.totalDiscountFormatted = this.formatValues(totalDiscount);
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

    public List<InventoryOrder> getProducts() {
        return products;
    }

    public void setProducts(List<InventoryOrder> products) {
        this.products = products;
    }

    public PagSeguroInfo getPagSeguroInfo() {
        return pagSeguroInfo;
    }

    public void setPagSeguroInfo(PagSeguroInfo pagSeguroInfo) {
        this.pagSeguroInfo = pagSeguroInfo;
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
        if(user!=null) {
            this.user = new UserInner();
            this.user.email = user.getEmail();
            this.user.id = user.getId();
            this.user.address = user.getAddress();
            this.user.displayName = user.getDisplayName();
            this.user.fullName = user.getFullName();
            this.user.firstName = user.getFirstName();
            this.user.lastName = user.getLastName();
        }
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
        this.totalFormatted = this.formatValues(total);
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

    public String getStatusCompra() {
        return this.statusCompra;
    }

    public void setStatusCompra(String statusCompra) {
        this.statusCompra = statusCompra;
    }
     public String getStatusEntrega() {
        return this.statusEntrega;
    }

    public void setStatusEntrega(String statusEntrega) {
        this.statusEntrega = statusEntrega;
    }

    public List<EmailSent> getEmailSents() {
        return emailSents;
    }

    public void setEmailSents(List<EmailSent> emailSents) {
        this.emailSents = emailSents;
    }

    @Document
    public class InventoryOrder {

        @Id
        private String sku;

        private Product product;

        private int quantity;

        private String size;
        private String type;
        private String color;

        private String genderSlug;

        private boolean orderOutOfStock;

        private boolean sellInOutOfStock;

        private double priceWithQuantity;

        private String priceWithQuantityFormatted;


        public String toString(){
            return "sku "+this.sku+" quantity "+this.quantity+" size "+this.size+" genderSlug "
                    +this.genderSlug+" priceWithQuantityFormatted "+this.priceWithQuantityFormatted
                    +" Price "+this.product.getPriceFormatted()+" color "+this.getColor()+" model "+this.getType()
                    +" title "+this.product.getTitle()+" description "+this.product.getDescription();
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



        public void cloneInventory(Inventory inventory){
            this.sku = inventory.getSku();
            this.product = inventory.getProduct();
            this.quantity = inventory.getQuantity();
            this.size = inventory.getSize();
            this.type = inventory.getType();
            this.color = inventory.getColor();
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
            return (((InventoryOrder)this).getSku().equals(((InventoryOrder)obj).getSku()));
        }

        public String formatValues(double price){
            Locale locale = new Locale ("pt", "BR");
            Locale.setDefault(locale);

            DecimalFormat formatter =
                    (DecimalFormat) NumberFormat.getCurrencyInstance(locale);

            return formatter.format(price);
        }
    }
    @Document
    public class EmailSent {

        private Utils.StatusCompra status;
        private Utils.StatusEntrega statusEntrega;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date createdDate = new Date();

        public String toString(){
            return " status "+this.status.name() + " Date " + this.createdDate ;
        }

        public EmailSent(){
        }
         public Utils.StatusEntrega getStatusEntrega() {
            return statusEntrega;
        }

        public void setStatusEntrega(Utils.StatusEntrega statusEntrega) {
            if(statusEntrega!=null)
                this.statusEntrega = statusEntrega;
            else
                this.statusEntrega = Utils.StatusEntrega.SEMSTATUS;
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

        public String toString(){
            return " email "+this.email + " firstName " + this.firstName +
                    " lastName " + this.lastName;
        }

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
    @Document
    public class PagSeguroInfo {

        private String code;
        private String reference;
        private String type;

        private String cancellationSource;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date date = new Date();

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date lastEventDate = new Date();

        private String paymentMethodType;
        private String paymentMethodCode;
        private String grossAmount;
        private String discountAmount;
        private String netAmount;

        private Date escrowEndDate;
        private String extraAmount;
        private String installmentCount;

        private String creditorFees;

        private String installmentFeeAmount;

        private String operationalFeeAmount;

        private String intermediationRateAmount;

        private String intermediationFeeAmount;

        private String senderEmail;
        private String senderName;
        private String senderNumber;
        private String senderAreaCode;



        public PagSeguroInfo(){
        }

        public String toString(){
            return "code "+this.code +"- type- "+this.type +"- cancellationSource "+this.cancellationSource +
                    "- date "+this.date +"- lastEventDate "+this.lastEventDate
                    +"- paymentMethodType "+this.paymentMethodType +"- paymentMethodCode "+this.paymentMethodCode +
                    "- grossAmount "+this.grossAmount+"- discountAmount "+this.discountAmount+
                    "- netAmount "+this.netAmount+"- extraAmount "+this.extraAmount+"- installmentCount "+this.installmentCount+
                    "- installmentFeeAmount "+this.installmentFeeAmount+
                    "- operationalFeeAmount "+this.operationalFeeAmount+"- intermediationRateAmount "+this.intermediationRateAmount+
                    "- intermediationFeeAmount "+this.intermediationFeeAmount+
                    "- installmentFeeAmount "+this.installmentFeeAmount+
                    "- senderEmail "+this.senderEmail+"- senderName "+this.senderName+"- senderNumber "+this.senderNumber+"- senderAreaCode "+this.senderAreaCode;

        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCancellationSource() {
            return cancellationSource;
        }

        public void setCancellationSource(String cancellationSource) {
            this.cancellationSource = cancellationSource;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Date getLastEventDate() {
            return lastEventDate;
        }

        public void setLastEventDate(Date lastEventDate) {
            this.lastEventDate = lastEventDate;
        }

        public String getPaymentMethodType() {
            return paymentMethodType;
        }

        public void setPaymentMethodType(String paymentMethodType) {
            this.paymentMethodType = paymentMethodType;
        }

        public String getPaymentMethodCode() {
            return paymentMethodCode;
        }

        public void setPaymentMethodCode(String paymentMethodCode) {
            this.paymentMethodCode = paymentMethodCode;
        }

        public String getGrossAmount() {
            return grossAmount;
        }

        public void setGrossAmount(String grossAmount) {
            this.grossAmount = grossAmount;
        }

        public String getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(String discountAmount) {
            this.discountAmount = discountAmount;
        }

        public String getNetAmount() {
            return netAmount;
        }

        public void setNetAmount(String netAmount) {
            this.netAmount = netAmount;
        }

        public Date getEscrowEndDate() {
            return escrowEndDate;
        }

        public void setEscrowEndDate(Date escrowEndDate) {
            this.escrowEndDate = escrowEndDate;
        }

        public String getExtraAmount() {
            return extraAmount;
        }

        public void setExtraAmount(String extraAmount) {
            this.extraAmount = extraAmount;
        }

        public String getInstallmentCount() {
            return installmentCount;
        }

        public void setInstallmentCount(String installmentCount) {
            this.installmentCount = installmentCount;
        }

        public String getCreditorFees() {
            return creditorFees;
        }

        public void setCreditorFees(String creditorFees) {
            this.creditorFees = creditorFees;
        }

        public String getInstallmentFeeAmount() {
            return installmentFeeAmount;
        }

        public void setInstallmentFeeAmount(String installmentFeeAmount) {
            this.installmentFeeAmount = installmentFeeAmount;
        }

        public String getOperationalFeeAmount() {
            return operationalFeeAmount;
        }

        public void setOperationalFeeAmount(String operationalFeeAmount) {
            this.operationalFeeAmount = operationalFeeAmount;
        }

        public String getIntermediationRateAmount() {
            return intermediationRateAmount;
        }

        public void setIntermediationRateAmount(String intermediationRateAmount) {
            this.intermediationRateAmount = intermediationRateAmount;
        }

        public String getIntermediationFeeAmount() {
            return intermediationFeeAmount;
        }

        public void setIntermediationFeeAmount(String intermediationFeeAmount) {
            this.intermediationFeeAmount = intermediationFeeAmount;
        }

        public String getSenderEmail() {
            return senderEmail;
        }

        public void setSenderEmail(String senderEmail) {
            this.senderEmail = senderEmail;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getSenderNumber() {
            return senderNumber;
        }

        public void setSenderNumber(String senderNumber) {
            this.senderNumber = senderNumber;
        }

        public String getSenderAreaCode() {
            return senderAreaCode;
        }

        public void setSenderAreaCode(String senderAreaCode) {
            this.senderAreaCode = senderAreaCode;
        }
    }
}



