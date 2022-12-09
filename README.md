
# Ejemplo Spring REST  (SI-2022, semana 4)

Ejemplo de endpoints REST con Spring-MVC (implementación directa)

Construido sobre proyecto [pedidos-spring-22](https://github.com/esei-si-dagss/pedidos-spring-22)

## Estructura del proyecto
El proyecto resultante sigue una organización relativamente habitual en proyectos Spring. 
En este caso, al tratarse de una aplicación muy sencilla es más compleja de lo necesario y sufre de cierta "_sobreingeniería_".

* Paquete **entidades**: incluye las entidades del dominio
* Paquete **daos**: incluye la definición de los `interfaces` _xxxDAO_ que heredan de `JpaRepository<E,K>` y aportan las operaciones CRUD (_create_, _read_, _update_, _delete_) sobre las entidades del dominio (se inyectarán _proxies_ con las implementaciones de estos interfaces cuando se requiera su inyección con `@Autowired`, que implícitamente estarían marcadas con `@Repository`)
* Paquete **servicios**: incluye los _servicios_ que encapsulan operaciones de la lógica de la aplicación
    - las operaciones se definen en el `interface`_xxxService_ y se implementan en la clase _xxxServiceImpl_, marcada con `@Service`
    - los métodos de los _xxxServiceImpl_ delegan las operaciones en los _DAO_ necesarios/correspondientes, inyectados con `@Autowired` (no es imprescindible una correspondencia directa con las entidades presente en el dominio)
    - las operaciones de _xxxService_ cumplen dos funciones complementarias:
       1. ocultan y coordinan otros _Service_ o _DAO_, siguiendo un patrón _Facade_ que oculta a la capa superior la interacción entre esos obejtos
       2. algunos de sus métodos están marcados con `@Transactional` delimitando operaciones trasaccionales que serán invocadas desde la capa superior, sus modificaciones se ejecutan todas o ninguna y mantienen las propiedades ACID 
* Paquete **controladores**: incluye las clases marcadas con `@RestController` que aportan los métodos mapeados a las peticiones HTTP que conforman el API REST de la aplicación
    - cada _xxxController_ delega en uno o más _yyyService_ (inyectados con `@Autowired`) la implementación de la lógica que corresponda (no es imprescindible una correspondencia directa con las entidades presente en el dominio)
    - los controladores están anotados con `@CrossOrigin(origins = "*")` para permitir accesos al API REST desde aplicaciones Javascript (ver https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors)

## Añadidos
* En `pom.xml`:
    * Añadida dependencia con _starter_ para Spring MVC (`spring-boot-starter-web`) 
* En paquete `entidades`: 
	* Añadida anotación `@JsonIgnore` en relación `@ManyToOne` de `LineaPedido` hacia `Pedido` para evitar ciclos en la serialziación JSON de los pedidos.
* Creado paquete `services` con:
	* `ClienteService` y `ClienteServiceImpl.java` 	
	* `ArticuloService` y `ArtculoServiceImpl.java` (gestiona Articulos y Familias)	
	* `AlmacenService` y `AlmacenServiceImpl.java`  (gestiona Almacenes y ArticuloAlmane [=stocks])
	* `PedidoService` y `PedidoServiceImpl`	
* Creado paquete `controllers` con:
   * `ClienteController`: API REST para cliente (en `http://localhost:8080/api/clientes`)
   * `ArticuloController`: API REST para artículos (en `http://localhost:8080/api/articulos`)
   * `FamiliaController`: API REST para familias (en `http://localhost:8080/api/familias`)
   * `PedidoController`: API REST para pedidos (en `http://localhost:8080/api/pedidos`)
   * `AlmacenController`: API REST para almacenes y stock de artículos en almacén (en `http://localhost:8080/api/almacenes`) 
   

## Ejecución del proyecto

En Spring Tool Suite: Proyecto 'pedidos-spring' `[botón derecho] > Run As > Spring Boot App`

Desde línea de comandos:
```sh
mvn spring-boot:run
```


**Nota**: 

* El .jar resultante ejecuta su propio contenedor de Servlet Apache Tomcat embebido, deplegando la aplicación en http://localhos:8080/api
* Se puede acceder desde navegador o `curl` a las URIs del API
   - http://localhost/8080/clientes
   - http://localhost/8080/clientes/1111111A 
   - http://localhost/8080/articulos/3
   - http://localhost/8080/almacenes/1/articulos
   - http://localhost/8080/almacenes/1/articulos/2/stock


### EXTRA: SpringDoc OpenAPI (generador de documentación on-line OpenAPI/Swagger) [no es parte de Spring]

1. Descomentar en el `pom.xml` las  dependencias que activan el proyecto `springdoc-openapi`
   - Detalles en https://springdoc.org/

2. Ejecutar de nuevo el proyecto (con `mvn spring-boot:run`) y acceder con un navegador a la URL http://localhost:8080/swagger-ui.html para ver la documentación autogenerada para el API REST.


## Añadidos (9/12/2022)

Añadida una versión del API REST usando Spring HATEOAS (https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
* Añadidas al `pom.xml` del proyecto las dependencias `spring-boot-starter-hateoas` (ofrece `EntityModel`, `Link`, etc) y `spring-boot-starter-validation` (ofrece `@Valid` para forzar chequeo de anotaciones Bean Validation en los datos recibidos en peticiones REST)
* Añadido paquete `controladores.hateoas` con las implementaciones de los endpoint HATEOAS disponibles en las URL bajo `http://localhost:8080/api/v2`.
* Añadidos en ese paquete las versiones modificadas de los controladores: `AlmacenControllerV2.java`, `ArticuloControllerV2.java`,  `ClienteControllerV2.java`, `FamiliaControllerV2.java`, `PedidoControllerV2.java`
    - Todos ellos replican los endpoints de la versión anterior, devolviendo objetos `EntityModel<Entidad>` en lugar de devolver directamente las entidades correspondientes.
    - Estos `EntityModel<Entidad>` funcionan como especia de DTOs (_Data Transfer Objects_) encapsulando la/las entidad/entidades devueltas y añadiéndolo mediante objetos `Link` hipernelaces a otros recursos/entidades relacionados.
    - En el ejemplo no se usan hiperenlaces HATEOAS al 100%, limitandose a vincularlo a las entidades simples. En el caso de colecciones no se usa `CollectionModel` sino que se devuelven directamente listas de `EntityModel<E>`

Añadido un ejemplo de documentación de APIs REST usando _SpringDOC OpenAPI_ (https://springdoc.org/ y https://www.openapis.org/)
* Añadida al `pom.xml` del proyecto la dependencia `springdoc-openapi-ui` 
* Añadidas en `ClienteControllerV2.java` (y parcialmente en  `AlmacenControllerV2.java`) las anotaciones para vincular metadatos y generar la documentación automática de los _endpoints_.
* La documentación on-line generada y el interfaz de pruebas _SwaggerUI_ estará disponible en http://localhost:8080/swagger-ui.html   
* La definición OpenAPI 3.x de los _endpoints_ definidos estará disponible en http://localhost:8080/v3/api-docs (formato JSON) y en http://localhost:8080/v3/api-docs.yml (formato YAML,ver https://spec.openapis.org/oas/latest.html, editable en https://editor.swagger.io/)