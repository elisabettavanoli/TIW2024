# TIW – Group Management

Practice project for the **Tecnologie Informatiche per il Web (TIW)** exam at Politecnico di Milano (A.Y. 2023/2024): a small "group management" web app built with the exact architecture required by the TIW lab exam, Java Servlets + JDBC on the backend, plain HTML/CSS/JavaScript on the frontend, no frameworks.

Users can sign up and log in, create groups (with a title, a duration and a minimum/maximum number of participants), see the groups they created and the ones they've been invited to, inspect a group's details and participant list, and leave/remove a group.

## Stack

- **Backend**: Java Servlets (`it.polimi.tiw.controllers`) talking to MySQL via JDBC (`it.polimi.tiw.dao`), deployed on Apache Tomcat 9
- **Frontend**: plain HTML/CSS/JavaScript, calling the servlets via `fetch` (`homeManagement.js`, `loginManagement.js`, `signupManagement.js`)