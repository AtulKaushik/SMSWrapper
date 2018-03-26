/**
 * A model class for
 * About the app/author
 * item types
 * */

package com.mobilleon.smswrapper.model;

import android.graphics.drawable.Drawable;


/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class AboutAppAuthorItem {
    
    public AboutAppAuthorItem(){
        super();
    }
    
    private Drawable mImageIcon;
    private String mItemHeader;
    private String mItemDescription;
    private ABOUT_ITEM_TYPE mAboutType;
    
    public Drawable getmImageIcon() {
        return mImageIcon;
    }
    public String getmItemHeader() {
        return mItemHeader;
    }
    public String getmItemDescription() {
        return mItemDescription;
    }
    public void setmImageIcon(Drawable mImageIcon) {
        this.mImageIcon = mImageIcon;
    }
    public void setmItemHeader(String mItemHeader) {
        this.mItemHeader = mItemHeader;
    }
    public void setmItemDescription(String mItemDescription) {
        this.mItemDescription = mItemDescription;
    }
    
    public ABOUT_ITEM_TYPE getmAboutType() {
        return mAboutType;
    }
    public void setmAboutType(ABOUT_ITEM_TYPE mAboutType) {
        this.mAboutType = mAboutType;
    }

    /**
     * enum for about
     * the app/author
     * item/request types
     * */
    public static enum ABOUT_ITEM_TYPE {
        SHARE_APP("share_app"), RATE_APP("rate_app"),USER_FEEDBACK("user_feedback"), 
        FIND_FACEBOOK("find_facebbok"), FOLLOW_TWITTER("follow_twitter"), CIRCLE_GOOGLE_PLUS("circle_google_plus");
        
        private final String about_type;
        
        private ABOUT_ITEM_TYPE(String about_type) {
                this.about_type = about_type;
        }
        
        public String getAboutType() {
                return about_type;
        }
}
    
}
