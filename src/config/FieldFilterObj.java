package config;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;


public class FieldFilterObj {
	public String name;
	public List<String> values;
	public FieldFilterObj(String name, JSONArray values){
		this.name = name;
		this.values = new ArrayList<String>();
		for(int i=0;i<values.size();i++){
			this.values.add((String)values.get(i));
		}
	}
	public FieldFilterObj(String name, String value){
		this.name = name;
		this.values  = new ArrayList<String>();
		this.values.add(value);
	}
}
