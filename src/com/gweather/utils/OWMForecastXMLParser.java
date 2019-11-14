package com.gweather.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

import com.gweather.app.WeatherInfo;
import com.gweather.app.WeatherInfo.Forecast;

public class OWMForecastXMLParser extends DefaultHandler {
	private static final String TAG = "Gweather.OWMWeatherXMLParser";
	
	private final static String TAG_TIME = "time";
	private final static String TAG_SYMBOL = "symbol";
	private final static String TAG_TEMPERATURE = "temperature";
	private final static String QNAME_MAX = "max";
	private final static String QNAME_MIN = "min";
	private final static String QNAME_NUMBER = "number";
	private final static String QNAME_NAME = "name";
	private final static String QNAME_TIME_DAY = "day";
	private final static String QNAME_VAR = "var";
	
	private Context mContext;
	private WeatherInfo mWeatherInfo;
	private Forecast forecast;
	
	private String woeid;
	
	public OWMForecastXMLParser(Context context, WeatherInfo info, String woeid) {
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
			
			for (WeatherInfo.Forecast forecast : mWeatherInfo.getForecasts()) {
				weatherCode = Integer.valueOf(forecast.getCode());
				textRes = WeatherDataUtil.getInstance().getWeatherTextResByCode(weatherCode);
				if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE != textRes) {
					forecast.setText(mContext.getResources().getString(textRes));
				}
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

		String day = "";
		if (TAG_TIME.equals(localName)) {
			forecast = mWeatherInfo.new Forecast();
			day = attributes.getValue(QNAME_TIME_DAY);
			forecast.setDate(day);
			
			
			try {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date date = formatter.parse(day);
				c.setTime(date);
				int week = c.get(Calendar.DAY_OF_WEEK);
				forecast.setDay(Utils.getWeekDes(week));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (TAG_SYMBOL.equals(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String qn = attributes.getQName(i);
				if(QNAME_NUMBER.equals(qn)) {
					forecast.setCode(attributes.getValue(i));
				} else if (QNAME_NAME.equals(qn)) {
					forecast.setText(attributes.getValue(i));
				} else if (QNAME_VAR.equals(qn)) {
					String iconCode = attributes.getValue(i);
					if (!iconCode.isEmpty() && iconCode.length() >= 3) {
						forecast.setIconCode(iconCode.substring(0, 2));
					} else {
						forecast.setIconCode("01d");
					}
					//forecast.setIconCode(attributes.getValue(i).substring(0, 2));
				}
			}
		} else if (TAG_TEMPERATURE.equals(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String qn = attributes.getQName(i);
				if(QNAME_MAX.equals(qn)) {
					forecast.setHigh(attributes.getValue(i));
				} else if (QNAME_MIN.equals(qn)) {
					forecast.setLow(attributes.getValue(i));
				}
			}
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		
		if (TAG_TIME.equals(localName)) {
			mWeatherInfo.getForecasts().add(forecast);
		}
		
	}
}
