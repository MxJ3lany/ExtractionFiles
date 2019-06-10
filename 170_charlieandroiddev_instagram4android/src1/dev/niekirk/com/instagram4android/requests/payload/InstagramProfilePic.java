package dev.niekirk.com.instagram4android.requests.payload;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by root on 08/06/17.
 */

@Getter
@Setter
@ToString(callSuper = true)
public class InstagramProfilePic implements Serializable {

    public String url;
    public int width;
    public int height;

}
