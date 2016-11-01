function areaListPrint(selector, menuData, url){
	
	var transform = {
			'base' : [
				{"tag":"li", "style":"float:left;margin:5px;","children":function(){
	            	return json2html.transform(this,transform.parseParentData)
	           	}}
			],	
			 
			'parseParentData' : [
			    {"tag":"a","class":"tit","href":"#","html":"${areaName}", "provinceCode":"${provinceCode}", "areaCode":"${areaCode}"}
			]
		};
	
	document.getElementById(selector).innerHTML = json2html.transform(menuData,transform.base);
}