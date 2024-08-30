{
	let createdGroupsList, invitedGroupsList, groupDetails, newGroup, participants;
	let attemptsLeft = 3;
	let pageOrchestrator = new PageOrchestrator();
	
    //load della home page
	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // Initialize the components
			pageOrchestrator.refresh(); // Display initial content
		}
	}, false);

	//messaggio di benvenuto
	function PersonalMessage(_username, _welcomeMessage) {
		this.username = _username;
		this.welcomeMessage = _welcomeMessage;

		this.show = function() {
			this.welcomeMessage.textContent = `Welcome ${this.username}!`;
		}
	}
	
	
	//funzione per mostrare i gruppi creati dall'utente
	function CreatedGroupsList(_alert, _createdGroupsList) {
		this.alert = _alert;
		this.createdGroupsList = _createdGroupsList;

		this.reset = function() {
			this.createdGroupsList.style.visibility = "hidden";
			document.getElementById("trash-container").style.visibility = "hidden"; // Nascondi il cestino
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GetCreatedGroups", null, function(req) {
				if (req.readyState == 4) {
					var message = req.responseText;
					if (req.status == 200) {
						var groupsToShow = JSON.parse(req.responseText);
						if (groupsToShow.length == 0) {
							self.alert.textContent = "Non hai creato alcun gruppo attivo!";
							return;
						}
						self.update(groupsToShow);
						if (next) next();
					} else if (req.status == 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					} else {
						self.alert.textContent = message;
					}
				}
			});
		};

		this.update = function(arrayGroups) {
			this.createdGroupsList.innerHTML = "";
			var self = this;
			arrayGroups.forEach(function(group) {
				var listItem = document.createElement("li");
				var anchor = document.createElement("a");
				anchor.textContent = group.title;
				anchor.setAttribute('groupId', group.id);
				anchor.href = "#";
				anchor.addEventListener("click", (e) => {
					groupDetails.show(e.target.getAttribute("groupId"), true);
					document.getElementById("trash-container").style.visibility = "visible";
				}, false);
				listItem.appendChild(anchor);
				self.createdGroupsList.appendChild(listItem);
			});
			this.createdGroupsList.style.visibility = "visible";
		}

		this.autoclick = function(groupId) {
			var e = new Event("click");
			var selector = "a[groupId='" + groupId + "']";
			var anchorToClick = (groupId) ? document.querySelector(selector) : this.createdGroupsList.querySelector("a");
			if (anchorToClick) {
				anchorToClick.dispatchEvent(e);
			}
		}
	}

	//funzione per mostrare i gruppi a cui partecipa l'utente
	function InvitedGroupsList(_alert, _invitedGroupsList) {
		this.alert = _alert;
		this.invitedGroupsList = _invitedGroupsList;

		this.reset = function() {
			this.invitedGroupsList.style.visibility = "hidden";
			document.getElementById("trash-container").style.visibility = "hidden"; // Nascondi il cestino
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GetInvitedGroups", null, function(req) {
				if (req.readyState == 4) {
					var message = req.responseText;
					if (req.status == 200) {
						var groupsToShow = JSON.parse(req.responseText);
						if (groupsToShow.length == 0) {
							self.alert.textContent = "Non ci sono gruppi attivi a cui sei stato invitato.";
							return;
						}
						self.update(groupsToShow);
						if (next) next();
					} else if (req.status == 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					} else {
						self.alert.textContent = message;
					}
				}
			});
		};

		this.update = function(arrayGroups) {
			this.invitedGroupsList.innerHTML = ""; // Empty the list
			var self = this;
			arrayGroups.forEach(function(group) {
				var listItem = document.createElement("li");
				var anchor = document.createElement("a");
				anchor.textContent = group.title;
				anchor.setAttribute('groupId', group.id);
				anchor.href = "#";
				anchor.addEventListener("click", (e) => {
					groupDetails.show(e.target.getAttribute("groupId"), false);
					document.getElementById("trash-container").style.visibility = "hidden"; // Nascondi il cestino per i gruppi invitati
				}, false);
				listItem.appendChild(anchor);
				self.invitedGroupsList.appendChild(listItem);
			});
			this.invitedGroupsList.style.visibility = "visible";
		}

		this.autoclick = function(groupId) {
			var e = new Event("click");
			var selector = "a[groupId='" + groupId + "']";
			var anchorToClick = (groupId) ? document.querySelector(selector) : this.invitedGroupsList.querySelector("a");
			if (anchorToClick) {
				anchorToClick.dispatchEvent(e);
			}
		}
	}

	//funzione per mostarte i dettagli di un dato gruppo
	function GroupDetails(group) {
		this.alert = group['alert'];
		this.title = group['title'];
		this.date = group['date'];
		this.duration = group['duration'];
		this.min = group['min'];
		this.max = group['max'];
		this.detailcontainer = group['container'];
		this.detailcontainer.style.visibility = "hidden";
		this.show = function(groupId, creator) {
			document.getElementById("groups-alert").style.visibility = "hidden"; // Mostra il cestino solo per il creatore del gruppo
			var self = this;
			sessionStorage.setItem('groupId', groupId);
			makeCall("GET", "GetGroupDetails?groupId=" + groupId, null, function(req) {
				if (req.readyState == 4) {
					var message = req.responseText;
					if (req.status == 200) {
						var group = JSON.parse(req.responseText);
						self.update(group);
						participants.show(null, groupId);
						self.detailcontainer.style.visibility = "visible";
						if (creator) {
							document.getElementById("trash-container").style.visibility = "visible"; // Mostra il cestino solo per il creatore del gruppo
						}
					} else if (req.status == 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					} else {
						alert.textContent = message;
					}
				}
			});
		};

		this.update = function(g) {
			this.title.textContent = g.title;
			this.date.textContent = g.date;
			this.duration.textContent = g.duration;
			this.min.textContent = g.min;
			this.max.textContent = g.max;
		};
	}
	
	
	//funzione per mostrate i partecipanti di un dato gruppo
	function Participants(alert, participants) {
		this.alert = alert;
		this.participantsList = participants;
		this.participantsList.style.visibility = "hidden";

		this.show = function(next, groupId) {
			var self = this;

			makeCall("GET", "GetParticipants?groupId=" + groupId, null, function(req) {
				if (req.readyState == 4) {
					var message = req.responseText;
					if (req.status == 200) {
						var participantsToShow = JSON.parse(req.responseText);
						if (participantsToShow.length == 0) {
							self.alert.textContent = "Non ci sono partecipanti";
							return;
						}
						self.update(participantsToShow);
						if (next) next();
					} else if (req.status == 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					} else {
						self.alert.textContent = message;
					}
				}
			});
		};

		this.update = function(arrayParticipants) {
			this.participantsList.innerHTML = ""; // Empty the list
			var self = this;
			arrayParticipants.forEach(function(user) {
				var listItem = document.createElement("li");
				var anchor = document.createElement("a");
				anchor.textContent = user.name + " " + user.lastname;
				anchor.href = "#";
				listItem.setAttribute('data-username', user.username);
				listItem.setAttribute('draggable', true);
				listItem.addEventListener('dragstart', dragStart);
				listItem.addEventListener('dragend', dragEnd);

				listItem.appendChild(anchor);
				self.participantsList.appendChild(listItem);
			});
			this.participantsList.style.visibility = "visible";
		};
	}

	function dragStart(event) {
		event.dataTransfer.setData("text", event.currentTarget.getAttribute('data-username'));
	}

	function dragEnd(event) {
		event.preventDefault();
	}

	document.addEventListener('dragover', (event) => {
		event.preventDefault();
	});
	
	
	//drop sul cestino per la rimozione di un partecipante
	document.getElementById("trash-icon").addEventListener('drop', (event) => {
		event.preventDefault();
		var username = event.dataTransfer.getData("text");
		var groupId = sessionStorage.getItem("groupId");
		
		if (!username || !groupId) {
			document.getElementById("groups-alert").textContent = "Invalid group ID or username";
			return;
		}

		makeCall("POST", "CheckRemoval?groupId=" + groupId + "&username=" + username, null, function(req) {
			if (req.readyState == 4) {
				var message = req.responseText;
				document.getElementById("groups-alert").style.visibility = "visible";
				if (req.status == 200) {
					participants.show(null, groupId);
					document.getElementById("groups-alert").textContent = message;
				} else if (req.status == 403) {
					window.location.href = req.getResponseHeader("Location");
					window.sessionStorage.removeItem('username');
				} else {
					document.getElementById("groups-alert").textContent = message;
				}
			}
		});
	});

	document.getElementById("submitButton").addEventListener('click', (e) => {
		event.preventDefault();
		var title = document.getElementById('group-title').value;
		var duration = document.getElementById('group-duration').value;
		var min = document.getElementById('group-min').value;
		var max = document.getElementById('group-max').value;

		// Check if form values are valid
		if (!title || !duration || !min || !max) {
			alert('Please fill out all fields');
			return;
		}

		// Save group data in session storage
		sessionStorage.setItem('group-title', title);
		sessionStorage.setItem('group-duration', duration);
		sessionStorage.setItem('group-min', min);
		sessionStorage.setItem('group-max', max);

		// Show modal to select users
		openModal();
	});

	function openModal() {
		document.getElementById("user-selection-modal").style.display = "block";
		loadUserList();
	}

	function closeModal() {
		document.getElementById("user-selection-modal").style.display = "none";
		attemptsLeft = 3; // Reset attempts when modal is closed
		document.getElementById("modal-alert").textContent = "";
	}

	function loadUserList() {
		// Load the list of users except the current user
		makeCall("GET", "ShowUsers", null, function(req) {
			if (req.readyState === 4) {
				if (req.status === 200) {
					var users = JSON.parse(req.responseText);
					var userList = document.getElementById("user-list");
					userList.innerHTML = ""; // Clear the list
					users.forEach(user => {
						var listItem = document.createElement("li");
						var checkbox = document.createElement("input");
						checkbox.type = "checkbox";
						checkbox.value = user.username;
						listItem.appendChild(checkbox);
						listItem.appendChild(document.createTextNode(`${user.name} ${user.lastname}`));
						userList.appendChild(listItem);
					});
				} else if (req.status === 403) {
					window.location.href = req.getResponseHeader("Location");
					window.sessionStorage.removeItem('username');
				} else {
					document.getElementById("modal-alert").textContent = req.responseText;
				}
			}
		});
	}

	function inviteUsers() {
		var checkboxes = document.querySelectorAll("#user-list input[type='checkbox']:checked");
		var usernames = Array.from(checkboxes).map(cb => cb.value);
		var minParticipants = parseInt(document.getElementById("group-min").value);
		var maxParticipants = parseInt(document.getElementById("group-max").value);

		if (usernames.length < minParticipants || usernames.length > maxParticipants) {
			attemptsLeft--;
			if (attemptsLeft <= 0) {
				closeModal();
				//alert("Impossibile creare gruppo");
				document.getElementById("groups-alert").textContent = "Impossibile creare gruppo";
			} else {
				var n;
				if (usernames.length < minParticipants) {
					n = minParticipants - usernames.length;
					document.getElementById("modal-alert").textContent = `Troppo pochi utenti selezionati, aggiungerne almeno ${n}`;
				} else {
					n = usernames.length - maxParticipants;
					document.getElementById("modal-alert").textContent = `Troppo utenti selezionati, eliminarne almeno ${n}`;
				}

			}
		} else {
			createGroup(usernames);
			closeModal();
		}
	}

	// Create group function
	function createGroup(selectedUsers) {
		var title = sessionStorage.getItem('group-title');
		var duration = sessionStorage.getItem('group-duration');
		var min = sessionStorage.getItem('group-min');
		var max = sessionStorage.getItem('group-max');

		var url = `CreateGroup?title=${title}&duration=${duration}&min=${min}&max=${max}&users=${selectedUsers.join(',')}`;

		makeCall('POST', url, null, function(req) {
			if (req.readyState == 4) {
				if (req.status == 200) {
					alert('Group created successfully');
					window.location.href = 'Home.html';
				} else {
					alert('Error creating group');
					window.location.href = 'Home.html';
				}
			}
		});
	}



	function PageOrchestrator() {
		this.start = function() {
			personalMessage = new PersonalMessage(sessionStorage.getItem('username'), document.getElementById("welcome-message"));
			personalMessage.show();

			createdGroupsList = new CreatedGroupsList(document.getElementById("created-groups-alert"), document.getElementById("created-groups-list"));
			invitedGroupsList = new InvitedGroupsList(document.getElementById("invited-groups-alert"), document.getElementById("invited-groups-list"));

			groupDetails = new GroupDetails({
				alert: document.getElementById("groups-alert"),
				title: document.getElementById("title"),
				date: document.getElementById("date"),
				duration: document.getElementById("duration"),
				min: document.getElementById("min"),
				max: document.getElementById("max"),
				container: document.getElementById("group-details"),
			});

			participants = new Participants(document.getElementById("groups-alert"), document.getElementById("participants-list"));

			document.querySelector("a[href='Logout']").addEventListener('click', () => {
				window.sessionStorage.removeItem('username');
			});
		};

		this.refresh = function() {
			createdGroupsList.show();
			invitedGroupsList.show();
			document.getElementById("trash-container").style.visibility = "hidden";// Nascondi il cestino all'inizio
			document.getElementById("groups-alert").style.visibility = "hidden";
		};
	}
}