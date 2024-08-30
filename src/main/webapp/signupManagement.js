/**
 * Singup management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("signupButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    var pwd1 = form.querySelector("input[name=pwd1]").value;
    var pwd2 = form.querySelector("input[name=pwd2]").value;
    
    if(pwd1!==pwd2){
		document.getElementById("signupMsg").textContent = "Passwords do not match";
		return;
	}
    if (form.checkValidity()) {
      makeCall("POST", 'CheckSignup', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	//sessionStorage.setItem('username', message);
            	document.getElementById("signupMsg").textContent = message;
                //window.location.href = "index.html";
                break;
              case 400: // bad request
                document.getElementById("signupMsg").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("signupMsg").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("signupMsg").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();