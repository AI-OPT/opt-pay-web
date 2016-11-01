

$(function(){ 
//控制一级菜单样式
  $("#menuone li ").click(function(e){
	  	 $("#main_iframe").attr("src","");
		 $("#menuone li").each(function(){
			 $(this).find("h2:first-child").removeClass("select");
		 });
		 $(this).find("h2:first-child").addClass("select");
		 //假联动
		 
		 if($(this).attr("class")=="common_nav xiaoshou1"){
			 $("#menutwo li ").hide();
			 $(".xiaoshou").show();
		 }else if($(this).attr("class")=="common_nav pay1"){
			 $("#menutwo li ").hide();
			 $(".pay").show();
		 }else if($(this).attr("class")=="common_nav service1"){
			 $("#menutwo li ").hide();
			 $(".service").show();
		 }else if($(this).attr("class")=="common_nav system1"){
			 $("#menutwo li ").hide();
			 $(".system").show();
		 }else if($(this).attr("class")=="common_nav order1"){
			 $("#menutwo li ").hide();
			 $(".order").show();
		 }else if($(this).attr("class")=="common_nav audit1"){
			 $("#menutwo li ").hide();
			 $(".audit").show();
		 }else if($(this).attr("class")=="common_nav share1"){
			 $("#menutwo li ").hide();
			 $(".share").show();
		 }else if($(this).attr("class")=="common_nav channel1"){
			 $("#menutwo li ").hide();
			 $(".channel").show();
		 }else{
			 $("#menutwo li").show();
			 $(".system").hide();
			 $(".pay").hide();
			 $(".xiaoshou").hide();
			 $(".order").hide();
			 $(".audit").hide();
			 $(".service").hide();
			 $(".share").hide();
		 }
	});
//控制二级菜单样式
  $("#menutwo li ").click(function(e){
		 $("#menutwo li").nextAll().andSelf().removeClass("select");
		 $(this).addClass("select");    
	});
}); 


