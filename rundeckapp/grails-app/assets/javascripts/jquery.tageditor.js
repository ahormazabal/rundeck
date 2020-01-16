(function( $ ) {
    $.fn.tagEditor = function() {
        this.each(function () {

            var parseTextIntoTags = function (text/*: string*/) {
                var result = [];
                var matchesNonAllowedCharacters = /[^\w-]+/;
                var splitter = /[\s,]+/;

                if (text) {
                    jQuery.each(text.split(splitter), function (idx, item) {
                        var candidate = item.replace(matchesNonAllowedCharacters, "");
                        if (candidate) result.push(candidate);
                    });
                }

                return result;
            };

            var removeTagLink = function (removalTarget) {
                var remainingTags = [];
                jQuery.each(currentTags, function(idx, aTag){
                    if(aTag != removalTarget) remainingTags.push(aTag);
                });
                currentTags = remainingTags;

                updateTags();
            };

            var updateTags = function () {
                jQuery(targetTagListElement).html("");

                jQuery.each(currentTags, function (idx, aTagName) {
                    var newTagElement = jQuery(
                        '<li style="display:inline; margin-right:6px;" ><div href="#"  class="label label-muted" >'
                        + '<span style="color: #303030;">' + aTagName + '</span>'
                        + '<a href="#" style="margin-left:4px;" class="glyphicon glyphicon-remove"></a>'
                        + '</div></li>'
                    );

                    newTagElement.click(function () {
                        removeTagLink(aTagName);
                    });

                    newTagElement.appendTo(targetTagListElement);
                });
            };

            var updateHiddenField = function(){
                var newHiddenFieldContent = currentTags.join(",");
                tagHiddenInput.val(newHiddenFieldContent);
            };

            var processInputTextIntoTags = function(){
                var newTags = parseTextIntoTags(elem.val());
                elem.val("");

                jQuery.each( newTags, function(idx, aNewTag){
                    if( jQuery.inArray(aNewTag, currentTags) === -1 ) currentTags.push(aNewTag);
                });

                updateTags();
                updateHiddenField();
            }


            /*------------------------------------------------------------*/
            var elem = jQuery(this);

            //adds an enclosing div to re-style content correctly. Styles should be class based.
            elem.wrap('<div class="input-group tag-input-wrapper"></div>');

            /*moves all data from current input to hidden input*/
            var tagHiddenInput = jQuery('<input type="hidden"></input>');
            tagHiddenInput.attr("name", elem.attr('name'));
            tagHiddenInput.attr('id', "tag-input_" + elem.attr('id'));

            var initialInputValue = elem.val();
            elem.before(tagHiddenInput);
            elem.removeAttr("name");
            elem.val("");

            /*converts current comma/space-separated value to tag elements on page*/
            var targetTagListElement = jQuery('<ul class="tags-list input-group-addon list-unstyled" style="border-right: 0px;"></ul>');
            elem.before(targetTagListElement);

            var currentTags = parseTextIntoTags(initialInputValue);
            updateTags();
            updateHiddenField();

            /*wires focus/blur events to class change on addon*/
            jQuery(elem).focus(function () {
                jQuery(targetTagListElement).addClass("tags-list-follows-focus");
            });
            jQuery(elem).blur(function () {
                jQuery(targetTagListElement).removeClass("tags-list-follows-focus");
            });

            /*wires keyup/change/blur events for the original input*/
            jQuery(this).keyup(function (event) {
                if(event.keyCode == 8 && "" == elem.val()){
                    if( currentTags.length > 0 ){
                        currentTags.pop();
                        updateTags();
                        updateHiddenField();
                    }
                }
                if(event.keyCode == 32){
                    processInputTextIntoTags();
                }

                //TODO: a backspace from position 0 of cursor deletes the last tag
            });
            jQuery(this).blur(function () {
                processInputTextIntoTags();
            });
        });


        return this;
    };

}( jQuery ));