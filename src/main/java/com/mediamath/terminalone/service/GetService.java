package com.mediamath.terminalone.service;

import java.util.HashMap;
import java.util.List;

import com.mediamath.terminalone.QueryCriteria;
import com.mediamath.terminalone.Exceptions.ClientException;
import com.mediamath.terminalone.Exceptions.ParseException;
import com.mediamath.terminalone.utils.ConditionQuery;
import com.mediamath.terminalone.utils.Constants;
import com.mediamath.terminalone.utils.Filters;

public class GetService {
	
	/**
	 * Get.
	 * 
	 * @param query
	 * @return
	 * @throws ClientException
	 * @throws ParseException
	 */
	public StringBuffer get(QueryCriteria query) throws ClientException, ParseException {
		
		StringBuffer path=new StringBuffer("");
		
		String childPath = "";
		
		StringBuffer includePath = new StringBuffer("");
		
		//param collection String example "advertisers"
		if(!query.collection.equals(null)){
			path.append(query.collection);
		} else {
			throw new IllegalArgumentException("please specify: collection");
		}
		
		//param entity Int example ID 12121
		if(query.entity > 0){
			path.append("/"+String.valueOf(query.entity));
		}
		
		//param child String example: acl, permissions
		if(query.child!=null){
			childPath = constructChildPath(query.child);
			if(!path.toString().equalsIgnoreCase("") && !childPath.equalsIgnoreCase("")){
				path.append(childPath);
			}
		} //end of child
		
		//param limit, should be key=value pair. example organization : 123456
		if(query.limit.size()>0){
			path.append("/limit/");
			for(String s : query.limit.keySet()){
				if(!path.toString().equalsIgnoreCase("") && path.indexOf("?")!=-1){
					//TODO raise error
				}
				if(!path.toString().equalsIgnoreCase("")){
					path.append(s+"="+String.valueOf(query.limit.get(s)));
				}
			}
		}
		
		//param include
		if(query.includeConditionList != null && !query.includeConditionList.isEmpty()) {
			includePath = constructIncludePath(query.includeConditionList);
			
			if(!path.toString().equalsIgnoreCase("") && !includePath.toString().equalsIgnoreCase("")) {
				path.append(includePath.toString());
			}
		}//end of include
		
		//param sortby example: sortby=id
		if(query.sortBy!=null){
			if(!path.toString().equalsIgnoreCase("") && !includePath.toString().equalsIgnoreCase("") && path.indexOf("?")!=-1){
				path.append("&sort_by="+query.sortBy);
			}
			else{
				path.append("?sort_by="+query.sortBy);
			}
		}//end sortby
		
		//param pageLimit should not be > 100 example: page_limit=30 
		//and param pageOffset, should be > 0 example: page_offset=10 
		if(query.pageLimit > 100){
			throw new ClientException("Page_Limit parameter should not exceed 100");
		}
		else{
			String pagePath = "";
			pagePath = constructPaginationPath(query.pageLimit, query.pageOffset);
			if(!path.toString().equalsIgnoreCase("") && path.indexOf("?")!=-1){
				path.append("&"+pagePath);
			}
			else{
				path.append("?"+pagePath);
			}
		}//end pageLimit
		
		//param QUERY example 
		if(query.query!=null){
			if(!path.toString().equalsIgnoreCase("") && path.indexOf("?")!=-1){
				path.append("&q="+query.query);
			}
			else{
				path.append("?q="+query.query);
			}
		}
		
		
		return path;
	}
	
	public String find(QueryCriteria query) throws ClientException, ParseException  {
		
		StringBuffer qParamVal = new StringBuffer();
		
		if(query.queryOperator.equalsIgnoreCase(Filters.IN)){
			if(query.queryParams.getListValue()==null || (query.queryParams.getListValue()!=null && query.queryParams.getListValue().size() <1)){
				//TODO raise TypeError
			}else{
				qParamVal.append("(");
				if(query.queryParams.getListValue().get(0) instanceof String || query.queryParams.getListValue().get(0) instanceof Number){
					String prefix = "";
					for(Object obj : query.queryParams.getListValue()){
						qParamVal.append(prefix);
						qParamVal.append(String.valueOf(obj));
						prefix = ",";
					}
				}else{
					//TODO raise typeError
				}
				
				qParamVal.append(")");
			}
		}else{
			qParamVal.append(query.queryParamName);
			qParamVal.append(query.queryOperator);
			
			if(query.queryParams.getStrValue()!=null){
				qParamVal.append(query.queryParams.getStrValue());
			}
			else if(query.queryParams.getNumberValue() != null){
				qParamVal.append(query.queryParams.getNumberValue());
			}
			else if(query.queryParams.getBoolValue()==true){
				qParamVal.append(1);
			}
			else if(query.queryParams.getBoolValue()==false){
				qParamVal.append(0);
			}
	
		}
		
		qParamVal.toString();
		
	
		
		return qParamVal.toString();
		
	}


		/**
		 * Construct Child Path based on consumer request
		 * @param child
		 * @return
		 */
		private String constructChildPath(String child){
			String childPath="";
			HashMap<String, Integer> childMap = Constants.childPaths.get(child);
			for(String s : childMap.keySet()){
				if(s.equalsIgnoreCase("target_dimensions")){
					childPath += "?target_dimensions="+String.valueOf(childMap.get("target_dimensions"));
				}
				else{
					childPath +="?"+child;
				}
			}
		
			return childPath;
		}
		
		private StringBuffer constructIncludePath(List<ConditionQuery> includeConditionList){
			StringBuffer includePath = new StringBuffer("");
			for(ConditionQuery conditionquery : includeConditionList) {
				if(includePath.toString().equalsIgnoreCase("")) {
					if(conditionquery.getInclude() != null) {
						includePath.append("?with=" + conditionquery.getInclude());
						if(conditionquery.getWith() != null) {
							includePath.append("," + conditionquery.getWith());
						}
					}
				} else {
					if(conditionquery.getInclude() != null) {
						includePath.append("&with=" + conditionquery.getInclude());
						if(conditionquery.getWith() != null) {
							includePath.append("," + conditionquery.getInclude());
						}
					}
				}
			}
			
			return includePath;
		}
		
		private String constructPaginationPath(int pageLimit, int pageOffset){
			String pagePath = "";
			if(pageLimit>0 && pageLimit<=100){
				pagePath += "page_limit="+String.valueOf(pageLimit);
			}
			if(pageOffset> 0){
				if(!pagePath.toString().equalsIgnoreCase("")){
					pagePath +="&page_offset="+String.valueOf(pageOffset);
				}
				else{
					pagePath +="page_offset="+String.valueOf(pageOffset);
				}
			}
			
			return pagePath;
		}

}