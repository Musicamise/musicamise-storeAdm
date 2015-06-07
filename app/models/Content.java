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
public class Content {

    @Id
    private Utils.Content type;
    private String title;
    private String content;

    private List<String> images;


//only used on Home content
    private String email;
    private String facebook;
    private String twitter;
    private String gPlus;
    private String instagram;


    @Transient
    private List<File> imagesFiles;

 

}
