(function($){
	 
	$.PaginationTag = function(gridContainer, url, parameterContainer, pageInfo){
		this.settings = $.extend(true,{},$.PaginationTag.defaults);
		this.gridContainer = gridContainer;
		this.url = url;
		this.parameterContainer = parameterContainer;
		this.pageInfo = pageInfo;
	}
	
	$.extend($.PaginationTag,{
		defaults : {
			BTN_GO_FIRST: "#goFirst",
			BTN_GO_PRE: "#goPre",
			BTN_GO_NEXT: "#goNext" ,
			BTN_GO_LAST: "#goLast" ,
			CLASS_GO_FIRST: "goFirst",
			CLASS_GO_PRE: "goPre",
			CLASS_GO_NEXT: "goNext" ,
			CLASS_GO_LAST: "goLast" ,
			CLASS_GO_FIRST_DISABLED: "goFirstDisabled",
			CLASS_GO_PRE_DISABLED: "goPreDisabled",
			CLASS_GO_NEXT_DISABLED: "goNextDisabled" ,
			CLASS_GO_LAST_DISABLED: "goLastDisabled" ,
			CLASS_GO_PAGE: "goPage",
			BTN_GO : "#goPage",
			TEXT_PAGE_NO : "#pageNoTxt",
			CLASS_CURSORHAND: "crusorHand",
			CLASS_NOTALLOWED: "cursorNotAllow"
		},
		prototype :{ 
			/*初始化分页标签*/
			doTag : function(){
				var _this = this;
				var pageNo = _this.pageInfo.pageNo;
				var pageCount = _this.pageInfo.pageCount;
				var gridContainer = $(_this.gridContainer);
				if(!gridContainer.length){
					alert("分页控件初始化错误，没有指定分页控件所在的容器");
					return;
				}
				//如果当前页码为第一页，则首页与上一页不可用
				if(pageNo <= 1 ){
					//首页按钮事件
					gridContainer.find(_this.settings.BTN_GO_FIRST).addClass(_this.settings.CLASS_GO_FIRST_DISABLED).addClass(_this.settings.CLASS_NOTALLOWED);
					//上一页按钮事件
					gridContainer.find(_this.settings.BTN_GO_PRE).addClass(_this.settings.CLASS_GO_PRE_DISABLED).addClass(_this.settings.CLASS_NOTALLOWED); 
				}
				//如果当前页面大于1，则为上一页与首页绑定绑定事件
				if(pageNo > 1 ){
					//首页按钮事件
					gridContainer.find(_this.settings.BTN_GO_FIRST).addClass(_this.settings.CLASS_GO_FIRST).addClass(_this.settings.CLASS_CURSORHAND).bind("click",function(){
						_this.go(1); 
					});
					//上一页按钮事件
					gridContainer.find(_this.settings.BTN_GO_PRE).addClass(_this.settings.CLASS_GO_PRE).addClass(_this.settings.CLASS_CURSORHAND).bind("click",function(){
						_this.go(pageNo - 1); 
					}); 
				}
				//如果当前页小于总页数，则为下一页与最后一页按钮绑定时间
				if(pageNo < pageCount){ 
					//下一页按钮事件
					gridContainer.find(_this.settings.BTN_GO_NEXT).addClass(_this.settings.CLASS_GO_NEXT).addClass(_this.settings.CLASS_CURSORHAND).bind("click",function(){
						_this.go(pageNo + 1);
					}); 
					//最后页按钮事件
					gridContainer.find(_this.settings.BTN_GO_LAST).addClass(_this.settings.CLASS_GO_LAST).addClass(_this.settings.CLASS_CURSORHAND).bind("click",function(){
						_this.go(pageCount); 
					});
				}
				//如果当前页码为最后一页，则下一页与末页不可用
				if(pageNo == pageCount || pageCount<1){ 
					//下一页按钮事件
					gridContainer.find(_this.settings.BTN_GO_NEXT).addClass(_this.settings.CLASS_GO_NEXT_DISABLED).addClass(_this.settings.CLASS_NOTALLOWED);
					//最后页按钮事件
					gridContainer.find(_this.settings.BTN_GO_LAST).addClass(_this.settings.CLASS_GO_LAST_DISABLED).addClass(_this.settings.CLASS_NOTALLOWED);
				}
				//为页面跳转按钮绑定事件
				gridContainer.find(_this.settings.BTN_GO).addClass(_this.settings.CLASS_GO_PAGE).bind("click",function(){
					//获取设定的页码
					var gotoPageText = gridContainer.find(_this.settings.TEXT_PAGE_NO);
					var gotoPageNo = gotoPageText.val();
					if(!gotoPageNo.match(/^[0-9]*[1-9][0-9]*$/) || parseInt(gotoPageNo) > pageCount || parseInt(gotoPageNo) < 1){
						alert("输入值必须为大于0小于或等于" + pageCount + "的正整数");
						gotoPageText.focus();
						return false;
					}
					_this.go(gotoPageNo);
				}) 
			},
			/**跳转到指定的页面*/
			go: function(pageNo){
				var _this = this; 
				formPag.goAjax({
					type:"POST", 
					url:_this.url,
					showBusi: true,
					modal: true,
					message: "正在处理中，请稍候...", 
					postselectors: [_this.parameterContainer],
					postmode: "update",
					target: _this.gridContainer,
					data: {pageNo:pageNo},
					success:function(transport){  
						
					}
				});
			}
		} 
	});
})(jQuery);
