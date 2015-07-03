package models;

import bootstrap.S3Plugin;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import play.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alvaro on 18/03/2015.
 */
@Document()
public class SiteContent {

    @Id
    private Utils.SiteContent type;
    private String title;
    private String content;

    private boolean visible;

    private List<Image> images;


//only used on Home content
    private String email;
    private String facebook;
    private String twitter;
    private String gPlus;
    private String instagram;


    


    public SiteContent(){
        this.images = new ArrayList<>();
    }

    @Transient
    private List<File> imagesFiles;

    public Utils.SiteContent getType(){
        return this.type;
    }
    public String getTitle(){
        return this.title;
    }
    public String getContent(){
        return this.content;
    }
    public boolean isVisible(){
        return this.visible;
    } 
    public List<Image> getImages(){
        return this.images;
    }


    public String getEmail(){
        return this.email;
    }
    public String getFacebook(){
        return this.facebook;
    }
    public String getTwitter(){
        return this.twitter;
    }
    public String getGPlus(){
        return this.gPlus;
    }
    public String getInstagram(){
        return this.instagram;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public void setFacebook(String facebook){
        this.facebook = facebook;
    }
    public void setTwitter(String twitter){
        this.twitter = twitter;
    }
    public void setGPlus(String gPlus){
        this.gPlus = gPlus;
    }
    public void setInstagram(String instagram){
        this.instagram = instagram;
    }
    public void setImages(List<Image> images){
        this.images = images;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setVisible(boolean visible ){
        this.visible = visible;
    } 
    public void setType(Utils.SiteContent type){
        this.type = type;
    }

 

}
