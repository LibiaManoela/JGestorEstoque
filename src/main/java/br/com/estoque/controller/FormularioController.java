package br.com.estoque.controller;

import br.com.estoque.model.ItemEstoque;
import br.com.estoque.validation.ValidadorEstoque;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

//Tela 2: Controla o formulário de cadastro e edição completa de insumos com exibição de erros.
public class FormularioController {
    @FXML private Label tituloTela;
    @FXML private TextField campoNome;
    @FXML private TextField campoSku;
    @FXML private ComboBox<String> campoCategoria;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoEstoqueMinimo;
    @FXML private TextField campoPreco;

    @FXML private Label erroNome;
    @FXML private Label erroSku;
    @FXML private Label erroCategoria;
    @FXML private Label erroQuantidade;
    @FXML private Label erroEstoqueMinimo;
    @FXML private Label erroPreco;

    private PrincipalController principal;
    private ItemEstoque itemEdicao;
    private final ValidadorEstoque validador = new ValidadorEstoque();

    @FXML
    public void initialize() {
        campoCategoria.getItems().addAll(
                "Matéria-Prima", "Ferramentas", "Eletrônicos", "Embalagens", "Escritório", "Outros"
        );
    }

    public void configurar(PrincipalController principal, ItemEstoque itemEdicao) {
        this.principal = principal;
        this.itemEdicao = itemEdicao;
        limparErrosVisuais();

        if (itemEdicao != null) {
            tituloTela.setText("Editar Item de Estoque");
            campoNome.setText(itemEdicao.getNome());
            campoSku.setText(itemEdicao.getSku());
            campoCategoria.setValue(itemEdicao.getCategoria());
            campoQuantidade.setText(String.valueOf(itemEdicao.getQuantidade()));
            campoEstoqueMinimo.setText(String.valueOf(itemEdicao.getEstoqueMinimo()));
            campoPreco.setText(String.valueOf(itemEdicao.getPrecoUnitario()));
        } else {
            tituloTela.setText("Cadastrar Novo Item");
        }
    }

    @FXML
    public void salvar() {
        limparErrosVisuais();

        Map<String, String> erros = validador.validarCamposBrutos(
                campoNome.getText(),
                campoSku.getText(),
                campoCategoria.getValue(),
                campoQuantidade.getText(),
                campoEstoqueMinimo.getText(),
                campoPreco.getText()
        );

        if (!erros.isEmpty()) {
            exibirErrosCampos(erros);
            return;
        }

        ItemEstoque dados = new ItemEstoque(
                itemEdicao != null ? itemEdicao.getId() : null,
                campoNome.getText().trim(),
                campoSku.getText().trim().toUpperCase(),
                campoCategoria.getValue(),
                Integer.parseInt(campoQuantidade.getText().trim()),
                Integer.parseInt(campoEstoqueMinimo.getText().trim()),
                Double.parseDouble(campoPreco.getText().trim().replace(",", "."))
        );

        if (itemEdicao == null) {
            enviarCriacao(dados);
        } else {
            enviarAtualizacao(dados);
        }
    }

    private void enviarCriacao(ItemEstoque novoItem) {
        principal.exibirCarregando(true);
        principal.ocultarBanner();

        principal.getApiService().criar(novoItem)
                .thenAccept(criado -> Platform.runLater(() -> {
                    principal.exibirCarregando(false);
                    novoItem.setId(criado.getId() != null ? criado.getId() : (int) (System.currentTimeMillis() % 10000));
                    principal.getListaItens().add(0, novoItem);
                    principal.exibirSucesso("Item \"" + novoItem.getNome() + "\" cadastrado com sucesso.");
                    principal.abrirCatalogo();
                }))
                .exceptionally(erro -> {
                    Platform.runLater(() -> {
                        principal.exibirCarregando(false);
                        principal.exibirErro("Falha ao cadastrar item na API.", () -> enviarCriacao(novoItem));
                    });
                    return null;
                });
    }

    private void enviarAtualizacao(ItemEstoque itemAtualizado) {
        principal.exibirCarregando(true);
        principal.ocultarBanner();

        principal.getApiService().atualizar(itemAtualizado)
                .thenAccept(resposta -> Platform.runLater(() -> {
                    principal.exibirCarregando(false);
                    aplicarAtualizacaoNaLista(itemAtualizado);
                    principal.exibirSucesso("Item \"" + itemAtualizado.getNome() + "\" atualizado com sucesso.");
                    principal.abrirCatalogo();
                }))
                .exceptionally(erro -> {
                    Platform.runLater(() -> {
                        principal.exibirCarregando(false);
                        principal.exibirErro("Falha ao atualizar o item na API.", () -> enviarAtualizacao(itemAtualizado));
                    });
                    return null;
                });
    }

    private void aplicarAtualizacaoNaLista(ItemEstoque atualizado) {
        int indice = principal.getListaItens().indexOf(itemEdicao);
        if (indice >= 0) {
            principal.getListaItens().set(indice, atualizado);
        }
    }

    @FXML
    public void cancelar() {
        principal.abrirCatalogo();
    }

    private void exibirErrosCampos(Map<String, String> erros) {
        aplicarErro(campoNome, erroNome, erros.get("nome"));
        aplicarErro(campoSku, erroSku, erros.get("sku"));
        aplicarErro(campoCategoria, erroCategoria, erros.get("categoria"));
        aplicarErro(campoQuantidade, erroQuantidade, erros.get("quantidade"));
        aplicarErro(campoEstoqueMinimo, erroEstoqueMinimo, erros.get("estoqueMinimo"));
        aplicarErro(campoPreco, erroPreco, erros.get("preco"));
    }

    private void aplicarErro(Control campo, Label labelErro, String mensagem) {
        if (mensagem != null) {
            if (!campo.getStyleClass().contains("campo-invalido")) {
                campo.getStyleClass().add("campo-invalido");
            }
            labelErro.setText(mensagem);
            labelErro.setVisible(true);
            labelErro.setManaged(true);
        }
    }

    private void limparErrosVisuais() {
        Control[] campos = {campoNome, campoSku, campoCategoria, campoQuantidade, campoEstoqueMinimo, campoPreco};
        Label[] labels = {erroNome, erroSku, erroCategoria, erroQuantidade, erroEstoqueMinimo, erroPreco};

        for (Control c : campos) {
            c.getStyleClass().remove("campo-invalido");
        }
        for (Label l : labels) {
            l.setText("");
            l.setVisible(false);
            l.setManaged(false);
        }
    }
}
