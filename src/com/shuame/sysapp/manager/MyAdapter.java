package com.shuame.sysapp.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appmanager.R;

public class MyAdapter extends BaseExpandableListAdapter {
	private OnClickListener mOnCheckBoxClickListener;
	private List<String> group_list;
	private Map<Integer, ArrayList<Item>> item_list;
	private Context context;
	private static final int DATA_TAG_ID = 0x7f01001;

	public MyAdapter(Context context, OnClickListener onCheckBoxClickListener,
			List<String> group_list, Map<Integer, ArrayList<Item>> item_list) {
		mOnCheckBoxClickListener = onCheckBoxClickListener;
		this.group_list = group_list;
		this.item_list = item_list;
		this.context = context;
		Log.i("test3", "MyAdapter item_list:" + item_list);
	}

	public void updateItemChecked(View view) {
		Item item = (Item) view.getTag(DATA_TAG_ID);
		if (item != null) {
			item.bl = !item.bl;
			ItemHolder holder = (ItemHolder) view.getTag();
			if (holder != null) {
				holder.cb.setChecked(item.bl);
			}
			Log.i("test3", "updateItemChecked item.bl:" + item.bl);
		}
	}

	public int getGroupCount() {
		return group_list.size();
	}

	public int getChildrenCount(int groupPosition) {
		Log.i("test3", "getChildrenCount item_list:" + item_list);
		return item_list.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return group_list.get(groupPosition);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return item_list.get(groupPosition).get(childPosition);
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public boolean hasStableIds() {
		return true;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder groupHolder = null;
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(
					R.layout.sysapp_list_group, null);
			groupHolder = new GroupHolder();
			groupHolder.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) convertView.getTag();
		}
		groupHolder.tv.setText(group_list.get(groupPosition));
		return convertView;
	}

	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ItemHolder itemHolder = null;
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(
					R.layout.sysapp_list_view_item, null);
			itemHolder = new ItemHolder();
			itemHolder.iv = (ImageView) convertView
					.findViewById(R.id.sysappIcon);
			itemHolder.tv = (TextView) convertView
					.findViewById(R.id.sysappName);
			itemHolder.cb = (CheckBox) convertView.findViewById(R.id.choice);
			itemHolder.cb.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (mOnCheckBoxClickListener != null) {
						mOnCheckBoxClickListener.onClick(v);
					}
				}
			});
			// itemHolder.cb
			// .setOnCheckedChangeListener(new
			// CheckBox.OnCheckedChangeListener() {
			// public void onCheckedChanged(CompoundButton buttonView,
			// boolean isChecked) {
			// // 回调接口
			// Log.i("test3", "buttonView:" + buttonView
			// + ";groupPosition:" + groupPosition
			// + "childPosition:" + childPosition);
			// if (mOnCheckedChangeListener != null) {
			// mOnCheckedChangeListener.onCheckedChanged(
			// buttonView, isChecked);
			// }
			// }
			// });
			convertView.setTag(itemHolder);
			// if (groupPosition == 0) {
			// convertView.setTag(group_key, 0);
			// } else if (groupPosition == 1) {
			// convertView.setTag(group_key, 1);
			// } else if (groupPosition == 2) {
			// convertView.setTag(group_key, 2);
			// }
		} else {
			itemHolder = (ItemHolder) convertView.getTag();
		}
		Item item = (Item) getChild(groupPosition, childPosition);
		itemHolder.tv.setText(item.appName);
		itemHolder.iv.setImageDrawable(item.dw);
		// 设置checkbox
		itemHolder.cb.setChecked(item.bl);
		itemHolder.cb.setTag(DATA_TAG_ID, item);

		convertView.setTag(DATA_TAG_ID, item);

		return convertView;
	}

	public class ItemHolder {
		public TextView tv = null;
		public ImageView iv = null;
		public CheckBox cb = null;
	}

	public class GroupHolder {
		public TextView tv;
		public ImageView iv;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked);
	}

	public static class Item {
		public boolean bl = false;
		public String appName;
		public Drawable dw;
		public String packageName;
		public String sourcedir;
		public String apkBackupPath;
		
		public Item(String appName, boolean bl, Drawable dw, String packageName,String sourcedir,String apkBackupPath) {
			this.appName = appName;
			this.bl = bl;
			this.dw = dw;
			this.packageName = packageName;
			this.sourcedir = sourcedir; 
			this.apkBackupPath = apkBackupPath;
		}

		public int size() {
			return 0;
		}
	}
}
