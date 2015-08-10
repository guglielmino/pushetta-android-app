package com.gumino.pushetta.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applidium.headerlistview.SectionAdapter;
import com.gumino.pushetta.R;
import com.gumino.pushetta.core.dto.ChannelSubscribeRequest;
import com.gumino.pushetta.core.enums.ChannelSubscribeStatus;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RequestsAdapter extends SectionAdapter {
	Context context;
	List<ChannelSubscribeRequest> channels;

	Map<ChannelSubscribeStatus, List<ChannelSubscribeRequest>> requestsMap;
	Map<ChannelSubscribeStatus, String> headerLabels;
	SparseArray<ChannelSubscribeStatus> sectionsMap;

	public RequestsAdapter(Context ctx, List<ChannelSubscribeRequest> chs) {
		context = ctx;
		channels = chs;

		requestsMap = new HashMap<ChannelSubscribeStatus, List<ChannelSubscribeRequest>>();
		
		sectionsMap =  new SparseArray<ChannelSubscribeStatus>();
		Integer section = 0;
		for (ChannelSubscribeRequest req : channels) {
			ChannelSubscribeStatus key = req.getStatus();
			if (requestsMap.get(key) == null) {				
				requestsMap.put(key, new ArrayList<ChannelSubscribeRequest>());
				if (sectionsMap.indexOfValue(key) < 0){
					sectionsMap.put(section, key);
					section++;
				}
			}
			requestsMap.get(key).add(req);
		}
	
		headerLabels = new HashMap<ChannelSubscribeStatus, String>();
		headerLabels.put(ChannelSubscribeStatus.Accepted, ctx.getString(R.string.label_section_subscribed));
		headerLabels.put(ChannelSubscribeStatus.Penging, ctx.getString(R.string.label_section_pending));
		headerLabels.put(ChannelSubscribeStatus.Rejected, ctx.getString(R.string.label_section_rejected));
		
	}

	private ChannelSubscribeStatus SectionToStatus(int section) {
		ChannelSubscribeStatus status = ChannelSubscribeStatus.Accepted;
		
		Integer sectionInteger = Integer.valueOf(section);
		if ( sectionsMap.get(sectionInteger) != null){
			status = sectionsMap.get(sectionInteger);
		}
		
		return status;
	}

	@Override
	public boolean hasSectionHeaderView(int section) {
		return true;
	}

	@Override
	public int numberOfSections() {
		return requestsMap.keySet().size();
	}

	@Override
	public int numberOfRows(int section) {
		return requestsMap.get(SectionToStatus(section)).size();
	}

	@Override
	public View getRowView(int section, int row, View convertView,
			ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(
					R.layout.channel_subscriptions_listitem, parent, false);
			if (convertView == null) {
				throw new NullPointerException(
						"inflate of convertView unsucessful");
			}
		}

		TextView textChannelName = (TextView) convertView
				.findViewById(R.id.textChannelName);
		TextView textChannelDescription = (TextView) convertView
				.findViewById(R.id.textChannelDescription);

		ChannelSubscribeRequest ch = requestsMap.get(SectionToStatus(section))
				.get(row);

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

	@Override
	public Object getRowItem(int section, int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(
					context.getResources().getLayout(
							android.R.layout.simple_list_item_1), null);

		}

		((TextView) convertView).setText(headerLabels.get(SectionToStatus(section)));

		return convertView;
	}

}

