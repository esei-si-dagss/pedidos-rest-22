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

import es.uvigo.mei.pedidos.entidades.Almacen;
import es.uvigo.mei.pedidos.entidades.Articulo;
import es.uvigo.mei.pedidos.entidades.ArticuloAlmacen;
import es.uvigo.mei.pedidos.servicios.AlmacenService;
import es.uvigo.mei.pedidos.servicios.ArticuloService;

@RestController
@RequestMapping(path = "/api/almacenes", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class AlmacenController {
	@Autowired
	AlmacenService almacenService;
	@Autowired
	ArticuloService articuloService;

	@GetMapping()
	public ResponseEntity<List<Almacen>> buscarTodos(
			@RequestParam(name = "localidad", required = false) String localidad,
			@RequestParam(name = "articuloId", required = false) Long articuloId) {
		try {
			List<Almacen> resultado = new ArrayList<>();

			if (localidad != null) {
				resultado = almacenService.buscarPorLocalidad(localidad);
			} else if (articuloId != null) {
				resultado = almacenService.buscarPorArticuloId(articuloId);
			} else {
				resultado = almacenService.buscarTodos();
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
	public ResponseEntity<Almacen> buscarPorId(@PathVariable("id") Long id) {
		Optional<Almacen> almacen = almacenService.buscarPorId(id);

		if (almacen.isPresent()) {
			return new ResponseEntity<>(almacen.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping(path = "{id}")
	public ResponseEntity<HttpStatus> eliminar(@PathVariable("id") Long id) {
		try {
			Optional<Almacen> almacen = almacenService.buscarPorId(id);
			if (almacen.isPresent()) {
				almacenService.eliminar(almacen.get());
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Almacen> modificar(@PathVariable("id") Long id, @RequestBody Almacen almacen) {
		Optional<Almacen> almacenOptional = almacenService.buscarPorId(id);

		if (almacenOptional.isPresent()) {
			Almacen nuevoAlmacen = almacenService.modificar(almacen);
			return new ResponseEntity<>(nuevoAlmacen, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Almacen> crear(@RequestBody Almacen almacen) {
		try {
			Almacen nuevoAlmacen = almacenService.crear(almacen);
			URI uri = crearURIAlmacen(nuevoAlmacen);

			return ResponseEntity.created(uri).body(nuevoAlmacen);
		} catch (

		Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// GET {id}/articulos
	// Articulos de un Almacen
	@GetMapping(path = "{idAlmacen}/articulos")
	public ResponseEntity<List<ArticuloAlmacen>> buscarArticulosAlmacen(
			@PathVariable("idAlmacen") Long idAlmacen) {
		try {
			List<ArticuloAlmacen> resultado = new ArrayList<>();

			resultado = almacenService.buscarArticulosAlmacenPorAlmacenId(idAlmacen);

			if (resultado.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// GET {id}/articulos/{id}
	// Recuperar datos de un Articulo en el Almacén indicado
	@GetMapping(path = "{idAlmacen}/articulos/{idArticulo}")
	public ResponseEntity<ArticuloAlmacen> buscarArticuloAlmacenPorId(
			@PathVariable("idAlmacen") Long idAlmacen, @PathVariable("idArticulo") Long idArticulo) {
		Optional<ArticuloAlmacen> articuloAlmacen = almacenService
				.buscarArticuloAlmacenPorArticuloIdAlmacenId(idArticulo, idAlmacen);

		if (articuloAlmacen.isPresent()) {
			return new ResponseEntity<>(articuloAlmacen.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// GET {id}/articulos/{id}/stock
	// Recuperar directamente el stock (como entero) de un Articulo en el Almacén indicado
	@GetMapping(path = "{idAlmacen}/articulos/{idArticulo}/stock")
	public ResponseEntity<Integer> leerStockArticuloAlmacenPorId(
			@PathVariable("idAlmacen") Long idAlmacen, @PathVariable("idArticulo") Long idArticulo) {
		Optional<ArticuloAlmacen> articuloAlmacen = almacenService
				.buscarArticuloAlmacenPorArticuloIdAlmacenId(idArticulo, idAlmacen);

		if (articuloAlmacen.isPresent()) {
			return new ResponseEntity<>(articuloAlmacen.get().getStock(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	// PUT {id}/articulos/{id}
    // Actualizar los datos de Stock de un Artículo en el Almacén indicado")
	@PutMapping(path = "{idAlmacen}/articulos/{idArticulo}")
	public ResponseEntity<ArticuloAlmacen> modificarArticuloAlmacen(
			@PathVariable("idAlmacen") Long idAlmacen, @PathVariable("idArticulo") Long idArticulo,
		    @RequestBody ArticuloAlmacen articuloAlmacen) {
		// Recuperar nuevo stock
		Integer nuevoStock = articuloAlmacen.getStock();
		return _modificarStockArticuloAlmacen(idAlmacen, idArticulo, nuevoStock);
	}

	// PUT {id}/articulos/{id}/stock
	// Actualizar directamente el stock de un Articulo en el Almacén indicado
	@PutMapping(path = "{idAlmacen}/articulos/{idArticulo}/stock")
	public ResponseEntity<ArticuloAlmacen> modificarArticuloAlmacenDirecto(
			@PathVariable("idAlmacen") Long idAlmacen, @PathVariable("idArticulo") Long idArticulo,
		    @RequestBody Integer nuevoStock) {
		return _modificarStockArticuloAlmacen(idAlmacen, idArticulo, nuevoStock);
	}

	private ResponseEntity<ArticuloAlmacen> _modificarStockArticuloAlmacen(Long idAlmacen, Long idArticulo,
			Integer stock) {
		Optional<ArticuloAlmacen> articuloAlmacenOptional = almacenService
				.buscarArticuloAlmacenPorArticuloIdAlmacenId(idArticulo, idAlmacen);

		if (articuloAlmacenOptional.isPresent()) {
			ArticuloAlmacen articuloAlmacenAModificar = articuloAlmacenOptional.get();
			articuloAlmacenAModificar.setStock(stock);
			ArticuloAlmacen nuevoArticuloAlmacen = almacenService.modificarArticuloAlmacen(articuloAlmacenAModificar);
			return new ResponseEntity<>(nuevoArticuloAlmacen, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	// DELETE {id}/articulos/{id}
	// Eliminar los datos de Stock de un Artículo en el Almacén indicado
	@DeleteMapping(path = "{idAlmacen}/articulos/{idArticulo}")
	public ResponseEntity<HttpStatus> eliminarArticuloAlmacen(@PathVariable("idAlmacen") Long idAlmacen,
			@PathVariable("idArticulo") Long idArticulo) {
		try {
			Optional<ArticuloAlmacen> articuloAlmacen = almacenService
					.buscarArticuloAlmacenPorArticuloIdAlmacenId(idArticulo, idAlmacen);
			if (articuloAlmacen.isPresent()) {
				almacenService.eliminarArticuloAlmacen(articuloAlmacen.get());
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// POST {id}/articulos/
	// Crear los datos de Stock de un Artículo nuevo en el Almacén indicado
	@PostMapping(path = "{idAlmacen}/articulos")
	public ResponseEntity<ArticuloAlmacen> crearArticuloAlmacen(@PathVariable("idAlmacen") Long idAlmacen,
			@RequestBody ArticuloAlmacen articuloAlmacen) {
		Long idArticulo = articuloAlmacen.getArticulo().getId();
		Integer stock = articuloAlmacen.getStock();
		return _crearArticuloAlmacen(idAlmacen, idArticulo, stock);
	}

	// POST {id}/articulos/{id}/stock
	// Crear directamente el stock de un Articulo nuevo en el Almacén indicado
	@PostMapping(path = "{idAlmacen}/articulos/{idArticulo}/stock")
	public ResponseEntity<ArticuloAlmacen> crearArticuloAlmacenDirecto(
			@PathVariable("idAlmacen") Long idAlmacen, @PathVariable("idArticulo") Long idArticulo,
			@RequestBody Integer stock) {
		return _crearArticuloAlmacen(idAlmacen, idArticulo, stock);
	}

	private ResponseEntity<ArticuloAlmacen> _crearArticuloAlmacen(Long idAlmacen, Long idArticulo,
			Integer stock) {
		try {
			Optional<ArticuloAlmacen> articuloAlmacenOptional = almacenService
					.buscarArticuloAlmacenPorArticuloIdAlmacenId(idArticulo, idAlmacen);
			if (articuloAlmacenOptional.isEmpty()) {
				Optional<Almacen> almacen = almacenService.buscarPorId(idAlmacen);
				Optional<Articulo> articulo = articuloService.buscarPorId(idArticulo);

				if ((almacen.isPresent()) && (articulo.isPresent())) {
					ArticuloAlmacen nuevoArticuloAlmacen = almacenService.crearArticuloAlmacen(articulo.get(),
							almacen.get(), stock);
					URI uri = crearURIArticuloAlmacen(nuevoArticuloAlmacen);

					return ResponseEntity.created(uri).body(nuevoArticuloAlmacen);
				} else {
					return new ResponseEntity<>(HttpStatus.NOT_FOUND);
				}
			} else {
				// Ya existe el artículo en el almacen
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}


	// Construye la URI del nuevo recurso creado con POST
	private URI crearURIAlmacen(Almacen almacen) {
		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(almacen.getId()).toUri();
	}


	// Construye la URI del nuevo recurso creado con POST
	private URI crearURIArticuloAlmacen(ArticuloAlmacen articuloAlmacen) {
		Long idArticulo = articuloAlmacen.getArticulo().getId();
		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").path("articulos")
				.buildAndExpand(idArticulo).toUri();
	}

}
