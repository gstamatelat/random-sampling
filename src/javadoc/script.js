document.addEventListener('DOMContentLoaded', function () {
    var anchors = document.getElementsByTagName('a');
    var length = anchors.length;
    for (var i = 0; i < length; i++) {
        if (location.protocol === 'http:' || location.protocol === 'https:') {
            if (anchors[i].href.indexOf(location.origin) !== 0) {
                anchors[i].target = '_blank';
            }
        } else {
            if (anchors[i].href.indexOf('file:') !== 0) {
                anchors[i].target = '_blank';
            }
        }
    }
}, false);
