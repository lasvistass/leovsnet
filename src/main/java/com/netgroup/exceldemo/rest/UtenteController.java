package com.netgroup.exceldemo.rest;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netgroup.exceldemo.data.Utente;
import com.netgroup.exceldemo.exception.UserNotLoggedException;
import com.netgroup.exceldemo.service.LoginService;
import com.netgroup.exceldemo.service.UtenteService;
import com.netgroup.exceldemo.util.EncryptionUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

	@Autowired
	UtenteService uten;

	@Autowired
	EncryptionUtils encryption;

	@Autowired
	LoginService loginService;

	private static final Logger log = LoggerFactory.getLogger(UtenteController.class);

	@RequestMapping(value = "/register", method={RequestMethod.OPTIONS,RequestMethod.POST})
	public boolean addUser(@Valid @RequestBody Utente ute, BindingResult result) {
		if (result.hasErrors()) {
			return false;
		}
		ute.setPassword(encryption.encrypt(ute.getPassword()));
		log.info("password crypt " + ute.getPassword());
		uten.salva(ute);
		log.info("utente registrato con successo!");
		return true;
	}

	@GetMapping(value = "/{username}")
	public Utente getUtente(@PathVariable("username") String username) {
		Optional<Utente> u = uten.cercaUtente(username);
		return u.get();
	}

	@GetMapping
	public List<Utente> listaUtenti() {
		return uten.lista();
	}

	@RequestMapping(value = "/login", method={RequestMethod.OPTIONS,RequestMethod.POST})
	public ResponseEntity<JsonResponseBody> login(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password) {
		log.info(username + " " + password);
		try {
			Optional<Utente> userr = loginService.getUserFromDbAndVerifyPassword(username, password);
			if (userr.isPresent()) {
				Utente user = userr.get();
				String jwt = loginService.createJwt(user.getUsername(), user.getFirstName(), /*user.getPermission()*/"user",
						new Date());
				return ResponseEntity.status(HttpStatus.OK).header("jwt", jwt)
						.body(new JsonResponseBody(HttpStatus.OK.value(), "Success! User logged in!"));
			}
		} catch (UserNotLoggedException e1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponseBody(HttpStatus.FORBIDDEN.value(),
					"Login failed! Wrong credentials" + e1.toString()));
		} catch (UnsupportedEncodingException e2) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new JsonResponseBody(HttpStatus.FORBIDDEN.value(), "Token Error" + e2.toString()));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new JsonResponseBody(HttpStatus.FORBIDDEN.value(), "No corrispondence in the database of users"));
	}

	@GetMapping(value = "/log")
	public ResponseEntity<JsonResponseBody> logStandard() {
		String jwt = null;
		try {
			jwt = loginService.createJwt("", "", "user-standard", new Date());

			
		} catch (UnsupportedEncodingException e) {
		
			e.printStackTrace();
		}
return ResponseEntity.status(HttpStatus.OK).header("jwt", jwt)
					.body(new JsonResponseBody(HttpStatus.OK.value(), "Success! User logged in!"));
	}

	/*------------------------------*/
	@AllArgsConstructor
	public class JsonResponseBody {
		@Getter
		@Setter
		private int server;
		@Getter
		@Setter
		private Object response;
		
		
		
		public JsonResponseBody(int server, Object response) {
			super();
			this.server = server;
			this.response = response;
		}
		public int getServer() {
			return server;
		}
		public void setServer(int server) {
			this.server = server;
		}
		public Object getResponse() {
			return response;
		}
		public void setResponse(Object response) {
			this.response = response;
		}
		
		
	}

}
