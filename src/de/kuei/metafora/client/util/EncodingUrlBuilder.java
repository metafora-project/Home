package de.kuei.metafora.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Languages;

public class EncodingUrlBuilder extends UrlBuilder {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	
	public void setToolData(String[] data) {
		if (data != null && data.length >= 2) {
			setProtocol(data[0]);
			setHost(data[1]);

			if (data.length > 2) {
				setPath(data[2]);
			}

			if (data.length > 3) {
				try {
					int port = Integer.parseInt(data[3]);
					setPort(port);
				} catch (Exception e) {
					// ignore
				}
			}
		}else{
			Window.alert(language.ToolDataIsInvalid()+" "+data);
		}
	}

}
