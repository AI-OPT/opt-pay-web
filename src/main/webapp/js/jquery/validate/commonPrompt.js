/**
 * 表单验证 错误信息以浮动DIV提示 
 * error:错误信息对象 
 * element:当前验证的表单对象 
 * position:错误信息位置
 * top,right,buttom,left 
 * width:错误信息宽度 height:错误信息高度
 */
function handleErrorForFloatDiv(error, element, position, width, height) {
	//alert("1232");
	//浮框离空间的边距
	var padding = 1;
	// 错误对象的描述信息
	var errorMsg = $(error).text();
	// 信息展示区的id
	var errorIdDiv = $(element).attr("id") + "_er";
	// 拼装信息展示区的内容
	//上方浮动DIV,默认上方浮动
	var msgHtml = "<div class='zx_xiak'  id='" + errorIdDiv + "'><div class='zx_xiak_A'><p>"
	+ errorMsg + "</p></div><div class='zx_xiak_B'></div></div>";

	// 信息展示区的默认宽度
	if (typeof (width) == "undefined" || width == '') {
		width = $(element).width();
	}
	// 信息展示区的默认高度
	if (typeof (height) == "undefined" || height == '') {
		height = $(element).height();
	}
	/** 处理浮动DIV的显示位置 */
	if (typeof (position) == "undefined" || position == '') {
		position = 'top';
	}
	// 当前校验控件的坐标
	var offset = $(element).offset();
	if (position == 'top') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
			var topTmp = (offset.top - height)>0?(offset.top - height):(height - offset.top);
		var top = topTmp - padding;
	} else if (position == 'right') {
		// 当前校验控件的坐标 left
		var left = offset.left + $(element).width() + padding;
		// 当前校验控件的坐标 top
		var top = offset.top;
		//右边浮动DIV
		msgHtml = "<div class='hx_kuang' id='" + errorIdDiv + "'><p>"
		+ errorMsg + "</p></div>";
	} else if (position == 'buttom') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
		var top = offset.top + $(element).height() + padding;
		//下方浮动DIV
		msgHtml = "<div class='zx_kuang'  id='" + errorIdDiv + "'><div class='zx_kuang_A'></div><div class='zx_kuang_B'><p>"
		+ errorMsg + "</p></div></div>";
	} else if (position == 'left') {
		// 当前校验控件的坐标 left
		var left = offset.left - $(element).width() - padding;
		// 当前校验控件的坐标 top
		var top = offset.top;
	}
	//alert(msgHtml);
	if ($(element.parent()).find("div#" + errorIdDiv).length == 0) {
		// 第一次 浮动展示
		$(element).before(msgHtml);
		$(element).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	} else {
		$("#" + errorIdDiv).remove();
		$(element).before(msgHtml);
		$(element).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		//$("#" + errorIdDiv).css("position", "fixed");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	}
}
/**
 * 表单验证 错误信息以浮动DIV提示 
 * error:错误信息对象 
 * element:当前验证的表单对象 
 * position:错误信息位置
 * top,right,buttom,left 
 * width:错误信息宽度 height:错误信息高度
 */
function handleErrorForFloatRondomDiv(error, element, position, width, height) {
	//alert("1232");
	//浮框离空间的边距
	var padding = 1;
	// 错误对象的描述信息
	var errorMsg = error;
	// 信息展示区的id
	var errorIdDiv = $(element).attr("id") + "_er";
	// 拼装信息展示区的内容
	//上方浮动DIV,默认上方浮动
	var msgHtml = "<div class='zx_xiak'  id='" + errorIdDiv + "'><div class='zx_xiak_A'><p>"
	+ errorMsg + "</p></div><div class='zx_xiak_B'></div></div>";

	// 信息展示区的默认宽度
	if (typeof (width) == "undefined" || width == '') {
		width = $(element).width();
	}
	// 信息展示区的默认高度
	if (typeof (height) == "undefined" || height == '') {
		height = $(element).height();
	}
	/** 处理浮动DIV的显示位置 */
	if (typeof (position) == "undefined" || position == '') {
		position = 'top';
	}
	// 当前校验控件的坐标
	var offset = $(element).offset();
	if (position == 'top') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
			var topTmp = (offset.top - height)>0?(offset.top - height):(height - offset.top);
		var top = topTmp - padding;
	} else if (position == 'right') {
		// 当前校验控件的坐标 left
		var left = offset.left + $(element).width() + padding;
		// 当前校验控件的坐标 top
		var top = offset.top;
		//右边浮动DIV
		msgHtml = "<div class='hx_kuang' id='" + errorIdDiv + "'><p>"
		+ errorMsg + "</p></div>";
	} else if (position == 'buttom') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
		var top = offset.top + $(element).height() + padding;
		//下方浮动DIV
		msgHtml = "<div class='zx_kuang'  id='" + errorIdDiv + "'><div class='zx_kuang_A'></div><div class='zx_kuang_B'><p>"
		+ errorMsg + "</p></div></div>";
	} else if (position == 'left') {
		// 当前校验控件的坐标 left
		var left = offset.left - $(element).width() - padding;
		// 当前校验控件的坐标 top
		var top = offset.top;
	}
	//alert(msgHtml);
	if ($(element).parent().find("div#" + errorIdDiv).length == 0) {
		// 第一次 浮动展示
		$(element).before(msgHtml);
		$(element).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	} else {
		$("#" + errorIdDiv).remove();
		$(element).before(msgHtml);
		$(element).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		//$("#" + errorIdDiv).css("position", "fixed");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	}
}
/**
 * 表单验证 成功时 移除信息提示DIV
 */
