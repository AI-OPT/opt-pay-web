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
		url : webpath+'/common/area/getGnAreas',
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
 * 显示银行数据 ----zhouwj
 */
function showBankData(obj, data,defaultCode) {
	if(data != null && data.length > 0){
		// 遍历数据
		$.each(data, function(i, element) {
			if(i==0){
				obj.append("<option value=''>请选择</option>");
			}
			if(typeof (defaultCode) != "undefined" &&element.columnValue==defaultCode ){
				obj.append("<option value='"+ element.columnValue + "' selected>" + element.columnDesc + "</option>");
			}else{
				obj.append("<option value='"+ element.columnValue + "'>" + element.columnDesc + "</option>");
			}
			
		});
	}else{
		obj.append("<option value=''>请选择</option>");
	}
}

/**
 * 设置市级银行分行
 * ele 银行 select 控件对象
 * provinceCode 省份代码
 * cityCode 市代码
 */
//function setBank(eleID,provinceID,cityID){
////	var level = 4;  //4以后为银行的级别
////	var code = ele.value;
//	$.ajax({
//		url : webpath+'/custCompany/queryBank',
//		data : {
//			provinceCode : $(provinceID).attr("checked").val(),
//			cityCode : $(cityID).attr("checked").val()
//		},
//		type : 'post',
//		cache : false,
//		dataType : 'json',
//		success : function(data) {
//			if (data.success) {
//				$("#"+eleID).next().text("");
//				showBankData($("#"+eleID).next(),data.result,'');
//			}else{
//				$("#"+eleID).html('');
//				$.dialog.alert("获取信息出错，请稍后重试");
//			}
//		},
//		error : function() {
//			$.dialog.alert("系统异常！");
//		}
//	});
//}


/**
 * 设置区县
 * ele  地市select控件对象
 * countyId  该控件需要控制的区县元素的ID
 * countyCode  区县的默认选中值
 */
function setBank(ele,province,bankId,defaultCityCode) {
	var city = ele.value;
	$.ajax({
		url : webpath+'/common/area/getBank',
		data : {
			province : $('#'+province).val(),
			city : city
		},
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(rCounty) {
			if ((typeof (bankId) == "undefined" || bankId == '')) {
				$(ele).next().text("");
				showBankData($(ele).next(),rCounty,defaultCityCode);
			}else{
				$("#"+bankId).empty();
				showBankData($("#"+bankId),rCounty,defaultCityCode);
			}
			
			
		},
		error : function() {
			alert("异常！");
		}
	});
}


/**
 * 设置区县
 * ele  地市select控件对象
 * countyId  该控件需要控制的区县元素的ID
 * countyCode  区县的默认选中值
 */
function setBankByCityValue(cityValue,province,bankId,defaultCityCode) {
	var city = cityValue;
	$.ajax({
		url : webpath+'/common/area/getBank',
		data : {
			province : $('#'+province).val(),
			city : city
		},
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(rCounty) {
			if ((typeof (bankId) == "undefined" || bankId == '')) {
				$(ele).next().text("");
				showBankData($(ele).next(),rCounty,defaultCityCode);
			}else{
				$("#"+bankId).empty();
				showBankData($("#"+bankId),rCounty,defaultCityCode);
			}
			
			
		},
		error : function() {
			alert("异常！");
		}
	});
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
		url : webpath+'/common/area/getGnAreas',
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

/**
 * 设置银行
 * value -1
 * bankId 该空间需要控制的银行元素的ID
 * bankCode 默认选中值
 */
function setBankByPro(bankId){
	$('#'+bankId).empty();
	$('#'+bankId).append("<option value=''>请选择</option>");
}

