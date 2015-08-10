package com.gumino.pushetta.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gumino.pushetta.R;
import com.gumino.pushetta.core.dto.ChannelSubscribeRequest;
import com.gumino.pushetta.core.enums.ChannelSubscribeStatus;
import com.nostra13.universalimageloader.core.ImageLoader;

@Deprecated()
public class ChannelSubscriptionListAdapter extends BaseAdapter {
	Context context;
	List<ChannelSubscribeRequest> channels;
	
	Map<ChannelSubscribeStatus,List<ChannelSubscribeRequest>> requestsMap;
	
	public ChannelSubscriptionListAdapter(Context ctx, List<ChannelSubscribeRequest> chs) {
		context = ctx;
		channels = chs;
		
		requestsMap = new HashMap<ChannelSubscribeStatus,List<ChannelSubscribeRequest>>();
		for (ChannelSubscribeRequest req : channels) {
			ChannelSubscribeStatus key = req.getStatus();
			   if (requestsMap.get(key) == null) {
				   requestsMap.put(key, new ArrayList<ChannelSubscribeRequest>());
			   }
			   requestsMap.get(key).add(req);
			}
		
	}

	@Override
	public int getCount() {
		return channels.size() ;
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
		
		Integer itemType = this.getItemViewType(position);
		Log.d("TAG", itemType.toString());
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(
					R.layout.channel_subscriptions_listitem, parent, false);
			if(convertView == null){
				throw new NullPointerException("inflate of convertView unsucessful");
			}
		}

		TextView textChannelName = (TextView) convertView
				.findViewById(R.id.textChannelName);
		TextView textChannelDescription = (TextView) convertView
				.findViewById(R.id.textChannelDescription);

		ChannelSubscribeRequest ch = channels.get(position);

		textChannelName.setText(ch.getChannel().getName());
		textChannelDescription.setText(ch.getChannel().getDescription());

		// Load asyncrono dell'icona
		ImageView iv = (ImageView) convertView.findViewById(R.id.iconChannel);

		String url = ch.getChannel().getImage();
		if (url.length() > 0) {
			ImageLoader.getInstance().displayImage(url, iv);
		}
				
		return convertView;
	}
	
	/**
	 * Number of sections
	 */
	  @Override
      public int getViewTypeCount() {
          int types = requestsMap.size();
          return types > 0 ? types : 1;
      }
	  
	  /**
	   * Type of item 
	   */
	  @Override
      public int getItemViewType(int position) {
          return channels.indexOf(position) != -1 ? channels.get(position).getStatus().getValue() : 0;
      }

}
