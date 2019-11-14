package com.gweather.utils;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

import com.gweather.app.MainActivity;
import com.gweather.app.WeatherInfo;
import com.gweather.app.WeatherInfo.Forecast;



public class MultiWeatherXMLParser extends DefaultHandler {
	private static final String TAG = "Gweather.MultiWeatherXMLParser";
	
	private final static String TAG_CHANNEL = "channel";
	private final static String TAG_ITEM = "item";
	private final static String TAG_LINK = "link";
	private final static String TAG_CONDITION = "condition";
	private final static String TAG_FORECAST = "forecast";
	private final static String QNAME_CODE = "code";
	private final static String QNAME_DATE = "date";
	private final static String QNAME_DAY = "day";
	private final static String QNAME_TMP = "temp";
	private final static String QNAME_HIGH = "high";
	private final static String QNAME_LOW = "low";
	private final static String QNAME_TEXT = "text";
	
	private Context mContext;
	private List<WeatherInfo> mWeatherInfoList;
	private Forecast forecast;
	private WeatherInfo weatherInfo;
	
	private boolean newChannel = false;
	
	private String content;
	
	public MultiWeatherXMLParser(Context context, List<WeatherInfo> weatherInfoList) {
		mContext = context;
		this.mWeatherInfoList = weatherInfoList;
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		content = new String(ch, start, length);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (TAG_CHANNEL.equals(localName)) {
			newChannel = true;
		} else if (TAG_ITEM.equals(localName)) {
			weatherInfo = new WeatherInfo();
		} else if (TAG_CONDITION.equals(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String qn = attributes.getQName(i);
				if(QNAME_CODE.equals(qn)) {
					weatherInfo.getCondition().setCode(attributes.getValue(i));
				} else if (QNAME_DATE.equals(qn)) {
					weatherInfo.getCondition().setDate(attributes.getValue(i));
				} else if (QNAME_TMP.equals(qn)) {
					weatherInfo.getCondition().setTemp(attributes.getValue(i));
				} else if (QNAME_TEXT.equals(qn)) {
					weatherInfo.getCondition().setText(attributes.getValue(i));
				}
			};
		} else if (TAG_FORECAST.equals(localName)) {
			forecast = weatherInfo.new Forecast();
			for (int i = 0; i < attributes.getLength(); i++) {
				String qn = attributes.getQName(i);
				if(QNAME_CODE.equals(qn)) {
					forecast.setCode(attributes.getValue(i));
				} else if (QNAME_DATE.equals(qn)) {
					forecast.setDate(attributes.getValue(i));
				} else if (QNAME_DAY.equals(qn)) {
					forecast.setDay(attributes.getValue(i));
				} else if (QNAME_HIGH.equals(qn)) {
					forecast.setHigh(attributes.getValue(i));
				} else if (QNAME_LOW.equals(qn)) {
					forecast.setLow(attributes.getValue(i));
				} else if (QNAME_TEXT.equals(qn)) {
					forecast.setText(attributes.getValue(i));
				}
			}
			weatherInfo.getForecasts().add(forecast);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (TAG_ITEM.equals(localName)) {
			int weatherCode;
			int textRes;
			if (null == weatherInfo.getCondition().getCode()
					|| weatherInfo.getForecasts().size() < MainActivity.FORECAST_DAY) {
				Log.w(TAG, "endElement-code NULL / forecast get failed");
			} else {
				weatherCode = Integer.valueOf(weatherInfo.getCondition().getCode());
				textRes = WeatherDataUtil.getInstance().getWeatherTextResByCode(weatherCode);
				if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE != textRes) {
					weatherInfo.getCondition().setText(mContext.getResources().getString(textRes));
				}
				
				for (WeatherInfo.Forecast forecast : weatherInfo.getForecasts()) {
					weatherCode = Integer.valueOf(forecast.getCode());
					textRes = WeatherDataUtil.getInstance().getWeatherTextResByCode(weatherCode);
					if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE != textRes) {
						forecast.setText(mContext.getResources().getString(textRes));
					}
				}
				
				for (WeatherInfo info: mWeatherInfoList) {
					if (info.getWoeid().equals(weatherInfo.getWoeid())) {
						Log.i(TAG, "updating:"+info.getName());
						info.copyWeatherOnly(weatherInfo);
						info.setUpdateTime(System.currentTimeMillis());
						break;
					}
				}
			}
			
			weatherInfo = null;
		} else if (TAG_CHANNEL.equals(localName)) {
			newChannel = false;
		} else if(TAG_LINK.equals(localName)) {
			if (weatherInfo != null) {
				weatherInfo.setWoeid(getWoeidFromLink(content));
			}
		} 
	}
	
	private String getWoeidFromLink(String link) {
		String[] sa = link.split("-");
		return sa[sa.length-1].replace("/", "");
	}
}
