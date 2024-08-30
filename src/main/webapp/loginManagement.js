(function() { // avoid variables ending up in the global scope

  document.getElementById("loginButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', form,
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("loginMsg").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("loginMsg").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("loginMsg").textContent = message;
                break;
            }
          }
      });
    } else {
      form.reportValidity();
    }
  });

})();
