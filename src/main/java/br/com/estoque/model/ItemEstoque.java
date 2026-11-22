package br.com.estoque.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//Modelo de dados do material (id, nome, sku, categoria, quantidade, estoqueMinimo, precoUnitario).
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemEstoque {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String nome;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("category")
    private String categoria;

    @JsonProperty("stock")
    private Integer quantidade;

    @JsonProperty("minimumOrderQuantity")
    private Integer estoqueMinimo;

    @JsonProperty("price")
    private Double precoUnitario;

    public ItemEstoque() {
    }

    public ItemEstoque(Integer id, String nome, String sku, String categoria, Integer quantidade, Integer estoqueMinimo, Double precoUnitario) {
        this.id = id;
        this.nome = nome;
        this.sku = sku;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.precoUnitario = precoUnitario;
    }

    public boolean isEstoqueCritico() {
        if (quantidade == null || estoqueMinimo == null) {
            return false;
        }
        return quantidade <= estoqueMinimo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}
