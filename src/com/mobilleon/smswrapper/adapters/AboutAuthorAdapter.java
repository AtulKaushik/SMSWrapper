package com.mobilleon.smswrapper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobilleon.smswrapper.model.AboutAppAuthorItem;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.view.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class AboutAuthorAdapter extends BaseAdapter{

    private List<AboutAppAuthorItem> aboutItems = new ArrayList<AboutAppAuthorItem>();
    private AboutAppAuthorItem mAboutItem;
    private LayoutInflater mInflater;

    public AboutAuthorAdapter(Context context, List<AboutAppAuthorItem> aboutItems) {
        super();
        this.aboutItems = aboutItems;
        mInflater = LayoutInflater.from(context);
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Object item = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.about_author_list_item, null);   
            holder.mAboutItemHeader =  ((TextView) convertView.findViewById(R.id.about_auth_item_heading));
            holder.mImageView = ((ImageView) convertView.findViewById(R.id.about_auth_item_icon));
        
                convertView.setTag(holder); 
        } else {  
                holder = (ViewHolder) convertView.getTag();
        }
        
        if(item instanceof AboutAppAuthorItem){
            mAboutItem = aboutItems.get(position);
            holder.mAboutItemHeader.setText(String.format("%s", mAboutItem.getmItemHeader()).toString().trim());
            holder.mImageView.setImageDrawable(mAboutItem.getmImageIcon());
            if(mAboutItem.getmItemDescription() != null)
            holder.mAboutItemDescription.setText(String.format("%s", mAboutItem.getmItemDescription()).toString().trim());
        }
        return convertView;      
    }

    @Override
    public int getCount() {
        return aboutItems.size();
    }

    @Override
    public Object getItem(int position) {
        return aboutItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
