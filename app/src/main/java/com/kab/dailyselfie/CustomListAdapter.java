package com.kab.dailyselfie;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity mContext;
    private final String[] mItemname;
    private final Bitmap[] mImgid;
    private static final String TAG = "DailySelfie";

    public CustomListAdapter(Activity context, String[] itemname, Bitmap[] imgid) {
        super(context, R.layout.mylist, itemname);
        this.mContext = context;
        this.mItemname = itemname;
        this.mImgid = imgid;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        txtTitle.setText(mItemname[position]);
        imageView.setImageBitmap(mImgid[position]);
        extratxt.setText("Description " + mItemname[position]);

        return rowView;

    };
}