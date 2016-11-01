$(function () {
    var st = 180;
    $('div>').mouseenter(function () {
        $(this).find('div').stop(false, true).slideDown(st);
    }).mouseleave(function () {
        $(this).find('div').stop(false, true).slideUp(st);
    });
});