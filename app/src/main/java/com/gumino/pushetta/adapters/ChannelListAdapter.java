package com.gumino.pushetta.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gumino.pushetta.R;
import com.gumino.pushetta.core.dto.Channel;
import com.gumino.pushetta.core.enums.ChannelKind;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChannelListAdapter extends BaseAdapter {
	Context context;
	List<Channel> channels;

	public ChannelListAdapter(Context ctx, List<Channel> chs) {
		context = ctx;
		channels = chs;
	}

	@Override
	public int getCount() {
		return channels.size();
	}

	@Override
	public Object getItem(int position) {
		return position < channels.size() ? channels.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		long ret = 0;
		if (position < channels.size()) {
			ret = channels.get(position).hashCode();

		}
		return ret;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.channel_listitem, parent,
					false);
		}

		TextView textChannelName = (TextView) convertView
				.findViewById(R.id.textChannelName);
		TextView textChannelDescription = (TextView) convertView
				.findViewById(R.id.textChannelDescription);

		Channel ch = channels.get(position);
		
		ImageView iconLock = (ImageView) convertView.findViewById(R.id.iconLock);
		if(ch.getKind() == ChannelKind.Private){
			iconLock.setVisibility(View.VISIBLE);
		}
		else{
			iconLock.setVisibility(View.GONE);
		}
			

		textChannelName.setText(ch.getName());
		textChannelDescription.setText(ch.getDescription());
		// Load asyncrono dell'icona

		ImageView iv = (ImageView) convertView.findViewById(R.id.iconChannel);

		String url = ch.getImage();
		if (url != null && url.length() > 0) {
			ImageLoader.getInstance().displayImage(url, iv);
		}

		return convertView;
	}

}
