package models;


import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alvaro on 18/03/2015.
 */
public class UserInteration {


    private String value;

    private double points;

    private String interation;

    private String imageUrl;

}
