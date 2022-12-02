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

import es.uvigo.mei.pedidos.entidades.Pedido;
import es.uvigo.mei.pedidos.entidades.Cliente;
import es.uvigo.mei.pedidos.servicios.PedidoService;
import es.uvigo.mei.pedidos.servicios.ClienteService;

@RestController
@RequestMapping(path = "/api/pedidos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class PedidoController {
	@Autowired
	PedidoService pedidoService;

	@Autowired
	ClienteService clienteService;

	@GetMapping()
	public ResponseEntity<List<Pedido>> buscarTodos(
			@RequestParam(name = "clienteDNI", required = false) String clienteDni) {
		try {
			List<Pedido> resultado = new ArrayList<>();

			if (clienteDni != null) {
				Optional<Cliente> cliente = clienteService.buscarPorDNI(clienteDni);
				if (cliente.isPresent()) {
					resultado = pedidoService.buscarPorCliente(cliente.get());
				}
			} else {
				resultado = pedidoService.buscarTodos();
			}

			if (resultado.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(path = "{id}")
	public ResponseEntity<Pedido> buscarPorId(@PathVariable("id") Long id) {
		Pedido pedido = pedidoService.buscarPorIdConLineas(id);

		if (pedido != null) {
			return new ResponseEntity<>(pedido, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping(path = "{id}")
	public ResponseEntity<HttpStatus> eliminar(@PathVariable("id") Long id) {
		try {
			Optional<Pedido> pedido = pedidoService.buscarPorId(id);
			if (pedido.isPresent()) {
				pedidoService.eliminar(pedido.get());
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Pedido> modificar(@PathVariable("id") Long id, @RequestBody Pedido pedido) {
		Optional<Pedido> pedidoOptional = pedidoService.buscarPorId(id);

		if (pedidoOptional.isPresent()) {
			Pedido nuevoPedido = pedidoService.modificar(pedido);
			return new ResponseEntity<>(nuevoPedido, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido) {
		try {
			Pedido nuevoPedido = pedidoService.crear(pedido);
			URI uri = crearURIPedido(nuevoPedido);

			return ResponseEntity.created(uri).body(nuevoPedido);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	// Construye la URI del nuevo recurso creado con POST
	private URI crearURIPedido(Pedido pedido) {
		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(pedido.getId()).toUri();
	}

}
