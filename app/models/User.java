package models;


import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alvaro on 18/03/2015.
 */
@Document
public class User {

    @Id
    private String id;

    private String email;

    private String password;
    private String firstName;
    private String lastName;

    private boolean mkt;
    private boolean manager;
    private boolean active;

    private String notes;
    private List<String> tags;

    private List<Address> address;

    //password or active
    private String token;

    private String fbId;

    private List<Product> wishList;

    private List<UserInteration> userInterations;

    public User(){
        this.address = new ArrayList<>();
        this.tags = new ArrayList<>();
    }
    public User(String firstName, String email, String password){
        if(email !=null && !email.equals("")){
            if(password!=null&&!password.equals("")){
                this.firstName = firstName;
                this.email = email;
                setPassWord(password);
                //generateToken();
            }
            this.address = new ArrayList<>();
            this.tags = new ArrayList<>();

        }
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
        return this.firstName+" "+ this.lastName;
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

    public boolean isMkt() {
        return mkt;
    }

    public void setMkt(boolean mkt) {
        this.mkt = mkt;
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
