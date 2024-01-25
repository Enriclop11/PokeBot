function getSprite() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState === 4 && this.status === 200){
            document.getElementById("sprite").src = this.responseText;
        }
    };
    xhttp.open("GET", "/pokemon/wild/sprite", true);
    xhttp.send();
}
setInterval(getSprite, 1000);