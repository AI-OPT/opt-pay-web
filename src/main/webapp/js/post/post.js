/**
 * 设置市
 * ele  省分select控件对象
 * cityId  该控件需要控制的地市元素的ID
 * cityCode  地市的默认选中值
 */
function setCity(ele,cityId,cityCode) {
	var level = 2;
	var provinceCode = ele.value;
	$.ajax({
		url : webpath+'/common/postController/getAreas',
		data : {
			level : level,
			code : provinceCode
		},
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(rCity) {
			if ((typeof (cityId) == "undefined" || cityId == '')) {
				$(ele).next().text("");
				showData($(ele).next(),rCity,cityCode);
			}else{
				$("#"+cityId).empty();
				showData($("#"+cityId),rCity,cityCode);
			}
		},
		error : function() {
			alert("异常！");
		}
	});
}

/**
 * 显示数据
 */
function showData(obj, data,defaultCode) {
	if(data != null && data.length > 0){
		// 遍历数据
		$.each(data, function(i, element) {
			if(i==0){
				obj.append("<option value=''>请选择</option>");
			}
			if(typeof (defaultCode) != "undefined" &&element.cityCode==defaultCode ){
				obj.append("<option value='"+ element.cityCode + "' selected>" + element.areaName + "</option>");
			}else{
				obj.append("<option value='"+ element.cityCode + "'>" + element.areaName + "</option>");
			}
			
		});
	}else{
		obj.append("<option value=''>请选择</option>");
	}
}

/**
 * 设置区县
 * ele  地市select控件对象
 * countyId  该控件需要控制的区县元素的ID
 * countyCode  区县的默认选中值
 */
function setCounty(ele,countyId,countyCode) {
	var level = 3;
	var code = ele.value;
	$.ajax({
		url : webpath+'/common/postController/getAreas',
		data : {
			level : level,
			code : code
		},
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(rCounty) {
			if ((typeof (cityId) == "undefined" || cityId == '')) {
				$(ele).next().text("");
				showData($(ele).next(),rCounty,countyCode);
			}else{
				$("#"+cityId).empty();
				showData($("#"+countyId),rCounty,countyCode);
			}
		},
		error : function() {
			alert("异常！");
		}
	});
}

/**
 * 设置区县
 * value  -1
 * countyId  该控件需要控制的区县元素的ID
 * countyCode  区县的默认选中值
 */
function setCountyByPro(countyId) {
	$("#"+countyId).empty();
	$("#"+countyId).append("<option value=''>请选择</option>");
}

