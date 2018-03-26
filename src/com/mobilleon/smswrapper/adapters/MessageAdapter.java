package com.mobilleon.smswrapper.adapters;

import java.util.ArrayList;
import java.util.List;

import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.view.ViewHolder;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class MessageAdapter extends BaseAdapter implements Filterable {

    List<MyMessage> msgsList;
    List<MyMessage> mOriginalValues;
    List<String> mSenderValues;
    MyMessage message;
    LayoutInflater mInflater;
    boolean checkbox;
    List<MyMessage> mSelectedMessages = new ArrayList<MyMessage>();

    public MessageAdapter(Context context, List<MyMessage> msgList, boolean checkbox) {
        super();
        this.msgsList = msgList;
        this.checkbox = checkbox;
        mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Object item = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if(checkbox){
                convertView = mInflater.inflate(R.layout.list_item, null);  
                holder.mMessageSelector = (CheckBox) convertView.findViewById(R.id.msg_selector);
            }else{
                convertView = mInflater.inflate(R.layout.msg_list_item, null);  
            }
                
                holder.mMessageSender =  ((TextView) convertView.findViewById(R.id.sender));
                holder.mMessageDate =  ((TextView) convertView.findViewById(R.id.msg_date));
                holder.mMessage = ((TextView) convertView.findViewById(R.id.message));
                convertView.setTag(holder); 
        } else {  
                holder = (ViewHolder) convertView.getTag();
        }
        
        if(item instanceof MyMessage){
            message = msgsList.get(position);
            holder.mMessageSender.setText(String.format("%s", message.getSender()).toString().trim());
            //holder.mMessageDate.setText(String.format("%s", message.getdateObject().toGMTString()).toString());
            holder.mMessage.setText(Html.fromHtml(message.getTextContent()).toString().trim());
            if(checkbox){
                MessageSelectorListener msl = new MessageSelectorListener(message);
                holder.mMessageSelector.setOnCheckedChangeListener(msl);
            }
                
            
        }

        return convertView;      
    }

    @Override
    public int getCount() {
        return msgsList.size();
    }

    @Override
    public Object getItem(int position) {
        return msgsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<MyMessage> getSelectedMeassages() {
        ArrayList<MyMessage> selectedMessages = new ArrayList<MyMessage>();
        selectedMessages.addAll(mSelectedMessages);
        mSelectedMessages.clear();
        return selectedMessages;
    }
    
    private class MessageSelectorListener implements OnCheckedChangeListener {
        MyMessage message;
        public MessageSelectorListener(MyMessage message){
            super();
            this.message = message;
            
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                mSelectedMessages.add(message);
            }else{
               if(mSelectedMessages.contains(message))
                   mSelectedMessages.remove(message);
            }
        }
        
    }
    
    @Override
    public Filter getFilter() {
        /**
         * A filter object which will
         * filter message sender names
         * */
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                msgsList = (List<MyMessage>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values. Only filtered values will be shown on the list 
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation for publishing
                
                List<MyMessage> FilteredArrList = new ArrayList<MyMessage>();
                
                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<MyMessage>(msgsList); // saves the original data in mOriginalValues
                }
                
                if(mSenderValues == null){
                    mSenderValues = new ArrayList<String>();
                    for(MyMessage message : mOriginalValues){
                        mSenderValues.add(message.getSender());
                    }
                }

                /**
                 * 
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)  
                 *
                 **/
                
                if (constraint == null || constraint.length() == 0) {

                    /* CONTRACT FOR IMPLEMENTING FILTER : set the Original values to result which will be returned for publishing */
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    /* Do the filtering */
                    constraint = constraint.toString().toLowerCase();
                    
                    for (int i = 0; i < mSenderValues.size(); i++) {
                        String data = mSenderValues.get(i);
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(mOriginalValues.get(i));
                        }
                    }    
                    
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}
