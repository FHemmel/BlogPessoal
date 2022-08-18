package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@BeforeAll
	void start() {

		usuarioRepository.deleteAll();

	}

	@Test
	@Order(1)
	@DisplayName("Cadastrar Um Usuário")
	public void deveCriarUmUsuario() {
		HttpEntity<Usuario>requisicao = new HttpEntity<Usuario>(new Usuario(0L, 
				"Paulo Antunes", "linkfoto", "paulo_antunes@email.com", "senhasenha"));
		
		ResponseEntity <Usuario> resposta = testRestTemplate
				.exchange("/usuarios/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		assertEquals(HttpStatus.CREATED,resposta.getStatusCode());
		assertEquals(requisicao.getBody().getNome(), resposta.getBody().getNome());
		assertEquals(requisicao.getBody().getFoto(), resposta.getBody().getFoto());
		assertEquals(requisicao.getBody().getUsuario(), resposta.getBody().getUsuario());
	}
	
	@Test
	@Order(2)
	@DisplayName("Não deve permitir duplicação do usuário")
	public void naoDeveDuplicarUsuario() {
		usuarioService.cadastrarUsuario(new Usuario(0L, 
				"Maria Silva", "linkfoto", "maria_silva@email.com",  "senhasenha"));
		
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(new Usuario(0L, 
				"Maria Silva", "linkfoto", "maria_silva@email.com", "senhasenha"));
		
		ResponseEntity <Usuario> resposta = testRestTemplate
				.exchange("/usuarios/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		assertEquals(HttpStatus.BAD_REQUEST,resposta.getStatusCode());

	}
	@Test
	@Order(3)
	@DisplayName("Atualizar um Usuário")
	public void deveAtualizarUmUsuario() {

		/**
		 * Persiste um objeto da Classe Usuario no Banco de dados através do Objeto da Classe UsuarioService e
		 * guarda o objeto persistido no Banco de Dadoas no Objeto usuarioCadastrado, que será reutilizado abaixo. 
		 * 
		 * O Objeto usuarioCadastrado será do tipo Optional porquê caso o usuário não seja persistido no Banco 
		 * de dados, o Optional evitará o erro NullPointerException (Objeto Nulo).
		 */
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Juliana Andrews", "https://i.imgur.com/yDRVeK7.jpg", "juliana_andrews@email.com.br", "juliana123"));
		/**
		 *  Cria um Objeto da Classe Usuário contendo os dados do Objeto usuarioCadastrado, que foi persistido na
		 *  linha anterior, alterando os Atributos Nome e Usuário (Atualização dos Atributos). 
		 *  
		 * Observe que para obter o Id de forma automática, foi utilizado o método getId() do Objeto usuarioCadastrado.
		 */
		Usuario usuarioUpdate = new Usuario(usuarioCadastrado.get().getId(), 
			"Juliana Andrews Ramos", "https://i.imgur.com/yDRVeK7.jpg", "juliana_ramos@email.com.br", "juliana123");
		
		/**
		 * Insere o objeto da Classe Usuario (usuarioUpdate) dentro de um Objeto da Classe HttpEntity (Entidade HTTP)
		 */
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpdate);

		/**
		 * Cria um Objeto da Classe ResponseEntity (corpoResposta), que receberá a Resposta da Requisição que será 
		 * enviada pelo Objeto da Classe TestRestTemplate.
		 * 
		 * Na requisição HTTP será enviada a URL do recurso (/usuarios/atualizar), o verbo (PUT), a entidade
		 * HTTP criada acima (corpoRequisicao) e a Classe de retornos da Resposta (Usuario).
		 * 
		 * Observe que o Método Atualizar não está liberado de autenticação (Login do usuário), por isso utilizamos o
		 * Método withBasicAuth para autenticar o usuário em memória, criado na BasicSecurityConfig.
		 * 
		 * Usuário: root
		 * Senha: root
		 */
		ResponseEntity<Usuario> corpoResposta = testRestTemplate
			.withBasicAuth("root", "root")
			.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);

		/**
		 *  Verifica se a requisição retornou o Status Code OK (200) 
		 * Se for verdadeira, o teste passa, se não, o teste falha.
		 */
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());

		/**
		 * Verifica se o Atributo Nome do Objeto da Classe Usuario retornado no Corpo da Requisição 
		 * é igual ao Atributo Nome do Objeto da Classe Usuario Retornado no Corpo da Resposta
		 * Se for verdadeiro, o teste passa, senão o teste falha.
		 */
		assertEquals(corpoRequisicao.getBody().getNome(), corpoResposta.getBody().getNome());

		/**
		 * Verifica se o Atributo Usuario do Objeto da Classe Usuario retornado no Corpo da Requisição 
		 * é igual ao Atributo Usuario do Objeto da Classe Usuario Retornado no Corpo da Resposta
		 * Se for verdadeiro, o teste passa, senão o teste falha.
		 */
		assertEquals(corpoRequisicao.getBody().getUsuario(), corpoResposta.getBody().getUsuario());
	}

	@Test
	@Order(4)
	@DisplayName("Listar todos os Usuários")
	public void deveMostrarTodosUsuarios() {

		/**
		 * Persiste dois objetos diferentes da Classe Usuario no Banco de dados através do Objeto da Classe UsuarioService
		 */
		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Sabrina Sanches", "https://i.imgur.com/5M2p5Wb.jpg", "sabrina_sanches@email.com.br", "sabrina123"));
		
		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Ricardo Marques", "https://i.imgur.com/Sk5SjWE.jpg", "ricardo_marques@email.com.br", "ricardo123"));

		/**
		 * Cria um Objeto da Classe ResponseEntity (corpoResposta), que receberá a Resposta da Requisição que será 
		 * enviada pelo Objeto da Classe TestRestTemplate.
		 * 
		 * Na requisição HTTP será enviada a URL do recurso (/usuarios/all), o verbo (GET), a entidade
		 * HTTP será nula (Requisição GET não envia nada no Corpo da Requisição) e a Classe de retorno da Resposta 
		 * (String), porquê a lista de Usuários será do tipo String.
		 * 
		 * Observe que o Método All não está liberado de autenticação (Login do usuário), por isso utilizamos o
		 * Método withBasicAuth para autenticar o usuário em memória, criado na BasicSecurityConfig.
		 * 
		 * Usuário: root
		 * Senha: root
		 */
		ResponseEntity<String> resposta = testRestTemplate
			.withBasicAuth("root", "root")
			.exchange("/usuarios/all", HttpMethod.GET, null, String.class);

		/**
		 *  Verifica se a requisição retornou o Status Code OK (200) 
		 * Se for verdadeira, o teste passa, se não, o teste falha.
		 */
		assertEquals(HttpStatus.OK, resposta.getStatusCode());

	}
}