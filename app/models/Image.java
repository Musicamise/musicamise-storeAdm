package models;

import bootstrap.S3Plugin;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tinify.Options;
import com.tinify.Source;
import com.tinify.Tinify;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import play.Logger;
import play.Play;
import services.MongoService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Alvaro on 29/03/2015.
 */
public class Image {


    private  String name;
    private boolean frontImage;
    private boolean promotion;

    
    private String size;

    private String url;

    private String subtitle;
    private String redirectUrl;


    @Transient
    private File imageFile;

    public URL getUrl() throws MalformedURLException {
        return new URL("https://s3-sa-east-1.amazonaws.com/" + S3Plugin.s3Bucket + "/" + this.name);
    }

    public void saveFile(){
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            if(Play.application().configuration().getString("tiny.use").equals("true")){

                try {
                    Tinify.setKey(Play.application().configuration().getString("tiny.png"));
                    Tinify.validate();
                    Source source = Tinify.fromFile(imageFile.getCanonicalPath());

                    Options options = new Options()
                            .with("service", "s3")
                            .with("aws_access_key_id", Play.application().configuration().getString(S3Plugin.AWS_ACCESS_KEY)  )
                            .with("aws_secret_access_key", Play.application().configuration().getString(S3Plugin.AWS_SECRET_KEY) )
                            .with("region", "sa-east-1")
                            .with("path", S3Plugin.s3Bucket + "/" + this.name);

                    source.store(options);

                } catch(java.lang.Exception e) {
                    // Validation of API key failed.
                    Logger.error("error on tinypng "+ e.getMessage());
                }
            }else{
                PutObjectRequest putObjectRequest = new PutObjectRequest(S3Plugin.s3Bucket, name, imageFile);
                putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
                S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
            }

            try{

                this.setUrl(getUrl().toString());
            }catch(Exception e){

            }

        }
    }

    public void deleteFile() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(S3Plugin.s3Bucket, name);
        }
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");


    public void setName(String name){
        String nowhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        // String slug = NONLATIN.matcher(normalized).replaceAll("");
        String slug = nowhitespace;
        slug =  slug.toLowerCase(Locale.ENGLISH);
        this.name = slug;

    }

    public boolean isFrontImage() {
        return frontImage;
    }

    public void setFrontImage(boolean frontImage) {
        this.frontImage = frontImage;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public void setPromotion(boolean promotion) {
        this.promotion = promotion;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}
