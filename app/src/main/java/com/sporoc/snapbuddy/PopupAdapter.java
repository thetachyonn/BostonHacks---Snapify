package com.sporoc.snapbuddy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rriskhh on 28/10/17.
 */
public class PopupAdapter  extends BaseAdapter {

    private Activity activity;
    private List keywords;
    private static LayoutInflater inflater=null;
    private ContactListModel tempValues=null;

    public PopupAdapter(Activity activity, List keywords) {

        /********** Take passed values **********/
        this.activity = activity;
        this.keywords = keywords;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(keywords == null){
            return 0;
        }
        return keywords.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{

        public TextView name;
        public Button delete;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder holder;

        if(convertView == null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            final View vi = inflater.inflate(R.layout.keyword_list, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.name = (TextView) vi.findViewById(R.id.keyword_name);
            holder.delete = (Button) vi.findViewById(R.id.delete_keyword);


            /************  Set holder with LayoutInflater ************/
            holder.name.setText(keywords.get(position).toString());
            holder.delete.setTag(position);

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer index = (Integer) v.getTag();
                    MainActivity.keywords.remove(index.intValue());
                    MainActivity.adapter.notifyDataSetChanged();
                    //notifyDataSetChanged();
                }
            });
            convertView = vi;
        }


        return convertView;
    }
}
