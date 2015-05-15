$(function() {
    $(document).ready(function () {
        $('.next').on('click', function () {
            var current = $(this).data('currentBlock'),
                next = $(this).data('nextBlock');
            if (next > current)
                if (false === $('#newProjectForm').parsley().validate('block' + current))
                    return;

            $('.nav-tabs > .active').next('li').find('a').trigger('click');

        });
    });

    $('#addGood').click(function(){
        var rows = $('#main-table >tbody >tr').length;
        var amount = "amounts[" + rows + "]";
        var donation = "donations[" + rows + "]";
        if(rows > 3) return;
        $('#main-table tbody').append('<tr><td class="product-image"><input class="form-control" type="number" id="name" data-parsley-group="block2" required ' + amount + '" value=""></td><td class="product-title"><input class="form-control" id="name" data-parsley-group="block2" required type="text" name="' + donation + '" value=""></td></tr>');
    });
    $('#deleteGood').click(function(){
    	var rows = $('#main-table >tbody >tr').length;
    	if(rows < 2) return;
        $("#main-table tbody tr:last").detach();
    });

        $('#startsAtPicker').datetimepicker({format: 'DD-MM-YYYY'});
        $('#endsAtPicker').datetimepicker({format: 'DD-MM-YYYY'});
        $("#startsAtPicker").on("dp.change", function (e) {
            $('#endsAtPicker').data("DateTimePicker").minDate(e.date);
        });
        $("#endsAtPicker").on("dp.change", function (e) {
            $('#startsAtPicker').data("DateTimePicker").maxDate(e.date);
        });

});
