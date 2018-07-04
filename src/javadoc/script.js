document.addEventListener('DOMContentLoaded', function () {
    var anchors = document.getElementsByTagName('a');
    var length = anchors.length;
    for (var i = 0; i < length; i++) {
        if (anchors[i].href.indexOf('http://') === 0 || anchors[i].href.indexOf('https://') === 0) {
            anchors[i].target = '_blank';
        }
    }
}, false);
