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

import es.uvigo.mei.pedidos.entidades.Articulo;
import es.uvigo.mei.pedidos.servicios.ArticuloService;

@RestController
@RequestMapping(path = "/api/articulos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ArticuloController {
	@Autowired
	ArticuloService articuloService;

	@GetMapping()
	public ResponseEntity<List<Articulo>> buscarTodos(
			@RequestParam(name = "familiaId", required = false) Long familiaId,
			@RequestParam(name = "descripcion", required = false) String descripcion) {
		try {
			List<Articulo> resultado = new ArrayList<>();

			if (familiaId != null) {
				resultado = articuloService.buscarPorFamilia(familiaId);
			} else if (descripcion != null) {
				resultado = articuloService.buscarPorDescripcion(descripcion);
			} else {
				resultado = articuloService.buscarTodos();
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
	public ResponseEntity<Articulo> buscarPorId(@PathVariable("id") Long id) {
		Optional<Articulo> articulo = articuloService.buscarPorId(id);

		if (articulo.isPresent()) {
			return new ResponseEntity<>(articulo.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping(path = "{id}")
	public ResponseEntity<HttpStatus> eliminar(@PathVariable("id") Long id) {
		try {
			Optional<Articulo> articulo = articuloService.buscarPorId(id);
			if (articulo.isPresent()) {
				articuloService.eliminar(articulo.get());
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Articulo> modificar(@PathVariable("id") Long id, @RequestBody Articulo articulo) {
		Optional<Articulo> articuloOptional = articuloService.buscarPorId(id);

		if (articuloOptional.isPresent()) {
			Articulo nuevoArticulo = articuloService.modificar(articulo);
			return new ResponseEntity<>(nuevoArticulo, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Articulo> crear(@RequestBody Articulo articulo) {
		try {
			Articulo nuevoArticulo = articuloService.crear(articulo);
			URI uri = crearURIArticulo(nuevoArticulo);

			return ResponseEntity.created(uri).body(nuevoArticulo);
		} catch (

		Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	// Construye la URI del nuevo recurso creado con POST
	private URI crearURIArticulo(Articulo articulo) {
		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(articulo.getId()).toUri();
	}

}
