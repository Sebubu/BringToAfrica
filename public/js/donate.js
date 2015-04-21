$(document).ready(function () {
    $('.choose-pricing button').on('click', function () {
        var target = $(this).attr('data-target');
        $(target).attr('value', $(this).text());

    });
    $('.choose-pricing input.form-control').change(function() {
        var target = $(this).attr('data-target');
        $(target).attr('value', $(this).val());
    });
    $(document).on('submit', '#donate-form', function(event) {
        $.pjax.submit(event, '#pjax-container');
    })
});