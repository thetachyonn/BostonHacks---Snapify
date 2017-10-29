package com.sporoc.snapbuddy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rriskhh on 28/10/17.
 */
public class CustomListAdapter extends BaseAdapter {

    private Activity activity;
    private List contacts;
    private static LayoutInflater inflater=null;
    private ContactListModel tempValues=null;

    public CustomListAdapter(Activity activity, List contacts) {

        /********** Take passed values **********/
        this.activity = activity;
        this.contacts = contacts;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(contacts == null){
            return 0;
        }
        return contacts.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{

        public TextView name;
        public TextView phone;
        public CheckBox contactSelected;
    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.list_item, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.name = (TextView) vi.findViewById(R.id.name);
            holder.phone = (TextView) vi.findViewById(R.id.phone);
            holder.contactSelected = (CheckBox) vi.findViewById(R.id.contactSelected);

            /************  Set holder with LayoutInflater ************/
            tempValues = (ContactListModel)contacts.get(position);
            holder.name.setText(tempValues.getName());
            holder.phone.setText(tempValues.getPhone());
            if(tempValues.isSelected()){
                holder.contactSelected.setSelected(true);
            }

        }
        return vi;
    }
}
