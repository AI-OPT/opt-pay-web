
//控制主菜单
$(function(){ 
$(".headright  > .nav  > a").mouseover(function(e) {
	    $(this).addClass('select');
  });
  $(".headright  > .nav  > a").mouseout(function(e) {
	   $(this).removeClass('select');
  });
}); 

//控制主菜单
$(function(){ 
$(" .userinfo").mouseover(function(e) {
	    $(".userinfo  > h3  ").addClass('select');
	    $(".userinfo_list").show();	
		 $(" .user_listtable  tbody   tr ").mouseover(function(e){
			 $(this).addClass("hover");    
		});
		$(" .user_listtable  tbody   tr ").mouseout(function(e){
			 $(this).removeClass("hover"); 
		});
		 $(" .user_listtable  tbody   tr ").click(function(e){
			 $(" .user_listtable  tbody   tr ").removeClass('select');
			 $(this).addClass("select");    
		});
		  
  });
  $(" .userinfo ").mouseout(function(e) {
	   $(".userinfo  > h3   ").removeClass('select');
	   $(".userinfo_list").hide();	  
  });
}); 
