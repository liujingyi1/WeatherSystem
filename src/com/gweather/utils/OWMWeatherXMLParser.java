package com.gweather.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

import com.gweather.app.WeatherInfo;
import com.gweather.app.WeatherInfo.Forecast;

public class OWMWeatherXMLParser extends DefaultHandler {

	private static final String TAG = "Gweather.OWMWeatherXMLParser";
	
	private final static String TAG_SUN = "sun";
	private final static String TAG_TEMPERATURE = "temperature";
	private final static String TAG_WEATHER = "weather";
	private final static String TAG_LASTUPDATE = "lastupdate";
	
	private Context mContext;
	private WeatherInfo mWeatherInfo;
	private Forecast forecast;
	
	private String woeid;
	
	public OWMWeatherXMLParser(Context context, WeatherInfo info, String woeid) {
		mContext = context;
		mWeatherInfo = info;
		this.woeid = woeid;
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		/*int weatherCode;
		int textRes;
		if (null == mWeatherInfo.getCondition().getCode()) {
			Log.d(TAG, "endDocument-code NULL");
		} else {
			weatherCode = Integer.valueOf(mWeatherInfo.getCondition().getCode());
			textRes = WeatherDataUtil.getInstance().getWeatherTextResByCode(weatherCode);
			if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE != textRes) {
				mWeatherInfo.getCondition().setText(mContext.getResources().getString(textRes));
			}
		}*/
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		mWeatherInfo.setWoeid(woeid);
		
		if (TAG_SUN.equals(localName)) {
			String sunRise = attributes.getValue(0);
			String spStr[] = sunRise.split("T");
			mWeatherInfo.getCondition().setDate(spStr[0]);
		} else if (TAG_TEMPERATURE.equals(localName)) {
			mWeatherInfo.getCondition().setTemp(attributes.getValue(0));
		} else if (TAG_WEATHER.equals(localName)) {
			mWeatherInfo.getCondition().setCode(attributes.getValue(0));
			mWeatherInfo.getCondition().setText(attributes.getValue(1));
			String iconCode = attributes.getValue(2);
			if (!iconCode.isEmpty()) {
				mWeatherInfo.getCondition().setIconCode(iconCode.substring(0, 2));
			} else {
				mWeatherInfo.getCondition().setIconCode("01d");
			}
			//mWeatherInfo.getCondition().setIconCode(attributes.getValue(2).substring(0, 2));
			mWeatherInfo.setUpdateTime(System.currentTimeMillis());
		}//  else if (TAG_LASTUPDATE.equals(localName)) {
			
		//}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		
	}
	
}