function handleSuccessForFloatDiv(element) {
	var errorIdDiv = $(element).attr("id") + "_er";
	$("#" + errorIdDiv).remove();
}

/**
 * 表单验证 控件获得焦点后 隐藏信息提示DIV
 */
function hideForFloatDiv(element) {
	var errorIdDiv = $(element).attr("id") + "_er";
	$("#" + errorIdDiv).hide();
}

/**
 * 提示后厂异常信息 或者 普通提示
 * infoMsg 提示信息 
 * elementId 需要做浮动效果的控件Id 
 * position:top,right,buttom,left 
 * width 提示区域DIV的高度 
 * height 提示区域DIV的高度
 * 
 */
function showPromptDivByEleId(infoMsg, elementId, position, width, height) {
	//alert("showPromptDivByEleId"+"info:"+infoMsg+" elementId:"+elementId+" position:"+position+" width:"+width+" height:"+height);
	//浮框离空间的边距
	var padding = 1;
	// 信息展示区的id
	var errorIdDiv = elementId + "_er";
	// 拼装信息展示区的内容
	var msgHtml = "<div class='zx_xiak' id='" + errorIdDiv + "'><div class='zx_xiak_A'><p>"
	+ infoMsg + "</p></div><div class='zx_xiak_B'></div></div>";

	// 信息展示区的默认宽度
	if (typeof (width) == "undefined" || width == '') {
		width = $("#" + elementId).width();
	}
	// 信息展示区的默认高度
	if (typeof (height) == "undefined" || height == '') {
		height = $("#" + elementId).height();
	}

	/** 处理浮动DIV的显示位置 */
	if (typeof (position) == "undefined" || position == '') {
		position = 'top';
	}
	// 当前校验控件的坐标
	var offset = $("#" + elementId).offset();
	if (position == 'top') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
		var topTmp = (offset.top - height)>0?(offset.top - height):(height - offset.top);
		var top = topTmp - padding;
	} else if (position == 'right') {
		// 当前校验控件的坐标 left
		var left = offset.left + $("#" + elementId).width() + padding;
		// 当前校验控件的坐标 top
		var top = offset.top-padding;
		//右边浮动DIV
		msgHtml = "<div class='hx_kuang' id='" + errorIdDiv + "'><p>"
		+ infoMsg + "</p></div>";
	} else if (position == 'buttom') {
		// 当前校验控件的坐标 left
		var left = offset.left;
		// 当前校验控件的坐标 top
		var top = offset.top + $("#" + elementId).height() + padding;
		//下方
		msgHtml = "<div class='zx_kuang' id='" + errorIdDiv + "'><div class='zx_kuang_A'></div><div class='zx_kuang_B'><p>"
		+ infoMsg + "</p></div></div>";
	} else if (position == 'left') {
		// 当前校验控件的坐标 left
		var left = offset.left - $("#" + elementId).width() - padding;
		// 当前校验控件的坐标 top
		var top = offset.top;
	}

	if ($("#" + elementId).parent().find("div#" + errorIdDiv).length == 0) {
		$("#" + elementId).before(msgHtml);
		$("#" + elementId).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	} else {
		$("#" + errorIdDiv).remove();
		$("#"+elementId).before(msgHtml);
		$("#" + elementId).css("position", "relative");
		$("#" + errorIdDiv).css("position", "absolute");
		$("#" + errorIdDiv).css("width", width);
		$("#" + errorIdDiv).css("height ", height);
		$("#" + errorIdDiv).css("left", left);
		$("#" + errorIdDiv).css("top", top);
		$("#" + errorIdDiv).show();
		$("#" + errorIdDiv).delay(5000).hide(0);
	}
}

/**
 * 提示后厂异常信息 再次验证通过时移除提示DIV区
 * elementId 需要做浮动效果的控件Id
 */
function removePromptDivByEleId(elementId) {
	var errorIdDiv = $("#"+elementId).attr("id") + "_er";
	$("#" + errorIdDiv).remove();
}

/**
 * 提示后厂异常信息 需要隐藏提示DIV区
 * elementId 需要做浮动效果的控件Id
 * 
 */
function hidePromptDivByEleId(elementId) {
	var errorIdDiv = $("#"+elementId).attr("id") + "_er";
	$("#" + errorIdDiv).hide();
}
