package es.uvigo.mei.pedidos.controladores;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.uvigo.mei.pedidos.entidades.Cliente;
import es.uvigo.mei.pedidos.servicios.ClienteService;

@RestController
@RequestMapping(path = "/api/clientes",
                produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ClienteController {
	@Autowired
	ClienteService clienteService;

	@GetMapping()
	public ResponseEntity<List<Cliente>> buscarTodos(
			@RequestParam(name = "localidad", required = false) String localidad,
			@RequestParam(name = "nombre", required = false) String nombre) {
		try {
			List<Cliente> resultado = new ArrayList<>();

			if (localidad != null) {
				resultado = clienteService.buscarPorLocalidad(localidad);
			} else if (nombre != null) {
				resultado = clienteService.buscarPorNombre(nombre);
			} else {
				resultado = clienteService.buscarTodos();
			}

			if (resultado.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
				
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// OTRA OPCION: separar los @GetMapping para los accesos con QueryParams
	//
	// @GetMapping(params={"localidad"})
	// public List<Cliente> buscarPorLocalidad(@RequestParam(name = "localidad", required = true) String localidad) {...}
	// @GetMapping(params={"nombre"})
	// public List<Cliente> buscarPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {...}

	@GetMapping(path = "{dni}")
	public ResponseEntity<Cliente> buscarPorDNI(@PathVariable("dni") String dni) {
		Optional<Cliente> cliente = clienteService.buscarPorDNI(dni);

		if (cliente.isPresent()) {
			return new ResponseEntity<>(cliente.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	

	@DeleteMapping(path = "{dni}")
	public ResponseEntity<HttpStatus> eliminar(@PathVariable("dni") String dni) {
		try {
			Optional<Cliente> cliente = clienteService.buscarPorDNI(dni);
			if (cliente.isPresent()) {
				clienteService.eliminar(cliente.get());
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(path = "{dni}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Cliente> modificar(@PathVariable("dni") String dni, @RequestBody Cliente cliente) {
		Optional<Cliente> clienteOptional = clienteService.buscarPorDNI(dni);

		if (clienteOptional.isPresent()) {
			Cliente nuevoCliente = clienteService.modificar(cliente);
			return new ResponseEntity<>(nuevoCliente, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
		try {
			String dni = cliente.getDNI();
			if ((dni != null) && !dni.isBlank()) {
				Optional<Cliente> clienteOptional = clienteService.buscarPorDNI(dni);

				if (clienteOptional.isEmpty()) {
					Cliente nuevoCliente = clienteService.crear(cliente);
					URI uri = crearURICliente(nuevoCliente);

					return ResponseEntity.created(uri).body(nuevoCliente);

				}
			}
			// No aporta DNI o DNI ya existe
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// Construye la URI del nuevo recurso creado con POST 
	private URI crearURICliente(Cliente cliente) {
		return ServletUriComponentsBuilder.fromCurrentRequest()
		.path("/{dni}")
		.buildAndExpand(cliente.getDNI())
		.toUri();				
	}

}
