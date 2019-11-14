package com.gweather.app;


public class PresetCityInfo {

	private int _id;
	private String woeid;
	private String name;
	private String sortKey;
	private int isHotCity;
	private int isSelect;
	
	public PresetCityInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public int getID() {
		return _id;
	}
	
	public void setID(int id) {
		this._id = id;
	}
	
	public void setCityName(String cityString) {
		this.name = cityString;
	}
	
	public String getCityName() {
		return name;
	}
	
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	
	public String getSoryKey() {
		return sortKey;
	}
	
	public void setWoeId(String id) {
		woeid = id;
	}
	
	public String getWoeId() {
		return woeid;
	}
	
	public int isSelect() {
		return isSelect;
	}
	
	public void setSelect(int select) {
		isSelect = select;
	}
	
	public int isHotCity() {
		return isHotCity;
	}
	
	public void setHotCity(int is) {
		isHotCity = is;
	}

}
