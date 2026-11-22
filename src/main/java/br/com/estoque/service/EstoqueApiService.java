package br.com.estoque.service;

import br.com.estoque.model.ItemEstoque;
import br.com.estoque.model.RespostaEstoqueApi;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//Comunicação HTTP assíncrona (HttpClient com sendAsync) para GET, POST, PUT e DELETE.
public class EstoqueApiService {
    private static final String URL_BASE_REAL = "https://dummyjson.com/products";
    private static final String URL_BASE_FALHA = "https://endpoint-inexistente.local/products";

    private final HttpClient clienteHttp;
    private final ObjectMapper mapeadorJson;
    private boolean simularFalha = false;

    public EstoqueApiService() {
        this.clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapeadorJson = new ObjectMapper();
    }

    public void setSimularFalha(boolean simularFalha) {
        this.simularFalha = simularFalha;
    }

    public boolean isSimularFalha() {
        return simularFalha;
    }

    private String obterUrlBase() {
        return simularFalha ? URL_BASE_FALHA : URL_BASE_REAL;
    }

    public CompletableFuture<List<ItemEstoque>> listarTodos() {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(obterUrlBase() + "?limit=20"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();

        return clienteHttp.sendAsync(requisicao, HttpResponse.BodyHandlers.ofString())
                .thenApply(resposta -> {
                    validarStatusHttp(resposta);
                    try {
                        RespostaEstoqueApi dados = mapeadorJson.readValue(resposta.body(), RespostaEstoqueApi.class);
                        return dados.getItens();
                    } catch (Exception e) {
                        throw new RuntimeException("Falha ao interpretar os dados recebidos da API.", e);
                    }
                });
    }

    public CompletableFuture<ItemEstoque> criar(ItemEstoque novoItem) {
        try {
            String jsonCorpo = mapeadorJson.writeValueAsString(novoItem);

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(obterUrlBase() + "/add"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonCorpo))
                    .build();

            return clienteHttp.sendAsync(requisicao, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resposta -> {
                        validarStatusHttp(resposta);
                        try {
                            return mapeadorJson.readValue(resposta.body(), ItemEstoque.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Falha ao processar resposta de cadastro.", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<ItemEstoque> atualizar(ItemEstoque item) {
        try {
            ItemEstoque payload = new ItemEstoque(
                    null,
                    item.getNome(),
                    item.getSku(),
                    item.getCategoria(),
                    item.getQuantidade(),
                    item.getEstoqueMinimo(),
                    item.getPrecoUnitario()
            );
            String jsonCorpo = mapeadorJson.writeValueAsString(payload);

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(obterUrlBase() + "/" + item.getId()))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonCorpo))
                    .build();

            return clienteHttp.sendAsync(requisicao, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resposta -> {
                        validarStatusHttp(resposta);
                        try {
                            return mapeadorJson.readValue(resposta.body(), ItemEstoque.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Falha ao processar resposta de atualização.", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> excluir(int id) {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(obterUrlBase() + "/" + id))
                .timeout(Duration.ofSeconds(5))
                .DELETE()
                .build();

        return clienteHttp.sendAsync(requisicao, HttpResponse.BodyHandlers.ofString())
                .thenAccept(this::validarStatusHttp);
    }

    private void validarStatusHttp(HttpResponse<String> resposta) {
        int status = resposta.statusCode();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Erro HTTP " + status + " ao comunicar com o servidor.");
        }
    }
}
