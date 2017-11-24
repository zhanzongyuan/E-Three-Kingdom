package com.yalantis.euclid.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Oleksii Shliama on 1/27/15.
 */
public class EuclidListAdapter extends ArrayAdapter<Map<String, Object>> implements Filterable {

    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION_SHORT = "description_short";
    public static final String KEY_DESCRIPTION_FULL = "description_full";

    private final LayoutInflater mInflater;
    private List<Map<String, Object>> mData;
    private ArrayList<Map<String, Object>> mOriginalData;
    private final Object mLock = new Object();

    public EuclidListAdapter(Context context, int layoutResourceId, List<Map<String, Object>> data) {
        super(context, layoutResourceId, data);
        mData = data;
        mOriginalData = new ArrayList<>(data);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {

        return mData.size();
    }

    @Override
    public Map<String, Object> getItem(int position) {

        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= mData.size()) {
            return null;
        }
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mViewOverlay = convertView.findViewById(R.id.view_avatar_overlay);
            viewHolder.mListItemAvatar = (ImageView) convertView.findViewById(R.id.image_view_avatar);
            viewHolder.mListItemName = (TextView) convertView.findViewById(R.id.text_view_name);
            viewHolder.mListItemDescription = (TextView) convertView.findViewById(R.id.text_view_description);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        System.out.println("size = " + mData.size());
        System.out.println("position = " + position);

        if (mData.get(position) != null) {
            Uri uri = getImageUri(getContext(), (Bitmap) mData.get(position).get(KEY_AVATAR));
            Picasso.with(getContext()).load(uri)
                    .resize(EuclidActivity.sScreenWidth, EuclidActivity.sProfileImageHeight).centerCrop()
                    .placeholder(R.color.blue)
                    .into(viewHolder.mListItemAvatar);
            String description_short=mData.get(position).get(KEY_DESCRIPTION_SHORT).toString().replace("\\n", "\n");
            viewHolder.mListItemName.setText(mData.get(position).get(KEY_NAME).toString().toUpperCase());
            viewHolder.mListItemDescription.setText(description_short);
            viewHolder.mViewOverlay.setBackground(EuclidActivity.sOverlayShape);
        }

        System.out.println("getView ok");
        return convertView;
    }

    static class ViewHolder {
        View mViewOverlay;
        ImageView mListItemAvatar;
        TextView mListItemName;
        TextView mListItemDescription;

    }

    //下面为添加搜索功能的过滤器实现

    private ArrayFilter mFilter;

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalData == null) {
                synchronized (mLock) {
                    mOriginalData = new ArrayList<>(mData);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, Object>> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalData);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                ArrayList<Map<String, Object>> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalData);
                }

                final int count = values.size();
                ArrayList<Map<String, Object>> newValues = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    final Map<String, Object> value = values.get(i);
                    final String valueText = value.get(KEY_NAME).toString().toLowerCase();
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        for (int j = 0; j < wordCount; j++) {
                            if (words[j].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mData = (List<Map<String, Object>>) results.values;
            System.out.println("Data changed");
            System.out.println("Data size = " + mData.size());
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public void removeData(String name) {
        for (int i = 0; i < mOriginalData.size(); i++) {
            if (mOriginalData.get(i).get(KEY_NAME).equals(name)) {
                mOriginalData.remove(i);
                System.out.println("remove succeed!");
                break;
            }
        }
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).get(KEY_NAME).equals(name)) {
                mData.remove(i);
                break;
            }
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, null, null));
        return uri;
    }
}
