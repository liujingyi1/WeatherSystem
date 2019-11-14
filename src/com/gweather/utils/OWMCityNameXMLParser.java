package com.gweather.utils;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.gweather.app.CityInfo;

public class OWMCityNameXMLParser extends DefaultHandler {

	private final static String TAG_CITY = "city";

	private final static String TAG_COUNTRY = "country";

	private final static String TAG_COORD = "coord";

	private final static String QNAME_TYPE = "type";
	
	private List<CityInfo> mCityInfoLit;
	private CityInfo cityInfo;
	private String content;

	private boolean skip = false;
	
	private boolean isSouthWest = false;
	private boolean isNorthEast = false;

	public OWMCityNameXMLParser(List<CityInfo> cityInfoLit) {
		mCityInfoLit = cityInfoLit;
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
		skip = false;
		if (TAG_CITY.equals(localName)) {
			cityInfo = new CityInfo();
			cityInfo.setWoeid(attributes.getValue(0));
			cityInfo.setName(attributes.getValue(1));
		} else if (TAG_COORD.equals(localName)) {
			if (cityInfo != null) {
				cityInfo.getLocationInfo().setLon(attributes.getValue(0));
				cityInfo.getLocationInfo().setLat(attributes.getValue(1));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (TAG_COUNTRY.equals(localName)) {
			cityInfo.setCountry(content);
		} else if (TAG_CITY.equals(localName)) {
			mCityInfoLit.add(cityInfo);
		}
	}
}
